package hu.qgears.repocache;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.qgears.repocache.config.EClientMode;

public class ClientSetup {
	private static Logger log=LoggerFactory.getLogger(ClientSetup.class);
	
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
		if (!EClientMode.READ_ONLY.equals(mode)&&validInMinute != null) {
			try {
				int validity = Integer.parseInt(validInMinute);
				modeValidTill = Calendar.getInstance();
				modeValidTill.add(Calendar.MINUTE, validity);
			} catch (NumberFormatException e) {
				log.error("Client mode valid in minutes parameter must be int, value: " + validInMinute, e);
			}
		}
	}
	@Override
	public String toString() {
		return ""+id+": "+getMode()+" : "+(shawRealFolderListing?"" : "NOT ") + "Shaw real folder listing.";
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
