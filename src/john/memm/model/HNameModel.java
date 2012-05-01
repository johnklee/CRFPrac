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

import john.crf.data.HNIter;
import john.crf.data.HNSeq;
import john.crf.data.URLIter;
import john.crf.data.URLSeq;
import john.memm.feature.IFeature;
import john.memm.feature.PBFeature;
import john.memm.model.AbstModel.PI;
import john.memm.stat.StatisticDataWraper;

public class HNameModel extends AbstModel{
	protected HNIter 										dataIter = null;
	protected int											nlabels = -1;
	protected HashMap<String,HashMap<Integer,Integer>> 		trainStore = new HashMap<String,HashMap<Integer,Integer>>();
	public static boolean 									bDebug = false;
	protected HashMap<IFeature,Double>						featureList;
	protected Set<Integer>									tagSet = null;
	
	public HNameModel(int labelSize){
		this.nlabels = labelSize+1; 
		featureList = new HashMap<IFeature,Double>();
		tagSet = new HashSet<Integer>();
	}
	
	public HNameModel(Set<Integer> tagSet)
	{
		this.nlabels = tagSet.size()+1; 
		featureList = new HashMap<IFeature,Double>();
		this.tagSet = tagSet;
	}
	
	public static String entry(String token, int prevTag) {return String.format("%s_%d", token, prevTag);}
	protected void trainFeature(DataSequence seq, List<IFeature> list)
	{
		for(IFeature feat:list)
		{
			if(!feat.train(seq)) list.remove(feat);
		}
	}
	public void train() throws Exception
	{		
		allocFeat();
		HNSeq seq = null;		
		List<IFeature> tfList = new LinkedList<IFeature>();
		Iterator<IFeature> fKeyIter = featureList.keySet().iterator();
		while(fKeyIter.hasNext()) tfList.add(fKeyIter.next());
		while(dataIter.hasNext())
		{
			seq = dataIter.nextSeq();
			trainFeature(seq, tfList);
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
		
		//System.out.printf("\t[MEMM-Model] bDebug=%b...\n", bDebug);
		if(bDebug)
		{			
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
		System.out.printf("\t[MEMM-Model] Training is done!\n");
	}
	
	public void allocFeat(){
		if(this.featureList.size()==0)
		{
			this.allocFeat(new PBFeature(trainStore, tagSet), 1);
		}
		//this.allocFeat(new TestFeature(trainStore, tagSet), 1);		
	}
	
	public void test(String testDataPath, String outputPath) throws Exception
	{if(featureList.size()==0) allocFeat(); doTest(testDataPath, outputPath);}
	
	protected void outputTaggedData(BufferedWriter bw, HNSeq seq) throws Exception{
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
			if(pi!=null) {
				((HNSeq)seq).set_y(i, labels[i], pi);
			}
			else seq.set_y(i, labels[i]);
			prevTag = labels[i];
		}
		return labels;
	}
	
	@Override
	public int predictLast(DataSequence seq, PI pi)
	{
		int labels[] = new int[seq.length()];
		//int prevTag = -1;
		for(int i=0; i<seq.length(); i++)
		{
			labels[i] = predictTag((String)seq.x(i), i==0?-1:seq.y(i-1), i);			
			//prevTag = labels[i];
		}
		if(pi!=null) {
			((HNSeq)seq).set_y(seq.length()-1, labels[seq.length()-1], pi);
		}
		else seq.set_y(seq.length()-1, labels[seq.length()-1]);
		
		return labels[seq.length()-1];
	}
	
	public void doTest(String testDataPath, String outputPath) throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
		parseTestdata(testDataPath);
        HNSeq seq = null;
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
			System.out.printf("\t[MEMM-Model] Total %d sequence data...\n", dataIter.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int parseRawdata(String fn)
	{
		List<HNSeq> trainReds = new LinkedList<HNSeq>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File(fn)));
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.isEmpty()) continue;
				trainReds.add(new HNSeq(line));
			}
			br.close();
			if(dataIter==null) dataIter = new HNIter(trainReds);
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
		System.out.printf("\t[MEMM-Model] Save model statistic data to %s...Done!\n", fn);
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
        System.out.printf("\t[MEMM-Mode] Loading training model : store size=%d ; tag size=%d...Done\n", trainStore.size(), tagSet.size());
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
	
	@Override
	public void saveFeature(String path) {
		Iterator<IFeature> keyIter = featureList.keySet().iterator();
		IFeature feat = null;
		while(keyIter.hasNext())
		{
			feat = keyIter.next();
			try
			{
				//System.out.printf("\t[MEMM-Model] Save feature...\n");
				feat.saveFeatStat(path);
			}
			catch(Exception e){}
		}
		
	}

	@Override
	public void loadFeature(String path) {
		if(featureList.size()==0) allocFeat();
		Iterator<IFeature> keyIter = featureList.keySet().iterator();
		IFeature feat = null;
		while(keyIter.hasNext())
		{
			feat = keyIter.next();
			try
			{
				feat.loadFeatStat(path);
			}
			catch(Exception e){}
		}		
	}
	
	public int train(File trainFile)
	{
		HNIter datIter = null;
		List<HNSeq> trainReds = new LinkedList<HNSeq>();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(trainFile));
			String line = null;
			while((line=br.readLine())!=null)
			{
				if(line.startsWith("#") || line.isEmpty()) continue;
				trainReds.add(new HNSeq(line));
			}
			br.close();
			
			datIter = new HNIter(trainReds);
			System.out.printf("\t[MEMM-Model] Total %d sequence data...\n", datIter.size());
			
			HNSeq seq = null;		
			List<IFeature> tfList = new LinkedList<IFeature>();
			Iterator<IFeature> fKeyIter = featureList.keySet().iterator();
			while(fKeyIter.hasNext()) tfList.add(fKeyIter.next());
			while(datIter.hasNext())
			{
				seq = datIter.nextSeq();
				trainFeature(seq, tfList);
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
			
			System.out.printf("\t[MEMM-Model] Training is done!\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void main(String args[]) throws Exception
	{
		HNameModel memmModel = new HNameModel(100);
		//String trainPath = "training_data.txt";
		//String outTagPath = "memm_tagged_data.txt";
		//String statisticDataRaw = "statistic_data.raw";
		
		String trainPath = 	"Datas/Train/20101101_0000.shopping_trail";
		String trainPath2 = "Datas/Train/20101101_0030.shopping_trail";
		String testPath = 	"Datas/Test/HN/20101101_0100.shopping_trail_test";
		String outTagPath = "Datas/Test/HN/20101101_0100.shopping_trail_tagged";
		String answerPath = "Datas/Test/HN/20101101_0100.shopping_trail_answer";
		String saveModelPath = "IR/Model/MEMM/HN/stat.raw";
		String saveFeatPath = "IR/Model/MEMM/HN/";
		
		/*System.out.printf("\t[Main] Parsing training data...\n");
		memmModel.parseRawdata(trainPath);	
		memmModel.parseRawdata(trainPath2);
		System.out.printf("\t[Main] Training model...\n");
		memmModel.train();
		memmModel.saveStatisticData(saveModelPath);
		memmModel.saveFeature(saveFeatPath);*/
		
		/*Testing model*/
		memmModel.loadStatisticData(saveModelPath);
		memmModel.loadFeature(saveFeatPath);
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
	public PI validate(String testDataPath, String outputPath, boolean onlyLast) throws Exception {
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
				if(!onlyLast) predictSeq(seq, pi);
				else predictLast(seq, pi);
	        	outputTaggedData(bw, seq);
	        	seqCnt++;
			}
		}
		bw.close();
		br.close();
		System.out.printf("\t[MEMM-Mode] Total processing seq=%d...\n", seqCnt);
		return pi;
	}
}
