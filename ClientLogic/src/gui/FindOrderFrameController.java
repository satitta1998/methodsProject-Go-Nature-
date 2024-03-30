/**
 * The FindOrderFrameController class controls the user interface for finding orders,
 * allowing users to search for orders by order number and perform actions such as editing or canceling them.
 */
package gui;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client.ChatClient;
import client.ClientUI;
import entity.NextPage;
import entity.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class FindOrderFrameController {
	
    /**
     * Enumeration representing actions that can be performed on an order: EDIT or CANCEL.
     */
	public enum Action {
		EDIT, CANCEL
	}
	
	private Action action;
	
	//labels
	@FXML
	private Label lblEnteredOrderNum;
	@FXML
	private Label lblResult;
		
	//text fields
	@FXML
	private TextField txtEnteredOrderNum;
	
	//buttons
	@FXML
	private Button btnFind=null;
	@FXML
	private Button btnBack=null;

	//get entered order number
	private String getEnteredOrderNum() {
        return txtEnteredOrderNum.getText();
	}
	
    /**
     * Sets the action to be performed on the order (EDIT or CANCEL).
     * 
     * @param action The action to be performed on the order.
     */
	//set action EDIT or CANCEL
	public void setAction(Action action) {
		this.action = action;
	}

    /**
     * Handles the event when the "Find" button is pressed, searching for the entered order number and
     * performing the specified action (EDIT or CANCEL) on the order if found.
     * 
     * @param event The ActionEvent triggered by pressing the "Find" button.
     * @throws Exception If an error occurs during the process.
     */
	//Event for "Find" button
	public void pressFindBtn (ActionEvent event) throws Exception {
		
		try {
			
			String orderNumber = getEnteredOrderNum();
			
			// Check if the string contains any digit
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(orderNumber);
            boolean containsOnlyDigits = matcher.matches();
            if (!containsOnlyDigits)
                throw new IllegalArgumentException("Order number not valid, should contain only numbers.");  

				
			//send order number for searching and get the order
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("OrderGet"));
			arrmsg.add(new String("String"));
			arrmsg.add(new String(orderNumber));
			ClientUI.chat.accept(arrmsg);			
		
			if(ChatClient.result == false) {
				errorCase("Order number not found","Order number does not exist in the system.");
			}else {
				
				System.out.println("Order number found");
				
				//Find order page is the same page for edit and cancel an order
				switch(this.action) {
					case EDIT: {
						NextPage page = new NextPage(event, "/gui/OrderForm.fxml", "Order Form", "OrderFrameController", "pressFindBtn", ChatClient.order);
				    	page.Next();
						break;}
					
					case CANCEL: {
				    	NextPage page = new NextPage(event, "/gui/CancelOrderForm.fxml", "Cancel Order Form", "CancelOrderFrameController", "pressFindBtn", ChatClient.order);
				    	page.Next();
						break;}
				}	
			}			
		}catch (IllegalArgumentException e1) {
			errorCase("String does not contain numbers.","Order number should contain only numbers.");
		}catch (Exception e) {
			System.out.println("Error in FindOrderFrameController: pressFindBtn");
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
		}
		
	}

    /**
     * Handles the event when the "Back" button is pressed, navigating back to the previous page.
     * 
     * @param event The ActionEvent triggered by pressing the "Back" button.
     * @throws Exception If an error occurs during the process.
     */
	//Even for "Back" button
	public void pressBackBtn(ActionEvent event) throws Exception {
		try {
	    	NextPage page = new NextPage(event, "/gui/TravellerPage.fxml", "Traveller Page", "TravellerPageController", "pressBackBtn");
	    	page.Next();
		}catch (Exception e) {
			System.out.println("Error in FindOrderFrameController: pressBackBtn");
			System.out.println(e.getMessage());
		}
		
	}
	
    //private method for error cases
    private void errorCase(String strPrint, String strSet) {
		System.out.println(strPrint);
		lblResult.setText(strSet);
    }

}
