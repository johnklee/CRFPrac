package john.crf.data;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.util.List;

public class URLIter implements DataIter{
	private List<URLSeq> dataSequences = null;
	private int pos = 0;
	
	public URLIter(List<URLSeq> seqs) {this.dataSequences = seqs;}

	@Override
	public boolean hasNext() {
		return pos < size();
	}

	public int size(){return dataSequences.size();}
	public URLSeq nextSeq(){return dataSequences.get(pos++);}
	
	@Override
	public DataSequence next() {
		return dataSequences.get(pos++);
	}

	@Override
	public void startScan() {
		pos = 0;		
	}
	
	public int addSeqs(List<URLSeq> seqs){dataSequences.addAll(seqs); return size();}
}
