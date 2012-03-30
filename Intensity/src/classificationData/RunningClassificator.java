package classificationData;
/**
 * Simple Data-structure to save the collected
 * average-values of different intensity sub-classes
 * of running.
 * @author Benjamin Sauer
 *
 */
public class RunningClassificator extends ClassificationData {
	private static int anzahlTestwerte = 4;

	private static String className= "running";

	private static String[] subclasses = {	"running, 5 mph (12 min/mile), 08.3 MET",
											"running, 6 mph (10 min/mile),  09.8 MET",
											"running, 7 mph (8.5 min/mile), 11.0 MET",
											"running, 8 mph (7.5 min/mile), 11.8 MET",
											"running, 9 mph (6.5 min/mile), 12.8 MET"};

	private static double[][] intervals = 
	{
		{	(0.7617018987621311+0.8213679030388268+0.7608222184817967)/(anzahlTestwerte-1),			//3D-SIMPLE MEAN DATA
			(0.8362969758718966+0.9021505798672437+0.9028507226786198)/(anzahlTestwerte-1), 
			(0.8996677189730719), 
			(0.946521895716048),
			(0.9815216469751136)},

		{	(1.070486052125834+1.03858942163906+0.869195049600647)/(anzahlTestwerte-1),			//MAX-MIN DATA
			(1.129207197874871+1.4141009814548609+1.3366358411878427)/(anzahlTestwerte-1), 
			1.3113279458636775,
			1.3225471127165047,
			1.3366428694459747,
		}
	};

	public RunningClassificator (int type){
		super(className, subclasses, intervals[type]);	
		
	}
}
