package john.main;

import gays.tools.ArguParser;

import java.io.File;
import java.util.HashMap;

import john.main.enums.EModel;
import john.main.enums.EWType;
import john.memm.model.HNameModel;
import john.memm.model.AbstModel.PI;

public class Main {
	public static String VERSION = "0.1";
	
	public static void main(String args[]) throws Exception
	{
		HashMap<String,String> argDef = new HashMap<String,String>();
		argDef.put("-m,--Model", "Using Model (ex. CRF, MEMM)");
		argDef.put("-s,--StatData", "Path to load model data.");
		argDef.put("-f,--FeatureData", "Path to load feature data.");
		argDef.put("-t,--Type", "Working type (ex. train, test, tag");
		argDef.put("-i,--Input", "Input File. Depending on different working type, has different meaning.");
		argDef.put("-o,--Output", "Output path. Depending on different working type, has different meaning.");
		argDef.put("-h,--Help", "Show help message");
		
		ArguParser arguPsr = new ArguParser(argDef, args);
		if(args.length==0 || arguPsr.isSet("-h"))
		{
			System.out.printf("NTU NLP Lab tools for xxx project since 2012/04. v%s\n", VERSION);
			System.out.println("==============================================================");
			arguPsr.showArgus();
			System.out.println("==============================================================");
			return;
		}
		
		EModel model = null;
		EWType type = null;
		
		type = EWType.tf(arguPsr.getArguValue("-t"));		
		System.out.printf("\t[Info] Using Type=%s...\n", type);
		
		model = EModel.tf(arguPsr.getArguValue("-m"));
		System.out.printf("\t[Info] Using Model=%s...\n", model);
		
		if(type.equals(EWType.TEST))
		{
			/*Testing Mode*/
			File 				modelDataPath = null;
			File 				featDataPath = null;
			File 				testRawFile = null;
			
			//1. Setup model statistic data path
			if(arguPsr.isSet("-s")) modelDataPath = new File(arguPsr.getArguValue("-s"));
			else {System.out.printf("\t[Error] Please give model statistic data path! (-s=...)\n"); return;}
			if(modelDataPath.exists()) System.out.printf("\t[Info] Model statistic data=%s...\n", modelDataPath.getAbsolutePath());
			else {System.out.printf("\t[Error] Model statistic data path doesn't exist! (%s)\n", modelDataPath.getAbsolutePath()); return;}			
			
			//2. Setup test raw file path
			if(arguPsr.isSet("-i")) testRawFile = new File(arguPsr.getArguValue("-i"));
			else {System.out.printf("\t[Error] Please give test raw data path! (-i=...)\n"); return;}
			if(testRawFile.exists()) System.out.printf("\t[Info] Test raw data path=%s...\n", testRawFile.getAbsolutePath());
			else {System.out.printf("\t[Error] Test raw data doesn't exist! (%s)!", testRawFile.getAbsolutePath()); return;}
			
			//3. Decide model
			if(model.equals(EModel.MEMM))
			{
				//4. Setup features loading path
				if(arguPsr.isSet("-f")) featDataPath = new File(arguPsr.getArguValue("-f"));
				else {System.out.printf("\t[Error] Please give feature statistic data path! (-f=...)\n"); return;}
				if(featDataPath.exists() && featDataPath.isDirectory()) System.out.printf("\t[Info] Feature loading path=%s...\n", featDataPath.getAbsolutePath());
				else {System.out.printf("\t[Error] Feature loading path doesn't exist or it isn't a folder! (%s)\n", featDataPath.getAbsolutePath()); return;}
				
				john.memm.model.AbstModel			umodel = new HNameModel(-1);
				umodel.loadStatisticData(modelDataPath.getAbsolutePath());
				umodel.allocFeat();
				umodel.loadFeature(featDataPath.getAbsolutePath());
				modelDataPath = featDataPath = null;
				PI pi = umodel.validate(testRawFile.getAbsolutePath(), String.format("%s_tagged", testRawFile.getName()), false);
				System.out.printf("\t[MEMM-Mode] %s with total label hit rate=%f (Hit=%d;Miss=%d)!\n", testRawFile.getName(), pi.hitRate(), pi.hit, pi.miss);
				System.out.printf("\t[MEMM-Mode] %s with total seque hit rate=%f (Hit=%d;Miss=%d)!\n", testRawFile.getName(), pi.shitRate(), pi.s_hit, pi.s_miss);
			}
			else if(model.equals(EModel.CRF))
			{
				john.crf.AbstModel					umodel = new john.crf.HNameModel();
				umodel.nlabels = 100;
				umodel.loadStatisticData(modelDataPath.getAbsolutePath());
				john.crf.AbstModel.PI pi = umodel.validate(testRawFile.getAbsolutePath(), String.format("%s_tagged", testRawFile.getName()));
				System.out.printf("\t[MEMM-Mode] %s with total label hit rate=%f (Hit=%d;Miss=%d)!\n", testRawFile.getName(), pi.hitRate(), pi.hit, pi.miss);
				System.out.printf("\t[MEMM-Mode] %s with total seque hit rate=%f (Hit=%d;Miss=%d)!\n", testRawFile.getName(), pi.shitRate(), pi.s_hit, pi.s_miss);
			}
			
		}
		else if(type.equals(EWType.TAGGED))
		{
			/*Tagged Mode*/
		}
		else if(type.equals(EWType.TRAIN))
		{
			/*Train Mode*/
			File 				modelDataPath = null;
			File 				featDataPath = null;
			File 				trainFilePath = null;
			File 				outputFilePath = null;
			
			//1. Setup model statistic data path
			if(arguPsr.isSet("-s")){
				modelDataPath = new File(arguPsr.getArguValue("-s"));
				if(modelDataPath.exists()) System.out.printf("\t[Info] Model statistic data=%s...\n", modelDataPath.getAbsolutePath());
				else {System.out.printf("\t[Error] Model statistic data path doesn't exist! (%s)\n", modelDataPath.getAbsolutePath()); return;}
			}		
			
			//2. Setup features loading path
			if(arguPsr.isSet("-f")) {
				featDataPath = new File(arguPsr.getArguValue("-f"));
				if(featDataPath.exists() && featDataPath.isDirectory()) System.out.printf("\t[Info] Feature loading path=%s...\n", featDataPath.getAbsolutePath());
				else {System.out.printf("\t[Error] Feature loading path doesn't exist or it isn't a folder! (%s)\n", featDataPath.getAbsolutePath()); return;}
			}	
			
			//3. Setup train file path
			if(arguPsr.isSet("-i")) trainFilePath = new File(arguPsr.getArguValue("-i"));
			else {System.out.printf("\t[Error] Please give train data path! (-i=...)\n"); return;}
			if(trainFilePath.exists()) System.out.printf("\t[Info] Test raw data path=%s...\n", trainFilePath.getAbsolutePath());
			else {System.out.printf("\t[Error] Train data doesn't exist! (%s)!", trainFilePath.getAbsolutePath()); return;}
			
			//4. Setup output file path
			if(arguPsr.isSet("-o")) outputFilePath = new File(arguPsr.getArguValue("-o"));
			else {System.out.printf("\t[Error] Please give output statistic data path! (-o=...)\n"); return;}			
			
			//5. Decide model
			if(model.equals(EModel.MEMM))
			{
				//6. Train and output statistic data
				john.memm.model.AbstModel			umodel = new HNameModel(-1);
				if(modelDataPath!=null) umodel.loadStatisticData(modelDataPath.getAbsolutePath());
				
				if(featDataPath!=null) umodel.loadFeature(featDataPath.getAbsolutePath());
				else umodel.allocFeat();
				
				umodel.parseRawdata(trainFilePath.getAbsolutePath());
				umodel.train();
				umodel.saveStatisticData(new File(outputFilePath, "stat.raw").getAbsolutePath());
				umodel.saveFeature(outputFilePath.getAbsolutePath());
			}
			else if(model.equals(EModel.CRF));
			{
				
			}
		}
		else
		{
			System.out.printf("\t[Info] Unknown type=%s", type);
		}
	}
}
