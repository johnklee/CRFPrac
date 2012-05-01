package iitb.Model;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * 
 * @author Sunita Sarawagi
 * @since 1.1
 * @version 1.3
 */
public class FeatureIdentifier implements Cloneable, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4942036159933407311L;
	public int id;
    public Object name;
    public int stateId;
    public FeatureIdentifier() {
    }
    public FeatureIdentifier(int fid, int s, Object n) {
	init(fid,s,n);
    }
    public FeatureIdentifier(String strRep) {
    //System.out.printf("\t[Test] Rep: %s\n", strRep);
	StringTokenizer strTok = new StringTokenizer(strRep, ":");
	String tokens[] = strRep.split(":");
	if(tokens.length==3)
	{
		name = tokens[0];
		id = Integer.parseInt(tokens[1]);
		stateId = Integer.parseInt(tokens[2]);
	}
	else if(tokens.length==4)
	{
		name = tokens[0] + tokens[1];
		id = Integer.parseInt(tokens[2]);
		stateId = Integer.parseInt(tokens[3]);
	}
	else
	{
		System.err.printf("\t[FeatureIdentifier] Error Feature record : %s\n", strRep);
	}
	
    }
    public void init(int fid, int s, Object n) {
	name = n;
	id = fid;
	stateId = s;
    }
    public void init(int fid) {
	id = fid;
    }
    public void copy(FeatureIdentifier fid) {
	init(fid.id,fid.stateId,fid.name);
    }
    public int hashCode() {
	return id;
    }
    public boolean equals(Object o) {
	return (id == ((FeatureIdentifier)o).id);
    }
    public String getName() {
    	return name.toString();
    }
    public String toString() {
	return name.toString() + ":"  + id+ ":" + stateId;
    }
    public Object clone() {
	return new FeatureIdentifier(id,stateId,name);
    }
};

