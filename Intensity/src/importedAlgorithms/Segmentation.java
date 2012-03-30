
package importedAlgorithms;

import java.sql.Timestamp;
import java.util.Vector;

import mainFiles.Algorithm;

import org.apache.log4j.Logger;

/**
 * class that provides the segmentation into periodical and non-periodical segments
 * @author Alexander Stautner & Christian Mönnig
 */
public class Segmentation {

    private RawData rawData;
    private int dimensions;
    private double minCorrelation;
    private int patternSize;
    private int dimensionsWithSegments;
    private int minDimensionsWithSegments = 1;
    private int minLengthSegment;
    private Double[][] patterns;
    private Vector<Segment> segments = new Vector<Segment>();
    private Vector<Segment> nonSegments = new Vector<Segment>();
    private int[] shifts;
    private double[] shiftsCorrelation;
    private int lengthShiftSample;
	private static Logger logger = Logger.getLogger(Algorithm.class);

    

    public Segmentation(RawData rawData, double minCorrelation, int patternSize, int dimensions, int minLengthSegment, int lengthShiftSample)
    {
        this.rawData = rawData;
        this.minCorrelation = minCorrelation;
        this.patternSize = patternSize;
        this.dimensions = dimensions;
        this.minLengthSegment = minLengthSegment;
        patterns = new Double[dimensions][];
        shifts = new int[dimensions];
        shiftsCorrelation = new double[dimensions];
        this.lengthShiftSample = lengthShiftSample;

        

    }

    private void getPatterns(int startposition)
    {
        for(int i = 0 ; i < dimensions; i++)
        {
            Double[] values = rawData.getDimension(i);
            Double[] pattern = new Double[patternSize];
            System.arraycopy(values,startposition,pattern,0,patternSize);
            patterns[i] = pattern;
        }
    }

    private void calcShifts(int startposition)
    {
        for(int i = 0; i < dimensions; i++)
        {
            Double[] values = rawData.getDimension(i);
            Double[] sample = new Double[lengthShiftSample];
            System.arraycopy(values, startposition, sample, 0, lengthShiftSample);
            double correlation = Double.NEGATIVE_INFINITY;
            int shift = 0;
            Autocorrelation autocorrelation = new Autocorrelation();
            for(int j = 1; j<= lengthShiftSample-patternSize; j++)
            {
                Double[] testArray = new Double[patternSize];
                System.arraycopy(sample, j, testArray, 0, patternSize);
                double tempcorr = autocorrelation.calcAutocorrelation(testArray, patterns[i]);
                tempcorr = Math.round(tempcorr*1000000.)/1000000.;
                if(tempcorr > correlation)
                {
                    correlation = tempcorr;

                    shift = j;
                }

            }
            shifts[i] = shift;
            shiftsCorrelation[i] = correlation;
            //logger.info("Dimension: "+i +" Korrelation: "+correlation+ " Shift: "+shift);
        }
        
    }

    private int calcCorrelations(int startposition, Double[][] values)
    {
        Autocorrelation autocorr = new Autocorrelation();
        
        //logger.info("Startposition: "+startposition);
        
        int count = 0;
        for(int i = 0; i < dimensions; i++)
        {
          Double[] array = new Double[patternSize];
          System.arraycopy(values[i], startposition, array, 0, patternSize);
          double corr = autocorr.calcAutocorrelation(patterns[i], array);
          if(corr >= minCorrelation)
          {
              count++;
          }
        }
        
        return count;
    }

    private int bestShift()
    {
        int result = -1;
        double bestcorrelation = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < dimensions; i++)
        {
            if(shiftsCorrelation[i]> bestcorrelation)
            {
                bestcorrelation = shiftsCorrelation[i];
                result = i;
            }
           // System.out.println("ShiftsCorr: "+i+" "+shiftsCorrelation[i]);
        }
        //System.out.println("Result: "+result+" BestCorr "+ bestcorrelation);
        return result;
    }

    public void calcSegmentation()
    {
       Double[][] values = new Double[dimensions][];
       Timestamp[] timestamps = rawData.getTimestamps();
       boolean isSegment = false;
       Segment segment = new Segment(dimensions);
       Segment nonSegment = null;
       
       for(int i = 0; i < dimensions; i++)
       {
           values[i] = rawData.getDimension(i);
       }
       getPatterns(0);
       calcShifts(0);
       int bestShift = bestShift();
       if(shifts[bestShift]>0)
       {
           for(int i = 0; i <= values[0].length-lengthShiftSample; i=i+shifts[bestShift])
           {
               if(isSegment)
               {
                  int correlations = calcCorrelations(i, values);
                  //logger.info("Correlations isSegnment: "+correlations+" Shifts: "+shifts[0]);
                   if(correlations >= minDimensionsWithSegments)
                   {
                       Double[] segmentValues = new Double[shifts[bestShift]];
                       Timestamp[] timeValues = new Timestamp[shifts[bestShift]];
                       for(int j = 0; j < dimensions; j++)
                       {
                            System.arraycopy(values[j], i, segmentValues, 0, shifts[bestShift]);
                            segment.setDimension(j, segmentValues);
                            isSegment = true;
                       }
                       System.arraycopy(timestamps, i,timeValues,0, shifts[bestShift]);
                       segment.setTimestamps(timeValues);
                   }
                   else
                   {
                       if(segment.size() > minLengthSegment)
                       {
                            getSegments().add(segment);
                       }
                       else{
                    	   if(nonSegment == null){
                    		   nonSegment = segment;
                    	   }
                    	   else{
                    		   Timestamp t1 = nonSegment.getTimestamps().lastElement();
                    		   Timestamp t2 = segment.getTimestamps().firstElement();
                    		   if(Math.abs(t1.getTime()-t2.getTime())==40){
                    			   for(int j = 0; j < dimensions; j++){
                    				   Double[] tmpD = new Double[segment.size()];                    				   
                    				   segment.getValues(j).toArray(tmpD);
                    				   nonSegment.setDimension(j, tmpD);                    				   
                    			   }   
                    			   Timestamp[] tmpT = new Timestamp[segment.getTimestamps().size()];
                    			   segment.getTimestamps().toArray(tmpT);
                				   nonSegment.setTimestamps(tmpT);
                               }
                    		   else{
                    			   nonSegments.add(nonSegment);
                    			   nonSegment = segment;
                    		   }
                    	   }                    	                       	   
                    	   
                       }
                       i=i-shifts[bestShift];
                       isSegment=false;
                   }

               }
               else
               {
                   getPatterns(i);
                   calcShifts(i);
                   bestShift = bestShift();
                   segment = new Segment(dimensions);
                   
                   int correlations = calcCorrelations(i, values);
                 //System.out.println("Correlations isNotSegment: "+correlations+" Shifts: "+shifts[bestShift]);
                   if(correlations >= minDimensionsWithSegments)
                   {
                       Double[] segmentValues = new Double[shifts[bestShift]];
                       Timestamp[] timeValues = new Timestamp[shifts[bestShift]];
                       for(int j = 0; j < dimensions; j++)
                       {
                            System.arraycopy(values[j], i, segmentValues, 0, shifts[bestShift]);
                            segment.setDimension(j, segmentValues);
                            isSegment = true;

                       }
                       System.arraycopy(timestamps, i,timeValues,0, shifts[bestShift]);
                       segment.setTimestamps(timeValues);
                       getPatterns(i);
                       calcShifts(i);
                   }
                   else
                   {
                       if(segment.size() > minLengthSegment)
                       {
                            getSegments().add(segment);
                       }
                       else{
                    	   if(nonSegment == null){
                    		   nonSegment = segment;
                    	   }
                    	   else{
                    		   Timestamp t1 = nonSegment.getTimestamps().lastElement();
                    		   Timestamp t2 = segment.getTimestamps().firstElement();
                    		   if(Math.abs(t1.getTime()-t2.getTime())==40){
                    			   for(int j = 0; j < dimensions; j++){
                    				   Double[] tmpD = new Double[segment.size()];                    				   
                    				   segment.getValues(j).toArray(tmpD);
                    				   nonSegment.setDimension(j, tmpD);                    				   
                    			   }   
                    			   Timestamp[] tmpT = new Timestamp[segment.getTimestamps().size()];
                    			   segment.getTimestamps().toArray(tmpT);
                				   nonSegment.setTimestamps(tmpT); 
                               }
                    		   else{
                    			   nonSegments.add(nonSegment);
                    			   nonSegment = segment;
                    		   }
                    	   }  
                       }
                       i=i-shifts[bestShift];
                       isSegment=false;
                   }
               }

           }

           if(segment.size() > minLengthSegment)
           {
                getSegments().add(segment);
           }
           else{
        	   if(nonSegment!=null){
        		   if(segment.getTimestamps().lastElement().before(nonSegment.getTimestamps().firstElement())){
        			   nonSegments.add(segment);
        			   nonSegments.add(nonSegment);        			   
        		   }
        		   else{
        			   nonSegments.add(nonSegment);
        			   nonSegments.add(segment);
        		   }
               }        	   
        	   else{
        		   nonSegments.add(segment);
        	   }
           }           
       }
    }


    /**
     * @return the rawData
     */
    public RawData getRawData() {
        return rawData;
    }

    /**
     * @param rawData the rawData to set
     */
    public void setRawData(RawData rawData) {
        this.rawData = rawData;
    }

    /**
     * @return the dimensions
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions the dimensions to set
     */
    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * @return the minCorrelation
     */
    public double getMinCorrelation() {
        return minCorrelation;
    }

    /**
     * @param minCorrelation the minCorrelation to set
     */
    public void setMinCorrelation(double minCorrelation) {
        this.minCorrelation = minCorrelation;
    }

    /**
     * @return the patternSize
     */
    public int getPatternSize() {
        return patternSize;
    }

    /**
     * @param patternSize the patternSize to set
     */
    public void setPatternSize(int patternSize) {
        this.patternSize = patternSize;
    }

    /**
     * @return the dimensionsWithSegments
     */
    public int getDimensionsWithSegments() {
        return dimensionsWithSegments;
    }

    /**
     * @param dimensionsWithSegments the dimensionsWithSegments to set
     */
    public void setDimensionsWithSegments(int dimensionsWithSegments) {
        this.dimensionsWithSegments = dimensionsWithSegments;
    }

    /**
     * @return the minDimensionsWithSegments
     */
    public int getMinDimensionsWithSegments() {
        return minDimensionsWithSegments;
    }

    /**
     * @param minDimensionsWithSegments the minDimensionsWithSegments to set
     */
    public void setMinDimensionsWithSegments(int minDimensionsWithSegments) {
        this.minDimensionsWithSegments = minDimensionsWithSegments;
    }

    /**
     * @return the minLengthSegment
     */
    public int getMinLengthSegment() {
        return minLengthSegment;
    }

    /**
     * @param minLengthSegment the minLengthSegment to set
     */
    public void setMinLengthSegment(int minLengthSegment) {
        this.minLengthSegment = minLengthSegment;
    }


    /**
     * @return the segments
     */
    public Vector<Segment> getSegments() {
        return segments;
    }
    
    public double getSegmentLengthMean(){
    	double mean = 0.0;
    	for(Segment s : segments){
    		mean += s.size();
    	}
    	mean /= segments.size();
    	return mean;
    }
    
    public double getSegmentLengthVariance(){
    	double mean = this.getSegmentLengthMean();
    	double var = 0.0;
    	for(Segment s : segments){
    		var += Math.pow(s.size()-mean,2);    		
    	}
    	var /= segments.size();
    	return var;
    }
    
    /**
     * Get the data that isn't in a Segment
     * @return the data that isn't in a segment
     */
    public Vector<Segment> getNonSegments(){
    	return nonSegments;
    }
    
    /**
     * @return the percentage of RawData that has been successfully segmented
     */
    public double getPercentageOfDataInSegments(){
    	int samplesInSegments = getNumberOfSamplesInSegments();
    	return ((double)(samplesInSegments)/this.getRawData().getTimestamps().length)*100.0;
    }
    
    public int getNumberOfSamplesInSegments(){
    	int samplesInSegments = 0;
    	for(Segment s : this.getSegments()){
    		samplesInSegments += s.size();
    	}
    	return samplesInSegments;
    }

}
