package mainFiles;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
/**
 * Provides methods to generate a chart using JFreeChart.
 * @author Benjamin Sauer
 *
 */
public class ChartMaker {

	private XYSeries series = null;
	private XYSeriesCollection dataset = null;
	private String title = "title not Set";
	
	public ChartMaker(String title){
		// Create a simple XY chart 
		this.title=title;
		series = new XYSeries(title);
	}

	
	public void addToChart(int xAxis, double yAxis){
		
			series.add(xAxis,yAxis);
	}
	
	/**
	 * Generates the chart and saves it to 
	 * the given output path.
	 * @param filename
	 * @param output
	 */
	public void generateChart(String filename, String output){
		// Generate the graph 
		dataset = new XYSeriesCollection(); 
		dataset.addSeries(series);
		
		JFreeChart chart = ChartFactory.createXYLineChart(title, "Timeline", "Intensity", dataset, PlotOrientation.VERTICAL, true, true, false );
		try { ChartUtilities.saveChartAsJPEG(new File(output+"/"+filename), chart, 1000, 500 );
		} catch (IOException e) { System.err.println("Problem occurred creating chart. Check if output-Path is set correctly!");
		}
	}

	
	
}
