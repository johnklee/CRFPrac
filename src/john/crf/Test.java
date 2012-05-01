package john.crf;

import iitb.CRF.CRF;
import iitb.Model.FeatureGenImpl;
import iitb.Utils.Options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import john.crf.data.TIter;
import john.crf.data.TSeq;
import john.crf.features.TLDFeature;

public class Test {
	String 				modelGraphType = "naive";  // "semi-markov" or "naive"
	FeatureGenImpl 		featureGen;
	CRF 				crfModel;
	public int 			nlabels=5;  /*Number of label*/
	Options 			options;
	TIter 				dataIter = null;
	public String		baseDir = "John";
	public String		outDir = "Test";
	
	public Test(Options opts){
		this.options = opts;
	}
	
	public Test(){this(new Options());}

	public void  allocModel() throws Exception {
		featureGen = new FeatureGenImpl(modelGraphType, nlabels);
		
		//TLDFeature tldfetu = new TLDFeature(featureGen);
		//featureGen.addFeature(tldfetu, true);
		
        crfModel=new CRF(featureGen.numStates(),featureGen,options);
	}
	
	public void train() throws Exception
	{
		allocModel();
		featureGen.train(dataIter);
        double featureWts[] = crfModel.train(dataIter);
        System.out.printf("\t[Test] Training done...\n");
        //for(int i=0; i<featureWts.length; i++) System.out.printf("\t[Test] FeatureWts[%d]=%f...\n", i, featureWts[i]); 
        //featureGen.displayModel(featureWts);
        File bfdr = new File(baseDir+"/learntModels/"+outDir);
        if(!bfdr.exists()) bfdr.mkdirs();
        crfModel.write(baseDir+"/learntModels/"+outDir+"/crf.txt");
        featureGen.write(baseDir+"/learntModels/"+outDir+"/features.txt");
	}
	
	public void test(String testDataPath, String outputPath) throws Exception{
		allocModel();
        featureGen.read(baseDir+"/learntModels/"+outDir+"/features.txt");
        crfModel.read(baseDir+"/learntModels/"+outDir+"/crf.txt");
        doTest(testDataPath, outputPath);
	}
	
	public int[] predictSeq(TSeq seq)
	{
		int labels[] = new int[seq.size()];
		crfModel.apply(seq);
        featureGen.mapStatesToLabels(seq);		
		return seq.labels();
	}
	
	protected void outputTaggedData(BufferedWriter bw, TSeq seq) throws Exception{
		if(seq.size()>0)
		{
			bw.append(String.format("%s:%d", seq.x(0), seq.y(0)));
			for(int i=1; i<seq.size(); i++) bw.append(String.format("-%s:%d", seq.x(i), seq.y(i)));
			bw.append("\r\n");
		}
	}
	
	public void doTest(String testDataPath, String outputPath) throws Exception
	{
		File dir=new File(baseDir+"/out/"+outDir);		
        dir.mkdirs();
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
        parseRawdata(testDataPath);
        TSeq seq = null;
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
		HashMap<String,PI> piMap = new HashMap<String,PI>();
		File awPath = new File(answerPath);
		File tgPath = new File(taggedPath);
		if(awPath.exists() && tgPath.exists())
		{
			BufferedReader awRdr = new BufferedReader(new FileReader(awPath));
			BufferedReader tgRdr = new BufferedReader(new FileReader(tgPath));
			String answer = null;
			String tgdrst = null;
			TSeq aSeq = null;
			TSeq tSeq = null;
			int calcLine = 0;
			while((answer=awRdr.readLine())!=null && (tgdrst=tgRdr.readLine())!=null)
			{
				aSeq = new TSeq(answer);
				tSeq = new TSeq(tgdrst);
				if(aSeq.size()!=tSeq.size()) break;
				for(int i=0; i<aSeq.size(); i++)
				{
					if(!aSeq.token(i).equals(tSeq.token(i))) break;
					PI pi = piMap.get(aSeq.token(i)); if(pi==null) pi = new PI();
					if(aSeq.y(i) == tSeq.y(i)) pi.hit();
					else pi.miss();
					piMap.put(aSeq.token(i), pi);
				}
				calcLine++;
			}
			awRdr.close();
			tgRdr.close();
			System.out.printf("\t[Info] Total %d lines is calculated!\n", calcLine);
		}
		return piMap;
	}
	
	public void parseRawdata(String fn)
	{
		List<TSeq> trainReds = new LinkedList<TSeq>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.isEmpty()) continue;
				trainReds.add(new TSeq(line));
			}
			br.close();
			dataIter = new TIter(trainReds);
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
		String trainPath = "training_data.txt";
		String outTagPath = "tagged_data.txt";
		Test testModel = new Test();
		testModel.nlabels = 5;  // Total number of labels.
		testModel.parseRawdata(trainPath);		
		testModel.train(); // Training model
		
		/*Testing model*/
		testModel.test("test_data.txt", outTagPath);
		HashMap<String,PI> piMap = testModel.calcPI(trainPath, outTagPath);
		Iterator<String> keyIter = piMap.keySet().iterator();
		String key = null;
		while(keyIter.hasNext())
		{
			key = keyIter.next();
			System.out.printf("\t[Test] Token(%s) has hit rate=%f...\n", key, piMap.get(key).hitRate());			
		}
	}
}
