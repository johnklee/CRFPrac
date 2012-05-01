package john.crf.token;

import java.util.Random;

public class TToken {
	public String 	tokenStr = "";
	public int		label=-1;
	public float 	hitRate = 0;
	public int[]	noises;
	public Random 	rdm = null; 
	
	public TToken(String ts, int label, float c, int[] noise){
		this.tokenStr = ts;
		this.label = label;
		this.hitRate = c;
		this.noises = noise;
		rdm = new Random();
	}
	
	public int randomLabel()
	{
		if(rdm.nextFloat()<=hitRate && noises.length>0) return label;
		else
		{
			return noises[rdm.nextInt(noises.length)];
		}
	}
	
	public static void main(String args[])
	{
		int labels[] = {1, 2, 3, 4};
		TToken token = new TToken("A", 1, (float)0.7, labels);
		for(int i=0; i<100; i++)
		{
			System.out.printf("Token(%s) has label=%d...\n", token.tokenStr, token.randomLabel());
		}
	}
}
