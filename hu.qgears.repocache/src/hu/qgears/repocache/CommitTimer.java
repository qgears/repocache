package hu.qgears.repocache;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;

import hu.qgears.commons.UtilTimer;

public class CommitTimer implements Callable<Object>{
	private RepoCache rc;
	private StringBuilder commitMessage=new StringBuilder();
	private long lastUpdate;
	private long timeoutMillis=5*60*1000;
	private long millisToNanos=1000*1000;
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
	}
	public void executeCommit() throws IOException, NoFilepatternException, GitAPIException
	{
		synchronized (rc) {
			if(commitMessage.length()>0)
			{
				rc.git.add().addFilepattern(".").call();
				rc.git.commit().setMessage(commitMessage.toString()).call();
				rc.assertStatusClean();
				commitMessage=new StringBuilder();
			}
		}
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
				commitMessage=new StringBuilder();
			}
		}
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
