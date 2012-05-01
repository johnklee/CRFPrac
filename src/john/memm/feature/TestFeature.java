package john.memm.feature;

import iitb.CRF.DataSequence;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class TestFeature implements IFeature{
	private HashMap<String,HashMap<Integer,Integer>> 	trainStore = null;
	private HashMap<Integer,Double>						prevScoreDist = null;
	private Set<Integer>								tagList = null;
	
	public TestFeature(HashMap<String,HashMap<Integer,Integer>> trainData, Set<Integer> allTagList){
		this.trainStore = trainData;
		prevScoreDist = new HashMap<Integer,Double>();
		this.tagList = allTagList;
	}

	@Override
	public double score(String token, int prevTag, int curTag) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int label(String token, int prevTag, int pos) {
		if(prevTag!=-1) return prevTag;
		else {
			Random rdm = new Random();
			return (Integer)tagList.toArray()[rdm.nextInt(tagList.size())];
		}
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
