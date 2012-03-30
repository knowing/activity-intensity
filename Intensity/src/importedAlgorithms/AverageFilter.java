package importedAlgorithms;

/**
 * average filter  
 * 
 * @author Christian Mönnig
 *
 */

public class AverageFilter {
	
	public static double[][] filter(double[][] input, int filterWindowLength){
		double[][] result = new double[input.length][];
		for(int i=0;i<input.length;i++){
			result[i] = filter(input[i],filterWindowLength);
		}
		return result;
	}

	public static double[] filter(double[] input, int filterWindowLength){
		double[] result = new double[input.length];
		for(int i=0;i<input.length;i++){
			int end = i+filterWindowLength;
			for(int j=i;j<end;j++){
				result[i] += input[j%input.length]; //operate in circle mode...
			}
			result[i] /= filterWindowLength;
		}
		return result;
	}
	                     
}
