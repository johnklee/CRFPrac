package john.crf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SComtls {
	public static int lineCount(File f)
	{
		int cnt = 0;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			while((line=br.readLine())!=null)
			{
				line = line.trim();
				if(line.startsWith("#") || line.isEmpty()) continue;
				cnt++;
			}
			br.close();
		}
		catch(Exception e){e.printStackTrace();}
		return cnt;
	}
}
