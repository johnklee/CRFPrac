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

import john.crf.data.TIter;
import john.crf.data.TSeq;
import john.memm.feature.IFeature;
import john.memm.feature.PBFeature;
import john.memm.stat.StatisticDataWraper;

public class FOrderModel extends AbstModel{
	protected TIter 										dataIter = null;
	protected int											nlabels = -1;
	protected HashMap<String,HashMap<Integer,Integer>> 		trainStore = null;
	public static boolean 									bDebug = true;
	protected HashMap<IFeature,Double>						featureList;
	protected Set<Integer>									tagSet = null;
	
	public FOrderModel(int labelSize){
		this.nlabels = labelSize+1; 
		featureList = new HashMap<IFeature,Double>();
		tagSet = new HashSet<Integer>();
	}
	
	public FOrderModel(Set<Integer> tagSet)
	{
		this.nlabels = tagSet.size()+1; 
		featureList = new HashMap<IFeature,Double>();
		this.tagSet = tagSet;
	}
	
	public static String entry(String token, int prevTag) {return String.format("%s_%d", token, prevTag);}
	public void train() throws Exception
	{
		TSeq seq = null;
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
	
	protected void outputTaggedData(BufferedWriter bw, TSeq seq) throws Exception{
		if(seq.size()>0)
		{
			bw.append(String.format("%s:%d", seq.x(0), seq.y(0)));
			for(int i=1; i<seq.size(); i++) bw.append(String.format("-%s:%d", seq.x(i), seq.y(i)));
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
		parseRawdata(testDataPath);
        TSeq seq = null;
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
	
	public int parseRawdata(String fn)
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
	
	public static void main(String args[]) throws Exception
	{
		FOrderModel memmModel = new FOrderModel(4);
		String trainPath = "training_data.txt";
		String outTagPath = "memm_tagged_data.txt";
		String statisticDataRaw = "statistic_data.raw";
		
		memmModel.parseRawdata(trainPath);		
		memmModel.train();
		memmModel.saveStatisticData(statisticDataRaw);
		
		/*Testing model*/
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
