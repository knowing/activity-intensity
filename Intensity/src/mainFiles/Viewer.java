package mainFiles;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** 
* Provides a GUI for intensity analysis.
*/

public class Viewer extends JFrame implements ActionListener {

		private static final long serialVersionUID = 1L;
	
		private JButton ladeButtonIn;
		private JButton ladeButtonOut;
		private JButton normierungsButton;
		private JButton durchschnittsanalyse;
		private JButton extremwertanalyse;
		
	    private JFileChooser fc;
	    private JTextArea ausgabeBereich;
	    private JCheckBox peakPred;
	    private JCheckBox outlierRemoval;
	    private JCheckBox segmentation;
	    private JCheckBox wholeFileMean;
	    private JCheckBox classification;
	    private JLabel slideListener;
	    private JSlider koernungsSlider;
	    
		private String inPathString;
		private String outPathString;
		private int normierungswert = 1;
		
		Intensity intens;
		/**
		 * Generates a 600*325px window with all necessary input devices.
		 */
	    public Viewer() {
	    	

	        this.setTitle("Intensity Analyser");
	        
	        this.setSize(600, 325);
	        
	    	this.slideListener = new JLabel();
	    	ChangeListener listener = new SliderListener(slideListener);
	    	this.slideListener.setText("0.0");
	    	
	        this.ladeButtonIn = new JButton("Input Ordner waehlen");
	        this.ladeButtonOut = new JButton("Output Ordner waehlen");
	        this.normierungsButton = new JButton("Normierungsgrad");
	        this.durchschnittsanalyse = new JButton("Durchschnittsanalyse starten");
	        this.extremwertanalyse = new JButton("Extremwertanalyse starten");

	        this.fc = new JFileChooser();

	        this.peakPred = new JCheckBox("Peak-Simulation");
	        this.peakPred.setSelected(true);
	        this.outlierRemoval = new JCheckBox("Outlier Removal");
	        this.outlierRemoval.setSelected(true);
	        this.segmentation= new JCheckBox("Segmentation");
	        this.segmentation.setSelected(true);
	        this.wholeFileMean = new JCheckBox("Durchschnitt der Dateien");
	        this.classification = new JCheckBox("Klassifikation");

	        this.koernungsSlider= new JSlider(0, 30, 0); 
	     	this.koernungsSlider.putClientProperty("JSlider.isFilled", Boolean.TRUE ); 
	     	this.koernungsSlider.setPaintTicks(true); 
	     	this.koernungsSlider.setMajorTickSpacing(10); 
	     	this.koernungsSlider.setMinorTickSpacing(1); 
	     	this.koernungsSlider.setPaintLabels( false ); 
	     	this.koernungsSlider.setSnapToTicks( true ); 
	     	this.koernungsSlider.getLabelTable().put(new Integer(3), new JLabel(new Integer(3).toString(), JLabel.CENTER)); 
	     	this.koernungsSlider.setLabelTable( koernungsSlider.getLabelTable() ); 
	     	this.koernungsSlider.addChangeListener(listener); 
	     	 
	     	
	        this.ausgabeBereich = new JTextArea(30, 100);

	        JPanel buttonPanel = new JPanel();
	        buttonPanel.add(this.ladeButtonIn);
	        buttonPanel.add(this.ladeButtonOut);

	        JPanel checkBoxPanel = new JPanel();
	        checkBoxPanel.setLayout(new GridLayout(5,1));
	        checkBoxPanel.add(this.peakPred);
	        checkBoxPanel.add(this.outlierRemoval);
	        checkBoxPanel.add(this.segmentation);
	        checkBoxPanel.add(this.wholeFileMean);
	        checkBoxPanel.add(this.classification);

	        JPanel sliderPanel = new JPanel();
	        sliderPanel.setLayout(new GridLayout(2,2));
	        sliderPanel.add(koernungsSlider);
	        sliderPanel.add(this.slideListener);
	        sliderPanel.add(this.normierungsButton);
	        
	        JPanel mittelPanel = new JPanel();
	        mittelPanel.setLayout(new GridLayout(1,2));
	        mittelPanel.add(checkBoxPanel);
	        mittelPanel.add(sliderPanel);
	        
	        JPanel ausfuehrPanel = new JPanel();
	        ausfuehrPanel.setLayout(new GridLayout(1,2));
	        ausfuehrPanel.add(this.durchschnittsanalyse);
	        ausfuehrPanel.add(this.extremwertanalyse);
	        
	        Container contentPane = this.getContentPane();
	        contentPane.setLayout(new GridLayout(4, 1));
	        contentPane.add(buttonPanel);
	        contentPane.add(mittelPanel);
	        contentPane.add(ausfuehrPanel);
	        contentPane.add(this.ausgabeBereich);

	       
	        this.ladeButtonIn.addActionListener(this);
	        this.ladeButtonOut.addActionListener(this);
	        this.normierungsButton.addActionListener(this);
	        this.durchschnittsanalyse.addActionListener(this);
	        this.extremwertanalyse.addActionListener(this);


	        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    }
	

	    @Override
	    public void actionPerformed(ActionEvent e) {
	        Object source = e.getSource();
	        if (source == this.ladeButtonIn) {
	          this.inPathString = this.getDirectoryPath();
	          System.out.println("Input path:" +inPathString);
	        }
	        else if (source == this.ladeButtonOut) {
	        	this.outPathString = this.getDirectoryPath();
		        System.out.println("Output path:" +outPathString);
	        }else if (source == this.normierungsButton){
	        	this.normierungswert = this.getNormierungswert();
	        	System.out.println("Normierungswert: " + normierungswert);
	        }else if (source == this.durchschnittsanalyse){
	        	try {
					this.analyze(0);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }else if (source == this.extremwertanalyse){
	        	try {
					this.analyze(1);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }

	    
	    }
	        
	    /**
	     * Produces a intensity analysis and intensity graph with different options. 
	     * @param methode is the method of analysis: 0 = Durchschnittsanalyse, 1 = Extremwertanalyse
	     * @throws Exception
	     */
	    private void analyze(int methode) throws Exception{
	    	
	    	long start = System.currentTimeMillis();
			
	    	File pathIn = new File(inPathString);
	    	this.ausgabeBereich.append("Reading Files...");
	    	File[] files = pathIn.listFiles(new FilenameFilter() {					
				public boolean accept(File arg0, String arg1) {
					return arg1.toLowerCase().endsWith("csv");
				}
	    	});
	    	this.ausgabeBereich.append("All files read!\n");

	    	Intensity intens = new Intensity(files);  
	    	
	    	if (!this.peakPred.isSelected()){
	    		intens.stopPeakPrediction();
	    		this.ausgabeBereich.append("Peak prediction is not being used.\n");
	    	}else {this.ausgabeBereich.append("Peak simulation is being used\n");}
	    	
	    	if (this.outlierRemoval.isSelected()){	
	    		intens.doNotUseOutlierRemoval();
	    		this.ausgabeBereich.append("Outlier removal is being used\n");
	    		}else {	 this.ausgabeBereich.append("Outlier removal is not being used\n");}
	    	
	    	if (this.segmentation.isSelected()) {
	    		intens.segment( );
	    		this.ausgabeBereich.append("Segmentation is being used\n");
	    		}else {	 this.ausgabeBereich.append("Segmentation is not being used\n");}
	    	
	    	if (this.wholeFileMean.isSelected()) {
	    		intens.printWholeFileMean();
	    		this.ausgabeBereich.append("The Mean of each file will be printed to the console\n");
	    		}
	    	
	    	if (!this.classification.isSelected()){
	    		intens.doNotUseClassification();
	    		}else {	this.ausgabeBereich.append("The classification of each file will be printed to the console\n");
	    		}
	   
	    	intens.setDataNormingRate(this.normierungswert);
	    	intens.useDataBucketing(Double.parseDouble(this.slideListener.getText()));
	    	intens.generateGraph(methode,this.outPathString);
	    	
	    	this.ausgabeBereich.append("Generated graph saved.\n");
	       	this.ausgabeBereich.append("All jobs are done!\n");
	       	this.ausgabeBereich.append("Rechenzeit: " + (System.currentTimeMillis() - start)/1000.0 +"s\n");

		}
	  
	    	/**
	    	 * Reads the norming-value from a JOptionPane.
	    	 * @return
	    	 */
	     private int getNormierungswert(){
			 String input = JOptionPane.showInputDialog("Bitte Normierungswert (ca. 1-60) eingeben:");
			 int norm = Integer.parseInt(input);
	    	 return norm;	    	 
	     }
	    /**
	     * Gets the directory path for the input/output of data.
	     * @return
	     */
	     private String getDirectoryPath(){
		    	 fc.setCurrentDirectory(new java.io.File("."));
		            fc.setDialogTitle("Bitte wählen sie einen Ordner aus");
		            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		            fc.setAcceptAllFileFilterUsed(false);
		            //    
		            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		              return(fc.getSelectedFile().toString());
		              }
		            else {
		              return(fc.getCurrentDirectory().toString());
		              }
		 }
	    
	     class SliderListener implements ChangeListener { 
	    	 	JLabel tf; 
	    	 	public SliderListener(JLabel f) { 
	    	 	    tf = f; 
	    	 	} 
	    	 	public void stateChanged(ChangeEvent e) { 
	    	 	    JSlider s1 = (JSlider)e.getSource(); 
	    	 	    tf.setText(""+(double)s1.getValue()/10); 
	    	 	}

	   } 
	     
	    public static void main (String[] args){
	    	Viewer intensityViewer = new Viewer();
	    	intensityViewer.setVisible(true);
	    }

}
