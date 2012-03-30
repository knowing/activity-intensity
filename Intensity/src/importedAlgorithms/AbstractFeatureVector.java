package importedAlgorithms;

import java.sql.Timestamp;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

/**
 * class for an abstract feature vector
 * 
 * @author Christian Mönnig
 *
 */

public abstract class AbstractFeatureVector {
	
	protected Vector<Vector<Double>> featureVector = new Vector<Vector<Double>>();
	protected Vector<Timestamp[]> timestamps = new Vector<Timestamp[]>();
	protected String label;
	protected static final java.text.NumberFormat NF = NumberFormat.getInstance(Locale.GERMAN);
	
	public String getLabel(){
		return label;
	}
	
	public Vector<Vector<Double>> getFeatureVector(){
		return featureVector;
	}
	
	public int getAttributeCount(){
		if(featureVector!=null && featureVector.size()>0){
			return featureVector.get(0).size();
		}
		else return -1;
	}
	
	public double[][] getData(){
		double[][] data = new double[featureVector.size()][featureVector.get(0).size()];
	    for(int i=0;i<featureVector.size();i++){
	    	for(int j=0;j<featureVector.get(i).size();j++){
	    		data[i][j]=featureVector.get(i).get(j).doubleValue();
	    	}
	    }
	    return data;
	}
	
	public Vector<Timestamp[]> getTimestamps(){
		return timestamps;
	}
	
    public String toCSVString(boolean excelMode){
    	StringBuffer sb = new StringBuffer();
     	    	
    	for(Vector<Double> fv : featureVector){    		   		
    		for(Double dv : fv){    			
    			if(excelMode){
    				sb.append(NF.format(dv));    			
    				sb.append(';');
    			}
    			else{
    				sb.append(dv.toString());    			
    				sb.append(',');
    			}
    		}    	
    		if(label!=null){
    			sb.append(label);
    		}    		
    		sb.append("\n");
    	}
    	return sb.toString();
    }

}
