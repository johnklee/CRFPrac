package john.memm.model;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import john.crf.data.TSeq;
import john.memm.feature.IFeature;

public abstract class AbstModel {
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
	 * BD : Save feature training data into disk.
	 * @param path
	 */
	public abstract void saveFeature(String path);
	/**
	 * BD : Load in feature training data from disk.
	 * @param path
	 */
	public abstract void loadFeature(String path);
	
	/**
	 * BD : Predict label for input sequence as param 'seq'.
	 * @param seq
	 * @return
	 */
	public abstract int[] predictSeq(DataSequence seq, PI pi);
	
	/**
	 * BD : Predict the label of last one token(n) based on previous n-1 tokens.
	 * @param seq
	 * @param pi
	 * @return
	 */
	public int predictLast(DataSequence seq, PI pi){ throw new java.lang.UnsupportedOperationException();}
	
	/**
	 * BD : Train the model. Before doing this, please call API parseRawdata().
	 * @throws Exception
	 */
	public abstract void train() throws Exception;
	
	public abstract void test(String testDataPath, String outputPath) throws Exception;
	
	/**
	 * BD : Tagged file indicated by "testDataPath" and output to file indicated by "outputPath".
	 * @param testDataPath
	 * @param outputPath
	 * @throws Exception
	 */
	public abstract PI validate(String testDataPath, String outputPath, boolean onlyLast) throws Exception;
	
	/**
	 * BD : Load in the sequence data from file with path as param 'fn'.
	 * @param fn
	 */
	public abstract int parseRawdata(String fn);
	
	/**
	 * BD : Performance Index
	 * @author John
	 *
	 */
	public class PI /*Performance Index*/
	{
		public int s_hit = 0;
		public int s_miss = 0;
		public int hit = 0;
		public int miss = 0;
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
					if(aSeq.y(i) == tSeq.y(i)) pi.hit();
					else pi.miss();
					piMap.put(aSeq.token(i), pi);
				}
				calcLine++;
			}
			awRdr.close();
			tgRdr.close();
			System.out.printf("\t[Info] Total %d lines is calculated!\n", calcLine);
		}
		return piMap;
	}
}
