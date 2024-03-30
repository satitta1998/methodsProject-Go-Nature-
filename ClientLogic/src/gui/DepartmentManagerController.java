/**
 * The DepartmentManagerController class controls the user interface for department manager, providing functionality
 * to load park data, approve or deny new park information.
 */
package gui;

import java.util.ArrayList;
import client.ChatClient;
import client.ClientUI;
import entity.NextPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class DepartmentManagerController {

	private String parkName;

	@FXML
	private Text promptTxt;

	@FXML
	private Text availableSpaceTxt;

	@FXML
	private Button btnApprove;

	@FXML
	private Button btnDeny;

	@FXML
	private Button btnBack;

	@FXML
	private Text closesParkTxt;

	@FXML
	private Text configureGapTxt;

	@FXML
	private Text maxCapacityTxt;

	Boolean infoToApprove = true;
	
    /**
     * Loads data for the department manager interface, including available space in the park and new park information
     * pending approval.
     * 
     * @param parkName The name of the park for which data is being loaded.
     */
	// load data
	public void loadData(String parkName) {
		try {
			this.parkName = parkName;
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("AvilableSpaceGet"));
			arrmsg.add(new String("String"));
			arrmsg.add(new String(parkName));
			ClientUI.chat.accept(arrmsg);

			if (ChatClient.dataFromServer.equals(null))
				throw new NullPointerException("This park doesn't exists.");

			Integer spaceInPark = (Integer.parseInt(ChatClient.dataFromServer.get(0))
					- Integer.parseInt(ChatClient.dataFromServer.get(1)));
			availableSpaceTxt.setText(Integer.toString(spaceInPark));

			// load data
			// check if there are new information to approve
			arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("ParkCheckIfApproveRequired"));
			arrmsg.add(new String(parkName));
			ClientUI.chat.accept(arrmsg);

			// show the information
			if (ChatClient.result == true) {
				arrmsg.clear();
				arrmsg = new ArrayList<Object>();
				arrmsg.add(new String("ParkNewParamsGet"));
				arrmsg.add(new String("String"));
				arrmsg.add(new String(parkName));
				ClientUI.chat.accept(arrmsg);

				if (ChatClient.dataFromServer.equals(null))
					throw new NullPointerException("This park doesn't exists.");

				maxCapacityTxt.setText(ChatClient.dataFromServer.get(0));
				configureGapTxt.setText(ChatClient.dataFromServer.get(1));
				closesParkTxt.setText(ChatClient.dataFromServer.get(2));
			} else {

				// show prompt text
				this.promptTxt.setText("There are no new information to approve");
				infoToApprove = false;
			}

		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: loadData");
			System.out.println(e.getMessage());
		}
	}

    /**
     * Handles the event when the "Approve" button is pressed, updating park parameters in the database
     * based on the approved information.
     * 
     * @param event The ActionEvent triggered by the button press.
     */
	@FXML
	void pressApprove(ActionEvent event) {
		try {
			if (infoToApprove) {
				// send information to change the db
				ArrayList<Object> arrmsg = new ArrayList<Object>();
				ArrayList<String> updatePark = new ArrayList<String>();
				arrmsg.add(new String("ParkCurrentParamsUpdate"));
				arrmsg.add(new String("ArrayList<String>"));
				updatePark.add(new String(parkName));
				updatePark.add(new String(maxCapacityTxt.getText()));
				updatePark.add(new String(configureGapTxt.getText()));
				updatePark.add(new String(closesParkTxt.getText()));
				arrmsg.add(updatePark);
				ClientUI.chat.accept(arrmsg);

				if (ChatClient.result == false)
					throw new NullPointerException("Update manager doesn't succesful.");
				else
					this.promptTxt.setText("Approved successfully!");
				maxCapacityTxt.setText("");
				configureGapTxt.setText("");
				closesParkTxt.setText("");
			}

		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressApprove");
			System.out.println(e.getMessage());
		}
	}

	
    /**
     * Handles the event when the "Deny" button is pressed, reverting changes made by denying
     * the new park information.
     * 
     * @param event The ActionEvent triggered by the button press.
     */
	@FXML
	void pressDeny(ActionEvent event) {
		try {
			if (infoToApprove) {
				ArrayList<Object> arrmsg = new ArrayList<Object>();
				ArrayList<String> updatePark = new ArrayList<String>();
				arrmsg.add(new String("ParkCurrentParamsGet"));
				arrmsg.add(new String("String"));
				arrmsg.add(new String(parkName));
				ClientUI.chat.accept(arrmsg);

				if (ChatClient.dataFromServer.equals(null))
					throw new NullPointerException("This park doesn't exists.");

				updatePark.add(new String(parkName));
				updatePark.add(new String(ChatClient.dataFromServer.get(0))); // Capacity
				updatePark.add(new String(ChatClient.dataFromServer.get(1))); // Gap
				updatePark.add(new String(ChatClient.dataFromServer.get(2))); // Stay time

				arrmsg.clear();
				arrmsg = new ArrayList<Object>();
				arrmsg.add(new String("ParkCurrentParamsUpdate"));
				arrmsg.add(new String("ArrayList<String>"));
				arrmsg.add(updatePark);
				ClientUI.chat.accept(arrmsg);

				if (ChatClient.result == false)
					throw new NullPointerException("Update Park info woesn't succesful.");
				else
					this.promptTxt.setText("Denied successfully!");
					maxCapacityTxt.setText("");
					configureGapTxt.setText("");
					closesParkTxt.setText("");
			}

		} catch (IllegalArgumentException e) {
			this.promptTxt.setText(e.getMessage());
		} catch (NullPointerException e) {
			this.promptTxt.setText(e.getMessage());
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressApprove");
			System.out.println(e.getMessage());
		}
	}
	
    /**
     * Handles the event when the "Back" button is pressed, navigating back to the department manager menu.
     * 
     * @param event The ActionEvent triggered by the button press.
     */
	@FXML
	void pressBack(ActionEvent event) {
		try {
			NextPage page = new NextPage(event, "/gui/DepartmentManagerMenu.fxml", "Department Manager Menu",
					"DepartmentManagerMenuController", "pressLogoutBtn", parkName);
			page.Next();
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressLogOut");
			System.out.println(e.getMessage());
		}
	}

}
