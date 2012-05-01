package john.memm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountryMap {
	private String ctryConfigPath = "country.txt";
	private HashMap<String,String> ctryAbbrvMap = null;
	
	public CountryMap(){this(null);}
	public CountryMap(String configPath)
	{
		if(configPath!=null) this.ctryConfigPath = configPath;
		// Parsing configuration
		ctryAbbrvMap = new HashMap<String,String>();
		parsing();
	}
	
	protected void parsing()
	{
		File cfgFile = new File(this.ctryConfigPath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(cfgFile));
			Pattern ptn = Pattern.compile("([A-Za-z]{2}):.*#\\s*(-)?\\s*(.*)");
			Matcher mtr = null;
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.trim().isEmpty()) continue;
				mtr = ptn.matcher(line);
				if(mtr.find())
				{
					//System.out.printf("\t[CountryMap] Abbv=%s ; Dest=%s...\n", mtr.group(1), mtr.group(3));
					ctryAbbrvMap.put(mtr.group(1).toUpperCase(), mtr.group(3).trim());
				}
				else
				{
					System.err.printf("\t[CountryMap] WARN : Illegal Line = %s!\n", line);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {	
			System.err.printf("\t[CountryMap] Error : %s", e);
			e.printStackTrace();
		} catch (IOException e)
		{
			System.err.printf("\t[CountryMap] Error : %s", e);
			e.printStackTrace();
		}
	}
	
	public boolean isAcceptCC(String cc)
	{
		return ctryAbbrvMap.containsKey(cc.toUpperCase());
	}
	public String accessDest(String cc){
		if(cc==null) return "Unknown";
		String dest = ctryAbbrvMap.get(cc.toUpperCase());
		return dest!=null?dest:"Unknown";
	}
	public static void main(String args[])
	{
		CountryMap ctyMap = new CountryMap();
		
	}
}
