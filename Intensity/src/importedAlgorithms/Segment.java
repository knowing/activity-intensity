package importedAlgorithms;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

/**
 *
 * @author Alexander Stautner & Christian Mönnig
 */
public class Segment {
    private Vector<Vector<Double>> segmentValues;
    private Vector<Timestamp> timestamps;
    private String trueLabel;
    private String classifiedLabel;
    private String segmentID;
    private int periodOffset = 0;
    private static final java.text.NumberFormat NF = NumberFormat.getInstance(Locale.GERMAN);
    

    public Segment(int dimensions)
    {
        segmentValues = new Vector<Vector<Double>>();
        timestamps = new Vector<Timestamp>();
        for(int i = 0; i < dimensions; i++)
        {
            segmentValues.add(i, new Vector<Double>());
        }
    }

    public void setDimension(int dimension, Double[] values)
    {
        for(double value : values)
        {
            segmentValues.get(dimension).add(value);
        }
    }
    public void setTimestamps(Timestamp[] times)
    {
        for(Timestamp time : times)
        {
            timestamps.add(time);
        }
    }

    public Vector<Double> getValues(int dimension)
    {
        return segmentValues.get(dimension);
    }

    public int size()
    {
        return segmentValues.get(0).size();
    }
    
 
    
    public int getDimensions(){
    	return segmentValues.size();
    }
    
    public void setTrueLabel(String label)
    {
        this.trueLabel = label;
    }
    public String getTrueLabel()
    {
        return trueLabel;
    }

    /**
     * @return the classifiedLabel
     */
    public String getClassifiedLabel() {
        return classifiedLabel;
    }

    /**
     * @param classifiedLabel the classifiedLabel to set
     */
    public void setClassifiedLabel(String classifiedLabel) {
        this.classifiedLabel = classifiedLabel;
    }

    public String toCSVString(){
    	return this.toCSVString(",",true,false,-1);
    }
    
    public String toCSVString(boolean intValues){
    	return this.toCSVString(",",intValues,false,-1);
    }
    
    public String toCSVString(String separator, boolean intValues, boolean startAtOffset, int length){
    	StringBuffer sb = new StringBuffer();
    	int start = 0;
    	if(startAtOffset) start = this.getPeriodOffset();
    	int end = this.size();
    	if(length>0) end = Math.min((start + length), end);
    	
    	for(int i=start; i < end; i++){
    		sb.append(this.timestamps.get(i));    		
    		for(int j=0; j < segmentValues.size();j++){
    			sb.append(separator);
    			if(intValues){
    				sb.append((int)(segmentValues.get(j).get(i)*64));
    			}
    			else{
    				sb.append(NF.format(segmentValues.get(j).get(i)));
    			}
    		}
    		if(this.getSegmentID()!=null){
	    		sb.append(separator);
	    		sb.append(this.getSegmentID());
    		}
	    	if(this.getTrueLabel()!=null){
	    		sb.append(separator);
	    		sb.append(this.getTrueLabel());
	    	}
    		sb.append("\n");
    	}
    	return sb.toString();
    }

	public String getSegmentID() {
		return segmentID;
	}

	public void setSegmentID(String segmentID) {
		this.segmentID = segmentID;
	}

	public int getPeriodOffset() {
		return periodOffset;
	}

	public void setPeriodOffset(int periodOffset) {
		this.periodOffset = periodOffset;
	}

	public Vector<Timestamp> getTimestamps() {
		return timestamps;
	}
/* xxxxxxxxx BEN's DEBUG METHODS! xxxxxxxx*/
	
	//Sum of the save data slots
	public int getDimensionCapacitySum(){
    	int sum = 0;
    	for (Vector<Double> v:segmentValues){
    		sum += v.capacity();
    	}
        return sum;
    }
	
	//Mean value of a Single Dimension
	public double getSingleDimensionMean(int dimension){
		double meanCounter = 0;
		
		for (Double j:segmentValues.elementAt(dimension)){
				meanCounter+=Math.abs(j);
			}
		return (meanCounter/segmentValues.get(dimension).capacity());
	}	
	//Mean value of all Dimensions
	public double getWholeSegmentMean(){
		double meanCounter = 0;
		for (Vector<Double> i :segmentValues){
			for (Double j:i){
				meanCounter+=Math.abs(j);
			}
		}
		System.out.println("Segment count: "+ meanCounter);

		return (meanCounter/this.size());
	}
	

	

	
	
	
}
