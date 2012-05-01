package john.memm.feature;

import iitb.CRF.DataSequence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import john.memm.model.FOrderModel;
import john.memm.model.MOrderModel;
import john.memm.utils.TQueue;

public class MOFeature implements IFeature {
	private HashMap<String, HashMap<Integer, Integer>> trainStore = null;
	private HashMap<Integer, Double> prevScoreDist = null;
	private Set<Integer> tagList = null;
	private int order = 1;
	private TQueue tque = null;

	public MOFeature(HashMap<String, HashMap<Integer, Integer>> trainData,
			Set<Integer> allTagList, int order) {
		this.trainStore = trainData;
		prevScoreDist = new HashMap<Integer, Double>();
		this.tagList = allTagList;
		this.order = order;
		tque = new TQueue(order);
	}

	@Override
	public double score(String token, int prevTag, int curTag) {
		double prevVProb = 1;
		if (prevScoreDist.size() > 0) {
			if (prevScoreDist.containsKey(prevTag))
				prevVProb = prevScoreDist.get(prevTag);
			else
				prevVProb = 0;
		}

		HashMap<Integer, Integer> statData = trainStore.get(MOrderModel.entry(token, tque));
		if(statData==null) return 0;
		double sum = 0;
		double tc = 0;
		Iterator<Integer> keyIter = statData.keySet().iterator();
		int key;
		while (keyIter.hasNext()) {
			key = keyIter.next();
			if (key == curTag)
				tc = statData.get(key);
			sum += statData.get(key);
		}

		return prevVProb * (tc / sum);
	}

	@Override
	public int label(String token, int prevTag, int pos) {
		if (pos == 0) {
			prevScoreDist.clear();
			tque.clear();
		}
		tque.add(prevTag);
		
		int maxProbTag = -1;
		double maxProb = -1;
		double tmpProb = 0;
		HashMap<Integer, Double> tmpScoreDist = new HashMap<Integer, Double>();
		for (Integer tag : tagList) {
			tmpProb = score(token, prevTag, tag);
			if (tmpProb > maxProb) {
				maxProb = tmpProb;
				maxProbTag = tag;
			}
			tmpScoreDist.put(tag, tmpProb);
		}
		
		
		if(maxProb!=0) prevScoreDist = tmpScoreDist;
		return maxProbTag;
	}

	@Override
	public boolean train(DataSequence seq) {return false;}

	@Override
	public boolean saveFeatStat(String fn) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean loadFeatStat(String fn) {
		throw new java.lang.UnsupportedOperationException();
	}
}
