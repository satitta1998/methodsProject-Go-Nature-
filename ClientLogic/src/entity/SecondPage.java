/**
 * The SecondPage class facilitates the navigation to secondary pages in the application.
 * It is responsible for loading FXML files, initializing controllers, and displaying the secondary stages.
 */
package entity;

import gui.InvoiceController;
import gui.SMSController;
import gui.TotalVisitorsNumberReportPageController;
import gui.UsageReportPageController;
import gui.VisitingReportController;
import gui.CancellationReportController;
import gui.ChoiceWindowController;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SecondPage {
	
	private ActionEvent event;
	private String path;
	private String title;
	private String controller;
	private Object data;
	private String method;
	
    /**
     * Constructs a SecondPage object with specified parameters.
     * @param event The ActionEvent triggering the navigation.
     * @param path The path to the FXML file of the secondary page.
     * @param title The title of the secondary stage.
     * @param controller The name of the controller associated with the secondary page.
     * @param method The method to be called in the associated controller.
     */
	//Constructors
	public SecondPage(ActionEvent event, String path, String title, String controller, String method) {
		this.event = event; 
		this.path = path;
		this.title = title;
		this.controller = controller;
		this.method = method;
	}
	
    /**
     * Constructs a SecondPage object with specified parameters including data.
     * @param event The ActionEvent triggering the navigation.
     * @param path The path to the FXML file of the secondary page.
     * @param title The title of the secondary stage.
     * @param controller The name of the controller associated with the secondary page.
     * @param method The method to be called in the associated controller.
     * @param data The data object to be passed to the controller.
     */
	public SecondPage(ActionEvent event, String path, String title, String controller, String method, Object data) {
		this.event = event; 
		this.path = path;
		this.title = title;
		this.controller = controller;
		this.method = method;
		this.data = data;
	}
	
    /**
     * Constructs a SecondPage object with specified parameters including data, without an associated event.
     * @param path The path to the FXML file of the secondary page.
     * @param title The title of the secondary stage.
     * @param controller The name of the controller associated with the secondary page.
     * @param method The method to be called in the associated controller.
     * @param data The data object to be passed to the controller.
     */
	public SecondPage(String path, String title, String controller, String method, Object data) {
		this.path = path;
		this.title = title;
		this.controller = controller;
		this.method = method;
		this.data = data;
	}
	
    /**
     * Opens the secondary page based on the specified parameters.
     * This method loads the FXML file, initializes the associated controller,
     * and displays the secondary stage.
     * @throws Exception If an error occurs during page navigation.
     */
	//function for changing pages
	public void openSecondPage() throws Exception {
		Platform.runLater(() -> {
			try {
				FXMLLoader secondLoader = new FXMLLoader(getClass().getResource(path));
				Pane secondRoot = secondLoader.load();
				
				switch (controller) {
					case "InvoiceController": {
						InvoiceController invoiceController = secondLoader.getController();
						invoiceController.genarateInvoice((PriceGenerator)data);
						break;}
					
					case "ChoiceWindowController": {
						ChoiceWindowController choiceWindowController = secondLoader.getController();
						choiceWindowController.loadData((ArrayList<String>)data);
						break;}
					
					case "TotalVisitorsNumberReportPageController": {
						TotalVisitorsNumberReportPageController totalVisitorsNumberReportPageController = secondLoader.getController();
						totalVisitorsNumberReportPageController.loadData((ArrayList<String>)data);
						break;}
					
					case "UsageReportPageController": {
						UsageReportPageController usageReportPageController = secondLoader.getController();
						usageReportPageController.loadData((ArrayList<String>)data);
						break;}
					
					case "VisitingReportController": {
						VisitingReportController visitingReportController = secondLoader.getController();
						visitingReportController.loadData((ArrayList<String>)data);
						break;}
					
					case "CancellationReportController": {
						CancellationReportController cancellationReportController = secondLoader.getController();
						cancellationReportController.loadData((ArrayList<String>)data);
						break;}
					
					case "SMSController": {
						SMSController smsController = secondLoader.getController();
						smsController.loadSMS((ArrayList<String>)data);
						break;}
				}
				

		        Stage secondStage = new Stage();
		        secondStage.setTitle(title);
		        Scene secondScene = new Scene(secondRoot);
		        secondStage.setScene(secondScene);
		        secondStage.show();
			}catch (Exception e) {
				System.out.println("Error in SecondPage: openSecondPage");
				System.out.println(e.getMessage());
			}
		});
	}
	
	

}
