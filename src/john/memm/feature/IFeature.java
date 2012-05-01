package john.memm.feature;

import iitb.CRF.DataSequence;
import java.util.HashMap;

public interface IFeature {
	//HashMap<String,HashMap<Integer,Integer>> 	trainStore = null;	
	public double score(String token, int prevTag, int curTag);
	public int label(String token, int prevTag, int pos);
	public boolean train(DataSequence seq);
	public boolean saveFeatStat(String fn);
	public boolean loadFeatStat(String fn);
}
