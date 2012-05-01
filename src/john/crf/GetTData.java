package john.crf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import john.crf.token.TToken;
import john.crf.token.TTokenGen;

public class GetTData {
	public static void genTestData() throws Exception {
		
	}
	
	public static void genTrainData() throws Exception {
		String fn = "training_data.txt";
		String tfn = "test_data.txt";
		File fnh = new File(fn);
		File tfnh = new File(tfn);
		if(!fnh.exists()) fnh.createNewFile();
		if(!tfnh.exists()) tfnh.createNewFile();
		
		Random rdm = new Random();
		int labels[] = { 1, 2, 3, 4 };
		TToken token1 = new TToken("A", 1, (float) 0.7, labels);
		TToken token2 = new TToken("B", 2, (float) 0.85, labels);
		TToken token3 = new TToken("C", 3, (float) 0.5, labels);
		TToken token4 = new TToken("D", 4, (float) 0.9, labels);
		TToken token5 = new TToken("E", 1, (float) 0.95, labels);
		TToken token6 = new TToken("F", 2, (float) 0.4, labels);
		TToken token7 = new TToken("G", 3, (float) 1, labels);
		TToken token8 = new TToken("H", 4, (float) 0.6, labels);

		TTokenGen tokenGen = new TTokenGen();
		tokenGen.addTToken(token1, (float) 0.1);
		tokenGen.addTToken(token2, (float) 0.2);
		tokenGen.addTToken(token3, (float) 0.1);
		tokenGen.addTToken(token4, (float) 0.1);
		tokenGen.addTToken(token5, (float) 0.1);
		tokenGen.addTToken(token6, (float) 0.1);
		tokenGen.addTToken(token7, (float) 0.2);
		tokenGen.addLast(token8);

		BufferedWriter bw = new BufferedWriter(new FileWriter(fnh));
		BufferedWriter tbw = new BufferedWriter(new FileWriter(tfnh));
		for (int i = 0; i < 100; i++) {
			int j = 10 + rdm.nextInt(20);
			TToken tk = tokenGen.nextRandom();
			bw.append(String.format("%s:%d", tk.tokenStr, tk.randomLabel()));
			tbw.append(String.format("%s", tk.tokenStr));
			for (int k = 1; k < j; k++) {
				tk = tokenGen.nextRandom();
				bw.append(String.format("-%s:%d", tk.tokenStr, tk.randomLabel()));	
				tbw.append(String.format("-%s", tk.tokenStr));
			}
			bw.append("\r\n");
			tbw.append("\r\n");
		}
		bw.close();
		tbw.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		genTrainData();
	}
}
