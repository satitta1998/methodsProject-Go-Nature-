/**
 * The DepartmentManagerMenuController class controls the user interface for the department manager menu,
 * providing functionality to load parks, access reports, make requests, and log out.
 */
package gui;

import java.util.ArrayList;

import client.ChatClient;
import client.ClientUI;
import entity.NextPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;

public class DepartmentManagerMenuController {

	@FXML
	private Button btnLogout;

	@FXML
	private Button btnReports;

	@FXML
	private Button btnRequest;
	
    @FXML
    private Text errorTxt;

	@FXML
	private ComboBox<String> parkBox;

    /**
     * Loads data for the department manager menu, including the list of available parks.
     * 
     * @param s Placeholder parameter.
     */
	public void loadData(String s) {

		try {

			// Load Parks List
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("ParksListGet"));
			arrmsg.add(new String("Get"));
			arrmsg.add(new String("Get"));
			ClientUI.chat.accept(arrmsg);

			if (ChatClient.dataFromServer.get(0).equals("null"))
				throw new NullPointerException("The parks list doesn't exists.");

			this.parkBox.getItems().addAll(ChatClient.dataFromServer);
			this.parkBox.getItems().add("All parks");

		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		}
	}

    /**
     * Handles the event when a park is selected from the ComboBox, hiding the "Request" button
     * if "All parks" is selected.
     * 
     * @param event The ActionEvent triggered by selecting a park from the ComboBox.
     */
    @FXML
    void pressParkBox(ActionEvent event) {
    	if (this.parkBox.getValue().equals("All parks")) {
    		btnRequest.setVisible(false);
    	}else {
    		btnRequest.setVisible(true);
    	}
    }

    /**
     * Handles the event when the "Reports" button is pressed, navigating to the reports page
     * for the selected park.
     * 
     * @param event The ActionEvent triggered by pressing the "Reports" button.
     */
	@FXML
	void pressReportsBtn(ActionEvent event) {
		try {
			if (this.parkBox.getValue() == null)
				errorTxt.setText("You have to choose a park.");
			else {
				NextPage page = new NextPage(event, "/gui/DepartmentManagerReportsPage.fxml",
						"Department Manager Reports Page", "DepartmentManagerReportsPageController", "pressReportsBtn",
						this.parkBox.getValue());
				page.Next();
			}
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerMenuController: pressReportsBtn");
			System.out.println(e.getMessage());
		}
	}

    /**
     * Handles the event when the "Request" button is pressed, navigating to the request page
     * for the selected park.
     * 
     * @param event The ActionEvent triggered by pressing the "Request" button.
     */
	@FXML
	void pressRequestBtn(ActionEvent event) {
		try {
			if (this.parkBox.getValue() == null)
				errorTxt.setText("You have to choose a park.");
				
			else {
				NextPage page = new NextPage(event, "/gui/DepartmentManager.fxml", "Department Manager Request Page",
						"DepartmentManagerController", "pressRequestBtn", this.parkBox.getValue());
				page.Next();
			}
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerMenuController: pressRequestBtn");
			System.out.println(e.getMessage());
		}
	}

    /**
     * Handles the event when the "Logout" button is pressed, logging out the user and navigating
     * to the login page.
     * 
     * @param event The ActionEvent triggered by pressing the "Logout" button.
     */
	@FXML
	void pressLogout(ActionEvent event) {
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
				throw new NullPointerException("User wasn't logged out");

			}
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressLogOut");
			System.out.println(e.getMessage());
		}
	}
}
