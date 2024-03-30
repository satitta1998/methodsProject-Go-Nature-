package gui;
import entity.NextPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NewHomePageController {

    @FXML
    private Button btnExit;

    @FXML
    private Button btnIdentify;

    @FXML
    private Button btnLogin;

    
    //Event for "Log in as an employee"
    @FXML
    void pressLoginBtn(ActionEvent event) throws Exception {
    	try {
        	NextPage page = new NextPage(event, "/gui/Login.fxml", "", "LoginController", "pressLoginBtn");
        	page.Next();
    	}catch (Exception e) {
    		System.out.println("Error in NewHomePageController: pressLoginBtn");
    		System.out.println(e.getMessage());
    	}
    }

    //Event for "Identify as a traveller"
    @FXML
    void pressIdentifyBtn(ActionEvent event) throws Exception {
        try {
            NextPage page = new NextPage(event, "/gui/IdentifyPage.fxml", "Identify Page", "IdentifyPageController", "pressIdentifyBtn"); 
            page.Next();
        }catch (Exception e) {
            System.out.println("Error in NewHomePageController: pressIdentifyBtn");
            System.out.println(e.getMessage());
        }
    }

    
	// Event for "Exit" button
	public void pressExitBtn(ActionEvent event) throws Exception {
		System.out.println("Exit Home Page");
		System.exit(0);
	}
}
