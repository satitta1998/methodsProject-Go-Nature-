/**
 * The DepartmentWorkerController class controls the user interface for department worker,
 * providing functionality for registering guides and logging out.
 */
package gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client.ChatClient;
import client.ClientUI;
import entity.NextPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class DepartmentWorkerController {

	@FXML
	private Text errorTxt;

	@FXML
	private TextField guide_id;

	@FXML
	private Button btnGuideReg;

	@FXML
	private Button btnLogout;

    /**
     * Handles the event when the guide registration button is pressed, validating the entered guide ID
     * and registering the guide if it is valid and not already registered.
     * 
     * @param event The ActionEvent triggered by pressing the guide registration button.
     */
	@FXML
	void pressGuideRegBtn(ActionEvent event) {
		try {
			String guide = guide_id.getText();
			// Check if the string contains any digit
			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(guide);
			boolean containsOnlyDigits = matcher.matches();
			if (!containsOnlyDigits) {
				msgCase("You must enter valid guide id", "You must enter valid guide id");
				errorTxt.setFill(Color.RED);
			} else {
				ArrayList<Object> arrmsg = new ArrayList<Object>();
				arrmsg = new ArrayList<Object>();
				arrmsg.add(new String("GroupGuideCheck"));
				arrmsg.add(new String("String"));
				arrmsg.add(guide);
				ClientUI.chat.accept(arrmsg);

				if (ChatClient.result == true) {
					// the guide is already registered
					msgCase("The guide is already registered in the system",
							"The guide is already registered in the system,\n please enter a diffrent guide id");
					errorTxt.setFill(Color.RED);
				} else {
					ArrayList<String> RegistrationDetails = new ArrayList<>(Arrays.asList(guide));

					arrmsg.clear();
					arrmsg.add(new String("GuideRegistration"));
					arrmsg.add(new String("ArrayList<String>"));
					arrmsg.add(RegistrationDetails);
					ClientUI.chat.accept(arrmsg);
					if (ChatClient.result == true) {
						// the guide registered successfully
						msgCase("The guide registered successfully", "The guide registered successfully!");
						errorTxt.setFill(Color.GREEN);
					} else {
						msgCase("An error occurred, in Guide Registration ",
								"An error occurred, the guide is not registered in the system");
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in DepartmentWorkerController: GuideRegistration");
		}
	}

    /**
     * Handles the event when the logout button is pressed, logging out the user and navigating
     * back to the login page.
     * 
     * @param event The ActionEvent triggered by pressing the logout button.
     */
	@FXML
	void pressLogoutBtn(ActionEvent event) {
		try {
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("UserLogOut"));
			arrmsg.add(new String("String"));
			arrmsg.add(ChatClient.userName);
			ClientUI.chat.accept(arrmsg);

			if (ChatClient.result == true) {
				ChatClient.userName = "";
				NextPage page = new NextPage(event, "/gui/Login.fxml", "Login Page", "LoginController",
						"pressLogoutBtn");
				page.Next();
			} else {
				this.errorTxt.setText("The user wasn't logged out");

			}
		} catch (Exception e) {
			System.out.println("Error in ParkManagerController: pressLogOut");
			System.out.println(e.getMessage());
		}
	}

	// private method for messages
	private void msgCase(String strPrint, String strSet) {
		System.out.println(strPrint);
		errorTxt.setText(strSet);
		guide_id.setText("");
	}
}
