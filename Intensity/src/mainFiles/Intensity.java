
package mainFiles;

import importedAlgorithms.CSVRawLoader;
import importedAlgorithms.RawData;
import importedAlgorithms.Segment;
import importedAlgorithms.Segmentation;
import intensityCalculators.DimensionMean;
import intensityCalculators.MaxMinAnalyzer;
import intensityCalculators.MovementCalculator;

import java.io.File;
/*java.util.Vector is being used because of the usage 
 * in the algorithms provided by Christian Moennig. 
 * Future implementation won't use this deprecated class.
 */
import java.util.Vector;		

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import classificationData.RunningClassificator;
import classificationData.WalkingClassificator;

/**
 * Provides a structure for the analysis of the level of 
 * intensity for any given threedimensional accelerationdata
 * based on algorithms provided by Christian Moennig.
 * 
 * @author Benjamin Sauer
 * 
 *
 */
public class Intensity {
	
	private static Logger logger 				= Logger.getLogger(Algorithm.class);
	private Vector<RawData> rawData 			= null;							//contains the rawData of all loaded files
	private boolean usePeakPrediction 			= true;							//use truncated peak prediction
	private boolean useSegmentation 			= false; 						//use data segmentation into periodical and non-periodical data
	private boolean useOutlierRemoval			= true;						//use outlier removal to remove outlying data
	private int dataNormingRate					= 40;		 					//returns the same intensity value multiple times to represent segment length
	private boolean printWholeFileMean			= false;						//if set, the mean value of every file will be printed 
	private boolean useDataBucketing			= false;						//use data Bucketing to get a less accurate depiction of the data
	private boolean useClassification			= true;							//run a detailed classification after data norming
	private double bucketAccuracy 				= 0.0;							
	private final int OUTLIER_REMOVAL_WINDOW	= 3;
	private File[] files 						= null;
	private int weightOfUser					= 75;							//Weight in kg, to calculate kalorie-expenditure
	private int segmentedSamples, 
				sampleNumber, 
				nonSegmentedSamples 			= 0;
	private Vector<Segment> segments,								
							nonSegments			= null;							//contains the (non-)segments

	
	public Intensity(File[] files) throws Exception{
		this.files 		= files;
       	this.rawData 	= new Vector<RawData>();       	
	}
	
	public void setWeightOfUser(int weightOfUser_){
		this.weightOfUser = weightOfUser_;
	}
	
	public void stopPeakPrediction(){
		this.usePeakPrediction = false;
		logger.info("Peak prediction use was changed to: " + usePeakPrediction);
	}
	
	public void doNotUseClassification(){
		this.useClassification=false;
		logger.info("Classification use was changed to: " + useClassification);
	}
	
	public void doNotUseOutlierRemoval(){
		this.useOutlierRemoval = false;			
		logger.info("Use of outlier removal was changed to: " + useOutlierRemoval);
	}
	
	public void printWholeFileMean(){
		printWholeFileMean = true;	
		logger.info("Whole file mean will be printed: "+printWholeFileMean);
	}
	
	public void setDataNormingRate(int rate){
		this.dataNormingRate = rate*40;
	}
	
//	public void setNoMovementThreshold(double threshold){
//		this.noMovementThreshold = threshold;
//	}
	
	public void useDataBucketing(double accuracy_){
		if (accuracy_ > 0){
			this.bucketAccuracy = accuracy_;
			this.useDataBucketing = true;
			logger.info("Data bucketing was used with accuracy: " +bucketAccuracy);
		}else{
			logger.info("Bucketing is not being used because the accuracy is not bigger than 0.0");
		}
	}
	/**
	 * Prints the number of segmented samples.
	 */
	public void getSegmentedSamples(){
		if (segmentedSamples!=0)logger.info("Number of Samples in Segments: " + segmentedSamples);
		else logger.info("Number of Samples in Segments: "+ segmentedSamples + " (probably not segmented yet)");
	}
	/**
	 * Prints the number of non-segmented samples.
	 */
	public void getNonSegmentedSamples(){
		if (nonSegmentedSamples !=0)logger.info("Number of currently not segmented Samples: " + nonSegmentedSamples);
		else logger.info("Number of currently not segmented Samples: " + nonSegmentedSamples + " (probably not segmented yet)");
	}

	/**
	 * Prints the overall number of samples.
	 */
	public void getSampleNumber(){
		if (sampleNumber !=0)logger.info("Number of samples in Data: " + sampleNumber);
		else logger.info("Number of Samples: " + sampleNumber + " (probably empty raw-data file)");
	}
	
	public Vector<RawData> getRawData() throws Exception{
		if (!useSegmentation){														//If RawData is not segmented
	    	logger.info("Input data was not segmented! use instance.segment() to do so.");
			for(File file : files){
		    		RawData rd 		= null; 										//the temporary RawData storage
			    	String actClass = file.getName().split("-")[1];	
			    	rd = CSVRawLoader.loadCSV(file, 3);								//Load File
			    	if (useOutlierRemoval){											//Removes Outlier
			    		rd.removeOutlier(OUTLIER_REMOVAL_WINDOW);	
			    	}
			    	if(usePeakPrediction){											//Predict peaks
			    		rd.predictTruncatedPeaks();
			    	}	 
			    	rd.setTrueLabel(actClass);									   	//Label Raw Data
			    	sampleNumber += rd.getTimestamps().length;
			    	rawData.add(rd);	
			}	
		}
		logger.info("Removing outlying data is turned on: "+	useOutlierRemoval);
		logger.info("Predicting cut off peaks is turned on:"+ 	usePeakPrediction);
		return rawData;
	}
		
	/**
	 * Segments the raw-data using the Segmentation class.
	 */
	public void segment() throws Exception{
		useSegmentation = true;
		System.out.println("Segmenting Data.");


       	for(File file : files){
    		RawData rd 		= null; 										//the temporary RawData storage
	    	String actClass = file.getName().split("-")[1];	
	    	
	    	rd = CSVRawLoader.loadCSV(file, 3);								//Load File

	    	if (useOutlierRemoval){											//Removes outlying data
	    		rd.removeOutlier(OUTLIER_REMOVAL_WINDOW);
	    	}
	    	if(usePeakPrediction){											//Predict peaks
	    		rd.predictTruncatedPeaks();
	    	}	    	
	    	
	    	rd.setTrueLabel(actClass);									   	//Label Raw Data
	    	sampleNumber += rd.getTimestamps().length;
       
		    	Segmentation seg = new Segmentation(rd, 0.75, 25, 3, 100, 100);
			    seg.calcSegmentation();		    
			    this.segments = seg.getSegments();
			    this.nonSegments = seg.getNonSegments();
			    int nonSegmentIndex = 0;
			    Vector<Timestamp> tempNonSegmentTimeStamps = this.nonSegments.get(nonSegmentIndex).getTimestamps();
			    
				for(Segment tempSegment : segments){	
					
					if (nonSegmentIndex < this.nonSegments.size()){tempNonSegmentTimeStamps = this.nonSegments.get(nonSegmentIndex).getTimestamps();}		//Check for NonSegments
					//else {logger.info("No more Non Segments left!");}
					
					if (tempNonSegmentTimeStamps.firstElement().compareTo(tempSegment.getTimestamps().firstElement()) < 0){		//NonSegment if time "smaller" than Segments (just info)
							rd.addSegmentBorder(tempSegment.getTimestamps().firstElement(), tempSegment.getTimestamps().lastElement());
							//logger.info("Added NonSegment: from "+ tempSegment.getTimestamps().firstElement() + " to " + tempSegment.getTimestamps().lastElement());
							this.nonSegmentedSamples += tempSegment.size();
							nonSegmentIndex++;
							continue;
					}
					
				   	rd.addSegmentBorder(tempSegment.getTimestamps().firstElement(), tempSegment.getTimestamps().lastElement());
				   	//logger.info("Added Segment: from "+ tempSegment.getTimestamps().firstElement() + " to " + tempSegment.getTimestamps().lastElement());
				
				   	this.segmentedSamples += tempSegment.size();
				 	//System.out.println("Segmentgrš§e: "+s.size());
				    				    
		    	}
	       	
	       	rawData.add(rd);	    		    					    	//Add raw Data to file-Vektor
			}

		}
	
//	public Vector<Integer>[] calcZeroMovement(double movementThreshold) throws Exception{
//
//		MaxMinAnalyzer minMax = new MaxMinAnalyzer(rawData, 40);
//		Vector<Double>[] indexedIntensities = minMax.calcIntensity();
//		
//		@SuppressWarnings("unchecked")
//		Vector<Integer>[] zeroMovTimeIndices = new Vector[this.rawData.size()];
//
//		
//		int fileIndex = 0;
//		
//		for(Vector<Double> vd : indexedIntensities){
//			int i = 0;
//			Vector<Integer> indices = new Vector<Integer>();
//			for (Double dd : vd){
//				if(dd < movementThreshold){
//					indices.add(i);
//				}
//				i++;
//			}
//			zeroMovTimeIndices[fileIndex]=indices;
//			fileIndex++;
//		}		
//		return zeroMovTimeIndices;
//	}

	/**
	 * Calculates the intensity of all given files and 
	 * generates a jFree XY-Series graph depending on the given method.
	 * @param method specifies the selected method: 0 being the "Durchschnittsanalyse", 1 being the "Extremwertanalyse"
	 * @param output specifies the directory for the output
	 * @throws Exception
	 */
	public void generateGraph(int method,String output) throws Exception{
		Vector<Double>[] intensities = null; //java.util.vector is deprecated, but still being used because the segmentation uses java.util.vector
		int fileIndex = 0;
   		System.out.println("Calculating intensity for " + files[fileIndex].getName() +", please stand by.");

		switch (method) {
	        case 0:  {
	        	System.out.println("Method of calculation used: Simple 3D Mean");
		        DimensionMean dimCalc = new DimensionMean(this.getRawData(),dataNormingRate);
		        intensities = dimCalc.calcIntensity();

		    }break;
		    
	        case 1:  {
	        	System.out.println("Method of calculation used: Maximum/Minimum Difference");
		        MaxMinAnalyzer maxMinCalc = new MaxMinAnalyzer(this.getRawData(),dataNormingRate);
		        intensities = maxMinCalc.calcIntensity();
		    }break;
		    //The Movement Analyzer is not an option included in the Viewer GUI!
	        case 2:  {
	        	System.out.println("Method of calculation used: Movement Analyzer (experimental)");
		        MovementCalculator movCalc = new MovementCalculator(this.getRawData(),dataNormingRate);
		        intensities = movCalc.calcIntensity();
		    }break;
		    
	        default: logger.warn("Invalid Calculation Method, possible Methods 0-2"); break;
	       	}
		
			System.out.println("\n\nAnalysis of given Data:\n");
        for (Vector<Double> values : intensities){
    		
        	if(printWholeFileMean){				//calculates the mean of the whole files of already calculated intensities
    			double mean = 0;
    			int count = 0;
    			for (double d: values){
    				mean += d;
    				count++;
    			}
    			logger.debug("Mean of file "+files[fileIndex].getName()+" :" + mean/count);
    		}

        	if (useDataBucketing){
        		logger.debug("Grš§e danach: " + values.size());
				GranularityScaling bucketedData = new GranularityScaling(values,bucketAccuracy);
				values = bucketedData.scaleToGivenGranularity();
        	}
        	
			if(useClassification){
				
				System.out.println("3D-Intensity analysis of " + files[fileIndex].getName());
        		WalkingClassificator walkie = new WalkingClassificator(method);
        		walkie.classifyValues(values);
        		walkie.calculatePercentages();
        		System.out.println(walkie.toString());
	        	System.out.println("Number of calories:" + walkie.calculateCalories(this.weightOfUser)+ " (very Experimental!)");
        		System.out.println(walkie.getClassWithHighestPercentage());
        		
        		RunningClassificator runnie = new RunningClassificator(method);
        		runnie.classifyValues(values);
        		runnie.calculatePercentages();
        		System.out.println(runnie.toString());
        		System.out.println("Number of calories:" + runnie.calculateCalories(this.weightOfUser) + " (very Experimental!)");
        		System.out.println(runnie.getClassWithHighestPercentage());
        	}
        	


       		ChartMaker chart = new ChartMaker("\n3D-Intensity analysis of " + files[fileIndex].getName());
       		for(int i = 0; i < values.size();i++){
	       		chart.addToChart(i,values.get(i));
	       		//logger.debug("Added values: " + values.get(i));
       		}
	       	System.out.print("Generating Chart...");
	       	//Generates the actual chart with given parameters
		    chart.generateChart(files[fileIndex].getName()+" Method "+ method+ " Data Norming rate "+dataNormingRate+" accuracy "+ bucketAccuracy +".jpg",output);
	       	
	       	System.out.println(" -> Chart Generated!");
       		fileIndex++;
	    }
		
		
	} 

}