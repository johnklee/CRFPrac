package john.memm.stat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class StatisticDataWraper implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public StatisticDataWraper(HashMap<String,HashMap<Integer,Integer>> trainStore, Set<Integer> tagSet){
		this.trainStore = trainStore;
		this.tagSet = tagSet;
	}
	
	public HashMap<String,HashMap<Integer,Integer>> 	trainStore = null;
	public Set<Integer>									tagSet = null;
}
