package intensityCalculators;

import importedAlgorithms.RawData;

import java.sql.Timestamp;
import java.util.Vector;

import mainFiles.Algorithm;

import org.apache.log4j.Logger;
/**
 * Simple interface for intensity analyzing algorithms.
 * @author benjaminsauer
 *
 */
public interface IntensityAnalyzerInterface {
	
	static Vector<RawData> rawData = null;
	static Logger   logger 	= Logger.getLogger(Algorithm.class);
	
	public Vector<Double>[] calcIntensity() throws Exception;//Calculates the intensity of the rawData
	public double calcIntensity(Timestamp from, Timestamp to,int fileIndex) throws Exception; 


}
