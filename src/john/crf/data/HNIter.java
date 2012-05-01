package john.crf.data;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.util.List;

public class HNIter implements DataIter{
	private List<HNSeq> dataSequences = null;
	private int pos = 0;
	
	public HNIter(List<HNSeq> seqs) {this.dataSequences = seqs;}

	@Override
	public boolean hasNext() {
		return pos < size();
	}

	public int size(){return dataSequences.size();}
	public HNSeq nextSeq(){return dataSequences.get(pos++);}
	
	@Override
	public DataSequence next() {
		return dataSequences.get(pos++);
	}

	@Override
	public void startScan() {
		pos = 0;		
	}
	
	public int addSeqs(List<HNSeq> seqs){dataSequences.addAll(seqs); return size();}
}
