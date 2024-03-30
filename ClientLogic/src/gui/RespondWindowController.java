/**
 * The controller class for the Respond Window GUI.
 * This class handles displaying response messages and images to the user.
 */
package gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import entity.NextPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RespondWindowController {
	
	@FXML
    private Button btnClose;

    @FXML
    private Label lblResult;
    
    @FXML
    private ImageView resultImg;
    
    /**
     * Sets the image based on the result.
     * 
     * @param result The result message to determine which image to display.
     */
    //Setting image to the image view
    public void setImage(String result) {
    	try {
    		Image image;
    		
			if (result.equals("Cancelled successfully!")) {
			    image = new Image(getClass().getResourceAsStream("Pictures/V.png"));
			}else {
			    image = new Image(getClass().getResourceAsStream("Pictures/X.png"));
			}
			resultImg.setImage(image);
		} catch (Exception e) {
			System.out.println("Error in RespondWindowController: setImage");
			System.out.println(e.getMessage());
		}
    }
    
    /**
     * Sets the label with the provided text.
     * 
     * @param text The text to set in the label.
     */
    //Setting label with the text
    public void setLabel(String text) {
    	try {
    		lblResult.setText(text);
    	}catch (Exception e) {
    		System.out.println("Error in RespondWindowController: setLabel");
    		System.out.println(e.getMessage());
    	}
    }

    /**
     * Handles the event when the "Close" button is pressed.
     * Redirects to the Traveller Page GUI.
     * 
     * @param event The ActionEvent triggered by pressing the button.
     */
    //Event for "Close" button
    @FXML
    void pressCloseBtn(ActionEvent event) {
		try {
			
	    	NextPage page = new NextPage(event, "/gui/TravellerPage.fxml", "Traveller Page", "TravellerPageController", "pressIdentifyBtn");
	    	page.Next();
		}catch(Exception e) {
			System.out.println("Error in RespondWindowController: pressCloseBtn");
			System.out.println(e.getMessage());
		}
    }

}
