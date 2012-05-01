package john.memm.model;

import java.io.File;
import java.util.HashMap;

import john.memm.model.AbstModel.PI;

public class Main {
	public static void main(String[] args) throws Exception{
		String savePath = "IR/Model/MEMM/HN/DAY_11_07";
		String saveModePath = savePath+"/stat.raw";
		File trainFolder = new File("Datas/Train/2010_11_07");
		
		//String testFdr = 	"Datas/Test/2010_11_08/";
		String testPath = 	"Datas/Test/2010_11_08/20101108_2330.shopping_trail";
		String outTagPath = "Datas/Test/2010_11_08/20101108_0000.shopping_trail_tagged";
				
		AbstModel memmModel = new HNameModel(100);
		
		memmModel.loadStatisticData(saveModePath);
		memmModel.loadFeature(savePath);
		
		PI pi = memmModel.validate(testPath, String.format("%s_tagged", testPath), true);
		System.out.printf("\t[MEMM-Mode] %s with total hit rate=%f (Hit=%d;Miss=%d)!\n", testPath, pi.hitRate(), pi.hit, pi.miss);
		
		/*File testFdr = new File("Datas/Test/2010_11_08/");
		for(File f:testFdr.listFiles())
		{
			if(f.getAbsolutePath().endsWith("_tagged")) continue;
			PI pi = memmModel.validate(f.getAbsolutePath(), String.format("%s_tagged", f.getAbsolutePath()));
			System.out.printf("\t[MEMM-Mode] %s with total hit rate=%f (Hit=%d;Miss=%d)!\n", f.getAbsolutePath(), pi.hitRate(), pi.hit, pi.miss);
		}*/
	}
	
	public static void testHNameModel() {
		String savePath = "IR/Model/MEMM/HN/DAY_11_07";
		String saveModePath = savePath+"/stat.raw";
		File trainFolder = new File("Datas/Train/2010_11_07");
		
		String testPath = 	"Datas/Test/HN/20101101_0100.shopping_trail_test";
		String outTagPath = "Datas/Test/HN/20101101_0100.shopping_trail_tagged";
		String answerPath = "Datas/Test/HN/20101101_0100.shopping_trail_answer";
				
		AbstModel memmModel = new HNameModel(100);
				
		/*
		memmModel.allocFeat();
		for(File f:trainFolder.listFiles())
		{
			System.out.printf("\t[Main] Training from %s...\n", f.getAbsolutePath());
			memmModel.train(f);
		}
		memmModel.saveStatisticData(saveModePath);
		memmModel.saveFeature(savePath);*/
				
		try
		{
			memmModel.loadStatisticData(saveModePath);
			memmModel.loadFeature(savePath);
			memmModel.test(testPath, outTagPath);
			HashMap<String,PI> piMap = memmModel.calcPI(answerPath, outTagPath);
			PI tpi = piMap.remove("Total");
			/*Iterator<String> keyIter = piMap.keySet().iterator();
			String key = null;
			while(keyIter.hasNext())
			{
				key = keyIter.next();
				System.out.printf("\t[MEMM-Mode] Token(%s) has hit rate=%f...\n", key, piMap.get(key).hitRate());			
			}*/
			System.out.printf("\t[MEMM-Mode] Total hit rate=%f (%d)!\n", tpi.hitRate(), tpi.hit+tpi.miss);
		}
		catch(Exception e){e.printStackTrace();}
	}
}
