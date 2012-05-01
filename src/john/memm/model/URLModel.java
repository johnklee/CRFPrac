package john.memm.model;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import john.crf.URLModel.PI;
import john.crf.data.URLIter;
import john.crf.data.URLSeq;
import john.memm.feature.IFeature;
import john.memm.feature.PBFeature;
import john.memm.stat.StatisticDataWraper;

public class URLModel extends AbstModel{
	protected URLIter 										dataIter = null;
	protected int											nlabels = -1;
	protected HashMap<String,HashMap<Integer,Integer>> 		trainStore = null;
	public static boolean 									bDebug = true;
	protected HashMap<IFeature,Double>						featureList;
	protected Set<Integer>									tagSet = null;
	
	public URLModel(int labelSize){
		this.nlabels = labelSize+1; 
		featureList = new HashMap<IFeature,Double>();
		tagSet = new HashSet<Integer>();
	}
	
	public URLModel(Set<Integer> tagSet)
	{
		this.nlabels = tagSet.size()+1; 
		featureList = new HashMap<IFeature,Double>();
		this.tagSet = tagSet;
	}
	
	public static String entry(String token, int prevTag) {return String.format("%s_%d", token, prevTag);}
	public void train() throws Exception
	{
		URLSeq seq = null;
		trainStore = new HashMap<String,HashMap<Integer,Integer>>();
		while(dataIter.hasNext())
		{
			seq = dataIter.nextSeq();
			int prvtag = -1;
			for(int i=0; i<seq.size(); i++)
			{
				String entry = entry(seq.token(i), prvtag);
				HashMap<Integer,Integer> statData = trainStore.get(entry);
				if(statData==null) statData = new HashMap<Integer,Integer>();
				if(statData.containsKey(seq.y(i))) statData.put(seq.y(i), statData.get(seq.y(i))+1);
				else statData.put(seq.y(i), 1);
				trainStore.put(entry, statData);
				prvtag = seq.y(i);
				tagSet.add(seq.y(i));
			}
		}
		nlabels = tagSet.size()+1;
		
		System.out.printf("\t[MEMM-Model] bDebug=%b...\n", bDebug);
		if(bDebug)
		{
			System.out.printf("\t[MEMM-Model] Training is done!\n");
			System.out.printf("\t[MEMM-Model] Statistic data :\n");
			Iterator<String> keyIter = trainStore.keySet().iterator();
			String key = null;
			while(keyIter.hasNext())
			{
				key = keyIter.next();
				System.out.printf("\t[MEMM-Model] Show Feature-%s :\n", key);
				HashMap<Integer,Integer> statData = trainStore.get(key);
				int klabel = -1;
				Iterator<Integer> labIter = statData.keySet().iterator();				
				while(labIter.hasNext())
				{
					klabel = labIter.next();
					System.out.printf("\t\tLabel(%d) --> %d...\n", klabel, statData.get(klabel));
				}
			}
		}
	}
	
	public void allocFeat(){
		this.allocFeat(new PBFeature(trainStore, tagSet), 1);
		//this.allocFeat(new TestFeature(trainStore, tagSet), 1);
	}
	
	public void test(String testDataPath, String outputPath) throws Exception
	{allocFeat(); doTest(testDataPath, outputPath);}
	
	protected void outputTaggedData(BufferedWriter bw, URLSeq seq) throws Exception{
		if(seq.size()>0)
		{
			bw.append(String.format("%s:%d", seq.x(0), seq.y(0)));
			for(int i=1; i<seq.size(); i++) bw.append(String.format(" %s:%d", seq.x(i), seq.y(i)));
			bw.append("\r\n");
		}
	}

	protected int predictTag(String token, int prevTag, int pos)
	{
		HashMap<Integer,List<Double>> distMap = new HashMap<Integer,List<Double>>();
		Iterator<IFeature> keyIter = featureList.keySet().iterator();
		IFeature feat = null;
		while(keyIter.hasNext())
		{
			feat = keyIter.next();
			int predictTag = feat.label(token, prevTag, pos);
			if(distMap.containsKey(predictTag)) distMap.get(predictTag).add(featureList.get(feat));
			else
			{
				List<Double> wtList = new LinkedList<Double>();
				wtList.add(featureList.get(feat));
				distMap.put(predictTag, wtList);
			}
		}
		/*Logistic Regression*/
		int maxTag = -1;
		double maxProb = -1;		
		int ct = -1;
		Iterator<Integer> distMapKeyIter = distMap.keySet().iterator();
		while(distMapKeyIter.hasNext())
		{
			double tmpProb = 1;
			ct = distMapKeyIter.next();
			List<Double> wtList = distMap.get(ct);
			for(Double wt:wtList) tmpProb *= Math.exp(wt);
			if(tmpProb>maxProb)
			{
				maxTag = ct;
				maxProb = tmpProb;
			}
		}
		return maxTag;
	}
	
	@Override
	public int[] predictSeq(DataSequence seq, PI pi)
	{
		int labels[] = new int[seq.length()];
		int prevTag = -1;
		for(int i=0; i<seq.length(); i++)
		{
			labels[i] = predictTag((String)seq.x(i), prevTag, i);
			seq.set_y(i, labels[i]);
			prevTag = labels[i];
		}
		return labels;
	}
	
	public void doTest(String testDataPath, String outputPath) throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
		parseTestdata(testDataPath);
        URLSeq seq = null;
        int labels[] = null;
        while(dataIter.hasNext())
        {
        	seq = dataIter.nextSeq();
        	labels = predictSeq(seq, null);
        	outputTaggedData(bw, seq);
        }
        bw.close();
	}
	
	public void allocFeat(IFeature feat, double wt)
	{
		featureList.put(feat, wt);
	}
	
	public void parseTestdata(String fn)
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
	
	public int parseRawdata(String fn)
	{
		List<URLSeq> trainReds = new LinkedList<URLSeq>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.isEmpty()) continue;
				trainReds.add(new URLSeq(line));
			}
			br.close();
			if(dataIter==null) dataIter = new URLIter(trainReds);
			else dataIter.addSeqs(trainReds);
			System.out.printf("\t[MEMM-Model] Total %d sequence data...\n", dataIter.size());
			return dataIter.size();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public void saveStatisticData(String fn) throws Exception
	{
		File file = new File(fn);
		if(!file.exists()) file.createNewFile();
		StatisticDataWraper wrapper = new StatisticDataWraper(trainStore, tagSet);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(wrapper);
		oos.close();
	}
	
	public void loadStatisticData(String filename) throws Exception
	{
		File file = new File(filename);  
		  
        if (!file.exists())  
            throw new FileNotFoundException(); 
        
        FileInputStream fileInputStream = new FileInputStream(file);  
        ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);
        StatisticDataWraper wrapper = (StatisticDataWraper)objInputStream.readObject();
        this.trainStore = wrapper.trainStore;
        this.tagSet = wrapper.tagSet;
	}
	
	@Override
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
	
	public static void main(String args[]) throws Exception
	{
		URLModel memmModel = new URLModel(100);
		//String trainPath = "training_data.txt";
		//String outTagPath = "memm_tagged_data.txt";
		//String statisticDataRaw = "statistic_data.raw";
		
		String trainPath = 	"Datas/Train/20101101_0000.shopping_trail";
		String trainPath2 = "Datas/Train/20101101_0030.shopping_trail";
		String testPath = 	"Datas/Test/20101101_0100.shopping_trail_test";
		String outTagPath = "Datas/Test/20101101_0100.shopping_trail_tagged";
		String answerPath = "Datas/Test/20101101_0100.shopping_trail_answer";
		String saveModelPath = "IR/Model/MEMM/URL/stat.raw";
		
		System.out.printf("\t[Main] Parsing training data...\n");
		memmModel.parseRawdata(trainPath);	
		memmModel.parseRawdata(trainPath2);
		System.out.printf("\t[Main] Training model...\n");
		memmModel.train();
		memmModel.saveStatisticData(saveModelPath);
		
		/*Testing model*/
		memmModel.loadStatisticData(saveModelPath);
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

	@Override
	public void saveFeature(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadFeature(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PI validate(String testDataPath, String outputPath, boolean onlyLast)
			throws Exception {
		throw new java.lang.UnsupportedOperationException();		
	}
}

