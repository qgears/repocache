package hu.qgears.repocache;

import java.util.Calendar;

import hu.qgears.repocache.config.EClientMode;

public class ClientSetup {
	
	private String id;
	EClientMode mode;
	private Calendar modeValidTill = null;
	
	private boolean shawRealFolderListing = true;
	
	public ClientSetup(String client)
	{
		id = client;
		this.mode=EClientMode.normal;
	}

	public boolean isReadonly() {
		return getMode()==EClientMode.normal;
	}
	
	@Deprecated
	public boolean isUpdater() {
		return getMode()==EClientMode.updater;
	}

	@Deprecated
	public boolean isAdder() {
		return getMode()==EClientMode.updater||getMode()==EClientMode.adder;
	}
	public String getId() {
		return id;
	}
	public EClientMode getMode() {
		if (!isModeValid()) {
			mode=EClientMode.normal;
			modeValidTill=null;
		}
		return mode;
	}
	public void setMode(EClientMode mode, String validInMinute) {
		this.mode = mode;
		if (!EClientMode.normal.equals(mode)&&validInMinute != null) {
			try {
				int validity = Integer.parseInt(validInMinute);
				modeValidTill = Calendar.getInstance();
				modeValidTill.add(Calendar.MINUTE, validity);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public String toString() {
		return ""+id+": "+getMode()+" : "+(shawRealFolderListing?"" : "NOT ") + "Shaw real folder listing.";
	}
	@Deprecated
	public boolean isNoCacheTransparent() {
		return getMode()==EClientMode.noCacheTransparent;
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
