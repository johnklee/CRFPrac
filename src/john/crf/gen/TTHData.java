package john.crf.gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import john.crf.data.HNSeq;

public class TTHData {
	public static void main(String[] args) throws Exception {
		File trainFile = new File("Datas/Train/20101102_0000.shopping_trail");
		File outTestfile = new File("Datas/Test/HN/20101102_0000.shopping_trail_test");
		File outAnswerFile = new File("Datas/Test/HN/20101102_0000.shopping_trail_answer");
		
		if(trainFile.exists())
		{
			BufferedReader br = new BufferedReader(new FileReader(trainFile));
			if(!outTestfile.exists()) outTestfile.createNewFile();
			if(!outAnswerFile.exists()) outAnswerFile.createNewFile();
			BufferedWriter tbw = new BufferedWriter(new FileWriter(outTestfile));
			BufferedWriter abw = new BufferedWriter(new FileWriter(outAnswerFile));
			String line = null;
			HNSeq seq = null;
			StringBuffer testBuf = new StringBuffer();
			StringBuffer ansBuf = new StringBuffer();
			while((line=br.readLine())!=null)
			{
				if(line.trim().isEmpty()) continue;
				seq = new HNSeq(line);
				
				testBuf.append(seq.x(0));
				ansBuf.append(String.format("%s:%s", seq.x(0), seq.y(0)));
				for(int i=1; i<seq.size(); i++)
				{
					testBuf.append(String.format(" %s", seq.x(i)));
					ansBuf.append(String.format(" %s:%s", seq.x(i), seq.y(i)));
				}
				testBuf.append("\r\n");
				ansBuf.append("\r\n");
			}
			tbw.write(testBuf.toString().trim());
			abw.write(ansBuf.toString().trim());
			br.close();
			tbw.close();
			abw.close();
		}
		else
		{
			System.out.printf("\t[TTData] Train file doesn't exist! %s\n", trainFile.getAbsoluteFile());
		}
	}

}
