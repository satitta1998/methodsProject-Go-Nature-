package gui;

import java.util.ArrayList;
import java.util.Random;

import client.ChatClient;
import client.ClientUI;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TotalVisitorsNumberReportPageController {
	
    @FXML
    private TableColumn<ReportData, String> colDate;

    @FXML
    private TableColumn<ReportData, String> colGuidedVisit;

    @FXML
    private TableColumn<ReportData, String> colPrivFamVisit;
    
    @FXML
    private TableView<ReportData> tblVistitorsNum;
	
    @FXML
    private Button btnClose;

    @FXML
    private Text txtMonth;

    @FXML
    private Text txtYear;
    
    @FXML
    private Text txtError;

    
    private ArrayList<String> dataForReport;
    //data report = {0 - parkName, 1 - month, 2 - year
    
    //load report data
    public void loadData(ArrayList<String> dataForReport) {
    	try {
			//this.dataForReport = dataForReport;
			//this.txtMonth.setText(dataForReport.get(1));
			//this.txtYear.setText(dataForReport.get(2));
    		
    		//create order
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("CreateVisitorsNumReport"));
			arrmsg.add(new String("String"));
			arrmsg.add(dataForReport.get(0));
			ClientUI.chat.accept(arrmsg);
			
			if(ChatClient.result == false)
				throw new NullPointerException("No report created");
			
			this.txtError.setText("Report created successfully");

			//get dates from DB
			ArrayList<String> dataForRep = new ArrayList<String>();
			dataForRep.add(dataForReport.get(0)); //parkName
			dataForRep.add(dataForReport.get(1)); //month
			dataForRep.add(dataForReport.get(2)); //year
			
			arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("GetVisitorsNumReport"));
			arrmsg.add(new String("ArrayList<String>"));
			arrmsg.add(dataForRep);
			ClientUI.chat.accept(arrmsg);
			
			if(ChatClient.dataFromServer.get(0).equals("null"))
				throw new NullPointerException("No report returned from DB");

			//CHECK: NEED TO MAKE THE REAL CODE
			ObservableList<ReportData> reportData = FXCollections.observableArrayList();
			reportData.add(new ReportData("21-12-1998","15","89"));
			this.loadTableData(reportData);
			
			
		}catch(NullPointerException e) {
			this.txtError.setText(e.getMessage());
		}
    	catch (Exception e) {
			System.out.println("Error in TotalVisitorsNumberReportPageController: loadData");
			System.out.println(e.getMessage());
		}	
    }
    
    //Event for "Close" button
    @FXML
    void pressCloseBtn(ActionEvent event) {
    	try {
    		Button btn = (Button) event.getSource();
    		Stage stage = (Stage) btn.getScene().getWindow();
    		stage.close();
    	}catch (Exception e) {
    		System.out.println("Error in TotalVisitorsNumberReportPageController: pressCloseBtn");
    		System.out.println(e.getMessage());
    	}
    }
    
    //private class for report data
    private class ReportData {
    	private SimpleStringProperty date;
    	private SimpleStringProperty privateFamilyVisit;
    	private SimpleStringProperty guidedGroupVisit;
    	
    	public ReportData(String date, String privateFamilyVisit, String guidedGroupVisit) {
    		this.date = new SimpleStringProperty(date);
    		this.privateFamilyVisit = new SimpleStringProperty(privateFamilyVisit);
    		this.guidedGroupVisit = new SimpleStringProperty(guidedGroupVisit);
    	}
    	
    	public SimpleStringProperty date() {
    		return date;
    	}
    	
    	public SimpleStringProperty privateFamilyVisit() {
    		return privateFamilyVisit;
    	}
    	
    	public SimpleStringProperty guidedGroupVisit() {
    		return guidedGroupVisit;
    	}
    }//end private class
    
	private void loadTableData(ObservableList<ReportData> repData) {
		try {
			this.colDate.setCellValueFactory(cellData -> cellData.getValue().date());
			this.colGuidedVisit.setCellValueFactory(cellData -> cellData.getValue().guidedGroupVisit());
			this.colPrivFamVisit.setCellValueFactory(cellData -> cellData.getValue().privateFamilyVisit());
			this.tblVistitorsNum.setItems(repData);

		} catch (Exception e) {
			System.out.println("Error in ServerPortFrameController: loadTableData");
			System.out.println(e.getMessage());
		}
	}

}
