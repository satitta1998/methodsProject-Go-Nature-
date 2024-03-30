package gui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import client.ChatClient;
import client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CancellationReportController{
    @FXML
    private CategoryAxis X;

    @FXML
    private NumberAxis Y;

    @FXML
    private Button btnBack;

    @FXML
    private BarChart<String, Integer> chart;

    @FXML
    private Text txtError;

    @FXML
    private Text txtAvFully;

    @FXML
    private Text txtAvNotFully;
    
    private ArrayList<String> dataForReport;
    //data report = {0 - parkName\"All parks", 1 - day_from, 2 - month_from, 3 - year_from,
    //								4 - day_to, 5 - month_to, 6 - year_to}
    
    //load report data
    public void loadData(ArrayList<String> dataForReport) {
    	try {
        	this.dataForReport = dataForReport;
			//calculate the difference between the dates
        	if ( (  Integer.parseInt(dataForReport.get(2)) >= 1) && ( Integer.parseInt(dataForReport.get(2)) <= 9) )
        		dataForReport.set(2, "0"+dataForReport.get(2));
        	
        	if ( (  Integer.parseInt(dataForReport.get(5)) >= 1) && ( Integer.parseInt(dataForReport.get(5)) <= 9) )
        		dataForReport.set(5, "0"+dataForReport.get(5));
        	
        	if ( (  Integer.parseInt(dataForReport.get(1)) >= 1) && ( Integer.parseInt(dataForReport.get(1)) <= 9) )
        		dataForReport.set(1, "0"+dataForReport.get(1));
        	
        	if ( (  Integer.parseInt(dataForReport.get(4)) >= 1) && ( Integer.parseInt(dataForReport.get(4)) <= 9) )
        		dataForReport.set(4, "0"+dataForReport.get(4));

	        String date1String = new String(dataForReport.get(3)+"-"+dataForReport.get(2)+"-"+dataForReport.get(1)); // First date in YYYY-MM-DD format
	        String date2String = new String(dataForReport.get(6)+"-"+dataForReport.get(5)+"-"+dataForReport.get(4)); // Second date in YYYY-MM-DD format
	        //LocalDate date1 = LocalDate.parse(date1String);
	        //LocalDate date2 = LocalDate.parse(date2String);
	        
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	        LocalDate date1 = LocalDate.parse(date1String, formatter);
	        LocalDate date2 = LocalDate.parse(date2String, formatter);
	        long daysBetween = ChronoUnit.DAYS.between(date1, date2)+1;
	        
			//create arrays for data and set 0
			Integer avarageCancelled = 0;
			Integer avarageNotFullyCancelled = 0;
			Integer[] amountCancelled = new Integer[(int)daysBetween];
			Integer[] amountNotFullyCancelled = new Integer[(int)daysBetween];
			Integer totalAvarageCancelled = 0;
			Integer totalAvarageNotFullyCancelled = 0;
        	
    		if(dataForReport.get(0).equals("All parks")) {
    			//get the list of all parks
    			ArrayList<Object> arrmsg = new ArrayList<Object>();
    			arrmsg.add(new String("ParksListGet "));
    			arrmsg.add(new String("Go"));
    			arrmsg.add(new String("Go"));
    			ClientUI.chat.accept(arrmsg);
    			
    			if (ChatClient.dataFromServer.get(0).equals("null"))
    				throw new NullPointerException("The parks list doesn't exists.");
    			ArrayList<String> allParksArray = new ArrayList<>();
    			allParksArray.addAll(ChatClient.dataFromServer);
    			
    			for (int i = 0; i < amountCancelled.length; i++)
    			    amountCancelled[i] = 0;
    			for (int i = 0; i < amountNotFullyCancelled.length; i++)
    			    amountNotFullyCancelled[i] = 0;
    			
    			for(int i=0; i < allParksArray.size(); i++) {
    				
            		ArrayList<String> dataRep = new ArrayList<>();
            		dataRep.add(allParksArray.get(i)); //park name
            		dataRep.add(dataForReport.get(1)); //day_from
            		dataRep.add(dataForReport.get(2)); //month_from
            		dataRep.add(dataForReport.get(3)); //year_from
            		
            		dataRep.add(dataForReport.get(4)); //day_to
            		dataRep.add(dataForReport.get(5)); //month_to
            		dataRep.add(dataForReport.get(6)); //year_to
    				
        			arrmsg = new ArrayList<Object>();
        			arrmsg.add(new String("ShowCancellationReport"));
        			arrmsg.add(new String("ArrayList<String>"));
        			arrmsg.add(dataRep);
        			ClientUI.chat.accept(arrmsg);
        			
        			if(ChatClient.dataFromServer.get(0).equals("null"))
        				throw new NullPointerException("No report data");
        			
        			avarageCancelled += ChatClient.intDataFromServer.get(0).get(0);
        			avarageNotFullyCancelled += ChatClient.intDataFromServer.get(0).get(1);
        			
        			for(int j=0; j < daysBetween; j++) {
        				amountCancelled[j] += ChatClient.intDataFromServer.get(j+1).get(0);
        				amountNotFullyCancelled[j] += ChatClient.intDataFromServer.get(j+1).get(1);
        			}
    			}
    			totalAvarageCancelled = avarageCancelled/allParksArray.size();
    			totalAvarageNotFullyCancelled = avarageNotFullyCancelled/allParksArray.size();
    		}else {
        		ArrayList<String> dataRep = new ArrayList<>();
        		dataRep.add(dataForReport.get(0)); //park name
        		dataRep.add(dataForReport.get(1)); //day_from
        		dataRep.add(dataForReport.get(2)); //month_from
        		dataRep.add(dataForReport.get(3)); //year_from
        		
        		dataRep.add(dataForReport.get(4)); //day_to
        		dataRep.add(dataForReport.get(5)); //month_to
        		dataRep.add(dataForReport.get(6)); //year_to
        		
    			ArrayList<Object> arrmsg = new ArrayList<Object>();
    			arrmsg.add(new String("ShowCancellationReport"));
    			arrmsg.add(new String("ArrayList<String>"));
    			arrmsg.add(dataRep);
    			ClientUI.chat.accept(arrmsg);
    			
    			if(ChatClient.dataFromServer.get(0).equals("null"))
    				throw new NullPointerException("No report data");
    		}

	        //guided
	        XYChart.Series<String, Integer> dataSeries1 = new XYChart.Series<String, Integer>();
	        for(int i = 1; i <= daysBetween; i++) {
	        	dataSeries1.setName("cancelled");
	        	
	        	 // Iterate over the dates
	        	LocalDate currentDate = date1;
	        	int j = 0;
	        	while (!currentDate.isAfter(date2)) {
	        		if (dataForReport.get(0).equals("All parks"))
	        			dataSeries1.getData().add(new XYChart.Data<>(currentDate.toString(), amountCancelled[j]));
	        		else
	        			dataSeries1.getData().add(new XYChart.Data<>(currentDate.toString(), ChatClient.intDataFromServer.get(i).get(0)));
	        		currentDate = currentDate.plusDays(1); // Move to the next day
	        		j++;
	        	}
	        }
	        
	        
	        //single
	        XYChart.Series<String, Integer> dataSeries2 = new Series<String, Integer>();
	        for(int i = 1; i <= daysBetween; i++) {
	        	//single
	        	dataSeries2.setName("not fully cancelled");
	        	
	        	 // Iterate over the dates
	        	LocalDate currentDate = date1;
	        	int j=0;
	        	while (!currentDate.isAfter(date2)) {
	        		if (dataForReport.get(0).equals("All parks"))
	        			dataSeries2.getData().add(new XYChart.Data<>(currentDate.toString(), amountNotFullyCancelled[j]));
	        		else
	        			dataSeries2.getData().add(new XYChart.Data<>(currentDate.toString(), ChatClient.intDataFromServer.get(i).get(1)));
	        		currentDate = currentDate.plusDays(1); // Move to the next day
	        		j++;
	        	}
	        }
	        
	        //set the average to the label
	        if (dataForReport.get(0).equals("All parks")) {
		        this.txtAvFully.setText(totalAvarageCancelled.toString());
		        this.txtAvNotFully.setText(totalAvarageNotFullyCancelled.toString());
	        }else {
		        this.txtAvFully.setText(ChatClient.intDataFromServer.get(0).get(0).toString());
		        this.txtAvNotFully.setText(ChatClient.intDataFromServer.get(0).get(1).toString());
	        }
  		
			chart.getData().addAll(dataSeries1, dataSeries2);
    		
    	}catch(NullPointerException e) {
    		this.txtError.setText(e.getMessage());
    	}catch(Exception e) {
    		System.out.println("Error in CancellationReportController: loadData");
    		System.out.println(e.getMessage());
    	}

    }
  

    //Event for "Back" button
    @FXML
    void pressBack(ActionEvent event) {
    	try {
    		Button btn = (Button) event.getSource();
    		Stage stage = (Stage) btn.getScene().getWindow();
    		stage.close();
    	}catch (Exception e) {
    		System.out.println("Error in CancellationReportController: pressBack");
    		System.out.println(e.getMessage());
    	}
    }
    
    
}
