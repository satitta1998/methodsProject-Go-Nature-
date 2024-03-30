/**
 * Controller class for the Park Manager Reports Page GUI.
 * This class handles user interactions and events related to generating reports for the park manager.
 */
package gui;

import java.time.YearMonth;
import java.util.ArrayList;
import entity.NextPage;
import entity.SecondPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;

public class ParkManagerReportsPageController {
	
    /**
     * Enumeration representing different types of reports.
     * Each enum constant has a description associated with it.
     */
	public enum ReportsTypes {
        /**
         * The total number of visitors report - segmented according to the type of visit.
         */
	    TOTAL_VISITORS_NUMBER("The total number of visitors report - segmented according to the type of visit"),
        /**
         * Usage report: when was the park not fully occupied.
         */
	    NOT_FULLY_OCCUPIED_PARK("Usage report: when was the park not fully occupied");

	    private final String description;

	    ReportsTypes(String description) {
	        this.description = description;
	    }

        /**
         * Get the description of the report type.
         * @return A string representing the description of the report type.
         */
	    public String getDescription() {
	        return description;
	    }
	}
	
    @FXML
    private Button btnBack;

    @FXML
    private Button btnCreateReport;

    @FXML
    private ComboBox<String> cmbMonth, cmbReportType, cmbYear;

    @FXML
    private Text txtError;
    
    private String parkName;
    
    /**
     * Loads data into the GUI elements.
     * @param parkName The name of the park for which reports are generated.
     */
    public void loadData (String parkName) {
    	try {
			//1. Load park name
			this.parkName = parkName;
			
			//2. Load reports types
			this.cmbReportType.getItems().addAll(ReportsTypes.TOTAL_VISITORS_NUMBER.getDescription(),
												ReportsTypes.NOT_FULLY_OCCUPIED_PARK.getDescription());
			
			//3. Load months
			ArrayList<String> months = new ArrayList<>();
			for(int i=1; i<13; i++) {
				months.add(""+i);
			}
			this.cmbMonth.getItems().addAll(months);
			
			//4. Load years
			ArrayList<String> years = new ArrayList<>();
			for(int i=2020; i<2041; i++) {
				years.add(""+i);
			}
			this.cmbYear.getItems().addAll(years);
		} catch (Exception e) {
			System.out.println("Error in ParkManagerReportsPageController: loadData");
			System.out.println(e.getMessage());
		}
    	
    	
    }
    
    /**
     * Handles the event when the "Create report" button is pressed.
     * This method validates the selected report type, month, and year, and opens the corresponding report page.
     * @param event The ActionEvent representing the button click event.
     */
    //Event for "Create report" button
    @FXML
    void pressCreateReportBtn(ActionEvent event) {
    	try {
    		ReportsTypes selectedReportType = null;
    		//1. Get report type, month and year
    		String reportType = this.cmbReportType.getValue();
    		String month = this.cmbMonth.getValue();
    		String year = this.cmbYear.getValue();
    		
    		//2. Check data
    		//if((reportType.isEmpty()) || (month.isEmpty()) || (year.isEmpty()))
    		if((reportType == null) || (month == null) || (year == null))
    			throw new IllegalArgumentException("You have to select the report type, month and year.");
    		this.txtError.setText("");
    		
    		
    		if (reportType != null) {
    			for (ReportsTypes type : ReportsTypes.values()) {
    				if (type.getDescription().equals(reportType)) {
    					selectedReportType = type;
    					break;
    				}
    			}
    		}
    		//3. Open report page
    		ArrayList<String> dataForReport = new ArrayList<>();
    		dataForReport.add(parkName);
    		dataForReport.add(month);
    		dataForReport.add(year);
    		
    		if (selectedReportType != null) {
    			switch (selectedReportType) {
    				case TOTAL_VISITORS_NUMBER: {
    					//the provided month and year are not the same as the current month and year
    			        YearMonth inputYearMonth = YearMonth.of(Integer.parseInt(dataForReport.get(2)), Integer.parseInt(dataForReport.get(1)));
    			        YearMonth currentYearMonth = YearMonth.now();
    			        if (!inputYearMonth.equals(currentYearMonth)) {
    			            throw new IllegalArgumentException("You can create report only for the current month and year.");
    			        }
    			        
    		        	SecondPage page = new SecondPage(event, "/gui/TotalVisitorsNumberReportPage.fxml", "", "TotalVisitorsNumberReportPageController", "pressBackBtn", dataForReport); 
    		        	page.openSecondPage();
    					break;}
    				case NOT_FULLY_OCCUPIED_PARK: {
    					SecondPage page = new SecondPage(event, "/gui/UsageReportPage.fxml", "", "UsageReportPageController", "pressBackBtn", dataForReport); 
    					page.openSecondPage();
    					break;}
    				default: {
    					System.out.println("No such report type");
    					break;}
    			}
    		}	
    	}catch (IllegalArgumentException e) {
    		System.out.println(e.getMessage());
    		this.txtError.setText(e.getMessage());
    	}catch (Exception e) {
    		System.out.println("Error in ParkManagerReportsPageController: pressCreateReportBtn");
    		System.out.println(e.getMessage());
    	}

    }
    
    /**
     * Handles the event when the "Back" button is pressed.
     * This method navigates the user back to the Park Manager page.
     * @param event The ActionEvent representing the button click event.
     */
    //Event for "Back" button
    @FXML
    void pressBackBtn(ActionEvent event) {
    	try {
        	NextPage page = new NextPage(event, "/gui/ParkManager.fxml", "Park Manager", "ParkManagerController", "pressBackBtn", parkName); 
        	page.Next();	
    	}catch (Exception e) {
    		System.out.println("Error in ParkManagerReportsPageController: pressBackBtn");
    		System.out.println(e.getMessage());
    	}
    }


}
