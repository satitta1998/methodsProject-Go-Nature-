/**
 * This class controls the functionality of the visiting report GUI.
 * It handles loading data for the report, displaying error messages, and handling button actions.
 */
package gui;

import java.util.ArrayList;

import client.ChatClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class VisitingReportController{

    @FXML
    private Button btnBack;

    @FXML
    private Button btnSubmit;

    @FXML
    private Text txtError;

    @FXML
    private BarChart<String, Integer> chart;

    @FXML
    private CategoryAxis X;

    @FXML
    private NumberAxis Y;
    
    @FXML
    private Text txtMonth, txtYear, txtDay;
    
    private ArrayList<String> dataForReport = new ArrayList<>();
    //data report = {0 - parkName, 1 - day, 2 - month, 3 - year}
    
    /**
     * Load data for the visiting report.
     *
     * @param dataForReport The data for the report, containing park name, day, month, and year.
     */
    //load report data
    public void loadData(ArrayList<String> dataForReport) {
		try {
	    	this.dataForReport.addAll(dataForReport);
	    	//this.dataForReport = dataForReport;
	    	this.txtDay.setText(dataForReport.get(1));
			this.txtMonth.setText(dataForReport.get(2));
			this.txtYear.setText(dataForReport.get(3));
			
			//create report
    		ArrayList<String> dataRep = new ArrayList<>();
    		dataRep.add(dataForReport.get(0)); //park name
    		dataRep.add(dataForReport.get(1)); //day
    		dataRep.add(dataForReport.get(2)); //month
    		dataRep.add(dataForReport.get(3)); //year
    		
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("CreateVisitsReport"));
			arrmsg.add(new String("ArrayList<String>"));
			arrmsg.add(dataRep);
			ClientUI.chat.accept(arrmsg);
			
			if(ChatClient.result == false)
				throw new NullPointerException("No report data");
    		
			//show report
			arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("ShowVisitsReport"));
			arrmsg.add(new String("ArrayList<String>"));
			arrmsg.add(dataRep);
			ClientUI.chat.accept(arrmsg);
			
			if(ChatClient.dataFromServer.get(0).equals("null"))
				throw new NullPointerException("No report data");
					
    		//guided
			XYChart.Series<String, Integer> dataSeries1 = new XYChart.Series<String, Integer>();
			dataSeries1.setName("group visit");
		    dataSeries1.getData().add(new XYChart.Data<>("9", ChatClient.intDataFromServer.get(0).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("10", ChatClient.intDataFromServer.get(1).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("11", ChatClient.intDataFromServer.get(2).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("12", ChatClient.intDataFromServer.get(3).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("13", ChatClient.intDataFromServer.get(4).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("14", ChatClient.intDataFromServer.get(5).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("15", ChatClient.intDataFromServer.get(6).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("16", ChatClient.intDataFromServer.get(7).get(0)));
		    dataSeries1.getData().add(new XYChart.Data<>("17", ChatClient.intDataFromServer.get(8).get(0)));

		    //single
			XYChart.Series<String, Integer> dataSeries2 = new Series<String, Integer>();
			dataSeries2.setName("Single visitor");
			dataSeries2.getData().add(new XYChart.Data<>("9", ChatClient.intDataFromServer.get(0).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("10", ChatClient.intDataFromServer.get(1).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("11", ChatClient.intDataFromServer.get(2).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("12", ChatClient.intDataFromServer.get(3).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("13", ChatClient.intDataFromServer.get(4).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("14", ChatClient.intDataFromServer.get(5).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("15", ChatClient.intDataFromServer.get(6).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("16", ChatClient.intDataFromServer.get(7).get(1)));
		    dataSeries2.getData().add(new XYChart.Data<>("17", ChatClient.intDataFromServer.get(8).get(1)));

		    chart.getData().addAll(dataSeries1, dataSeries2);
    	}catch (NullPointerException e) {
    		this.txtError.setText(e.getMessage());
    	}catch (Exception e) {
			System.out.println("Error in VisitingReportController: loadData");
			System.out.println(e.getMessage());
		}
    }

    /**
     * Handles the action when the back button is pressed.
     *
     * @param event The action event generated by pressing the back button.
     */
    @FXML
    void pressBack(ActionEvent event) {
    	try {
    		Button btn = (Button) event.getSource();
    		Stage stage = (Stage) btn.getScene().getWindow();
    		stage.close();
    	}catch (Exception e) {
    		System.out.println("Error in VisitingReportController: pressBack");
    		System.out.println(e.getMessage());
    	}
    }


}

