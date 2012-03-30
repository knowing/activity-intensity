package importedAlgorithms;

public class PrimitiveArrayConverter {
	
	public static Double[] convertDoubleArray(double[] array){
		Double[] result = new Double[array.length];
		for(int i=0;i<array.length;i++){
			result[i] = Double.valueOf(array[i]);
		}
		return result;
	}
	
	public static Double[][] convertDoubleArray(double[][] array){
		Double[][] result = new Double[array.length][];
		for(int i=0;i<array.length;i++){
			result[i] = convertDoubleArray(array[i]);
		}
		return result;
	}
	
	public static double[] convertDoubleArray(Double[] array){
		double[] result = new double[array.length];
		for(int i=0;i<array.length;i++){
			result[i] = array[i].doubleValue();
		}
		return result;
	}
	
	public static double[][] convertDoubleArray(Double[][] array){
		double[][] result = new double[array.length][];
		for(int i=0;i<array.length;i++){
			result[i] = convertDoubleArray(array[i]);
		}
		return result;
	}

}
