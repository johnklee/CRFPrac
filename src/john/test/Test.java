package john.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import john.memm.model.AbstModel.PI;
import john.memm.model.FOrderModel;
import john.memm.model.MOrderModel;

public class Test {

	public static void testFOModel() throws Exception
	{
		FOrderModel memmModel = new FOrderModel(4);
		String trainPath = "training_data.txt";
		String outTagPath = "memm_tagged_data.txt";
		String statisticDataRaw = "statistic_data.raw";
				
		/*Testing model*/
		memmModel.loadStatisticData(statisticDataRaw);
		memmModel.test("test_data.txt", outTagPath);
		HashMap<String,PI> piMap = memmModel.calcPI(trainPath, outTagPath);
		Iterator<String> keyIter = piMap.keySet().iterator();
		String key = null;
		while(keyIter.hasNext())
		{
			key = keyIter.next();
			System.out.printf("\t[MEMM-Mode] Token(%s) has hit rate=%f...\n", key, piMap.get(key).hitRate());			
		}
	}
	
	public static void testMOModel(int order) throws Exception
	{
		MOrderModel memmModel = new MOrderModel(order, 4);
		String trainPath = "training_data.txt";
		String outTagPath = String.format("memm_order%d_tagged_data.txt", order);
		String statisticDataRaw = String.format("statistic_order%d_data.raw", order);
		
		memmModel.parseRawdata(trainPath);		
		memmModel.train();
		memmModel.saveStatisticData(statisticDataRaw);
				
		/*Testing model*/
		memmModel.loadStatisticData(statisticDataRaw);
		memmModel.test("test_data.txt", outTagPath);
		HashMap<String,PI> piMap = memmModel.calcPI(trainPath, outTagPath);
		Iterator<String> keyIter = piMap.keySet().iterator();
		String key = null;
		while(keyIter.hasNext())
		{
			key = keyIter.next();
			System.out.printf("\t[MEMM-Mode] Token(%s) has hit rate=%f...\n", key, piMap.get(key).hitRate());			
		}
	}
	
	public static void main(String[] args) throws Exception{
		//testFOModel();
		//testMOModel(1);
		String test = "item.rakuten.co.jp/kojika-land/z_uw_001/ 1227 58:1";
		String prf = test.substring(0, test.lastIndexOf(":"));
		String lf = test.substring(test.lastIndexOf(":")+1, test.length());
		System.out.printf("%s  <>  %s", prf, lf);
	}

}
