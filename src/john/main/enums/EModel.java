package john.main.enums;

public enum EModel {
	CRF("CRF"), MEMM("MEMM");
	
	private String msg;
	private EModel(String m){msg = m;}
	
	public boolean equals(String str)
	{
		if(msg.equalsIgnoreCase(str)) return true;
		return false;
	}
	
	/**
	 * BD : Transfer string to corresponding EModel. Default is EMEMM.
	 * @param str
	 * @return
	 */
	public static EModel tf(String str)
	{
		EModel mds[] = EModel.values();
		for(EModel m:mds)
		{
			if(m.equals(str))return m;
		}
		return EModel.MEMM;
	}
	
	@Override
	public String toString() {return msg;}
}
