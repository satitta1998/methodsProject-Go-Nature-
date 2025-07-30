package gui;

import GoNatureServer.GoNatureServer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import server.ServerUI;

/**
 * The HomePageController class controls the behavior of the home page of the server application.
 */
public class HomePageController {

	@FXML
	private TextField port_id;

	@FXML
	private Button btnOpen;

	@FXML
	private Button btnExit;

	@FXML
	private Text errorTxt;

    /**
     * Starts the server home page.
     *
     * @param primaryStage The primary stage for the home page.
     * @throws Exception if an error occurs during the initialization.
     */
	public void start(Stage primaryStage) throws Exception {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/gui/HomePage.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setTitle("Server Home Page");
			primaryStage.setScene(scene);

			primaryStage.show();

		} catch (Exception e) {
			System.out.println("Error in HomePageController: start");
			System.out.println(e.getMessage());
		}
	}

    /**
     * Handles the action event when the "Open Server" button is clicked.
     * It starts the server with the specified port number and opens the server frame.
     *
     * @param event The action event triggered by clicking the "Open Server" button.
     */
	@FXML
	// Event for "Open Sever" button
	public void openServer(ActionEvent event) {
		String portNumber = port_id.getText();

		if (portNumber.trim().isEmpty()
				|| (Integer.valueOf(portNumber.trim()) < 1024 || Integer.valueOf(portNumber.trim()) > 49151)) {
			portNumber = "5555";
		}// else {
			ServerUI.runServer(portNumber);
			try {
				FXMLLoader loader = new FXMLLoader();

				((Node) event.getSource()).getScene().getWindow().hide(); // hiding primary window
				Stage primaryStage = new Stage();
				Pane root = loader.load(getClass().getResource("/gui/ServerFrame.fxml").openStream());

				Scene scene = new Scene(root);

				ServerPortFrameController controller = loader.getController();
				controller.setIp();
				GoNatureServer.controller = controller;
				primaryStage.setTitle("Connection status");
				primaryStage.setScene(scene);
				primaryStage.show();
			} catch (Exception e) {
				System.out.println("Error in HomePageController: pressOpenBtn");
				System.out.println(e.getMessage());
			}
	//	}
	}

    /**
     * Handles the action event when the "Exit" button is clicked.
     * It exits the home page.
     *
     * @param event The action event triggered by clicking the "Exit" button.
     * @throws Exception if an error occurs during the exit process.
     */
	// Event for "Exit" button
	public void pressExitBtn(ActionEvent event) throws Exception {
		System.out.println("Exit Home Page");
		System.exit(0);
	}

}
