package hu.qgears.repocache.config;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.util.StringUtils;

public class ClientSetup {
	private static Log log=LogFactory.getLog(ClientSetup.class);
	
	private String id;
	EClientMode mode;
	private Calendar modeValidTill = null;
	
	private boolean shawRealFolderListing = true;
	
	public ClientSetup(String client)
	{
		id = client;
		this.mode=EClientMode.READ_ONLY;
	}

	public boolean isReadonly() {
		return getMode()==EClientMode.READ_ONLY;
	}
	
	public String getId() {
		return id;
	}
	public EClientMode getMode() {
		if (!isModeValid()) {
			mode=EClientMode.READ_ONLY;
			modeValidTill=null;
		}
		return mode;
	}
	public void setMode(EClientMode mode, String validInMinute) {
		this.mode = mode;
		if (!EClientMode.READ_ONLY.equals(mode)&& !StringUtils.isEmptyOrNull(validInMinute)) {
			try {
				int validity = Integer.parseInt(validInMinute);
				modeValidTill = Calendar.getInstance();
				modeValidTill.add(Calendar.MINUTE, validity);
			} catch (NumberFormatException e) {
				log.error("Client mode valid in minutes parameter must be int, value: " + validInMinute, e);
			}
		}
	}
	
	private String stillValidInMinutes () {
		long now = Calendar.getInstance().getTimeInMillis();
		if (modeValidTill == null) return "FOREVER";
		long validTill = modeValidTill.getTimeInMillis();
		long minutes = (validTill - now) / 1000 / 60;
		return minutes < 0 ? "0" : ""+minutes;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(id+": ");
		sb.append(getMode());
		if (modeValidTill!=null) {
			sb.append(" (valid for ");
			sb.append(stillValidInMinutes());
			sb.append(" minutes)");
		}
		sb.append(" : ");
		sb.append("Real folder listing ");
		sb.append(shawRealFolderListing ? "ENABLED." : "DISABLED.");
		return sb.toString();
	}

	private boolean isModeValid() {
		return modeValidTill==null || modeValidTill.after(Calendar.getInstance());
	}

	public boolean isShawRealFolderListing() {
		return shawRealFolderListing;
	}

	public void setShawRealFolderListing(boolean shawRealFolderListing) {
		this.shawRealFolderListing = shawRealFolderListing;
	}

}
