package classificationData;

/**
 * Simple Data-structure to save the collected
 * average-values of different intensity sub-classes
 * of walking.
 * @author Benjamin Sauer
 */
public class WalkingClassificator extends ClassificationData {
	
	private static int anzahlTestwerte = 4;
	private static String className = "walking";
	                      
	private static String[] subclasses = {	"walking, slow pace, 2.0 mph, 02.8 MET",	//FORM IST WICHTIG F†R SP€TERE ANALYSE VON KALORIEN!
											"walking, moderate pace, 2.8 to 3.2 mph, 03.5 MET",
											"walking, very brisk pace 4.0 mph, 05.0 MET",
											"walking, very,very brisk pace, 5.0 mph, 08.3 MET"};
	
	private static double[][] intervals = 
	{ 
	
		
		{	(0.49688756379006344+0.5208969177317668+0.49742636632600773	+0.5208969177317668)/anzahlTestwerte,	//Dimension-Mean
			(0.5677064111188681	+0.6212190142642675+0.5872150623106625	+0.6212190142642675)/anzahlTestwerte, 
			(0.6626484392874549	+0.7005670134624414+0.6932947501930582	+0.731550364755222)/anzahlTestwerte, 
			(0.7607473201402433 +0.8045963716612193+0.8800651648055039)/(anzahlTestwerte-1)},

			
		{	(0.51793273121882+0.5105953086598541+0.5149039588277812+0.5105953086598541)/anzahlTestwerte, 	//Max-Min DATA
			(0.7144948990537463+0.7753991094490319+0.6817178574008389+0.7753991094490319)/anzahlTestwerte, 
			(0.897450962900194+0.9865969690162358+0.7698357869973905+1.091797454247118)/anzahlTestwerte,
			(1.0726812335905824+0.9209331835107518+1.3860479301685598)/(anzahlTestwerte-1)}
	};

	
	public WalkingClassificator (int type){
		super(className,subclasses, intervals[type]);
	}
	
}
