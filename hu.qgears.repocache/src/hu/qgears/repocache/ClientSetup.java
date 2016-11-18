package hu.qgears.repocache;

public class ClientSetup {
	private String id;
	EClientMode mode;
	public ClientSetup()
	{
		this.mode=EClientMode.normal;
	}
	public ClientSetup(String client, EClientMode mode) {
		id=client;
		this.mode=mode;
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

}
