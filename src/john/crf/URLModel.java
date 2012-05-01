package john.crf;

import iitb.CRF.CRF;
import iitb.Model.FeatureGenImpl;
import iitb.Utils.Options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import john.crf.data.TSeq;
import john.crf.data.URLIter;
import john.crf.data.URLSeq;
import john.crf.utils.TimeStr;

/**
 * BD : URL Feature for CRF Model
 * SD :
 * 		/tmpdisk1/ycjuan/rakuten_trail/even_training         11/1~7
 *		/tmpdisk1/ycjuan/rakuten_trail/even_testing          11/8~14
 *
 * @author John
 *
 */
public class URLModel {
	String 				modelGraphType = "naive";  // "semi-markov" or "naive"
	FeatureGenImpl 		featureGen;
	CRF 				crfModel;
	public int 			nlabels=5;  /*Number of label*/
	Options 			options;
	URLIter 			dataIter = null;
	public String		baseDir = "IRProj";
	public String		outDir = "Test";
	
	public URLModel(Options opts){ this.options = opts;}
	
	public URLModel(){this(new Options());}

	public void  allocModel() throws Exception {
		featureGen = new FeatureGenImpl(modelGraphType, nlabels);
		
		//TLDFeature tldfetu = new TLDFeature(featureGen);
		//featureGen.addFeature(tldfetu, true);
		
        crfModel=new CRF(featureGen.numStates(),featureGen,options);
	}
	
	public void train() throws Exception
	{
		long st = System.currentTimeMillis();
		allocModel();
		featureGen.train(dataIter);
        double featureWts[] = crfModel.train(dataIter);
        System.out.printf("\t[Test] Training done...\n");
        //for(int i=0; i<featureWts.length; i++) System.out.printf("\t[Test] FeatureWts[%d]=%f...\n", i, featureWts[i]); 
        //featureGen.displayModel(featureWts);        
        System.out.printf("\t[Test] Total spending time : %s...\n", TimeStr.toStriing(System.currentTimeMillis()-st));
	}
	
	public void saveModel(String savePath) throws Exception
	{
		File bfdr = new File(savePath);
        if(!bfdr.exists()) bfdr.mkdirs();
        crfModel.write(new File(bfdr, "crf.txt").getAbsolutePath());
        featureGen.write(new File(bfdr, "features.txt").getAbsolutePath());
	}
	
	public void loadModel(String path) throws Exception
	{
		allocModel();
		File bfdr = new File(path);       
        crfModel.read(new File(bfdr, "crf.txt").getAbsolutePath());
        featureGen.read(new File(bfdr, "features.txt").getAbsolutePath());
	}
	
	public void test(String testDataPath, String outputPath) throws Exception{
		//allocModel();
        //featureGen.read(baseDir+"/learntModels/"+outDir+"/features.txt");
        //crfModel.read(baseDir+"/learntModels/"+outDir+"/crf.txt");
        doTest(testDataPath, outputPath);
	}
	
	public int[] predictSeq(URLSeq seq)
	{
		int labels[] = new int[seq.size()];
		crfModel.apply(seq);
        featureGen.mapStatesToLabels(seq);		
		return seq.labels();
	}
	
	protected void outputTaggedData(BufferedWriter bw, URLSeq seq) throws Exception{
		if(seq.size()>0)
		{
			bw.append(String.format("%s:%d", seq.x(0), seq.y(0)));
			for(int i=1; i<seq.size(); i++) bw.append(String.format(" %s:%d", seq.x(i), seq.y(i)));
			bw.append("\r\n");
		}
	}
	
	public void doTest(String testDataPath, String outputPath) throws Exception
	{
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
        parseTestData(testDataPath);
        URLSeq seq = null;
        int labels[] = null;
        while(dataIter.hasNext())
        {
        	seq = dataIter.nextSeq();
        	labels = predictSeq(seq);
        	outputTaggedData(bw, seq);
        }
        bw.close();
	}
	
	public HashMap<String,PI> calcPI(String answerPath, String taggedPath) throws Exception 
	{
		//System.out.printf("\t[URLModel] Answer file path=%s...\n", answerPath);
		//System.out.printf("\t[URLModel] Tagged file path=%s...\n", taggedPath);
		HashMap<String,PI> piMap = new HashMap<String,PI>();
		File awPath = new File(answerPath);
		File tgPath = new File(taggedPath);
		PI tpi = new PI();
		if(awPath.exists() && tgPath.exists())
		{
			BufferedReader awRdr = new BufferedReader(new FileReader(awPath));
			BufferedReader tgRdr = new BufferedReader(new FileReader(tgPath));
			String answer = null;
			String tgdrst = null;
			URLSeq aSeq = null;
			URLSeq tSeq = null;
			int calcLine = 0;
			while((answer=awRdr.readLine())!=null && (tgdrst=tgRdr.readLine())!=null)
			{
				//System.out.printf("\t[URLModel] Answer seq = %s...\n", answer);
				//System.out.printf("\t[URLModel] tagged seq = %s...\n", tgdrst);
				aSeq = new URLSeq(); aSeq.loadTestData(answer, false);
				tSeq = new URLSeq(); tSeq.loadTestData(tgdrst, false);
				//System.out.printf("\t[URLModel] answer seq=%d ; test seq=%d...\n", aSeq.size(), tSeq.size());
				if(aSeq.size()!=tSeq.size()) break;
				for(int i=0; i<aSeq.size(); i++)
				{
					if(!aSeq.token(i).equals(tSeq.token(i))) break;
					PI pi = piMap.get(aSeq.token(i)); if(pi==null) pi = new PI();
					if(aSeq.y(i) == tSeq.y(i)) {
						pi.hit();
						tpi.hit();
					}
					else {
						pi.miss();
						tpi.miss();
					}
					piMap.put(aSeq.token(i), pi);
				}
				calcLine++;
			}
			piMap.put("Total", tpi);
			awRdr.close();
			tgRdr.close();
			System.out.printf("\t[Info] Total %d lines is calculated!\n", calcLine);
		}
		return piMap;
	}
	
	public void parseTestData(String fn)
	{
		List<URLSeq> trainReds = new LinkedList<URLSeq>();
		try
		{
			File trainData = new File(fn);
			BufferedReader br = new BufferedReader(new FileReader(trainData));
			String line = null;
			URLSeq seq = null;
			while((line=br.readLine())!=null)
			{
				if(line.trim().isEmpty()) continue;
				seq = new URLSeq();				
				seq.loadTestData(line, true);
				if(seq.length()>0) trainReds.add(seq);
			}
			
			br.close();
			dataIter = new URLIter(trainReds);
			System.out.printf("\t[Test] Total %d sequence data...\n", dataIter.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseRawdata(String fn)
	{
		List<URLSeq> trainReds = new LinkedList<URLSeq>();
		try
		{
			File trainData = new File(fn);
			BufferedReader br = new BufferedReader(new FileReader(trainData));
			String line = null;
			//List<URLSeq> seques = new ArrayList<URLSeq>();
			URLSeq seq = null;
			while((line=br.readLine())!=null)
			{
				seq = new URLSeq(line);
				if(seq.length()>0) trainReds.add(seq);
			}
			
			br.close();
			if(dataIter==null) dataIter = new URLIter(trainReds);
			else dataIter.addSeqs(trainReds);
			System.out.printf("\t[Test] Total %d sequence data...\n", dataIter.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public class PI /*Performance Index*/
	{
		public int hit = 0;
		public int miss = 0;
		public int size(){return hit+miss;}
		public void hit(){hit++;}
		public void miss(){miss++;}
		public float hitRate() {return ((float)hit)/(hit+miss);}
		public float missRate() {return 1-hitRate();}
	}
	
	public static void main(String[] args) throws Exception{
		/*Training model*/
		String trainPath = "Datas/Train/20101101_0000.shopping_trail";
		String trainPath2 = "Datas/Train/20101101_0030.shopping_trail";
		String outTagPath = "Datas/Test/20101101_0100.shopping_trail_tagged";
		String answerPath = "Datas/Test/20101101_0100.shopping_trail_answer";
		String saveModelPath = "IR/Model/URL";
		URLModel testModel = new URLModel();
		testModel.nlabels = 100;  // Total number of labels.
		testModel.parseRawdata(trainPath);
		testModel.parseRawdata(trainPath2);
		testModel.train(); // Training model
		testModel.saveModel(saveModelPath);
		
		
		/*Testing model*/
		testModel.loadModel(saveModelPath);
		testModel.test("Datas/Test/20101101_0100.shopping_trail_test", outTagPath);
		HashMap<String,PI> piMap = testModel.calcPI(answerPath, outTagPath);
		PI tpi = piMap.remove("Total");
		Iterator<String> keyIter = piMap.keySet().iterator();
		String key = null;
		while(keyIter.hasNext())
		{
			key = keyIter.next();
			System.out.printf("\t[Test] Token(%s) has hit rate=%f...\n", key, piMap.get(key).hitRate());			
		}
		System.out.printf("\t[Test] Total hit rate=%f (%d)!\n", tpi.hitRate(), tpi.hit+tpi.miss);
	}
}
