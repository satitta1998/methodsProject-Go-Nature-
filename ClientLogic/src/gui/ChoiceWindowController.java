/**
 * The ChoiceWindowController class controls the user interface for making a choice between entering the waiting list or going back to choose another time.
 * It allows users to enter the waiting list for a selected order or return to the previous screen to choose another time slot.
 */
package gui;

import java.util.ArrayList;

import client.ChatClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.text.Text;

public class ChoiceWindowController {
	
    @FXML
    private Button btnEnterWaitingList;

    @FXML
    private Button btnGoBack;
    
    @FXML
    private Text txtResult;
    
    private ArrayList<String> orderData;
    
    /**
     * Loads the order data into the user interface.
     * 
     * @param orderData The ArrayList containing the data of the selected order.
     */
    public void loadData(ArrayList<String> orderData) {
    	this.orderData = orderData;
    	this.btnGoBack.setText("Go back and choose another time");
    }
    
    /**
     * Handles the event when the "Enter waiting list" button is pressed.
     * Attempts to enter the waiting list for the selected order and displays a response message.
     * 
     * @param event The ActionEvent triggered by pressing the "Enter waiting list" button.
     */
    //Event for "Enter waiting list" button
    @FXML
    void pressEnterWaitingList(ActionEvent event) {
    	try {
    		//enter the waiting list
			ArrayList<Object>  arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("EnterWaitList"));
			arrmsg.add(new String("ArrayList<String>"));
			arrmsg.add(orderData);
			ClientUI.chat.accept(arrmsg);
			
			if(ChatClient.dataFromServer.get(0).equals("couldnt find order id?")) {
				this.txtResult.setText("Error in entering the waiting list. Please contact us: GoNature@parks.co.il");
			}else {
				//entered the waiting list successfully
				this.txtResult.setText(new String("Your order in the waiting list. The order number is: " + ChatClient.dataFromServer.get(0)));
				this.btnGoBack.setText("Close");
			}
    	}catch (Exception e) {
    		System.out.println("Error in ChoiceWindowController: pressEnterWaitingList");
    		System.out.println(e.getMessage());
    	}
    }

    
    /**
     * Handles the event when the "Go back and choose another time" button is pressed.
     * Closes the current stage and returns to the previous screen to choose another time slot.
     * 
     * @param event The ActionEvent triggered by pressing the "Go back and choose another time" button.
     */
    //Event for "Go back and choose another time" button
    @FXML
    void pressGoBack(ActionEvent event) {
    	try {
    		Button btn = (Button) event.getSource();
    		Stage stage = (Stage) btn.getScene().getWindow();
    		stage.close();
    	}catch (Exception e) {
    		System.out.println("Error in ChoiceWindowController: pressGoBack");
    		System.out.println(e.getMessage());
    	}
    }

}
