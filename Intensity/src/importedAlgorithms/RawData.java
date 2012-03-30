package importedAlgorithms;

import mainFiles.Algorithm;

import importedAlgorithms.AverageFilter;
import importedAlgorithms.PrimitiveArrayConverter;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;


import org.apache.log4j.Logger;

/**
 * class to store the original raw data
 * @author Alexander Stautner & Christian Mönnig
 */
public class RawData {

	static Logger logger = Logger.getLogger(Algorithm.class);

	private static final int SEGMENTATION_RATE_WINDOW = 1500; //amount of timestamps to calc the segmentation rate for (25*60=1500 -> 1 Minute)
    private Double[][] rawdata;
    private Timestamp[] timestamps;
    private String trueLabel;
    private String[] classifiedLabels;
    private ArrayList<Map<String,Double>> classifiedDistributions;
    private List<Timestamp> segmentStarts = new ArrayList<Timestamp>();
    private List<Timestamp> segmentEnds = new ArrayList<Timestamp>();
    private HashMap<Timestamp, Double> segmentationRate = new HashMap<Timestamp, Double>();
    private boolean outlierRemoved = false;
    private boolean peaksPredicted = false;
    private static final java.text.NumberFormat NF = NumberFormat.getInstance(Locale.GERMAN);

    public RawData(int dimension)
    {
        rawdata = new Double[dimension][];
        //logger.info("Rawdata with dimension " + dimension + " created!");
    }

    public RawData(Double[][] rawdata)
    {
        this.rawdata = rawdata;
    }

    public void setDimension(int dimension, Double[] data)
    {
        rawdata[dimension] = data;
    }

    public Double[] getDimension(int dimension)
    {
        return rawdata[dimension];
    }

    /**
     * @return the timestamps
     */
    public Timestamp[] getTimestamps() {
        return timestamps;
    }

    /**
     * @param timestamps the timestamps to set
     */
    public void setTimestamps(Timestamp[] timestamps) {
        this.timestamps = timestamps;
        this.classifiedLabels = new String[timestamps.length];
        this.classifiedDistributions = new ArrayList<Map<String, Double>>(timestamps.length);
        for(int i=0;i<timestamps.length;i++){
        	classifiedDistributions.add(null);
        }
    }

    public void setTrueLabel(String label)
    {
        this.trueLabel = label;
    }

    public String getTrueLabel()
    {
        return trueLabel;
    }

	public String[] getClassifiedLabels() {
		return classifiedLabels;
	}
    
	public void setClassifiedLabels(List<Map<String,Double>> classifiedDistributions, Vector<Timestamp[]> time){		
		int i=0;
		for(int j=0;j<time.size();j++){
			Timestamp[] ta = time.get(j);
			for(int k=0;k<ta.length;k++){
				while(timestamps[i].before(ta[k])){
					i++;
				}
				if(classifiedLabels[i]==null){
					this.classifiedDistributions.set(i,classifiedDistributions.get(j));
					String cla = null;
					double max = Double.NEGATIVE_INFINITY; 
					for(String key : classifiedDistributions.get(j).keySet()){
						if(classifiedDistributions.get(j).get(key).doubleValue()>max){
							max = classifiedDistributions.get(j).get(key).doubleValue();
							cla = key;
						}
					}
					classifiedLabels[i] = cla;
				}
				else{
					System.err.println("warning: "+this.getTrueLabel()
							+" - classification label has been overwritten for timestamp "+timestamps[i].toString());					
				}					
				i++;
			}			
		}
	}
	
	public void setReClassifiedLabels(String[] labels){
		this.classifiedLabels = labels;
	}
	
	/**
	 * uses an average filter to remove outlier
	 * @param windowLength
	 */
	public void removeOutlier(int windowLength){
		if(outlierRemoved){
			System.err.println("Warning: Outlier have already been removed - multiple removals could cause misleading results...");
		}
		rawdata = PrimitiveArrayConverter.convertDoubleArray(AverageFilter.filter(PrimitiveArrayConverter.convertDoubleArray(rawdata), windowLength));
		outlierRemoved = true;
	}
	
	/**
	 * predicts the truncated peaks
	 * @throws Exception
	 */
	public void predictTruncatedPeaks() throws Exception{
		if(peaksPredicted){
			System.err.println("Warning: Peaks have already been predicted - multiple predictions could cause misleading results...");
		}
		double maxValue = 2.0;
		for(int i=0;i<rawdata.length;i++){
			double[] data = PrimitiveArrayConverter.convertDoubleArray(rawdata[i]);			
			
			for(int j=0;j<data.length;){
				double value = data[j];
				if(Math.abs(value)==maxValue){
					int count = 0;					
					while(j+count<data.length && value==data[j+count]){
						count++;						
					}
					
					if(count>=2){
						int start = j;
						int end = j + count -1;
						
						int beforeIndex = Math.max(0, start-2);
						int afterIndex = Math.min(end+2, data.length-1);
						double before = 0.0;
						double after = 0.0;
						
						if(start!=beforeIndex){
							for(int b=beforeIndex;b<start;b++){
								 before += data[b+1] - data[b];
							}
							before /= start-beforeIndex;
						}

						if(end!=afterIndex){
							for(int a=afterIndex;a>end;a--){
								 after += data[a-1] - data[a];
							}
							after /= afterIndex-end;
						}

						
						double d = (before+after)/2;						
						
						for(int k=1;k<(count/2);k++){
							data[start + k] += Math.sqrt(k)*d;
							data[end - k] +=  Math.sqrt(k)*d;
							if(k==((count/2)-1)){
								if(Math.abs(before)>Math.abs(after)){
									data[end-k] += d;
								}
								else{
									data[start+k] += d;
								}
							}
						}
						if(count%2!=0){
							data[start + ((count/2))] +=   Math.sqrt(count/2)*d;
						}
						if(count==2){
							if(Math.abs(before)>Math.abs(after)){
								data[end] += d;
							}
							else{
								data[start] += d;
							}
						}
					}
					j += count;
				}
				else{
					j++;
				}
			}

			rawdata[i] = PrimitiveArrayConverter.convertDoubleArray(data);
		}
		peaksPredicted = true;
	}
	
	/**
	 * adds a new Segment border for an periodical segment
	 * @param start
	 * @param end
	 */
	public void addSegmentBorder(Timestamp start, Timestamp end){
		segmentStarts.add(start);
		segmentEnds.add(end);
	}
	
	/**
	 * calculates the surrounding segmentation rate
	 * @return surrounding segmentation rate
	 * @throws Exception
	 */
	public Map<Timestamp,Double> calcSegmentationRates() throws Exception{
		if(segmentStarts!=null && segmentStarts.size()>0){
			
			segmentationRate.clear();
			
			Collections.sort(segmentStarts);
			Collections.sort(segmentEnds);						
			
			int segIndex = 0;
			for(int i=0;i<timestamps.length; i += SEGMENTATION_RATE_WINDOW ){
				int segCount = 0;
				int nonSegCount = 0;
				for(int j=i;j<Math.min(timestamps.length, i+SEGMENTATION_RATE_WINDOW);j++){
					Timestamp t = timestamps[j];
					if((t.equals(segmentStarts.get(segIndex)) || t.after(segmentStarts.get(segIndex))) && t.before(segmentEnds.get(segIndex))){
						segCount++;
					}
					else if(t.equals(segmentEnds.get(segIndex))){
						segCount++;
						segIndex = Math.min(segIndex+1, segmentStarts.size()-1);
					}
					else{
						nonSegCount++;
					}
				}
				Double rate = Double.valueOf((double)(segCount)/(double)(segCount+nonSegCount));
				segmentationRate.put(timestamps[i], rate);				
			}
			return segmentationRate;
		}
		else{
			throw new Exception("segment bordes haven't been set yet...");
		}
	}	
		
    public String toCSVString(String separator, boolean intValues){
    	StringBuffer sb = new StringBuffer();    	
    	for(int i=0; i < timestamps.length; i++){
    		sb.append(this.timestamps[i]);    		
    		for(int j=0; j < rawdata.length;j++){
    			sb.append(separator);
    			if(intValues){
    				sb.append((int)(rawdata[j][i]*64));
    			}
    			else{
    				sb.append(NF.format(rawdata[j][i]));
    			}
    		}    		
    		sb.append("\n");
    	}
    	return sb.toString();
    }
    
    public Map<String,Double> getClassifiedDistribution(int sampleId){
    	return classifiedDistributions.get(sampleId);
    }
    
    public List<Timestamp>  getSegmentStartBorders(){
    	
    	return segmentStarts;
    }
    
    public List<Timestamp>  getSegmentEndBorders(){
    	
    	return segmentEnds;
    }


}
