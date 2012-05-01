package john.crf.utils;

public class TimeStr {
	private long timeElapse = 0;
	
	public TimeStr(long ms)
	{
		this.timeElapse = ms;
	}
	
	public String toString(){		
		long totSec = timeElapse/1000;
		long sec = totSec%60;
		long totMin = totSec/60;
		long min = totMin%60;
		long totHr = totMin/60;
		long hr = totHr%24;
		long totDy = totHr/24;
		StringBuffer sb = new StringBuffer("");
		if(totDy>0) sb.append(String.format("%d day%s", totDy, totDy>1?"s":""));
		if(hr>0) sb.append(String.format(" %d hr%s", hr, totHr>1?"s":""));
		if(min>0) sb.append(String.format(" %d min%s", min, totMin>1?"s":""));
		if(sec>0) sb.append(String.format(" %d sec", sec));
		return sb.toString().trim();
	}
	
	public static String toStriing(long ts){return new TimeStr(ts).toString();}
	
	public static void main(String args[])
	{
		TimeStr ts = new TimeStr(123000456);
		System.out.printf("Time elapse %s\n", TimeStr.toStriing(45678));
	}
}
