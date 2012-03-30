/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package importedAlgorithms;

import java.util.Vector;


/**
 *
 * @author Alexander Stautner
 */
public class Autocorrelation {

    private double[] calcMeanAndSTD(Double[] numbers)
    {
        int n = numbers.length;
        double mean = 0;
        double std = 0;
        double squares = 0;
        for(int i = 0; i < n; i++)
        {
            mean += numbers[i];
            squares += numbers[i]*numbers[i];
        }
        mean /= n;
        squares /= n;
        double meanSquare = mean * mean;
        std = Math.sqrt(squares - meanSquare);
        double[] result = {mean,std};
        return result;
        
    }
    
    public double calcAutocorrelation(Double[] sample, Double[] data)
    {
        double result = 0;
        if(sample.length == data.length)
        {
            double[] meanStdSample = calcMeanAndSTD(sample);
            double[] meanStdData = calcMeanAndSTD(data);
            for(int j = 0; j < sample.length; j++)
            {
                result+= (sample[j]-meanStdSample[0])*(data[j]-meanStdData[0]);

            }
            result /= sample.length;
            result /= meanStdSample[1]*meanStdData[1];
        }
        return result;
    }

    public double calcAutocorrelationVector(Vector<Double> sample, Vector<Double> data)
    {
        Double[] sampleVec = new Double[sample.size()];
        Double[] dataVec = new Double[data.size()];
        sample.toArray(sampleVec);
        data.toArray(dataVec);
        return calcAutocorrelation(sampleVec, dataVec);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
        
        Double[] sample = {1.,2.,3.,1.};
        Double[] data = {1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.,3.,1.,2.};
        
        Autocorrelation auto = new Autocorrelation();
        int steps = data.length/sample.length;
        for(int i = 0; i < steps; i++)
        {
            Double[] test = new Double[sample.length];
            System.arraycopy(data, i*sample.length, test, 0, sample.length);
            System.out.println(auto.calcAutocorrelation(sample, test));
        }
    }


}
