package gui;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import client.ChatClient;
import client.ClientController;
import client.ClientUI;
import entity.NextPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
//import javafx.scene.image.ImageView;

import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SettingsPageController {

	// labels
	@FXML
	private Label lblWellcome;

	// buttons
	@FXML
	private Button btnConnectToServer = null;
	@FXML
	private Button btnExit = null;

	// text fields
	@FXML
	private TextField txtIpAddress;
	@FXML
	private TextField txtPortNumber;

	@FXML
	private Text errorTxt;

	public void start(Stage primaryStage) throws Exception {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/gui/SettingsPage.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setTitle("Settings Page");
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (Exception e) {
			System.out.println("Error in SettingsPageController: start");
			System.out.println(e.getMessage());
		}
	}

	// Event for "connect" button
	public void connectToServer(ActionEvent event) throws Exception {

		try {
			String address = txtIpAddress.getText(), portNum = txtPortNumber.getText();
			// Check if the string is valid
			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(portNum);
			boolean portContainsOnlyDigits = matcher.matches();
			if (portContainsOnlyDigits || address.trim().isEmpty()) {
				System.out.println("You must enter ip address and port number");
				errorTxt.setText("The ip or port number is not valid, please try again");
				txtIpAddress.setText("");
				txtPortNumber.setText("");

			} else {
				try {
					ArrayList<Object> arrmsg = new ArrayList<Object>();
					arrmsg.add(new String("ConnectToServer"));
					arrmsg.add(new String("String"));
					arrmsg.add(new String("Conect"));
					ClientUI.chat = new ClientController(address, Integer.valueOf(portNum));
					ClientUI.chat.accept(arrmsg);

				} catch (Exception e) {
					System.out.println("you must enter valid ip and port numbers");
					errorTxt.setText(
							"you must enter valid ip address and valid port number in order to connect to server");
					txtIpAddress.setText("");
					txtPortNumber.setText("");
					return;
				}
				if (ChatClient.result) {
					ChatClient.result = false;
					NextPage page = new NextPage(event, "/gui/NewHomePage.fxml", "Home Page", "NewHomePageController",
							"connectToServer"); // need to add path and title
					page.Next();

				} else {
					System.out.println("couldnt connect to server");
				}
			}
		} catch (Exception e) {
			System.out.println("Error in SettingsPageController: connectToServer");
		}

	}

	// Event for "Exit" button
	public void pressExitBtn(ActionEvent event) throws Exception {
		System.out.println("Exit Home Page");
		System.exit(0);
	}

}
