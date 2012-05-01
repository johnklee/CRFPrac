package john.crf;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import john.crf.data.TSeq;
import john.memm.feature.IFeature;

public abstract class AbstModel {
	public int 			nlabels=5;  /*Number of label*/
	
	/**
	 * BD : Allocate default feature.
	 */
	public abstract void allocFeat();
	/**
	 * BD : Allocate feature as param 'feat' with weight as param 'wt'.
	 * @param feat
	 * @param wt
	 */
	public abstract void allocFeat(IFeature feat, double wt);
	/**
	 * BD : Save trained statistic data into file with path as param 'fn'.
	 * @param fn
	 * @throws Exception
	 */
	public abstract void saveStatisticData(String fn) throws Exception;
	/**
	 * BD : Load trained statistic data from binary file with path as param 'filename'.
	 * @param filename
	 * @throws Exception
	 */
	public abstract void loadStatisticData(String filename) throws Exception;
	/**
	 * BD : Predict label for input sequence as param 'seq'.
	 * @param seq
	 * @return
	 */
	public abstract int[] predictSeq(DataSequence seq);
	
	/**
	 * BD : Predict label for input sequence as param 'seq'.
	 * @param seq
	 * @return
	 */
	public abstract int[] predictSeq(DataSequence seq, PI pi);
	
	/**
	 * BD : Train the model. Before doing this, please call API parseRawdata().
	 * @throws Exception
	 */
	public abstract void train() throws Exception;
	/**
	 * BD : Load in the sequence data from file with path as param 'fn'.
	 * @param fn
	 */
	public abstract void parseRawdata(String fn);
	
	/**
	 * BD : Tagged file indicated by "testDataPath" and output to file indicated by "outputPath".
	 * @param testDataPath
	 * @param outputPath
	 * @throws Exception
	 */
	public abstract PI validate(String testDataPath, String outputPath) throws Exception;
	
	/**
	 * BD : Performance Index
	 * @author John
	 *
	 */
	public class PI /*Performance Index*/
	{
		public HashMap<Integer,HashMap<Integer,Integer>> missTagStat = new HashMap<Integer,HashMap<Integer,Integer>>();
		public int hit = 0;
		public int miss = 0;
		public int s_hit = 0;
		public int s_miss = 0;
		public int size(){return hit+miss;}
		public int ssize(){return s_hit+s_miss;}
		public void hit(){hit++;}
		public void shit(){s_hit++;}
		public void miss(){miss++;}
		public void smiss(){s_miss++;}
		public float hitRate() {return ((float)hit)/(hit+miss);}
		public float shitRate() {return ((float)s_hit)/(s_hit+s_miss);}
		public float missRate() {return 1-hitRate();}
		public float smissRate() {return 1-shitRate();}
		public void miss(int answer, int tagged){
			miss++;
			if(missTagStat.containsKey(answer)) {
				HashMap<Integer,Integer> mtagged = missTagStat.get(answer);
				if(mtagged.containsKey(tagged)) mtagged.put(tagged, mtagged.get(tagged)+1);
				else mtagged.put(tagged, 1);
				missTagStat.put(answer, mtagged);
			}
			else
			{
				HashMap<Integer,Integer> mtagged = new HashMap<Integer,Integer>();
				mtagged.put(tagged, 1);
				missTagStat.put(answer, mtagged);
			}
		}
	}	
	/**
	 * BD : Calculate the PI by comparing answer indicated by file path as param 'answerPath' and tagged sequence by file path as 
	 *      param 'taggedPath'
	 * @param answerPath
	 * @param taggedPath
	 * @return
	 * @throws Exception
	 */
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
			TSeq aSeq = null;
			TSeq tSeq = null;
			int calcLine = 0;
			while((answer=awRdr.readLine())!=null && (tgdrst=tgRdr.readLine())!=null)
			{
				aSeq = new TSeq(answer);
				tSeq = new TSeq(tgdrst);
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
}
