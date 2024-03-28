package gui;

import client.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SMSController {

    @FXML
    private Button btnApprove;

    @FXML
    private Button btnClose;

    @FXML
    private Text txtMsg;

    @FXML
    private Text txtResult;
    
    public void loadSMS(String sms) {
    	try {
    		
    		if(sms.equals("null"))
    			throw new NullPointerException("No message");

    		
    		this.txtMsg.setText(sms);
    		
    	}catch (Exception e) {
    		System.out.println("Error in SMSController: loadSMS");
    		System.out.println(e.getMessage());	
    	}
    }
    
    //Event for "Approve" button
    @FXML
    void pressApproveBtn(ActionEvent event) {
    	///////////////
    }
    

    @FXML
    void pressCloseBtn(ActionEvent event) {
    	try {
    		Button btn = (Button) event.getSource();
    		Stage stage = (Stage) btn.getScene().getWindow();
    		stage.close();
    	}catch (Exception e) {
    		System.out.println("Error in SMSController: pressCloseBtn");
    		System.out.println(e.getMessage());
    	}
    }
}
