package classificationData;

import java.util.Vector;
/**
 * Abstract class for the classification of intensity-values into activity subclasses.
 * @author Benjamin Sauer
 *
 */
public abstract class ClassificationData {

	private final String className;
	private final String[] SUBCLASS_NAMES;
	private final int NUMBEROFSUBCLASSES; //Number depending on number of subclasses in compendium of physical activities
	private final double[] classIntervals;		// Final values, calculated by generating intensity-normed data.
	private int numberOfValues;
	private int[] classificationCounter;	
	private double[] classPercentages;
	private int lowerThanMinCounter = 0;
	private double lowerThanMinPercentage = 0;


	public String getClassName(){
		return this.className;
	}
	public ClassificationData(String className_, String[] subclassNames_, double[] classIntervals_){
		this.className=className_;
		if (subclassNames_.length != classIntervals_.length){System.out.println("ClassNames/ClassIntervals do not fit! Errors ahead...");}
		this.SUBCLASS_NAMES = subclassNames_;
		this.classIntervals = classIntervals_;
		this.NUMBEROFSUBCLASSES = classIntervals.length;
		this.classificationCounter = new int[NUMBEROFSUBCLASSES]; 
		this.classPercentages = new double[NUMBEROFSUBCLASSES];
	}
	/**
	 * Calculates the percentage of values in the different intensity subclasses.
	 */
	public void calculatePercentages(){
		this.lowerThanMinPercentage = ((double) this.lowerThanMinCounter/numberOfValues);
		for (int i = 0; i < NUMBEROFSUBCLASSES; i++){
			this.classPercentages[i] =  ((double) classificationCounter[i]/numberOfValues);
		}
	}
	
	public double[] getPercentages(){
		return this.classPercentages;
	}
	/**
	 * Prints the results of the calculation including the calculated percentages.
	 */
	public String toString(){
		if (classPercentages != null){
			String returnValues = "\n";
			returnValues = returnValues + (100*this.lowerThanMinPercentage + "% "+ "has an even lower intensity than "+SUBCLASS_NAMES[0] +"\n");
			double initial = 100;

			for(int i = 0; i<NUMBEROFSUBCLASSES-1; i++){
				initial -= 100*classPercentages[i];
				returnValues = returnValues + (100*classPercentages[i] + "% "+ "is: "+SUBCLASS_NAMES[i] +"\n");
			}
			returnValues = returnValues + (100*classPercentages[NUMBEROFSUBCLASSES-1] + "% "+ "has an even greater intensity than "+SUBCLASS_NAMES[(this.NUMBEROFSUBCLASSES-1)] +"\n");
//			returnValues = returnValues + initial + "% not classified, probably activity with higher intensity.";
			return returnValues;
		}
		return ("Action not possible, try to classify the data first!");
	}
	
	public void classifyValues(Vector<Double> values){
		this.numberOfValues = values.size();
		for (double d : values){
			if(!(d < 0)){findSubclass(d,0);}
			else System.out.println("Inputvalue smaller than 0!");
		}		
	}

	private void findSubclass(double value, int counter) {

		if (counter < NUMBEROFSUBCLASSES){
			if (!(counter == 0)){
				if (Double.compare(value, classIntervals[counter]) < 0){
					classificationCounter[counter-1]++;
//					System.out.println("Classification Counter an Stelle " + counter+ ": "+classificationCounter[counter]);
//					System.out.println("Value "+value+ " added to "+ SUBCLASS_NAMES[counter]);
					}else {
						findSubclass(value,counter + 1);
					}	
			}else if  ((Double.compare(value, classIntervals[counter]) < 0)) { 		//counter == 0 here
			 this.lowerThanMinCounter++;
			}else {
				findSubclass(value,counter + 1);
			}
					
		}else {
			classificationCounter[NUMBEROFSUBCLASSES-1]++;
//			System.out.println("Klasse konnte nicht zugeordnet werden, IntensitŠt "+value+" zu hoch fŸr diese Klasse!");
		}
		
	}
	
	public double calculateCalories(double weight){					//EXPERIMENTAL!
		double calories = 0;
		String metValueName = this.SUBCLASS_NAMES[0].substring(this.SUBCLASS_NAMES[0].length()-8, this.SUBCLASS_NAMES[0].length()-3);
		double metValueNumber = Double.parseDouble(metValueName); 
		double caloriesPerSecond = weight * metValueNumber/60/60;
//		calories += this.lowerThanMinCounter*40/1000 * caloriesPerSecond;  		//Lower than classified calorie-values!
		int i = 0;

		for (int j:this.classificationCounter){
			metValueName = this.SUBCLASS_NAMES[i].substring(this.SUBCLASS_NAMES[i].length()-8, this.SUBCLASS_NAMES[i].length()-3);
			metValueNumber = Double.parseDouble(metValueName); 
			caloriesPerSecond = weight * metValueNumber/60/60;
			calories += j*40/1000 * caloriesPerSecond;  
			i++;
		}
		return calories;		
		
	}
	/**
	 * Returns the class with the highest percentage of 
	 * classified values in the set.
	 * @return
	 */
	public String getClassWithHighestPercentage(){
	
		int result = this.getIndexOfClassWithHighestPercentage();
		String time = "" + this.numberOfValues*40/1000/60/60 +"h "+this.numberOfValues*40/1000/60+"m";

		if (result != -1){return ("Average class of analyzed data for "+time+ " :"+this.SUBCLASS_NAMES[result]);}
		else return ("Average class of analyzed data for "+time+ " : even lower intensity than "+SUBCLASS_NAMES[0]);
	}
	
	private int getIndexOfClassWithHighestPercentage(){
		
		double tempsum = this.lowerThanMinCounter;
		int result = -1;
		for (int i = 0; i < this.NUMBEROFSUBCLASSES; i++){
			if( this.classificationCounter[i] > tempsum){
				tempsum = this.classificationCounter[i];
				result = i;
			}
		}
		return result;
	}
	
}
