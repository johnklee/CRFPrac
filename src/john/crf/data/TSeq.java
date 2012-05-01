package john.crf.data;

import iitb.CRF.DataSequence;

import java.util.ArrayList;
import java.util.List;

public class TSeq implements DataSequence{
	private List<String> tokens = null;
	private List<Integer> tags = null;
	
	public TSeq(String line)
	{
		tokens = new ArrayList<String>();
		tags = new ArrayList<Integer>();
		String rtk[] = line.split("-");
		for(String stk:rtk)
		{
			String tk[] = stk.split(":");
			if(tk.length==2)
			{
				try{tags.add(Integer.valueOf(tk[1])); tokens.add(tk[0]);}catch(Exception e){}
			}
			else if(tk.length==1)
			{
				tokens.add(tk[0]);
				tags.add(-1);
			}
		}
		/*addData("A", 1);
		addData("B", 2);
		addData("C", 3);
		addData("E", 2);
		addData("D", 4);
		addData("F", 3);
		addData("Z", 3);
		addData("G", 2);
		addData("B", 2);
		addData("K", 4);
		addData("L", 1);
		addData("B", 2);
		addData("F", 3);*/
	}

	protected void addData(String token, Integer tag)
	{
		tokens.add(token);
		tags.add(tag);
	}
	
	@Override
	public int length() {
		return tokens.size();
	}

	@Override
	public int y(int i) {
		return tags.get(i);
	}

	@Override
	public Object x(int i) {
		return tokens.get(i);
	}
	
	public String token(int i) {return tokens.get(i);}

	@Override
	public void set_y(int i, int label) {
		tags.set(i, label);		
	}
	
	public int size(){return tokens.size();}
	public int[] labels(){
		int labels[] = new int[tags.size()];
		for(int i=0; i<tags.size(); i++) labels[i] = tags.get(i);
		return labels;
	}
}
