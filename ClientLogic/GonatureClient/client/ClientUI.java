/**
 * The ClientUI class represents the main entry point for the client application.
 * It extends JavaFX Application class and provides the start method to initialize
 * the JavaFX application.
 */
package client;
import javafx.application.Application;

import javafx.stage.Stage;

import java.util.Vector;
import client.ClientController;
import gui.SettingsPageController;


public class ClientUI extends Application {
	
    /**
     * Reference to the ClientController managing the client-side functionality.
     * Only one instance of ClientController should exist throughout the application.
     */
	public static ClientController chat; //only one instance

    /**
     * The main method of the ClientUI class.
     * It launches the JavaFX application.
     *
     * @param args Command line arguments passed to the application.
     * @throws Exception if an exception occurs during the application startup.
     */
	public static void main( String args[] ) throws Exception
	   { 
		    launch(args);  
	   } // end main
	 
	
    /**
     * The start method initializes the JavaFX application.
     * It creates and starts the settings page controller to display settings.
     *
     * @param primaryStage The primary stage of the JavaFX application.
     * @throws Exception if an exception occurs during the initialization.
     */
	@Override
	public void start(Stage primaryStage) throws Exception {
		 
						  		
		 SettingsPageController settingsPageFrame = new SettingsPageController();
		 
		 settingsPageFrame.start(primaryStage);
	}
	
	
}
