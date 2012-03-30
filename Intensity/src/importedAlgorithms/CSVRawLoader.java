package importedAlgorithms;

import importedAlgorithms.RawData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Vector;


/**
 * @author Christian Mönnig
 */

public class CSVRawLoader {


    public static RawData loadCSV(File path, int dimensions){
        RawData result = new RawData(dimensions);
        BufferedReader buffCsvReader = null;
        try{            
            buffCsvReader = new BufferedReader(new FileReader(path));            
            String line;
            Vector<Double> xValues = new Vector<Double>();
            Vector<Double> yValues = new Vector<Double>();
            Vector<Double> zValues = new Vector<Double>();
            Vector<Timestamp> timestamps = new Vector<Timestamp>();
            while((line = buffCsvReader.readLine()) != null)
            {
                String[] values = line.split(",");
                Timestamp timestamp = Timestamp.valueOf(values[0]);
                //add 0.5 to all values because byte values are in the range of -128 to +127
                double x = ((Double.parseDouble(values[1])+0.5)/127.5)*2;
                double y = ((Double.parseDouble(values[2])+0.5)/127.5)*2;
                double z = ((Double.parseDouble(values[3])+0.5)/127.5)*2;
                timestamps.add(timestamp);
                xValues.add(x);
                yValues.add(y);
                zValues.add(z);

            }
            Double[] xArray = new Double[xValues.size()];
            xValues.toArray(xArray);
            result.setDimension(0, xArray);
            Double[] yArray = new Double[yValues.size()];
            yValues.toArray(yArray);
            result.setDimension(1, yArray);
            Double[] zArray = new Double[zValues.size()];
            zValues.toArray(zArray);
            result.setDimension(2, zArray);
            Timestamp[] timestampArray = new Timestamp[timestamps.size()];
            timestamps.toArray(timestampArray);
            result.setTimestamps(timestampArray);
            result.setTrueLabel(path.getName());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally{
        	try{
        		buffCsvReader.close();
        	}
        	catch(IOException ioe){
        		//do nothing        		
        	}
        }
        return result;
    }   
}
