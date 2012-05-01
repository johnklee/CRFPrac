package john.memm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TLDMap {
	public static String UNKNOWN_TLD = "Other";
	private String ctryConfigPath = "tld.txt";
	private HashMap<String,String> tldMap = null;
	private CountryMap ccMap = null;
	
	public TLDMap(CountryMap cm){this(cm, null);}
	public TLDMap(CountryMap cm, String configPath)
	{
		this.ccMap = cm;
		if(configPath!=null) this.ctryConfigPath = configPath;
		// Parsing configuration
		tldMap = new HashMap<String,String>();
		parsing();
	}
	public TLDMap()
	{
		this(new CountryMap(), null);
	}
	
	protected void parsing()
	{
		File cfgFile = new File(this.ctryConfigPath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(cfgFile));
			Pattern ptn = Pattern.compile("([A-Za-z]+)\\s*:(.*)");
			Matcher mtr = null;
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.trim().isEmpty()) continue;
				mtr = ptn.matcher(line);
				if(mtr.find())
				{
					//System.out.printf("\t[TLDMap] TLD=%s ; Dest=%s...\n", mtr.group(1), mtr.group(2));
					tldMap.put(mtr.group(1).toUpperCase(), mtr.group(2).trim());
				}
				else
				{
					System.err.printf("\t[TLDMap] WARN : Illegal Line = %s!\n", line);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {	
			System.err.printf("\t[TLDMap] Error : %s", e);
			e.printStackTrace();
		} catch (IOException e)
		{
			System.err.printf("\t[TLDMap] Error : %s", e);
			e.printStackTrace();
		}
	}
	
	public TLD retrveTLDFromHostname(String hn)
	{
		String ph[] = hn.split("\\.");
		if(ph.length>2)
		{
			return retrveTLD(String.format("%s.%s", ph[ph.length-2], ph[ph.length-1]));
		}
		else if(ph.length==2)
		{
			return retrveTLD(hn);
		}
		else
		{
			return retrveTLD(hn);
		}
	}
	public TLD retrveTLD(String phn)
	{
		String itms[] = phn.split("\\.");
		if(itms.length==2)
		{
			String tld = itms[1].toUpperCase();
			if(tldMap.containsKey(tld))
			{
				return new TLD(tld, "gTLD", tldMap.get(tld));
			}
			else if(ccMap.isAcceptCC(tld))
			{
				String stld = itms[0].toUpperCase();
				if(tldMap.containsKey(stld))
				{
					return new TLD(phn.toUpperCase(), "ccTLD+gTLD", tldMap.get(stld));
				}
				else
				{
					return new TLD(tld, "ccTLD", ccMap.accessDest(tld));
				}
			}
		}
		else
		{
			System.out.printf("\t[TLDMap] Warn : Illegal phn=%s!\n", phn);
		}
		return new TLD(UNKNOWN_TLD, "Unknown", "");
	}
	
	public static void main(String args[])
	{
		TLDMap tldm = new TLDMap(new CountryMap());
		TLD tld = tldm.retrveTLDFromHostname("www.notexist");
		System.out.printf("\t[Info] TLD=%s\n", tld.tld);
		System.out.printf("\t[Info] %s\n", tld.dest);
		System.out.printf("\t[Info] %s\n", tld.sdest);
	}
	
	public class TLD{		
		public String tld = null;
		public String dest = null;
		public String sdest = null;
		
		public TLD(String tld, String sdest, String dest){this.tld = tld; this.dest=dest; this.sdest=sdest;}
	}
}
