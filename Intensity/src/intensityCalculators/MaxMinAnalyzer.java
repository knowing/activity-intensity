package intensityCalculators;

import importedAlgorithms.RawData;

import java.sql.Timestamp;
import java.util.Vector;

import mainFiles.Algorithm;

import org.apache.log4j.Logger;
/**
 * Calculates the intensity based on the
 * difference of following Maximum and Minimum 
 * in a 3D-Graph ("Extremwertanalyse") including data-norming.
 * 
 * @author Benjamin Sauer
 *
 */
public class MaxMinAnalyzer implements IntensityAnalyzerInterface{

	Vector<RawData> rawData 				= null;
	static Logger   logger 					= Logger.getLogger(Algorithm.class);
	private int dataNormingRate 			= 40;

	public MaxMinAnalyzer(Vector<RawData> rawData_,int useDataNorming_){
		this.rawData = rawData_;
		this.dataNormingRate = useDataNorming_;
		if (dataNormingRate <= 0){logger.info("Data norming is turned off, norming rate has to be larger than 0");}
	}

/**
 * Calculates the intensity of the given
 * raw Data by analyzing the difference
 * of preceding max-/min-values..
 * @author Benjamin Sauer
 */
	public Vector<Double>[] calcIntensity() throws Exception {
		int fileIndex = 0;
		@SuppressWarnings("unchecked")
		Vector<Double>[] indexedReturn = new Vector[this.rawData.size()];
		for (RawData raw : this.rawData){
			Vector<Double> sampleReturn = new Vector<Double>();
			
			if(raw.getSegmentStartBorders().size()!=0 && dataNormingRate != 0){						//raw.getSegmentStartBorders().size()!=0 is true if segmentation is used.
				for(int i =0; i < raw.getSegmentStartBorders().size(); i++){						//Calculates and saves intensity values for all Segments 
			       	Timestamp start = raw.getSegmentStartBorders().get(i);
			       	

			    	if (i < raw.getSegmentStartBorders().size() -1){
				       	Timestamp nextStart = raw.getSegmentStartBorders().get(i+1);
				       	double intensity = this.calcIntensity(start, nextStart,fileIndex);					//segment to end
				       	for(int y = 0;y < (nextStart.getTime() - start.getTime())/dataNormingRate ;y++){					
				       		sampleReturn.add((intensity));
				       	}
			    	}
			     } 
				
			}else{																					
				System.out.println("Calculation for this module not possible without standard Data Norming of 1!");
			}

	       	indexedReturn[fileIndex] = sampleReturn;
//			logger.debug("Voids: "+ (raw.getDimension(0).length));
//			logger.debug("full: "+ (indexedReturn[fileIndex].size()));
	       	fileIndex++;
		}    	
		return indexedReturn;
	}
	
	/**
	 * Calculates the intensity of the given
	 * raw Data by analyzing the difference
	 * of preceding max-/min-values.
	 * The method is at first looking for
	 * the right index of the given timestamps.
	 * Afterwards it calculates the absolute difference
	 * of following max-values 
	 * (values which are preceded by lower and followed by lower values)
	 * and analogue calculated min-values.
	 * @param from The Timestamp to begin the analysis
	 * @param to the Timestamp to end the analysis
	 * @param fileIndex index of the rawData
	 */
	public double calcIntensity(Timestamp from, Timestamp to, int fileIndex)throws Exception {
		
		int arrayStart = 0;
		int arrayEnd = 0;
		double count = 0;

		RawData tempRawData = rawData.get(fileIndex);						
		while (from !=  tempRawData.getTimestamps()[arrayStart]){arrayStart++;}						//Search for the Start-Index in DataArray
	
		while (to !=  tempRawData.getTimestamps()[arrayEnd]){		arrayEnd++;}					//Search for the End-Index in DataArra		
		arrayEnd++;																					//Otherwise the last sample would be lost
		
		Double[][] dimensionArray = {	tempRawData.getDimension(0),
										tempRawData.getDimension(1),
										tempRawData.getDimension(2)};

		double dimSum		= 0;
		
		for (Double[] dimension : dimensionArray ){
			int arrayStartTemp 	= arrayStart;
			double minMaxSum	= 0;
			double maxTemp 		= dimension[arrayStart];
			double minTemp 		= dimension[arrayStart];
			boolean maxFound 	= false;
			boolean minFound	= false;
			count = 0;														//Number of Min/Max values
			while(arrayStartTemp != arrayEnd){								//Calculating the mean of the Max-Min Difference Values End Timesample
																			// If max is found, look for min, if min is found, look for max. 
																			// If difference was calculated and last value found was min, look for max
																			// otherwise min-value
					double tempvalue = dimension[arrayStartTemp];
					if (!maxFound 	&& Double.compare(tempvalue, maxTemp) > 0 
									&& Double.compare(tempvalue, dimension[arrayStartTemp+1]) > 0)	{
						maxTemp = dimension[arrayStartTemp];
	       				minMaxSum += Math.abs(maxTemp-minTemp);

	       				count ++;
	       				minTemp	= dimension[(arrayStartTemp+1)];
	       				maxFound = true; 	
	       				minFound = false;		
		       		}
					
	       			if (!minFound 	&& Double.compare(tempvalue, minTemp) < 0 
	       							&& Double.compare(tempvalue, dimension[arrayStartTemp+1]) < 0){
	       				minTemp = dimension[arrayStartTemp];
	       				minMaxSum +=  Math.abs(maxTemp-minTemp);
	       				maxTemp = dimension[(arrayStartTemp+1)];
	       				count ++;
	       				maxFound = false;
       					minFound = true;
 	       			}
	       			
	       			arrayStartTemp++;
	       	}	
			
			if (minMaxSum == 0 && count == 0){
				minMaxSum += (maxTemp - minTemp);
				count = 1;
			}
			dimSum += (minMaxSum/count);

		}
		return (dimSum/3);
	}




}
