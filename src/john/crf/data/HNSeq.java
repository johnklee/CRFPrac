package john.crf.data;

import iitb.CRF.DataSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import john.memm.model.AbstModel.PI;


public class HNSeq implements DataSequence{
	public int 					miss = 0;
	private List<Integer> 		labelList;
	private List<String> 		tokenList;
	private String 				uid;
	private String 				cty;
	
	/**
	 * BD : URL Sequence
	 * FMT :
	 * 	[userID]\t[country]\t[categorySequence]\t[URLSequence]\t[timeStamp] each is separated with [tab]
	 *	[categorySequence],[URLSequence],[timeStamp] sequence data is separated with space
	 * @param datas
	 */
	public HNSeq(String datas) 
	{
		labelList = new ArrayList<Integer>();
		tokenList = new ArrayList<String>();
		String blocks[] = datas.trim().split("\t");
		if(blocks.length==5)
		{
			uid = blocks[0].trim();
			cty = blocks[1].trim();
			String cateSeq[] = blocks[2].trim().split(" ");
			String urlSeq[] = blocks[3].trim().split(" ");
			if(cateSeq.length != urlSeq.length) 
			{
				System.out.printf("\t[URLSeq] Illegal data : Category seq len=%d ; URL seq len=%d!\n", cateSeq.length, urlSeq.length);
			}
			else
			{
				for(String cate:cateSeq)
				{
					try
					{
						if(cate.contains(","))
						{
							String cates[] = cate.split(",");
							labelList.add(Integer.valueOf(cates[0]));
						}
						else
						{
							labelList.add(Integer.valueOf(cate));
						}
					} catch(Exception e)
					{
						System.out.printf("\t[URLSeq] Error on Category data=%s!\n", cate);
						labelList.clear();
					}
				}
				for(String url:urlSeq)
				{
					/*Skip query string*/
					if(url.contains("/"))
					{
						url = url.substring(0, url.indexOf("/"));
					}
					//System.out.printf("\t[HNSeq] Token : %s...\n", url);
					tokenList.add(url);
				}
			}
		}
		else
		{
			System.out.printf("\t[URLSeq] Illegal data : %s\n", datas);
		}
	}

	public HNSeq(){
		labelList = new ArrayList<Integer>();
		tokenList = new ArrayList<String>();
	}
	
	public void loadTestData(String line, boolean test)
	{
		String pairs[] = line.split(" ");
		for(String pair:pairs)
		{
			if(test)
			{
				//System.out.printf("\t[HNSeq] Token=%s...\n", pair);
				tokenList.add(pair);
				labelList.add(0);
			}
			else
			{
				if(pair.contains(":"))
				{				
					//String items[] = pair.split(":");
					try
					{
						tokenList.add(pair.substring(0, pair.lastIndexOf(":")));
						labelList.add(Integer.valueOf(pair.substring(pair.lastIndexOf(":")+1, pair.length())));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					System.out.printf("\t[URLSeq] Illegal data : %s\n", pair);
				}				
			}
		}
	}
	
	@Override
	public int length() {
		return tokenList.size();
	}
	
	public void set_y(int i, int v, PI pi)
	{
		if(labelList.get(i)!=v) pi.miss();
		else pi.hit();
		if(i==labelList.size()-1)
		{
			if(labelList.get(i)!=v) pi.smiss();
			else pi.shit();
		}
		labelList.set(i, v);
	}
	
	@Override
	public void set_y(int i, int v) {	
		labelList.set(i, v);
	}

	public String token(int i) {
		return tokenList.get(i);
	}
	
	@Override
	public Object x(int i) {
		return tokenList.get(i);
	}

	@Override
	public int y(int i) {
		return labelList.get(i);
	}	
	
	public int size(){return tokenList.size();}
	
	public static void main(String args[]) throws Exception
	{
		File trainData = new File("Datas/Train/20101101_0000.shopping_trail");
		BufferedReader br = new BufferedReader(new FileReader(trainData));
		String line = null;
		List<HNSeq> seques = new ArrayList<HNSeq>();
		HNSeq seq = null;
		while((line=br.readLine())!=null)
		{
			seq = new HNSeq(line);
			if(seq.length()>0) seques.add(seq);
		}
		System.out.printf("\t[Main] URLIter size=%d...\n", seques.size());
		br.close();
	}
	
	public int[] labels()
	{
		int labs[] = new int[labelList.size()];
		for(int i=0; i<labelList.size(); i++) labs[i] = labelList.get(i);
		return labs;
	}
}
