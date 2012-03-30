package mainFiles;
import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;

 

public class Algorithm {

	/**
	 * Simple Main-Routine.
	 * The main routine to calculate intensities for 
	 * 3D-Acceleration data in *.csv format.
	 * For a GUI, use the Viewer-Class
	 * VAR inPathString is the Path from where the file should 
	 * be read, VAR outputPath is the path where the generated
	 * charts should be saved to.
	 * 
	 */
	private static Logger logger = Logger.getLogger(Algorithm.class);

	public static void main(String[] args) throws Exception {
		String inPathString = "/Users/benjaminsauer/Dropbox/Bachelorarbeit/eclipse/Sendsor/src/testdata/in";	//default input path (location of CSV testdata)
		String outputPath	= "/Users/benjaminsauer/Dropbox/Bachelorarbeit/Charts/";
		if (args.length == 2){
			inPathString = args[0];
			outputPath 	= args[1];		
		}

		long start = System.currentTimeMillis();
		
    	File pathIn = new File(inPathString);
    	
    	File[] files = pathIn.listFiles(new FilenameFilter() {					
			public boolean accept(File arg0, String arg1) {
				return arg1.toLowerCase().endsWith("csv");
			}
    	});
    	//Main Part of the algorithm    	
    	Intensity intens = new Intensity(files);  
//      intens.startstopPeakPrediction();
//    	intens.doNotUseOutlierRemoval();
    	intens.segment( );
//    	intens.printWholeFileMean();
    	intens.doNotUseClassification();
   
    	//intens.calcZeroMovement(0.5);
    	intens.setDataNormingRate(1);
    	intens.useDataBucketing(0.0);
//    	intens.setNoMovementThreshold(0.1);
    	intens.generateGraph(1,outputPath);
    	//intens.generateGraph(1,outputPath);
       	System.out.println("All jobs are done!");
       	System.out.println("Rechenzeit: " + (System.currentTimeMillis() - start)/1000.0 +"s");

	}

}
