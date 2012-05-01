package john.memm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoryMap {
	private String 							ctryConfigPath = "cate.txt";
	private HashMap<Integer,String> 		cateMap = null;
	private HashMap<String,Set<Integer>> 	cgCateMap = null;
	public ECategoryType 					type = ECategoryType.DetailGroup;
	
	public enum ECategoryType{
		CoraseGroup, DetailGroup;
	}

	public CategoryMap(){this(null);}
	public CategoryMap(String configPath)
	{
		if(configPath!=null) this.ctryConfigPath = configPath;
		// Parsing configuration
		cateMap = new HashMap<Integer,String>();		
		cgCateMap = new HashMap<String,Set<Integer>>();
		parsing();		
	}
	
	protected void addCoraseGroup(String cg, int cate)
	{
		Set<Integer> gcat = cgCateMap.get(cg);
		if(gcat==null) gcat = new HashSet<Integer>();
		gcat.add(cate);
		cgCateMap.put(cg, gcat);
	}
	
	protected void parsing()
	{
		File cfgFile = new File(this.ctryConfigPath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(cfgFile));
			Pattern ptn = Pattern.compile("(\\d+)\\s*:(.*):(.*)");
			Pattern ptn2 = Pattern.compile("(\\d+)\\s*:(.*)");
			Matcher mtr = null;
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.trim().isEmpty()) continue;
				mtr = ptn.matcher(line);
				if(mtr.find())
				{
					//System.out.printf("\t[CountryMap] Cate=%s (%s) ; Dest=%s...\n", mtr.group(1), mtr.group(2), mtr.group(3));
					cateMap.put(Integer.valueOf(mtr.group(1)), mtr.group(3).trim());
					addCoraseGroup(mtr.group(2).trim(), Integer.valueOf(mtr.group(1)));
				}
				else
				{
					mtr = ptn2.matcher(line);
					if(mtr.find())
					{
						System.out.printf("\t[CountryMap] Cate=%s (Unknown (UN)) ; Dest=%s...\n", mtr.group(1), mtr.group(2));
						cateMap.put(Integer.valueOf(mtr.group(1)), mtr.group(2).trim());
						addCoraseGroup("Unknown (UN)", Integer.valueOf(mtr.group(1)));
					}
					else
					{
						System.err.printf("\t[CountryMap] WARN : Illegal Line = %s!\n", line);
					}
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
	
	public String retrvDest(int cat){
		String dest = null;
		if(type.equals(ECategoryType.DetailGroup))
		{
			dest = cateMap.get(cat);
		}
		else
		{
			Iterator<String> keyIter = cgCateMap.keySet().iterator();
			String key = null;
			while(keyIter.hasNext())
			{
				key = keyIter.next();
				if(cgCateMap.get(key).contains(cat)) return key;
			}
		}
		return dest!=null?dest:"Unknown";
	}
	
	public String retrvSDest(int cat){
		String dest = null;
		//System.out.printf("\t[CategoryMap] Type=%s", type);
		if(type.equals(ECategoryType.DetailGroup))
		{
			dest = String.format("Cat(%02d)", cat);
		}
		else
		{
			Iterator<String> keyIter = cgCateMap.keySet().iterator();
			String key = null;
			while(keyIter.hasNext())
			{
				key = keyIter.next();
				if(cgCateMap.get(key).contains(cat)) return key;
			}
		}
		return dest!=null?dest:"Unknown";
	}
	
	public static void main(String args[])
	{
		CategoryMap cateMap = new CategoryMap();
	}
}
