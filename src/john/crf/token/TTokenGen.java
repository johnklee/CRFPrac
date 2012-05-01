package john.crf.token;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TTokenGen {
	public List<TokenWraper> buildCL = null;
	public HashMap<TToken,Float> model;
	public float cur = 0;
	public Random rdm = null;
	
	public TTokenGen(){model = new HashMap<TToken,Float>(); rdm = new Random();}
	
	public boolean addTToken(TToken token, float p)
	{
		if(p<1 && cur+p<=1)
		{
			if(model.containsKey(token))
			{
				float origp = model.get(token);
				if((cur-origp+p)<=1) {
					cur = cur - origp + p;
					model.put(token, p);
					if(cur==1) buildCL();
					return true;
				}				
			}
			else
			{
				cur+=p;
				model.put(token, p);
				if(cur==1) buildCL();
				return true;
			}
		}
		return false;
	}
	
	public boolean addLast(TToken token)
	{
		if(cur<1)
		{
			float p = 0;
			if(model.containsKey(token))
			{
				float origp = model.get(token);
				p = 1 - (cur-origp);			
			}
			else
			{
				p = 1 - cur;				
			}			
			model.put(token, p);
			cur = 1;
			buildCL();
			return true;
		}
		return false;
	}
	
	protected void buildCL()
	{
		buildCL = new LinkedList<TokenWraper>();
		Iterator<TToken> keyIter = model.keySet().iterator();
		TToken tt = null;
		while(keyIter.hasNext())
		{
			tt = keyIter.next();
			buildCL.add(new TokenWraper(tt, model.get(tt)));
		}
	}
	
	public TToken nextRandom()
	{
		if(cur!=1) {
			System.out.printf("\t[Error] cur=%f!\n", cur);
			return null;
		}
		else
		{
			float rt = rdm.nextFloat();
			float ar = 0;
			for(TokenWraper tw:buildCL)
			{
				ar += tw.cp;
				if(rt<ar) return tw.token;				
			}
			return buildCL.get(buildCL.size()-1).token;
		}
	}
	
	class TokenWraper{
		public TToken 	token;
		public float 	cp=0;
		public TokenWraper(TToken t, float c){this.token=t; this.cp = c;}
	}
	
	public static void main(String args[])
	{
		int labels[] = {1, 2, 3, 4};
		TToken token1 = new TToken("A", 1, (float)0.7, labels);
		TToken token2 = new TToken("B", 2, (float)0.85, labels);
		TToken token3 = new TToken("C", 3, (float)0.5, labels);
		TToken token4 = new TToken("D", 4, (float)0.9, labels);
		TToken token5 = new TToken("E", 1, (float)0.95, labels);
		TToken token6 = new TToken("F", 2, (float)0.4, labels);
		TToken token7 = new TToken("G", 3, (float)0.8, labels);
		TToken token8 = new TToken("H", 4, (float)0.6, labels);
	
		TTokenGen tokenGen = new TTokenGen();
		tokenGen.addTToken(token1, (float)0.1);
		tokenGen.addTToken(token2, (float)0.2);
		tokenGen.addTToken(token3, (float)0.1);
		tokenGen.addTToken(token4, (float)0.1);
		tokenGen.addTToken(token5, (float)0.1);
		tokenGen.addTToken(token6, (float)0.1);
		tokenGen.addTToken(token7, (float)0.2);
		tokenGen.addLast(token8);
		
		TToken rt = null;
		for(int i=0; i<100; i++)
		{
			rt = tokenGen.nextRandom();
			System.out.printf("Token(%s) has label=%d...\n", rt.tokenStr, rt.randomLabel());
		}
	}
}
