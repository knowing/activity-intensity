package intensityCalculators;

import importedAlgorithms.RawData;

import java.sql.Timestamp;
import java.util.Vector;
import javax.vecmath.Point3d;

import mainFiles.Algorithm;

import org.apache.log4j.Logger;
/**
 * Intensity calculator ("Bewegungsanalyse") based on the euclidian distance 
 * of two 3D-samples including data-norming (algorithm is not further used, just for educational purposes).
 * @author Benjamin Sauer
 */
public class MovementCalculator implements IntensityAnalyzerInterface {

	Vector<RawData> rawData 				= null;
	static Logger   logger 					= Logger.getLogger(Algorithm.class);
	private int dataNormingRate 			= 40;
	private 		int stepCounter = 0;

	public MovementCalculator (Vector<RawData> rawData_, int useDataNorming_){
		rawData = rawData_;
		this.dataNormingRate = useDataNorming_;
		if (dataNormingRate <= 0){logger.info("Data norming is turned off, norming rate has to be larger than 0");}
	}

	public Vector<Double>[] calcIntensity() throws Exception {

		int fileIndex = 0;
		@SuppressWarnings("unchecked")
		Vector<Double>[] indexedReturn = new Vector[this.rawData.size()];
		
		for (RawData raw : this.rawData){
			Vector<Double> sampleReturn = new Vector<Double>();
			if(raw.getSegmentStartBorders().size()!=0 && dataNormingRate != 0){												//raw.getSegmentStartBorders().size()!=0 is true if segmentation is used.
				for(int i =0; i < raw.getSegmentStartBorders().size(); i++){						//Calculates and saves intensity values for all Segments 
			       	Timestamp start = raw.getSegmentStartBorders().get(i);

			       	if (i == 0){
							double intensity = this.calcIntensity(raw.getTimestamps()[0], start, fileIndex);
				       		sampleReturn.add((intensity));
			       	}
			    	if (i < raw.getSegmentStartBorders().size() -1){
				       	Timestamp nextStart = raw.getSegmentStartBorders().get(i+1);
				       	double intensity = this.calcIntensity(start, nextStart,fileIndex);					////DATA NORMING PART
				       	for(int y = 0;y < (nextStart.getTime() - start.getTime())/dataNormingRate ;y++){					//divided by dataNormingRate to reduce DataOutput
				       		sampleReturn.add((intensity));
				       	}
			    	}
			     } 
				
			}else{									
				System.out.println("Calculation for this module not possible without segmentation and at least data norming of 1!");
			}
			
	       	indexedReturn[fileIndex] = sampleReturn;
	       	fileIndex++;
		}    	
		logger.info("Stepcounter "+ stepCounter);
		return indexedReturn;
	}
	


	@Override
	public double calcIntensity(Timestamp from, Timestamp to, int fileIndex)throws Exception {

		int arrayStart = 0;
		int arrayEnd = 0;

		RawData tempRawData = rawData.get(fileIndex);						
		while (tempRawData.getTimestamps()[arrayStart] != from  ){arrayStart++;}						//Search for the Start-Index in DataArray
	
		while (tempRawData.getTimestamps()[arrayEnd] != to){		arrayEnd++;}					//Search for the End-Index in DataArra		
		
		
		int count = 1;		
		int sampleCounter = 0;
		int verschiebungsfaktor = 25;
		int startpunkt = findPointOfMinMov(arrayStart,(arrayStart+verschiebungsfaktor),fileIndex);
		int endpunkt;
		
		while (startpunkt+verschiebungsfaktor < arrayEnd){			
			endpunkt = findPointOfMinMov(startpunkt+1,(startpunkt+verschiebungsfaktor),fileIndex);
//			logger.debug("startpunkt: " + startpunkt);
//			logger.debug("endpunkt: " + endpunkt);

			sampleCounter+= endpunkt-startpunkt; 
//			logger.debug("Stepsize "+ (endpunkt-startpunkt));
			count++;
			arrayStart = endpunkt;
			startpunkt = endpunkt;
//			logger.debug(count);
//			logger.debug("Samplecounter "+sampleCounter);
		}
		if (arrayStart+verschiebungsfaktor > arrayEnd && arrayStart < arrayEnd){
			endpunkt = findPointOfMinMov(startpunkt+1,arrayEnd,fileIndex);
			sampleCounter+=endpunkt-startpunkt;
			count++;
			arrayStart = endpunkt;
			startpunkt = endpunkt;
		}
//		logger.debug("Samplecounter: " +sampleCounter + " count " + count);
//		logger.debug("DurchschnittsschrittlŠnge: "+ sampleCounter/(double)count);
//		logger.info("Durchschnittsgeschwindigkeit:" + (sampleCounter/(double)count)*40/1000*3.6 + "km/h"); 
		return  -(sampleCounter/(double)count);
	}

	private int findPointOfMinMov (int start_, int end_, int fileIndex){
//		logger.debug("Anfang: " + start_+ " Ende: " + end_);
		int start = start_;
//		double sum = 	Math.pow(rawData.get(fileIndex).getDimension(0)[start],2) +
//						Math.pow(rawData.get(fileIndex).getDimension(1)[start],2) +
//						Math.pow(rawData.get(fileIndex).getDimension(2)[start],2);
//		sum = Math.sqrt(sum);
		
		Point3d from = new Point3d(	rawData.get(fileIndex).getDimension(0)[start],
									rawData.get(fileIndex).getDimension(1)[start],
									rawData.get(fileIndex).getDimension(2)[start]);
		int minMovIndex = start;
		
		while (start != end_){																//mean of difference of acceleration to fixed point
			Point3d tempPoint = new Point3d(	rawData.get(fileIndex).getDimension(0)[start],
												rawData.get(fileIndex).getDimension(1)[start],
												rawData.get(fileIndex).getDimension(2)[start]);
//			tempSum = Math.sqrt(tempSum);
//			if ((sum - tempSum > 0)){
//			sum = tempSum;
			if (from.distance(tempPoint) < 0.2){
			minMovIndex = start;
//			logger.debug("summe: " +minMovIndex);
			}
			start++;
		}
		return minMovIndex;
		
		
	}
	

}
