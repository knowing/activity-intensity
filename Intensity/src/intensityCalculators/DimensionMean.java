package intensityCalculators;

import importedAlgorithms.RawData;

import java.sql.Timestamp;
import java.util.Vector;

import mainFiles.Algorithm;

import org.apache.log4j.Logger;

/**
 * Provides a interface for a mean-based intensity analysis ("Durchschnittsanalyse") including data-norming.
 * @author Benjamin Sauer
 *
 */
public class DimensionMean implements IntensityAnalyzerInterface{
	
	private Vector<RawData> rawData = null;
	private static Logger logger 		= Logger.getLogger(Algorithm.class);
	private int dataNormingRate 		= 40;
	
	public DimensionMean(Vector<RawData> rawData_,int useDataNorming_){
		this.rawData = rawData_;
		this.dataNormingRate = useDataNorming_;
		if (dataNormingRate <= 0){logger.info("Data norming is turned off, norming rate has to be larger than 0");}
	}


	

	/**
	 * Calculates the intensity of all
	 * the given rawData.
	 * @return double[][] with Index of intensity is Fileindex
	 * @throws Exception 
	 */
	public Vector<Double>[] calcIntensity() throws Exception{
	
		int fileIndex = 0;
		@SuppressWarnings("unchecked")
		Vector<Double>[] indexedReturn = new Vector[this.rawData.size()];
		for (RawData raw : this.rawData){
			Vector<Double> sampleReturn = new Vector<Double>();
			if(raw.getSegmentStartBorders().size()!=0 && dataNormingRate != 0){						//raw.getSegmentStartBorders().size()!=0 is true if segmentation is being used.
				for(int i =0; i < raw.getSegmentStartBorders().size(); i++){						//Calculates and saves intensity values for all Segments 
			       	Timestamp start = raw.getSegmentStartBorders().get(i);
			       	

			    	if (i < raw.getSegmentStartBorders().size() -1){	//Parts which are not segmented, are not taken into account!
				       	Timestamp nextStart = raw.getSegmentStartBorders().get(i+1);
				       	double intensity = this.calcIntensity(start, nextStart,fileIndex);					//DATA NORMING PART
				       	for(int y = 0;y < (nextStart.getTime() - start.getTime())/dataNormingRate ;y++){	
				       		sampleReturn.add(intensity);
				       	}
			    	}
			     } 
			}else{													//If segmentation is not being used, calculate average for every timestamp
				for(int i = 0; i < raw.getTimestamps().length; i++){
					double intensity = this.calcIntensity(raw.getTimestamps()[i], raw.getTimestamps()[i], fileIndex);
					sampleReturn.add(intensity);
				}
			}
	       	indexedReturn[fileIndex] = sampleReturn;
//			logger.debug("Voids: "+ (raw.getDimension(0).length));
//			logger.debug("full: "+ (indexedReturn[fileIndex].size()));

	       	fileIndex++;
		}    	
		
		return indexedReturn;
	}
	
	/**Returns the intensity of
	 * a given file by calculating
	 * the average of all three dimensions.
	 * @param from	first Timestamp-Index
	 * @param to	last Timestamp-Index
	 * @param fileIndex	file of rawData vector to be used
	 * @throws Exception
	 */
	public double calcIntensity (int from_, int to_, int fileIndex){
		int arrayStart = from_;
		int arrayEnd = to_;
		double tempsum = 0;		
		int meanDivider = arrayEnd - arrayStart;													//Amount of Samples
		RawData tempRawData = rawData.get(fileIndex);	
		
		while(arrayStart != arrayEnd){																//Summing up the Samples of all dimensions between the Start and End Timesample
       			tempsum += 		(Math.abs( tempRawData.getDimension(0)[arrayStart]) 
       						+ 	Math.abs( tempRawData.getDimension(1)[arrayStart]) 
       						+	Math.abs( tempRawData.getDimension(2)[arrayStart]))/3;
       			arrayStart++;
       	}		
       	
			
		return tempsum/(meanDivider);
	}
	
	/**Returns the intensity of
	 * a given file by calculating
	 * the average of all three dimensions.
	 * @param from	first Timestamp
	 * @param to	last Timestamp
	 * @param fileIndex	file of rawData vector to be used
	 * @throws Exception
	 */
	public double calcIntensity(Timestamp from, Timestamp to,int fileIndex) throws Exception{			
		int arrayStart = 0;
		int arrayEnd = 0;

		RawData tempRawData = rawData.get(fileIndex);						
		while (tempRawData.getTimestamps()[arrayStart] != from  ){arrayStart++;}						//Search for the Start-Index in DataArray
	
		while (tempRawData.getTimestamps()[arrayEnd] != to){		arrayEnd++;}					//Search for the End-Index in DataArra		
		arrayEnd++;																					//Otherwise the last sample would be lost
		
		return calcIntensity(arrayStart,arrayEnd,fileIndex);
	}


}
