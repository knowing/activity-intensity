package mainFiles;

import java.util.Vector;
/**
 * Reduces the data to a given granularity ("Kšrnung der Daten")
 * of details. 
 * The bigger the given granularity, the 
 * more the values will tend towards the 
 * mean of the file.
 * @author Benjamin Sauer
 *
 */
public class GranularityScaling {

	private Vector<Double> values = null;
	private double granularity = 0;

	public GranularityScaling(Vector<Double> values_, double accuracy_) {
		this.values = values_;
		this.granularity = accuracy_;
	}
	
	/**
	 * The function reduces the details of a given file to a certain granularity.
	 * By summing up all the values that are not below or above a start-value+
	 * the given granularity a mean of the granularity can be calculated.
	 * @return
	 */
	public Vector<Double> scaleToGivenGranularity() {

		Vector<Double> bucketedValues = new Vector<Double>();
		double tempsum = 0;
		double startValue = values.get(0);
		int count = 0;
		int outliers = 0;
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) > (startValue + granularity)		//If value is smaller or bigger the startvalue+granularity a new analysis will be started.
					|| values.get(i) < (startValue - granularity)
					|| i == (values.size() - 1)) {
				if (count != 0){
					for (int j = 0; j < count + 1; j++) {
						bucketedValues.add(tempsum / count);
					}
				}else{
					for (int j = 0; j < count + 1; j++) {
						bucketedValues.add(tempsum);
					}
				}
				tempsum = values.get(i);
				startValue = values.get(i);
				count = 0;
				outliers++;
			} else {
				tempsum += values.get(i);
				count++;
			}

		}
		return bucketedValues;

	}

}
