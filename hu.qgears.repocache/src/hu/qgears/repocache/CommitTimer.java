package hu.qgears.repocache;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

import hu.qgears.commons.UtilEvent;
import hu.qgears.commons.UtilTimer;

public class CommitTimer implements Callable<Object>{
	private static Log log=LogFactory.getLog(CommitTimer.class);
	private RepoCache rc;
	private StringBuilder commitMessage=new StringBuilder();
	private long lastUpdate;
	private long timeoutMillis=5*60*1000;
	private long millisToNanos=1000*1000;
	public final UtilEvent<CommitTimer> commitStateChanged=new UtilEvent<>();
	public CommitTimer(RepoCache rc) {
		super();
		this.rc = rc;
	}
	public void addCommit(String message) {
		if(message.length()==0)
		{
			message="Auto update";
		}
		synchronized (rc) {
			if(commitMessage.length()>0)
			{
				commitMessage.append("\n");
			}
			commitMessage.append(message);
			lastUpdate=System.nanoTime();
		}
		UtilTimer.getInstance().executeTimeout(timeoutMillis+10, this);
		commitStateChanged.eventHappened(this);
	}
	
	/**
	 * Executes a commit on the cache, so that only automatically added commit
	 * messages will be saved. This method calls {@link #executeCommit(String)}
	 * with {@code null} as the parameter.
	 * @throws IOException passed up from {@link #executeCommit(String)} 
	 * @throws NoFilepatternException passed up from {@link #executeCommit(String)}
	 * @throws GitAPIException passed up from {@link #executeCommit(String)}
	 * @see #executeCommit(String)
	 */
	public void executeCommit() throws NoFilepatternException, IOException, GitAPIException {
		executeCommit(null);
	}
	
	/**
	 * Executes a commit on the cache.
	 * @param commitMessage if not null, it will be prepended to the 
	 * automatically added commit messages
	 * @throws IOException passed up from {@link RepoCache#assertStatusClean()}
	 * @throws NoFilepatternException TODO 
	 * @throws GitAPIException TODO
	 */
	public void executeCommit(final String commitMessage) 
			throws IOException, NoFilepatternException, GitAPIException {
		if (commitMessage != null && !commitMessage.isEmpty()) {
			this.commitMessage.insert(0, commitMessage + System.lineSeparator());
		}
		
		synchronized (rc) {
			if(this.commitMessage.length()>0)
			{
				rc.git.add().addFilepattern(".").call();
				rc.git.commit().setMessage(this.commitMessage.toString()).call();
				rc.assertStatusClean();
				log.info("Git commit executed!");
				this.commitMessage=new StringBuilder();
			}
		}
		commitStateChanged.eventHappened(this);
	}
	public void executeRevert() throws IOException, NoFilepatternException, GitAPIException
	{
		synchronized (rc) {
			if(commitMessage.length()>0)
			{
				rc.git.add().addFilepattern(".").call();
				rc.git.stashCreate().call();
				rc.git.stashDrop().call();
				rc.assertStatusClean();
				log.info("Git revert executed!");
				commitMessage=new StringBuilder();
			}
		}
		commitStateChanged.eventHappened(this);
	}
	@Override
	public Object call() throws Exception {
		long now=System.nanoTime();
		synchronized (rc) {
			if((now-(lastUpdate+timeoutMillis*millisToNanos))>0)
			{
				if(commitMessage.length()>0)
				{
					executeCommit();
				}
			}
		}
		return null;
	}
	public String getCurrentStagingMessage() {
		synchronized (rc) {
			StringBuilder ret=new StringBuilder();
			if(commitMessage.length()>0)
			{
				long now=System.nanoTime();
				ret.append("Timeout: "+((lastUpdate+timeoutMillis*millisToNanos)-now)/millisToNanos+" millis\n");
			}
			ret.append(commitMessage);
			return ret.toString();
		}
	}

}
