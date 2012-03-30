package importedAlgorithms;

import importedAlgorithms.AbstractFeatureVector;
import importedAlgorithms.Segment;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * Class to build the feature vector
 * @author Christian Mönnig
 *
 */

public class AugmentedSignalFV extends AbstractFeatureVector {
	
	private static final int OUTLIER_WINDOW = 3;	
	private static final int MIN_SAMPLES_IN_WINDOW = 40;
	
	private int windowSize = 80;
	private boolean useWholeSegmentForFV = true; 	
	private boolean useAlternativeFeatures = false;
	private int arModelOrder = 3;	
	
	private double rmse = 0.0;
	private double tempRMSE = 0.0;
	
	private Map<Timestamp, Double> segmentationRates;	
	
	/**
	 * 
	 * @param data 2-dimensional array with accelerometer data for each axis. data[0] = x-axis, data[1] = y-axis, data[2] = z-axis
	 * @param timestamps the associated timestamps for the data samples
	 */
	public AugmentedSignalFV(double[][] data, Timestamp[] timestamps){		
		this.useWholeSegmentForFV = false;
		this.init(data,timestamps,false);
	}
	
	/**
	 * 
	 * @param data 2-dimensional array with accelerometer data for each axis. data[0] = x-axis, data[1] = y-axis, data[2] = z-axis
	 * @param label class label for the given data
	 * @param timestamps the associated timestamps for the data samples
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 */
	public AugmentedSignalFV(double[][] data, String label, Timestamp[] timestamps, boolean removeOutlier){
		this.label = label;
		this.useWholeSegmentForFV = false;
		this.init(data,timestamps, removeOutlier);
	}
	
	/**
	 * 
	 * @param data 2-dimensional array with accelerometer data for each axis. data[0] = x-axis, data[1] = y-axis, data[2] = z-axis
	 * @param label class label for the given data
	 * @param timestamps the associated timestamps for the data samples
	 * @param arOrder order of the AR-Model
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 */
	public AugmentedSignalFV(double[][] data, String label, Timestamp[] timestamps, int arOrder, boolean removeOutlier){
		this.label = label;
		this.arModelOrder = arOrder;
		this.useWholeSegmentForFV = false;
		this.init(data,timestamps,removeOutlier);		
	}
	
	/**
	 * @param ss Vector with data segments
	 * @param label class label for the given data
	 * @param useAlternativeFeatures use alternative Features for the FV -> intended for non-segment data
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 */
	public AugmentedSignalFV(Vector<Segment> ss, String label,boolean useAlternativeFeatures, boolean removeOutlier){
		this.label = label;				
		this.useAlternativeFeatures = useAlternativeFeatures;
		preprareSegmentData(ss,removeOutlier);
	}
	
	/**
	 * @param ss Vector with data segments
	 * @param label class label for the given data
	 * @param useAlternativeFeatures use alternative Features for the FV -> intended for non-segment data
	 * @param segmentationRates segmenation rates for the considered data. The i-th value is valid from timestamp i until timestamp i+1 
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 */
	public AugmentedSignalFV(Vector<Segment> ss, String label,boolean useAlternativeFeatures, Map<Timestamp, Double> segmentationRates, boolean removeOutlier){
		this.label = label;		
		this.useAlternativeFeatures = useAlternativeFeatures;
		this.segmentationRates = segmentationRates;
		preprareSegmentData(ss,removeOutlier);
	}
	
	/**
	 * 
	 * @param ss Vector with data segments
	 * @param label class label for the given data 
	 * @param useAlternativeFeatures use alternative Features for the FV -> intended for non-segment data
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 * @param windowSize amount of samples to use for one FV
	 */
	public AugmentedSignalFV(Vector<Segment> ss, String label,boolean useAlternativeFeatures, boolean removeOutlier, int windowSize){
		this.label = label;
		this.useAlternativeFeatures = useAlternativeFeatures;
		this.windowSize = windowSize;
		this.useWholeSegmentForFV = false;
		preprareSegmentData(ss,removeOutlier);
	}
	
	/**
	 * 
	 * @param ss Vector with data segments
	 * @param label class label for the given data
	 * @param useAlternativeFeatures use alternative Features for the FV -> intended for non-segment data
	 * @param segmentationRates segmenation rates for the considered data. The i-th value is valid from timestamp i until timestamp i+1 
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 * @param windowSize amount of samples to use for one FV
	 */
	public AugmentedSignalFV(Vector<Segment> ss, String label,boolean useAlternativeFeatures, Map<Timestamp, Double> segmentationRates, boolean removeOutlier, int windowSize){
		this.label = label;
		this.useAlternativeFeatures = useAlternativeFeatures;
		this.windowSize = windowSize;
		this.useWholeSegmentForFV = false;
		this.segmentationRates = segmentationRates;
		preprareSegmentData(ss,removeOutlier);
	}
	
	/**
	 * 
	 * @param ss Vector with data segments
	 * @param label class label for the given data 
	 * @param useAlternativeFeatures use alternative Features for the FV -> intended for non-segment data
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 * @param wholeSegmentForFv use the whole length of the segment as window size for the FV
	 */
	public AugmentedSignalFV(Vector<Segment> ss, String label,boolean useAlternativeFeatures, boolean removeOutlier, boolean wholeSegmentForFv){
		this.label = label;		
		this.useAlternativeFeatures = useAlternativeFeatures;
		this.useWholeSegmentForFV = wholeSegmentForFv;
		preprareSegmentData(ss,removeOutlier);
	}
	
	/**
	 * 
	 * @param ss Vector with data segments
	 * @param label class label for the given data
	 * @param useAlternativeFeatures use alternative Features for the FV -> intended for non-segment data 
	 * @param segmentationRates segmenation rates for the considered data. The i-th value is valid from timestamp i until timestamp i+1 
	 * @param removeOutlier wheter to remove or not to remove outlier with a moving average filter
	 * @param wholeSegmentForFv use the whole length of the segment as window size for the FV
	 */
	public AugmentedSignalFV(Vector<Segment> ss, String label,boolean useAlternativeFeatures, Map<Timestamp, Double> segmentationRates, boolean removeOutlier, boolean wholeSegmentForFv){
		this.label = label;		
		this.useAlternativeFeatures = useAlternativeFeatures;
		this.useWholeSegmentForFV = wholeSegmentForFv;
		this.segmentationRates = segmentationRates;
		preprareSegmentData(ss,removeOutlier);
	}
	
	private void preprareSegmentData(Vector<Segment> ss,boolean removeOutlier){		
		for(Segment s : ss){			
			double[][] data = new double[s.getDimensions()][s.size()];
			Timestamp[] ts = new Timestamp[s.size()];
			s.getTimestamps().copyInto(ts);
			for(int d=0;d<s.getDimensions();d++){
				for(int i=0;i<s.size();i++){
					data[d][i] = s.getValues(d).get(i);
				}
			}							
			init(data,ts,removeOutlier);	
		}
	}
	
	private void init(double[][] data, Timestamp[] ts, boolean removeOutlier){
		try{					
			if(removeOutlier){
				data = AverageFilter.filter(data, OUTLIER_WINDOW);
			}			
			if(useWholeSegmentForFV){
				double avgSegRate = getSegmentationRate(ts[0], ts[ts.length-1]);				
				featureVector.add(buildFV(data,avgSegRate));
				timestamps.add(ts);
			}
			else{
				for(int i=0;i<data[0].length;i+=windowSize){
					int samplesInWindow = Math.min(windowSize, data[0].length-i);
					if(samplesInWindow>=MIN_SAMPLES_IN_WINDOW){ //the fv needs a minimum of different samples
						double[][] window = new double[data.length][windowSize];
						Timestamp[] tWindow = new Timestamp[samplesInWindow];						
						for(int k=0;k<windowSize;k += samplesInWindow){ //repeat data if neseccary to fill the window
							for(int j=0;j<data.length;j++){
								System.arraycopy(data[j],i,window[j],0+k,Math.min(windowSize-k,samplesInWindow));						
							}
						}					
						System.arraycopy(ts,i, tWindow, 0, tWindow.length);
						double avgSegRate = getSegmentationRate(tWindow[0], tWindow[tWindow.length-1]);
						featureVector.add(buildFV(window,avgSegRate));
						timestamps.add(tWindow);
					}
				}
			}
			rmse += (tempRMSE / featureVector.size()*data.length);
			tempRMSE = 0.0;
		}
		catch(Exception e){
			e.printStackTrace();		
		}
	}
	
	/**
	 * builds a feature vector for the given samples
	 * @param window sample window
	 * @param segmentationRate surrounding segmenation rate
	 * @return feature vector for the given sample window
	 * @throws Exception
	 */
	private Vector<Double> buildFV(double[][] window, double segmentationRate) throws Exception{
		Vector<Double> fv = new Vector<Double>();
		
		if(!useAlternativeFeatures){
			//calculate AR coeffients for each dimension
			for(int j=0;j<window.length;j++){
				double[] ar = AutoRegression.calculateARCoefficients(window[j], arModelOrder, true);
				tempRMSE += AutoRegression.calculateRMSE(window[j], ar, true);										
				for(double c : ar){
					if(Double.valueOf(c).isNaN()){
						throw new Exception();
					}
					fv.add(c);
				}								
			}	
			
			//SMA
			fv.add(calcSignalMagnitudeArea(window));						
			
			//Tilt Angle
			fv.add(calcTiltAngle(window));
			
			//Average Peak Amplitude
			for(int j=0;j<window.length;j++){
				fv.add(calcAveragePeakAmplitude(window[j]));			
			}
			
			//Surrounding Segmenation Rate
			if(segmentationRate>=0){
				fv.add(segmentationRate);
			}			
			
			//Inter Axis Correlation
			/*
			for(int j=0;j<window.length;j++){
				for(int k=j+1;k<window.length;k++){
					//fv.add(calcAxisCorrelation(window[j], window[k]));
				}
			}
			*/
			
			//mean and variance
			/*
			for(int j=0;j<window.length;j++){
				double mean = calcMean(window[j]);
				fv.add(mean);
				fv.add(calcVariance(window[j],mean));				
			}
			*/
		}
		else{
			
			//calculate AR coeffients for each dimension
			for(int j=0;j<window.length;j++){
				double[] ar = AutoRegression.calculateARCoefficients(window[j], arModelOrder, true);
				tempRMSE += AutoRegression.calculateRMSE(window[j], ar, true);										
				for(double c : ar){
					fv.add(c);
				}								
			}					
			
			//mean and variance
			/*
			for(int j=0;j<window.length;j++){
				double mean = calcMean(window[j]);
				//fv.add(mean);
				//fv.add(calcVariance(window[j],mean));				
			}
			*/
			
			//SMA
			fv.add(calcSignalMagnitudeArea(window));
			
			//Surrounding Segmenation Rate
			if(segmentationRate>=0){
				fv.add(segmentationRate);
			}				
			
			//Tilt Angle
			fv.add(calcTiltAngle(window));
				
			//Average Peak Amplitude
			for(int j=0;j<window.length;j++){
				fv.add(calcAveragePeakAmplitude(window[j]));			
			}	
			
			//Inter Axis Correlation
			/*
			for(int j=0;j<window.length;j++){
				for(int k=j+1;k<window.length;k++){
					fv.add(calcAxisCorrelation(window[j], window[k]));
				}
			}
			*/			
			
		}
		
		return fv;
	}
	
	/**
	 * returns the surrounding segementaion stamp according to the timestamps
	 * @param start start timestamp
	 * @param end end timestamp
	 * @return surrounding segmentation rate
	 */
	private double getSegmentationRate(Timestamp start, Timestamp end){
		if(segmentationRates!=null){
			List<Timestamp> ts = new ArrayList<Timestamp>(segmentationRates.keySet());
			Collections.sort(ts);
			int i=0;
			while(i < ts.size() && start.before(ts.get(i))){
				i++;
			}
			double rate = segmentationRates.get(ts.get(i));
			int buckets = 1;
			i++;
			while(i < ts.size() && end.after(ts.get(i))){
				rate += segmentationRates.get(ts.get(i));
				buckets++;
				i++;
			}
			return rate/buckets;
		}
		else{
			return -1.0;
		}
	}
	
	/**
	 * calculates the signal magnitude area - a commonly used enery measure
	 * it takes the absolute values of all axis and sums them up
	 * @param input accelerometer samples for which to calculate the SMA
	 * @return the SMA value
	 */
	private double calcSignalMagnitudeArea(double[][] input){
		double sma = 0.0;
		for(int i=0;i<input[0].length;i++){
			for(int d=0;d<input.length;d++){
				sma += Math.abs(input[d][i]);
			}
		}
		//normalize according to input length		
		sma /= (input[0].length);
		return sma;
	}
	
	/**
	 * calculates the mean tilt angle for the given input samples
	 * @param input accelerometer samples for which the mean tilt angle is calculated
	 * @return mean tilt angle in the range of 0.0 and Pi
	 */
	private double calcTiltAngle(double[][] input){
		double[] meanVector = new double[3];		
		for(int i=0;i<input[0].length;i++){
			meanVector[0] += input[0][i];
			meanVector[1] += input[1][i];
			meanVector[2] += input[2][i];			
		}
		double vectorNorm = 0.0;
		for(int i=0;i<meanVector.length;i++){
			vectorNorm += meanVector[i]*meanVector[i];
		}
		vectorNorm = Math.sqrt(vectorNorm);
		double cos = meanVector[2]/vectorNorm;			
		
		return Math.acos(cos);
	}
	
	/**
	 * calculates the mean
	 * @param window data to calc the mean
	 * @return mean of the window data
	 */
	private double calcMean(double[] window){
		double mean = 0.0;		
		for(int i=0;i<window.length;i++){
			mean += window[i];
		}
		mean /= window.length;
		return mean;
	}
	
	/**
	 * calculates the variance
	 * @param window data to calc the variance
	 * @param mean mean value of the sample window
	 * @return variance
	 */
	private double calcVariance(double[] window, double mean){
		double var = 0.0;
		for(int i=0;i<window.length;i++){
			var += Math.pow(window[i]-mean,2);
		}
		var /= window.length;
		return var;
	}
	
	/**
	 * Calculates the peaks of the given samples. 
	 * According to the mean of the samples, the negative (if mean is negative)
	 * or positive (if mean is positive) peaks are choosen. 
	 * The first and last sample of the given window are not considered for peak calculation.
	 *  
	 * @param w sample window
	 * @return List with indexes for the input array. The samples at these positions are 
	 * considered as peaks
	 */
	private List<Integer> calcPeaks(double[] w){
		double[] window = w.clone();
		
		int minPeakDistance = 10;
		int minPeakCount = 3;
		int threshold = 95;
		int minThreshold = 70;
		double thresholdValue = 0.0;
		double maxPeak = Double.MIN_VALUE;;		
		ArrayList<Integer> peaks = new ArrayList<Integer>();
				
		double mean = this.calcMean(window);
		if(mean<0){//turn around values, so that negatives peaks are counted
			for(int i=0;i<window.length;i++){
				window[i] *= -1;
			}
		}
		
		window = AverageFilter.filter(window, 3); //remove outlier
		
		for(int i=1;i<window.length-1;i++){ //determine the max. peak; ignore the first and the last sample to get a better peak quality
			if(window[i]>maxPeak){
				maxPeak = window[i];
			}
		}
		maxPeak = Math.min(maxPeak, 2.0); //avoid overlarge max-peaks caused by truncated-peaks prediction
		thresholdValue = maxPeak * threshold / 100.0;
		
		while(peaks.size()<minPeakCount && threshold>=minThreshold){ //try to get at least "minPeakCount" Peaks
			peaks.clear();
			for(int i=1;i<window.length-1;i++){//ignore the first and the last sample to get a better peak quality
				if(window[i]>thresholdValue){
					peaks.add(Integer.valueOf(i));					
				}				
			}
			
			//decrease threshold values
			threshold -= 2;
			thresholdValue = maxPeak * threshold / 100.0;			
			
			//remove nearby peaks (according to minPeakDistance)
			Integer[] peaksCopy = new Integer[peaks.size()];
			peaks.toArray(peaksCopy);
			for(int i=0;i<peaks.size()-1;i++){
				for(int j=i+1;j<peaks.size();j++){
					if((peaks.get(j)-peaks.get(i))<=minPeakDistance){
						if(window[peaks.get(i)] < window[peaks.get(j)]){
							peaksCopy[i] = -1;						
						}
						else{
							peaksCopy[j] = -1;	
						}
					}
				}
			}
			peaks.clear();
			for(int i=0;i<peaksCopy.length;i++){
				if(peaksCopy[i]>=0){
					peaks.add(peaksCopy[i]);
				}
			}
		}
		
		return peaks;
	}
	
	/**
	 * Calculates the average peak for the given samples.
	 * According to the mean of the samples, the negative (if mean is negative)
	 * or positive (if mean is positive) peaks are choosen for calculation 
	 * 
	 * @param window
	 * @return the average peak (positive or negative) or 1.0 respectively -1.0 
	 * as defaul peak if not at least 3 peaks are present.
	 */
	private double calcAveragePeakAmplitude(double[] window){		
		List<Integer> peaks = calcPeaks(window);
		double amplitude = 0.0;		
		if(peaks.size()>=3){
			for(int i=0;i<peaks.size();i++){
				amplitude += window[peaks.get(i)];			
			}
			amplitude /= peaks.size();			
		}
		else{
			if(window[peaks.get(0)]>0){ //consider the avg. peak as 1.0 respectively -1.0 if there aren't at least 3 peaks
				amplitude = 1.0;
			}
			else{
				amplitude = -1.0;
			}
		}
		return amplitude;
	}
	
	/**
	 * calculates the inter axis correlation for two axes
	 * @param axis1 first axis
	 * @param axis2 second axis
	 * @return inter axis correlation
	 */
	private double calcAxisCorrelation(double[] axis1, double[] axis2){
		double mean1 = calcMean(axis1);
		double mean2 = calcMean(axis2);
		double std1 = Math.sqrt(calcVariance(axis1, mean1));
		double std2 = Math.sqrt(calcVariance(axis2, mean2));
		
		double m = 0.0;
		for(int i=0;i<axis1.length;i++){
			m += (axis1[i]-mean1)*(axis2[i]-mean2);
		}
		m /= axis1.length;
		m /= (std1*std2);
		
		return m;
	}
	
		
}
