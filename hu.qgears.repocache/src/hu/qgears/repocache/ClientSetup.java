package hu.qgears.repocache;

import java.util.Calendar;

public class ClientSetup {
	private String id;
	EClientMode mode;
	private Calendar validTill = null;
	public ClientSetup()
	{
		this.mode=EClientMode.normal;
	}
	public ClientSetup(String client, EClientMode mode) {
		id=client;
		this.mode=mode;
		if (EClientMode.updater.equals(mode)) {
			validTill = Calendar.getInstance();
			validTill.add(Calendar.HOUR, 8);
		}
	}

	public boolean isUpdater() {
		return mode==EClientMode.updater;
	}

	public boolean isAdder() {
		return mode==EClientMode.updater||mode==EClientMode.adder;
	}
	public String getId() {
		return id;
	}
	public EClientMode getMode() {
		return mode;
	}
	@Override
	public String toString() {
		return ""+id+": "+mode;
	}
	public boolean isNoCacheTransparent() {
		return mode==EClientMode.noCacheTransparent;
	}
	public boolean isValidSetup() {
		return validTill==null || validTill.after(Calendar.getInstance());
	}

}
