package gui;

import server.ServerUI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import GoNatureServer.GoNatureServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

/**
 * The ServerPortFrameController class controls the behavior of the server port frame in the GUI.
 */
public class ServerPortFrameController {

	public enum Columns {
		IP_ADDRESS, HOST_NAME, STATUS
	}

	// button
	@FXML
	private Button btnClose;
	@FXML
	private Button btnGetConUsers;

	// table
	@FXML
	private TableView<ConnectionData> tblConStatus;
	@FXML
	private TableColumn<ConnectionData, String> colIPAddress;
	@FXML
	private TableColumn<ConnectionData, String> colHostName;
	@FXML
	private TableColumn<ConnectionData, String> colStatus;
	@FXML
	private Text Ip_text;
	
	private ServerPortFrameController me;
	

	private static ArrayList<ConnectionToClient> client_conn_data = new ArrayList<>();

    /**
     * Loads table data into the table view.
     *
     * @param connectionData The list of connection data to be loaded into the table.
     */
	public void loadTableData(ObservableList<ConnectionData> connectionData) {
		try {
			colIPAddress.setCellValueFactory(cellData -> cellData.getValue().ipAddressProperty());
			colHostName.setCellValueFactory(cellData -> cellData.getValue().hostNameProperty());
			colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
			tblConStatus.setItems(connectionData);

		} catch (Exception e) {
			System.out.println("Error in ServerPortFrameController: loadTableData");
			System.out.println(e.getMessage());
		}
	}


    /**
     * Handles the action event when the "Close Server" button is clicked.
     * It exits the server frame.
     *
     * @param event The action event triggered by clicking the "Close Server" button.
     */
	// Event for "Close Server" button
	public void pressCloseBtn(ActionEvent event) {
		System.out.println("Exit server frame");
		System.exit(0);
	}

    /**
     * Handles the action event when the "Get connected users" button is clicked.
     * It updates the table to display the connected users.
     *
     * @param event The action event triggered by clicking the "Get connected users" button.
     */
	// Event for "Get connected users" button
	public void pressGetConUsersBtn(ActionEvent event) {
		try {
			updateTable();
		} catch (Exception e) {
			System.out.println("Error in ServerPortFrameController: pressGetConUsersBtn");
			System.out.println(e.getMessage());
		}

	}
	// End function
	
    /**
     * Adds a client connection to the list of connected clients and updates the table.
     *
     * @param client The client connection to be added.
     */
	public void addClient(ConnectionToClient client) {
		client_conn_data.add(client);
		updateTable();
	}
	
    /**
     * Removes a client connection from the list of connected clients and updates the table.
     *
     * @param client The client connection to be removed.
     */
	public void removeClient (ConnectionToClient client) {
		client_conn_data.remove(client);
		updateTable();
	}

    /**
     * Updates the table to display the connected users.
     */
	public void updateTable() {
		String ipAddress;
		String hostName;
		String connectionStatus;
		
		ObservableList<ConnectionData> connectionData = FXCollections.observableArrayList();
		if (!client_conn_data.isEmpty()) {
			for (ConnectionToClient c : client_conn_data) {
				if (c.isAlive()) {
					ipAddress = new String(c.getInetAddress().getHostAddress().toString());
					hostName = new String(c.getInetAddress().getHostName().toString());
					connectionStatus = new String(String.valueOf(c.isAlive()));

					System.out.println("Client ip: " + c.getInetAddress().getHostAddress().toString());
					System.out.println("Client host name: " + c.getInetAddress().getHostName().toString());
					System.out.println("Client stats: " + String.valueOf(c.isAlive()));

					connectionData.add(new ConnectionData(ipAddress, hostName, connectionStatus));
				} else {
					System.out.println("c is dead");
				}
			}
		}
		this.loadTableData(connectionData);
	}

    /**
     * The ConnectionData class represents the data of a connected client.
     */
	// private class for connected clients data
	private class ConnectionData {
		private SimpleStringProperty ipAddress;
		private SimpleStringProperty hostName;
		private SimpleStringProperty status;

        /**
         * Constructs a ConnectionData object with the specified IP address, host name, and connection status.
         *
         * @param ipAddress      The IP address of the connected client.
         * @param hostName       The host name of the connected client.
         * @param connectionStatus The connection status of the connected client.
         */
		public ConnectionData(String ipAddress, String hostName, String status) {
			this.ipAddress = new SimpleStringProperty(ipAddress);
			this.hostName = new SimpleStringProperty(hostName);
			this.status = new SimpleStringProperty(status);
		}

        /**
         * Gets the IP address property.
         *
         * @return The IP address property.
         */
		public SimpleStringProperty ipAddressProperty() {
			return ipAddress;
		}

        /**
         * Gets the host name property.
         *
         * @return The host name property.
         */
		public SimpleStringProperty hostNameProperty() {
			return hostName;
		}

        /**
         * Gets the status property.
         *
         * @return The status property.
         */
		public SimpleStringProperty statusProperty() {
			return status;
		}
	}
	
    /**
     * Sets the IP address in the text field.
     */
	public void setIp() {
		try {
			Ip_text.setText(String.valueOf(InetAddress.getLocalHost()).split("/")[1]);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}
	


	
}// END CLASS
