package john.crf.data;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.util.List;

public class TIter implements DataIter{
	private List<TSeq> trainReds = null;
	private int pos = 0;
	
	public TIter(List<TSeq> records)
	{
		this.trainReds = records;		
	}

	@Override
	public void startScan() {
		pos = 0;		
	}
	
	public int size(){return trainReds.size();}

	@Override
	public boolean hasNext() {
		return pos < size();
	}

	@Override
	public DataSequence next() {
		return trainReds.get(pos++);
	}
	
	public TSeq nextSeq(){return trainReds.get(pos++);}
}
