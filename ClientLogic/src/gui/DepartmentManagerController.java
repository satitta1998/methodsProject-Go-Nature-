package gui;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Button btnRefresh;

    @FXML
    private Text closesParkTxt;

    @FXML
    private Text configureGapTxt;

    @FXML
    private Text maxCapacityTxt;
    
  //load data
    public void loadData(String parkName) 
    {
    	try {
    		ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("AvilableSpaceGet"));
			arrmsg.add(new String(parkName));
			arrmsg.add(new String("Get"));
			
			//////////OPEN///////////////
			//ClientUI.chat.accept(arrmsg);
			
			//////////CHECK///////////
			ChatClient.dataFromServer = new ArrayList<String>();
			ChatClient.dataFromServer.add(new String("15"));
			ChatClient.dataFromServer.add(new String("5"));
			
			if (ChatClient.dataFromServer.equals(null))
				throw new NullPointerException("This park doesn't exists.");
			
			Integer spaceInPark = (Integer.parseInt(ChatClient.dataFromServer.get(0)) - Integer.parseInt(ChatClient.dataFromServer.get(1)));
			availableSpaceTxt.setText(Integer.toString(spaceInPark));
    		
    		this.parkName = parkName;
    		//load data
    		//check if there are new information to approve
    		arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("ParkCheckIfApproveRequired"));
			arrmsg.add(new String(parkName));
			///////////OPEN///////////////////
			//ClientUI.chat.accept(arrmsg);
			
			/////////CHECK//////////////
			ChatClient.result = false;
			
			//show the information 
    		if(ChatClient.result == true) {
				arrmsg.clear();
				arrmsg.add(new String("ParkNewParamsGet"));
				arrmsg.add(new String("String"));
				arrmsg.add(new String(parkName));
				
				///////OPEN////////////
				//ClientUI.chat.accept(arrmsg);
				
				/////////CHECK///////////////
				ChatClient.dataFromServer = new ArrayList<String>();
				ChatClient.dataFromServer.add(new String("23"));
				ChatClient.dataFromServer.add(new String("32"));
				ChatClient.dataFromServer.add(new String("5"));
				ChatClient.dataFromServer.add(new String("7"));
				
				if (ChatClient.dataFromServer.equals(null))
					throw new NullPointerException("This park doesn't exists.");
				
				
				maxCapacityTxt.setText(ChatClient.dataFromServer.get(0));
				configureGapTxt.setText(ChatClient.dataFromServer.get(1));
				closesParkTxt.setText(ChatClient.dataFromServer.get(2));
		        spaceInPark = (Integer.parseInt(ChatClient.dataFromServer.get(0)) - Integer.parseInt(ChatClient.dataFromServer.get(3)));
		        availableSpaceTxt.setText(Integer.toString(spaceInPark));
    		}else {
    			
    			//show prompt text
    			this.promptTxt.setText("There are no new information to approve");
    		}
	        
    	}catch (NullPointerException e) {
    		System.out.println(e.getMessage());
    	}catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: loadData");
			System.out.println(e.getMessage());
    }
   }
    
    @FXML
    void pressApprove(ActionEvent event) {
    	try {
    		//send information to change the db
    		ArrayList<Object> arrmsg = new ArrayList<Object>();
    		ArrayList<String> updatePark = new ArrayList<String>();
    		arrmsg.add(new String("ParkCorrentParamsUpdate"));
    		arrmsg.add(new String("ArrayList<String>"));
    		updatePark.add(new String(parkName));
    		updatePark.add(new String(maxCapacityTxt.getText()));
    		updatePark.add(new String(configureGapTxt.getText()));
    		updatePark.add(new String(closesParkTxt.getText()));
    		updatePark.add(new String("0"));
    		updatePark.add(new String("0"));
    		updatePark.add(new String("0"));
    		arrmsg.add(updatePark);
    		///////////OPEN///////////////
    		//ClientUI.chat.accept(arrmsg);
    		
    		/////////////CHECK//////////////
    		ChatClient.result = true;
    		
			
			if (ChatClient.result == false)
				throw new NullPointerException("Update manager doesn't succesful.");
			else
				this.promptTxt.setText("Approved successfully!");
    	}catch (NullPointerException e) {
    		System.out.println(e.getMessage());
    	}catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressApprove");
			System.out.println(e.getMessage());
    }
    }

    @FXML
    void pressDeny(ActionEvent event) {
    	try {
    		
    		// 1. Check capacity
    		String checkCapacity = this.maxCapacityTxt.getText();
    		if (checkCapacity.trim().isEmpty()) {
    			this.promptTxt.setText("String for capacity cant be empty");
    		} else {
    			// Check if the string contains any digit
    			Pattern pattern_cap = Pattern.compile("\\d");
    			Matcher matcher_cap = pattern_cap.matcher(checkCapacity);
    			if (!matcher_cap.find())
    				throw new IllegalArgumentException("capacity should contain only numbers");

    			if (Integer.parseInt(checkCapacity) < 1)
    				throw new IllegalArgumentException("capacity should be greater then 0");
    			// 2. Check the gap
    			String checkGap = this.configureGapTxt.getText();
    			if (checkGap.trim().isEmpty()) {
    				this.promptTxt.setText("String for gap cant be empty");
    			} else {
    				// Check if the string contains any digit
    				Pattern pattern_gap = Pattern.compile("\\d");
    				Matcher matcher_gap = pattern_gap.matcher(checkGap);
    				if (!matcher_gap.find())
    					throw new IllegalArgumentException("gap should contain only numbers");

    				if (Integer.parseInt(checkGap) < 1)
    					throw new IllegalArgumentException("gap should be greater then 0");
    			}

    			// 3. Check time of stay
    			String checkTimeOfStay = this.closesParkTxt.getText();
    			if (checkTimeOfStay.trim().isEmpty()) {
    				this.promptTxt.setText("String for time of stay cant be empty");
    			} else {
    				// Check if the string contains any digit
    				Pattern pattern_tos = Pattern.compile("\\d");
    				Matcher matcher_tos = pattern_tos.matcher(checkTimeOfStay);
    				if (!matcher_tos.find())
    					throw new IllegalArgumentException("time of stay should contain only numbers");

    				if (Integer.parseInt(checkTimeOfStay) < 1)
    					throw new IllegalArgumentException("time of stay should be greater then 0");
    			}
    		}
    		ArrayList<Object> arrmsg = new ArrayList<Object>();
    		ArrayList<String> updatePark = new ArrayList<String>();
    		arrmsg.add(new String("ParkCurrentParamsGet"));
            arrmsg.add(new String("String"));
            arrmsg.add(new String(parkName));
            
            //////////////OPEN////////////
            //ClientUI.chat.accept(arrmsg);
            
            /////////CHECK///////////
            ChatClient.dataFromServer = new ArrayList<String>();
            ChatClient.dataFromServer.add("36");
            ChatClient.dataFromServer.add("6");
            ChatClient.dataFromServer.add("60");
            ChatClient.dataFromServer.add("9");

            if (ChatClient.dataFromServer.equals(null))
                throw new NullPointerException("This park doesn't exists.");
            
    		updatePark.add(new String(parkName));
    		updatePark.add(new String(ChatClient.dataFromServer.get(0))); //Capacity
    		updatePark.add(new String(ChatClient.dataFromServer.get(1)));	//Gap
    		updatePark.add(new String(ChatClient.dataFromServer.get(2))); //Stay time
    		updatePark.add(new String("0"));
    		updatePark.add(new String("0"));
    		updatePark.add(new String("0"));
    		
    		arrmsg.clear();
    		arrmsg.add(new String("ParkCurrentParamsUpdate"));
    		arrmsg.add(new String("ArrayList<String>"));
    		arrmsg.add(updatePark);
    		//////////////////OPEN/////////////
			//ClientUI.chat.accept(arrmsg);
    		
    		//////////CHEK/////////////
    		ChatClient.result = true;
    		
			if (ChatClient.result == false)
				throw new NullPointerException("Update Park info woesn't succesful.");
			else
				this.promptTxt.setText("Denied successfully!");
    	}catch (IllegalArgumentException e) {
    		this.promptTxt.setText(e.getMessage());
    	}catch (NullPointerException e) {
    		this.promptTxt.setText(e.getMessage());
    	}catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressApprove");
			System.out.println(e.getMessage());
    }
    }

    @FXML
    void pressBack(ActionEvent event) {
    	try {
        	NextPage page = new NextPage(event, "/gui/DepartmentManagerMenu.fxml", "Department Manager Menu", "DepartmentManagerMenuController", "pressLogoutBtn", parkName); 
        	page.Next();
    	}catch (Exception e) {
    		System.out.println("Error in DepartmentManagerController: pressLogOut");
    		System.out.println(e.getMessage());
    	}
    }

    @FXML
    void pressRefreshbtn(ActionEvent event) {
    	try {
    		ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("AvilableSpaceGet"));
			arrmsg.add(new String(parkName));
			arrmsg.add(new String("Get"));
			
			//////////OPEN///////////////
			//ClientUI.chat.accept(arrmsg);
			
			//////////CHECK///////////
			ChatClient.dataFromServer = new ArrayList<String>();
			ChatClient.dataFromServer.add(new String("15"));
			ChatClient.dataFromServer.add(new String("5"));
			
			if (ChatClient.dataFromServer.equals(null))
				throw new NullPointerException("This park doesn't exists.");
			
			Integer spaceInPark = (Integer.parseInt(ChatClient.dataFromServer.get(0)) - Integer.parseInt(ChatClient.dataFromServer.get(1)));
			availableSpaceTxt.setText(Integer.toString(spaceInPark));
    	}catch (NullPointerException e) {
    		this.promptTxt.setText(e.getMessage());
    	}catch (Exception e) {
			System.out.println("Error in DepartmentManagerController: pressRefreshbtn");
			System.out.println(e.getMessage());
    }
   }

}


