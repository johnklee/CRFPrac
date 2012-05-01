package john.memm.feature;

import iitb.CRF.DataSequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import john.crf.data.HNSeq;
import john.memm.model.FOrderModel;
import john.memm.utils.TLDMap;
import john.memm.utils.TLDMap.TLD;

public class PBFeature implements IFeature{
	private HashMap<String,HashMap<Integer,Integer>>	tldTrainStore = null;
	private HashMap<String,HashMap<Integer,Integer>> 	trainStore = null;
	private HashMap<Integer,Double>						prevScoreDist = null;
	private Set<Integer>								tagList = null;
	private TLDMap 										tldMap = null;
	
	public PBFeature(HashMap<String,HashMap<Integer,Integer>> trainData, Set<Integer> allTagList){
		this.trainStore = trainData;
		prevScoreDist = new HashMap<Integer,Double>();
		tldTrainStore = new HashMap<String,HashMap<Integer,Integer>>();
		this.tagList = allTagList;
		tldMap = new TLDMap();
	}

	/**
	 * - BD
	 * 
	 * - Reference
	 * 	 * http://localhost/JJWiki/Wiki.jsp?page=NLP%20Maximum%20Entropy%20Markov%20Models#section-NLP+Maximum+Entropy+Markov+Models-LearningMaximumEntropyMarkovModel
	 */
	@Override
	public double score(String token, int prevTag, int curTag) {
		double prevVProb = 1;
		if(prevScoreDist.size()>0)
		{
			if(prevScoreDist.containsKey(prevTag)) prevVProb = prevScoreDist.get(prevTag);
			else prevVProb = 0;
		}
		
		HashMap<Integer,Integer> statData = trainStore.get(FOrderModel.entry(token, prevTag));
		
		if(statData!=null)
		{
			double sum = 0;
			double tc = 0;
			Iterator<Integer> keyIter = statData.keySet().iterator();
			int key;
			while(keyIter.hasNext())
			{
				key = keyIter.next();
				if(key==curTag) tc = statData.get(key); 
				sum+=statData.get(key);
			}
			
			return prevVProb * (tc/sum);
		}
		else
		{			
			TLD tld = tldMap.retrveTLDFromHostname(token);
			statData = tldTrainStore.get(tld.tld);
			if(statData==null) return 0;
			double sum = 0;
			double tc = 0;
			Iterator<Integer> keyIter = statData.keySet().iterator();
			int key;
			while(keyIter.hasNext())
			{
				key = keyIter.next();
				if(key==curTag) tc = statData.get(key); 
				sum+=statData.get(key);
			}
			//System.out.printf("\t[PBFeature] Using TLD Mode on Token=%s(tld='%s') with prob=%f...\n", token, tld.tld, prevVProb * (tc/sum));
			return prevVProb * (tc/sum);
		}
		//return 0;
	}

	@Override
	public int label(String token, int prevTag, int pos) {
		if(pos==0) prevScoreDist.clear();
				
		int maxProbTag = -1;
		double maxProb = -1;
		double tmpProb = 0;
		HashMap<Integer,Double>	tmpScoreDist = new HashMap<Integer,Double>();
		for(Integer tag:tagList)
		{
			tmpProb = score(token, prevTag, tag);
			if(tmpProb>maxProb) 
			{
				maxProb = tmpProb;
				maxProbTag = tag;
			}
			tmpScoreDist.put(tag, tmpProb);
		}		
		
		prevScoreDist = tmpScoreDist;
		return maxProbTag;
	}
	@Override
	public boolean train(DataSequence seq) {		
		HNSeq hseq = (HNSeq) seq;		
		int prevTag = -1;
		int curTag = -1;
		TLD tld = null;
		for(int i=0; i<hseq.length(); i++)
		{	
			curTag = hseq.y(i);
			tld = tldMap.retrveTLDFromHostname(hseq.token(i));
			//System.out.printf("\t[Info] PBFeature training tld=%s...\n", tld.tld);
			if(tldTrainStore.containsKey(tld.tld))
			{
				HashMap<Integer,Integer> tagCnt = tldTrainStore.get(tld.tld);
				if(tagCnt.containsKey(curTag)) tagCnt.put(curTag, tagCnt.get(curTag)+1);
				else tagCnt.put(curTag, 1);
				tldTrainStore.put(tld.tld, tagCnt);
			}
			else
			{
				HashMap<Integer,Integer> tagCnt = new HashMap<Integer,Integer>();
				tagCnt.put(curTag, 1);
				tldTrainStore.put(tld.tld, tagCnt);
			}
			prevTag = curTag;
		}
		return true; // true means more ; false means no more.
	}
	
	@Override
	public boolean saveFeatStat(String path) {
		File file = new File(new File(path), "PBFeature.raw");  
		  
        try {  
            ObjectOutputStream objOutputStream = new ObjectOutputStream(new FileOutputStream(file));  
            objOutputStream.writeObject(tldTrainStore); 
            objOutputStream.close(); 
            System.out.printf("\t[PBFeature] Save tld info to %s...Done!\n", path);
            return true;
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        return false;
	}

	@Override
	public boolean loadFeatStat(String path) {
		File file = new File(new File(path), "PBFeature.raw"); 
		
		try {  
            FileInputStream fileInputStream = new FileInputStream(file);  
            ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);  
            tldTrainStore = (HashMap<String,HashMap<Integer,Integer>>)objInputStream.readObject(); 
            objInputStream.close();  
            System.out.printf("\t[PBFeature] Load tld info from %s...Done!\n", path);
            return true;
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
		return false;
	}
}
