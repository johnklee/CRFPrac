package john.main.enums;

public enum EWType {
	TRAIN("train"), TEST("test"), TAGGED("tagged");
	
	private String msg;
	private EWType(String m){msg = m;}
	
	public boolean equals(String str)
	{
		if(msg.equalsIgnoreCase(str)) return true;
		return false;
	}
	
	/**
	 * BD : Transfer string to corresponding EWType. TEST is default enum.
	 * @param str
	 * @return
	 */
	public static EWType tf(String str)
	{
		EWType ts[] = EWType.values();
		for(EWType t:ts) if(t.equals(str)) return t;
		return EWType.TEST;
	}
	
	@Override
	public String toString() {return msg;}
}
