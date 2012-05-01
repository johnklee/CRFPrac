package john.crf.features;

import iitb.CRF.DataSequence;
import iitb.Model.FeatureGenImpl;
import iitb.Model.FeatureImpl;
import iitb.Model.FeatureTypes;

public class TLDFeature extends FeatureTypes{
	private static final long serialVersionUID = 1L;
	
	public TLDFeature(FeatureGenImpl fgen) {
		super(fgen);
	}	
	
	@Override
	public void train(DataSequence data, int pos) {
		System.out.printf("\t[TLDFetu] Train with pos=%d...\n", pos);
	}

	/**
	 * BD :
	 * 	This method is used to instruct the FeatureTypes to generate all the features for label of the token at the 
	 * 	position pos in the data sequence data. The previous position indicates the previous label assigned to the 
	 * 	current sequence. Since, this implementation of the CRF is a one order markov model, a feature can have 
	 * 	dependancy on previous label of the current sequence only. This method initiates generation of the features 
	 * 	in given FeatureTypes.
	 */
	@Override
	public boolean startScanFeaturesAt(DataSequence data, int prevPos, int pos) {
		System.out.printf("\t[TLDFetu] data size=%d ; prevPos=%d ; pos=%d...\n", data.length(), prevPos, pos);
		train(data, pos);
		return true;
	}

	/**
	 * BD :
	 * 	This method is used to check if there are any more features for the current scan intitaited in startScanFeaturesAt().
	 */
	@Override
	public boolean hasNext() {	return false;}

	/**
	 * BD :
	 * 	An important function of this class, it basically contains the code for generating the next feature 
	 * 	i.e. this function will contain the code for capturing the characteristic that the user wants to capture in the form 
	 * 	of a feature. A Feature, object passed to this function as an argument, will be assigned the values of the newly 
	 * 	generated feature. The features are generated on-the-fly as and when needed for efficiency reasons.
	 */
	@Override
	public void next(FeatureImpl f) {}
}
