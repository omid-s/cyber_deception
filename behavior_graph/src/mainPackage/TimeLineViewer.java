package mainPackage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
//import org.eclipse.persistence.jpa.jpql.parser.DateTime;



import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import classes.SysdigRecordObject;

public class TimeLineViewer  extends ApplicationFrame{

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
	
	 private  ArrayList<SysdigRecordObject> sourceItems ;
	
    public TimeLineViewer(final String title , ArrayList<SysdigRecordObject> items) {

        super("TimeLine For process " + title);

        sourceItems = items.stream().filter( x-> x.proc_name.trim().toLowerCase().equals(title.trim().toLowerCase())).collect(Collectors.toCollection(ArrayList::new));
        
        final IntervalCategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);

        // add the chart to a panel...
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
        setContentPane(chartPanel);

    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    /**
     * Creates a sample dataset for a Gantt chart.
     *
     * @return The dataset.
     */
    public  IntervalCategoryDataset createDataset() {

      //  System.out.println ( new Date( 2015, 01,12,12,12,12  ).getTime()   );
      //  System.out.println ( new Date( 2015, 01,12,12,12,16  ).getTime()   );
        final TaskSeries s1 = new TaskSeries("Scheduled");
        
        for( SysdigRecordObject pick : sourceItems ){
        	s1.add(new Task ( pick.evt_type,
        			new SimpleTimePeriod( new Date (Long.valueOf( pick.evt_rawtime_ns )), 
        								 new Date( Long.valueOf( pick.evt_rawtime_ns ) + 1000000 ))
        			)
        			);
        	
        }
        
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s1);
       // collection.add(s2);

        return collection;
    }

        
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final IntervalCategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createGanttChart(
            "system call history List",  // chart title
            "Calls",              // domain axis label
            "Date",              // range axis label
            dataset,             // data
            true,                // include legend
            false,                // tooltips
            false                // urls
        );    
//        chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
        return chart;    
    }
    
  
	
	
}
