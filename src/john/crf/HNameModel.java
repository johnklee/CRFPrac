package john.crf;

import iitb.CRF.CRF;
import iitb.CRF.DataSequence;
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

import john.crf.AbstModel.PI;
import john.crf.data.HNIter;
import john.crf.data.HNSeq;
import john.crf.data.TSeq;
import john.crf.utils.TimeStr;
import john.memm.feature.IFeature;

public class HNameModel extends AbstModel{
	String 				modelGraphType = "naive";  // "semi-markov" or "naive"
	FeatureGenImpl 		featureGen;
	CRF 				crfModel;	
	Options 			options;
	HNIter 				dataIter = null;
	public String		baseDir = "IRProj";
	public String		outDir = "Test";
	
	public HNameModel(Options opts){ this.options = opts;}
	
	public HNameModel(){this(new Options());}

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
	
	protected void outputTaggedData(BufferedWriter bw, HNSeq seq) throws Exception{
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
        HNSeq seq = null;
        int labels[] = null;
        while(dataIter.hasNext())
        {
        	seq = dataIter.nextSeq();
        	labels = predictSeq(seq);
        	outputTaggedData(bw, seq);
        }
        bw.close();
	}

	
	public void parseTestData(String fn)
	{
		List<HNSeq> trainReds = new LinkedList<HNSeq>();
		try
		{
			File trainData = new File(fn);
			BufferedReader br = new BufferedReader(new FileReader(trainData));
			String line = null;
			HNSeq seq = null;
			while((line=br.readLine())!=null)
			{
				if(line.trim().isEmpty()) continue;
				seq = new HNSeq();				
				seq.loadTestData(line, true);
				if(seq.length()>0) trainReds.add(seq);
			}
			
			br.close();
			dataIter = new HNIter(trainReds);
			System.out.printf("\t[Test] Total %d sequence data...\n", dataIter.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseRawdata(String fn)
	{
		List<HNSeq> trainReds = new LinkedList<HNSeq>();
		try
		{
			File trainData = new File(fn);
			BufferedReader br = new BufferedReader(new FileReader(trainData));
			String line = null;
			//List<URLSeq> seques = new ArrayList<URLSeq>();
			HNSeq seq = null;
			while((line=br.readLine())!=null)
			{
				seq = new HNSeq(line);
				if(seq.length()>0) trainReds.add(seq);
			}
			
			br.close();
			if(dataIter==null) dataIter = new HNIter(trainReds);
			else dataIter.addSeqs(trainReds);
			System.out.printf("\t[Test] Total %d sequence data...\n", dataIter.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public HashMap<String,PI> calcPI(String answerPath, String taggedPath) throws Exception 
	{
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
			HNSeq aSeq = null;
			HNSeq tSeq = null;
			int calcLine = 0;
			while((answer=awRdr.readLine())!=null && (tgdrst=tgRdr.readLine())!=null)
			{
				aSeq = new HNSeq();
				tSeq = new HNSeq();
				aSeq.loadTestData(answer, false);
				tSeq.loadTestData(tgdrst, false);
				//System.out.printf("\t[Info] Answer seq has %d ; Test seq has %d...\n", aSeq.size(), tSeq.size());
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
						pi.miss(aSeq.y(i), tSeq.y(i));
						tpi.miss(aSeq.y(i), tSeq.y(i));
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
	
	public static void main(String[] args) throws Exception{
		/*Training model*/
		String trainPath = 	"Datas/Train/20101101_0000.shopping_trail";
		String trainPath2 = "Datas/Train/20101101_0030.shopping_trail";
		String outTagPath = "Datas/Test/HN/20101102_0000.shopping_trail_tagged";
		String answerPath = "Datas/Test/HN/20101102_0000.shopping_trail_answer";
		String testPath = 	"Datas/Test/HN/20101102_0000.shopping_trail_test";
		String saveModelPath = "IR/Model/HN/";
		String saveFeatPath = "IR/Model/HN/";
		HNameModel testModel = new HNameModel();
		testModel.nlabels = 100;  // Total number of labels.
		/*testModel.parseRawdata(trainPath);	
		testModel.parseRawdata(trainPath2);
		testModel.train(); // Training model
		testModel.saveModel(saveModelPath);*/
		
		
		/*Testing model*/
		testModel.loadModel(saveModelPath);
		testModel.test(testPath, outTagPath);
		HashMap<String,PI> piMap = testModel.calcPI(answerPath, outTagPath);
		PI tpi = piMap.remove("Total");
		Iterator<String> keyIter = piMap.keySet().iterator();
		String key = null;
		while(keyIter.hasNext())
		{
			key = keyIter.next();
			PI pi = piMap.get(key);
			if(pi.hitRate()<1)
			{
				System.out.printf("\t[Test] Token(%s) has hit rate=%f(%d)...\n", key, piMap.get(key).hitRate(), pi.miss+pi.hit);
				HashMap<Integer,HashMap<Integer,Integer>> mtMap = pi.missTagStat;
				Iterator<Integer> mtKeyIter = mtMap.keySet().iterator();
				int ak = -1;
				while(mtKeyIter.hasNext())
				{
					ak = mtKeyIter.next();
					System.out.printf("\t[Test]     Answer tag(%d) has below miss distribution :\n", ak);
					HashMap<Integer,Integer> mdist = mtMap.get(ak);
					Iterator<Integer> mdKeyIter = mdist.keySet().iterator();
					int mk = -1;
					while(mdKeyIter.hasNext())
					{
						mk = mdKeyIter.next();
						System.out.printf("\t\t\t%d -> %d...\n", mk, mdist.get(mk));
					}
				}
			}
		}
		System.out.printf("\t[Test] Total hit rate=%f (%d)!\n", tpi.hitRate(), tpi.hit+tpi.miss);
		HashMap<Integer,HashMap<Integer,Integer>> mtMap = tpi.missTagStat;
		Iterator<Integer> mtKeyIter = mtMap.keySet().iterator();
		int ak = -1;
		while(mtKeyIter.hasNext())
		{
			ak = mtKeyIter.next();
			System.out.printf("\t[Test]     Answer tag(%d) has below miss distribution :\n", ak);
			HashMap<Integer,Integer> mdist = mtMap.get(ak);
			Iterator<Integer> mdKeyIter = mdist.keySet().iterator();
			int mk = -1;
			while(mdKeyIter.hasNext())
			{
				mk = mdKeyIter.next();
				System.out.printf("\t\t\t%d -> %d...\n", mk, mdist.get(mk));
			}
		}
	}

	@Override
	public void allocFeat() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void allocFeat(IFeature feat, double wt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveStatisticData(String fn) throws Exception {
		saveModel(fn);		
	}

	@Override
	public void loadStatisticData(String filename) throws Exception {
		loadModel(filename);
		//crfModel.read(new File(filename).getAbsolutePath());
	}

	@Override
	public int[] predictSeq(DataSequence seq, PI pi)
	{
		int labels[] = new int[seq.length()];
		for(int i=0; i<seq.length(); i++) labels[i] = seq.y(i);
		crfModel.apply(seq);
        featureGen.mapStatesToLabels(seq);
        for(int i=0; i<seq.length(); i++)
        {
        	if(labels[i] != seq.y(i))
        	{
        		pi.miss(); labels[i] = seq.y(i);
        		if(i+1==seq.length()) pi.smiss();
        	}
        	else
        	{
        		pi.hit();
        		if(i+1==seq.length()) pi.shit();
        	}
        }
		return labels;
	}
	
	@Override
	public int[] predictSeq(DataSequence seq) {
		int labels[] = new int[seq.length()];
		crfModel.apply(seq);
        featureGen.mapStatesToLabels(seq);
        for(int i=0; i<seq.length(); i++) labels[i] = seq.y(i);
		return labels;
	}

	@Override
	public PI validate(String testDataPath, String outputPath) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(testDataPath)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
		
		PI pi = new PI();
		String line = null;
		HNSeq seq = null;
		int seqCnt = 0;
		while((line=br.readLine())!=null)
		{
			seq = new HNSeq(line);
			if(seq.size()>0)
			{
				predictSeq(seq, pi);
	        	outputTaggedData(bw, seq);
	        	seqCnt++;
			}
		}
		bw.close();
		br.close();
		System.out.printf("\t[CRF-Mode] Total processing seq=%d...\n", seqCnt);
		return pi;
	}
}
