package GoNatureServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gui.ServerPortFrameController;
import import_simulator.ImportSimulator;
import jdbc.MysqlConnection;
import ocsf.server.*;

public class GoNatureServer extends AbstractServer {

	// MySql connection for that server
	private Connection db_con;

	/** Used for listing connected clients */
	public static ServerPortFrameController controller;

	// Used for defining format pattern for DateTime objects
	private DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	// Used for tracking logged in clients
	private Map<String, ConnectionToClient> logged_in_clients = new HashMap<>();

	// Used for saving discounts - discounts might change in the future.
	private Map<String, Integer> discounts = new LinkedHashMap<>();

	/** Used for tracking the relevant client UI which connected to the server */
	public Map<String, ConnectionToClient> clients_with_orders = new HashMap<>();

	/**
	 * Constructs a new GoNatureServer with the specified port.
	 * 
	 * @param port the port number for the server
	 */
	public GoNatureServer(int port) {
		super(port);
	}

	/**
	 * Sends a response to the specified client.
	 * 
	 * @param client       the client(ConnectionToClient) to send the response to
	 * @param endpoint     the endpoint(String) for the response
	 * @param payload_type the type of payload(String)
	 * @param payload      the payload(Object) to send
	 */
	public void send_response(ConnectionToClient client, String endpoint, String payload_type, Object payload) {
		ArrayList<Object> response_arrlst;
		try {
			response_arrlst = new ArrayList<>();
			response_arrlst.add(endpoint); // Add Endpoint
			response_arrlst.add(payload_type); // Add Payload-type
			response_arrlst.add(payload); // Add Payload
			client.sendToClient(response_arrlst);
			return;
		} catch (IOException e) {
			System.out.println("[send_response|ERROR]:Failed to inform the client: " + client.getInetAddress()
					+ " server failed endpoint: " + endpoint);
			e.printStackTrace();
		}
	}

	/**
	 * Serving the client request based on the EndPoint EndPoints: USER LOGIN :
	 * Handles user login functionality, verifying credentials against a database
	 * and responding with user type and park name upon successful login. IsLoggedIn
	 * : Determines whether a specified user is currently logged in and responds
	 * with a boolean value indicating the login status. UserLogOut: Logs out a
	 * specified user and updates the login status accordingly. OrderCreate: Creates
	 * a new order based on the provided information and stores it in the database.
	 * OrderUpdate: Updates an existing order with new information and stores the
	 * changes in the database. OrderCancel: Cancels a specified order and updates
	 * its status in the database accordingly. OrderGet: Retrieves the details of a
	 * specific order from the database and sends them to the client. ParksListGet:
	 * Retrieves a list of park names from the database and sends them to the
	 * client. GroupGuideCheck: Checks whether a specified visitor is registered as
	 * a group guide and returns a boolean value indicating the result.
	 * OrderedEnter: Processes the entrance of a visitor into a park, updates
	 * relevant database tables, and returns a boolean value indicating the success
	 * of the operation. UnplannedEnter: Updates the current number of visitors in a
	 * park when an unplanned entrance occurs, also inserts the visit into the
	 * database. ExitRegistration: Registers the exit of visitors from the park,
	 * updating the current number of visitors and setting the exit time in the
	 * database. GuideRegistration: Registers a guide in the database if not already
	 * registered. GetPrices: Retrieves the current prices or discounts from the
	 * database and sends them to the client. EnterWaitList: Inserts an order into
	 * the waitlist table in the database for visitors who couldn't enter the park
	 * immediately. getPaidInAdvance: Checks if an order has been paid for in
	 * advance. setPaidInAdvance: Updates the payment status of an order in the
	 * database to indicate payment in advance. ParkCurrentParamsGet: Retrieves the
	 * current parameters (capacity, time constraints) of a park from the database.
	 * ParkCurrentParamsUpdate: Updates the current parameters of a park in the
	 * database. ParkNewParamsGet: Retrieves the new parameters requested for a park
	 * from the database. ParkNewParamsUpdate: Updates the new parameters of a park
	 * in the database. ParkCheckIfApproveRequired: Checks if there are any new
	 * parameter requests pending approval for parks. AvilableSpaceGet: Retrieves
	 * the available space (current visitors and capacity) in a park from the
	 * database.
	 * CreateVisitorsNumReport: Generates a report of the number of visitors, distinguishing between group and non-group visits, for a specified park within the current month.
     * GetVisitorsNumReport: Retrieves the number of visitors report for a specific park, month, and year, distinguishing between group and non-group visits.
     * CreateDatesUsageReport: Triggers the creation of a usage report for a park based on dates.
     * GetDatesUsageReport: Retrieves the dates usage report for a specific park, month, and year.
     * CreateVisitsReport: Initiates the creation of a report for visits to a park on a specified day, month, and year.
     * ShowVisitsReport: Displays the visits report for a park on a particular day, month, and year.
     * ShowCancellationReport: Generates a report on cancellation and missed orders for a park within a specified date range.
     * OrderApprove: Handles the approval of an order by updating the database with the visitor's confirmation.
	 * 
	 * @param msg    - message(Object) sent from client: Must be of a specific form:
	 *               {"EndPoint","EndPointType","payload"}
	 * @param client - (ConnectionToClient) information of the client requesting
	 *               serve.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {

		// Message Variables
		String endpoint;
		String payload_type;
		ArrayList<Object> arr_msg;
		ArrayList<String> arr = new ArrayList<String>();

		// Convert to ArrayList test
		try {
			arr_msg = (ArrayList<Object>) msg;
			System.out.println("[handleMessageFromClient | DEBUG]: converted msg to arr list");
		} catch (ClassCastException e_clas) {
			// controller.removeClient(client);
			System.out.println(
					"[handleMessageFromClient | ERROR]: msg from client is not ArrayList<Object> removing client from list");
			// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		////////////// Deal With Client Message ////////////////
		endpoint = (String) arr_msg.get(0);
		payload_type = (String) arr_msg.get(1);
		String db_table = ""; // Table name to be used
		PreparedStatement prepared_statement = null;
		PreparedStatement ps = null;
		ResultSet result_set = null;

		// choose End-point
		switch (endpoint) {

		// -------------------------------------------------------------------------------------
		/** Used for listing connected clients */
		case "ConnectToServer":
			System.out.println("[ConnectToServer|INFO]: ConnectToServer enpoint trigered");
			send_response(client, new String("ConnectToServer"), new String("Boolean"), new Boolean(true));
			break;

		// -------------------------------------------------------------------------------------
		// * USER LOGIN : Handles user login functionality, verifying credentials
		// against a database and responding with user type and park name upon
		// successful login. */
		case "UserLogin":
			System.out.println("[UserLogin|INFO]: UserLogin enpoint trigered");
			String user_type_from_db = "null";
			String parkName_from_db = "null";
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;

			db_table = "users";

			try {
				ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);

				// TODO ORENB: delete logging user data
				String username_from_client = payload.get(0);
				System.out.println(
						"[UserLogin|DEBUG]: extracted username: " + username_from_client + " from clients payload");
				String password_from_client = payload.get(1);
				System.out.println(
						"[UserLogin|DEBUG]: extracted password: " + password_from_client + " from clients payload");

				// prepare MySQL query prepare
				prepared_statement = db_con.prepareStatement(
						"SELECT type,parkName FROM " + db_table + " WHERE username=? AND password=?;");
				prepared_statement.setString(1, username_from_client);
				prepared_statement.setString(2, password_from_client);
				result_set = prepared_statement.executeQuery();

				// Check MySql Result
				if (!result_set.next()) { // ResultSet is empty
					System.out.println("[loginUser|INFO]: ResultSet is empty - didnt not find the username: "
							+ username_from_client);

				} else {
					user_type_from_db = result_set.getString("type");
					parkName_from_db = result_set.getString("parkName");
					System.out.println("[loginUser|INFO]:ResultSet is not empty " + username_from_client
							+ " was found returning to client: " + user_type_from_db + " and park name: "
							+ parkName_from_db);

					/// LOGIN and already logged in test
					if (logged_in_clients.get(username_from_client) == null) {
						logged_in_clients.put(username_from_client, client);
						System.out.println("[UserLogin | info]: logged-in: " + username_from_client);

					} else {
						System.out.println("[UserLogin | ERROR]: User alrady logged-in");

						send_response(client, new String("UserLogin"), new String("ErrorString"),
								new String("Client already logged-in"));

					}
				}

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[UserLogin | ERROR]: Client sent payload for UserLogin ep which is not an ArrayList<String>");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Response to client

			send_response(client, new String("UserLogin"), new String("ArrayList<String>"),
					new ArrayList<String>(Arrays.asList(user_type_from_db, parkName_from_db)));

			break;

		// -------------------------------------------------------------------------------------

		/*
		 * Determines whether a specified user is currently logged in and responds with
		 * a boolean value indicating the login status.
		 */
		case "IsLoggedIn":

			System.out.println("[IsLoggedIn|INFO]: IsLoggedIn enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;

			boolean is_logged_in = false;

			db_table = "users";

			try {
				String payload = (String) arr_msg.get(2); // payload is the username
				for (String username : logged_in_clients.keySet()) {
					if (username.equals(payload)) {
						is_logged_in = true;
						System.out.println("[IsLoggedIn|INFO]:" + payload + " is logged in");
						break;
					}
				}

				// Response to client

				send_response(client, new String("IsLoggedIn"), new String("Boolean"), is_logged_in);

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[IsLoggedIn_ep | ERROR]: Client sent payload for IsLoggedIn_ep ep which is not an String");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
			} catch (Exception e) {
				e.printStackTrace();

			}
			break;

		// -------------------------------------------------------------------------------------

		/**
		 * UserLogOut: Logs out a specified user and updates the login status
		 * accordingly.
		 */
		case "UserLogOut":

			System.out.println("[UserLogOut|INFO]: UserLogOut enpoint trigered");

			if (!checkType(client, payload_type, "String", endpoint)) {
				return;
			}

			boolean is_logged_out = false;
			is_logged_in = false;

			db_table = "users";

			try {
				String payload = (String) arr_msg.get(2); // payload is the username

				// Test if logged in
				boolean client_send_logged_off_user = false;
				if (logged_in_clients.get(payload) == null) {
					System.out.println("[UserLogOut|ERROR]:" + payload + " is not even logged in....");
					client_send_logged_off_user = true;
				}

				for (String username : logged_in_clients.keySet()) {
					if (username.equals(payload)) {
						is_logged_in = true;
						System.out.println("[UserLogOut|INFO]:" + payload + " is logged in");

						break;
					}
				}

				// Log user out
				logged_in_clients.remove(payload);
				if (logged_in_clients.get(payload) == null && !client_send_logged_off_user) {
					System.out.println("[UserLogOut|DEBUG]:" + payload + " was removed from map");
					is_logged_out = true;
				}

				// Response to client

				send_response(client, new String("UserLogOut"), new String("Boolean"), is_logged_out);
				return;

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[UserLogOut_ep | ERROR]: Client sent payload for UserLogOut_ep ep which is not an String");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// -------------------------------------------------------------------------------------

			/**
			 * OrderCreate: Creates a new order based on the provided information and stores
			 * it in the database.
			 */
		case "OrderCreate":
			int orderId = -1;
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;

			db_table = "orders";
			try {
				// Extract pay-load
				ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);
				String visitor_id = payload.get(0);
				String parkName = payload.get(1);
				String time_of_visit = payload.get(2);
				String visitor_number = payload.get(3);
				String visitor_email = payload.get(4);
				String visitor_phone = payload.get(5);
				System.out.println("[OrderCreate | DEBUG]: extracted: " + payload);

				//
				if (checkOrderTime(payload)) {
					// prepare MySQL query prepare
					PreparedStatement preparedStatement = db_con.prepareStatement("INSERT INTO " + db_table
							+ " (`visitor_id`, `parkName`, `time_of_visit`, `visitor_number`, `visitor_email`, `visitor_phone`, `status`, `paid`, `reminderMsgSend`, `visitorConfirmedOrder`)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					preparedStatement.setString(1, visitor_id);
					preparedStatement.setString(2, parkName);
					preparedStatement.setString(3, time_of_visit);
					preparedStatement.setString(4, visitor_number);
					preparedStatement.setString(5, visitor_email);
					preparedStatement.setString(6, visitor_phone);
					preparedStatement.setString(7, "Active"); // Status should be 'Active'
					preparedStatement.setBoolean(8, false); // paid
					preparedStatement.setBoolean(9, false); // reminderMsgSend
					preparedStatement.setBoolean(10, false); // visitorConfirmedOrder
					preparedStatement.executeUpdate();
					// -------------------------------------------------

					prepared_statement = null;
					prepared_statement = db_con.prepareStatement("SELECT MAX(orderId) AS max FROM orders");
					result_set = prepared_statement.executeQuery();
					if (!result_set.next()) {
						System.out.println("[OrderCreate | ERROR]: couldnt get max id!");
						send_response(client, new String("OrderCreate"), new String("ErrorString"),
								new String("couldnt get max from DB"));
					}
					orderId = result_set.getInt("max");

					/// OREN SMS DEVELOPING:
					clients_with_orders.put(String.valueOf(orderId), client);
					System.out.println("[OrderCreate | INFO]: client:" + client
							+ " was added to clients_with_orders map with ORDERID: " + orderId);

					////////////////
				}

				// Catch Problems
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[UserLogin | ERROR]: Client sent payload for UserLogin ep which is not an ArrayList<String>");

				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
			} catch (SQLException e_sql) {
				System.out.println("[OrderCreate | ERROR]: MySQL query execution error");
				e_sql.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();

			}

			// Response to client

			send_response(client, new String("OrderCreate"), new String("Integer"), new Integer(orderId));

			break;

		// -------------------------------------------------------------------------------------
		/**
		 * OrderUpdate: Updates an existing order with new information and stores the
		 * changes in the database.
		 */
		case "OrderUpdate":
			boolean orderUpdated = false;
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;

			db_table = "orders";
			try {
				// Extract pay-load (Pay attention order_id as oppose to Order Create with
				// visitor_id)
				ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);
				String order_id = payload.get(0);
				String visitor_id = payload.get(1);
				String parkName = payload.get(2);
				String time_of_visit = payload.get(3);
				String visitor_number = payload.get(4);
				String visitor_email = payload.get(5);
				String visitor_phone = payload.get(6);
				System.out.println("[OrderUpdate | DEBUG]: extracted: " + payload);

				// Check if order exists
				prepared_statement = db_con.prepareStatement("SELECT * FROM " + db_table + " WHERE orderId=?;");
				prepared_statement.setString(1, order_id);
				result_set = prepared_statement.executeQuery();

				// Check MySql Result for orderId Client sent
				if (!result_set.next()) { // ResultSet is empty
					System.out.println(
							"[OrderUpdate|INFO]: ResultSet is empty - didnt not find the orderId: " + order_id);
					// Response to client

					send_response(client, new String("OrderUpdate"), new String("ErrorString"),
							new String("order_id not in db: " + order_id));

					return;
				}
				System.out.println("[OrderUpdate|INFO]:ResultSet is not empty " + order_id + " was found");

				// Update payload since it has order_id at index 0
				System.out.println("[OrderUpdate |DEBUG ]: payload before update: " + payload);
				payload.remove(0);
				System.out.println("[OrderUpdate |DEBUG ]: payload updated: " + payload);

				//// CHECK IF USER ALLOWED TO UPDATE WITHPUT checkOrderTime test
				Boolean coreElementsOfOrderChanged = true;
				if (result_set.getString("time_of_visit").equals(time_of_visit)
						&& result_set.getString("visitor_number").equals(visitor_number)
						&& result_set.getString("parkName").equals(parkName)) {
					coreElementsOfOrderChanged = false;
				}

				if (!coreElementsOfOrderChanged || checkOrderTime(payload)) { // TODO ORENB: Here will be algorithm for
																				// problematic time instead of
					// true
					// prepare MySQL query prepare
					prepared_statement = db_con.prepareStatement("UPDATE " + db_table
							+ " SET `visitor_id`=?, `parkName`=?, `time_of_visit`=?, `visitor_number`=?, `visitor_email`=?, `visitor_phone`=?"
							+ " WHERE `orderId`=?;");

					prepared_statement.setString(1, visitor_id);
					prepared_statement.setString(2, parkName);
					prepared_statement.setString(3, time_of_visit);
					prepared_statement.setString(4, visitor_number);
					prepared_statement.setString(5, visitor_email);
					prepared_statement.setString(6, visitor_phone);
					prepared_statement.setString(7, order_id);
					int rowsAffected = prepared_statement.executeUpdate();
					if (rowsAffected > 0) {
						System.out.println("[OrderUpdate|INFO] Update successful. " + rowsAffected + " rows updated.");
						orderUpdated = true;
					} else {
						System.out.println("[OrderUpdate|ERROR] No records were updated.");

						send_response(client, new String("OrderUpdate"), new String("ErrorString"),
								new String("Capacity check passed but db failed to udpate"));

						return;
					}
				}

				// Catch Problems
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[OrderUpdate | ERROR]: Client sent payload for OrderUpdate ep which is not an ArrayList<String>");

				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
			} catch (SQLException e_sql) {
				System.out.println("[OrderUpdate | ERROR]: MySQL query execution error");
				e_sql.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();

			}

			// Response to client

			send_response(client, new String("OrderUpdate"), new String("Boolean"), orderUpdated);

			break;

		// -------------------------------------------------------------------------------------

		/**
		 * OrderCancel: Cancels a specified order and updates its status in the database
		 * accordingly.
		 */
		case "OrderCancel":
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			boolean cancel_order_test_succeeded = false;
			db_table = "orders";
			try {
				// Extract pay-load
				String payload = (String) arr_msg.get(2);
				System.out.println("[OrderCancel | DEBUG]: extracted: " + payload);

				cancel_order_test_succeeded = cancelOrder(Integer.valueOf(payload));

				// Response to client
				send_response(client, new String("OrderCancel"), new String("Boolean"),
						new Boolean(cancel_order_test_succeeded));
			}
			// Catch Problems
			catch (ClassCastException e_clas) {
				System.out.println(
						"[UserLogin | ERROR]: Client sent payload for UserLogin ep which is not an ArrayList<String>");

				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
			} catch (SQLException e_sql) {
				System.out.println("[OrderCreate | ERROR]: MySQL query execution error");
				e_sql.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();

			}

			break;
		// -------------------------------------------------------------------------------------
		/**
		 * OrderGet: Retrieves the details of a specific order from the database and
		 * sends them to the client.
		 */
		case "OrderGet":
			System.out.println("[OrderGet|INFO]: OrderGet enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;

			try {
				ps = db_con.prepareStatement("SELECT * FROM orders WHERE orderId = ?");
				ps.setString(1, (String) arr_msg.get(2));
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) { // order not found in DB
					System.out.println(
							"[OrderGet | ERROR]: order_id not found in DB, sending order not found response to client");
					send_response(client, new String("OrderGet"), new String("String"), new String("null"));
					return;
				}
				// add order parameters to ArrayList to send to client
				arr.add(new String(String.valueOf(rs.getInt("orderId"))));
				arr.add(new String(rs.getString("parkName")));
				arr.add(new String(rs.getString("time_of_visit")));
				arr.add(new String("" + rs.getInt("visitor_number")));
				arr.add(new String(rs.getString("visitor_email")));
				arr.add(new String(rs.getString("visitor_phone")));
				arr.add(new String(rs.getString("visitor_id")));
				// send Order to Client
				send_response(client, new String("OrderGet"), new String("ArrayList<String>"), arr);
				System.out.println("[OrderGet|INFO]: OrderGet sent response of ArrayList<>");
				System.out.println(arr);
			} catch (SQLException e) {
				System.out.println("[OrderGet | ERROR]: SQLException was thrown!");
				e.printStackTrace();
			}
			break;

		// -------------------------------------------------------------------------------------
		/**
		 * ParksListGet: Retrieves a list of park names from the database and sends them
		 * to the client.
		 */
		case "ParksListGet":
			try {
				ps = db_con.prepareStatement("Select parkName FROM parks");
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					arr.add(rs.getString("parkName"));
				}
				if (arr.isEmpty()) {
					// no Parks???
				}
				send_response(client, new String("ParksListGet"), new String("ArrayList<String>"), arr);
			} catch (SQLException e) {
				System.out.println("[ParksListGet | ERROR]: SQLException was thrown!");
				e.printStackTrace();
			}

			break;

		// -------------------------------------------------------------------------------------
		/**
		 * GroupGuideCheck: Checks whether a specified visitor is registered as a group
		 * guide and returns a boolean value indicating the result.
		 */
		case "GroupGuideCheck":
			System.out.println("[GroupGuideCheck|INFO]: GroupGuideCheck enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			boolean guide_test_succeeded = false;
			db_table = "guides";
			try {
				String payload = (String) arr_msg.get(2);

				// prepare MySQL query prepare
				prepared_statement = db_con.prepareStatement("SELECT * FROM " + db_table + " WHERE visitor_id=?;");
				prepared_statement.setString(1, payload);
				result_set = prepared_statement.executeQuery();

				// Check MySql Result
				if (!result_set.next()) { // ResultSet is empty
					System.out.println(
							"[GroupGuideCheck|INFO]: ResultSet is empty - didnt not find the GroupGuide: " + payload);

				} else {
					System.out.println("[GroupGuideCheck|INFO]:ResultSet is not empty " + payload + " was found");
					guide_test_succeeded = true;
				}

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[GroupGuideCheck | ERROR]: Client sent payload for GroupGuideCheck ep which is not an String");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Response to client

			send_response(client, new String("GroupGuideCheck"), new String("Boolean"), guide_test_succeeded);

			break;

		// -------------------------------------------------------------------
		/**
		 * OrderedEnter: Processes the entrance of a visitor into a park, updates
		 * relevant database tables, and returns a boolean value indicating the success
		 * of the operation.
		 */
		case "OrderedEnter":
			System.out.println("[OrderedEnter|INFO]: OrderedEnter enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;

			boolean ordered_enterance_test_succeeded = false;
			db_table = "orders";
			try {
				String payload = (String) arr_msg.get(2);

				// Get the relevant order from DB
				prepared_statement = db_con.prepareStatement(
						"SELECT parkName, visitor_number, visitor_id, status FROM " + db_table + " WHERE orderId=?;");
				prepared_statement.setString(1, payload);
				result_set = prepared_statement.executeQuery();

				// Check MySql Result
				if (!result_set.next()) { // ResultSet is empty
					System.out.println(
							"[OrderedEnter|INFO]: ResultSet is empty - didnt not find the orderId: " + payload);

				} else {
					System.out.println("[OrderedEnter|INFO]:ResultSet is not empty " + payload + " was found");

					if (!result_set.getString("status").equals("Cancelled") && !result_set.getString("status").equals("Entered")) // OREN UPDATE - DO NOT ALLOW CANCALED
																				// ORDERS TO ENTER PARK
					{
						/// OREN UPDATE FOR CANCELLATION REPORT
						// change status of relevant order to 'Entered'
						PreparedStatement update_order_status_stmt = db_con
								.prepareStatement("UPDATE " + db_table + " SET status = 'Entered'  WHERE orderId=?;");
						update_order_status_stmt.setInt(1, Integer.parseInt(payload));
						int rowsAffected = update_order_status_stmt.executeUpdate();
						if (rowsAffected > 0) {
							System.out.println(
									"[OrderedEnter |INFO]: updated " + db_table + " , rows effected: " + rowsAffected);
						} else {
							System.out.println("[OrderedEnter|ERROR]:failed to updated " + db_table
									+ " , rows effected: " + rowsAffected);

							send_response(client, new String("OrderedEnter"), new String("ErrorString"),
									new String("Server was not able to update order  " + payload
											+ " to status - entered and aborted"));

							return;
						}
					} else { /// ALSO ADDED BY ORENB TO SUPPORT DENIYING ACCESS TO CANCELED ORDERS
						System.out.println("[OrderedEnter | ERROR ]:Order " + payload + " is Cancelled - no enterance");
						// Response to client

						send_response(client, new String("OrderedEnter"), new String("Boolean"), false);

						return;
					}

				}
				////////////////// OREN UPDATE FOR CANCELATION REPORT END

				String parkNameExtracted = result_set.getString(1);
				String visitor_number_extracted = result_set.getString(2);
				String visitor_id = result_set.getString("visitor_id");
				System.out.println("[OrderedEnter|DEBUG]:extacted park name: " + parkNameExtracted);
				db_table = "parks";

				// Update the relevant park with the entrance data
				PreparedStatement preparedStatement = db_con.prepareStatement(
						"UPDATE " + db_table + " SET currentVisitors = currentVisitors + ? WHERE parkName=?;");
				preparedStatement.setInt(1, Integer.parseInt(visitor_number_extracted));
				preparedStatement.setString(2, parkNameExtracted);
				int rowsAffected = preparedStatement.executeUpdate();
				if (rowsAffected > 0) {
					System.out.println("[OrderedEnter|INFO]: updated parks, rows effected: " + rowsAffected);
					ordered_enterance_test_succeeded = true;

				} else {
					System.out.println("[OrderedEnter|ERROR]:failed to update parks, rows effected: " + rowsAffected);
				}

				if (ordered_enterance_test_succeeded) {
					// insert into visits table (assuming no incorrect use by employees)

					ps = db_con.prepareStatement(
							"INSERT INTO visits (visitor_id, parkName, timeOfEntrence, numberOfVisitors, isGroup) VALUES (?, ?, ?, ?, ?)");
					ps.setString(1, visitor_id);
					ps.setString(2, parkNameExtracted);
					ps.setString(3, LocalDateTime.now().format(f));
					ps.setString(4, visitor_number_extracted);
					ps.setBoolean(5, isGroupOrder(payload));

					rowsAffected = ps.executeUpdate();

					if (rowsAffected != 1) {
						// row was not inserted
						System.out.println("[OrderedEnter | ERROR]: visit was not inserted into visits table");
						ordered_enterance_test_succeeded = false;
						// here will be a query to revert currentvisitors as entrance was not completed
						// correctly
					} else {
						System.out.println("[OrderedEnter | INFO]: visit inserted successfully into table");
					}
				}

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[OrderedEnter | ERROR]: Client sent payload for OrderedEnter ep which is not an String");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Response to client

			send_response(client, new String("OrderedEnter"), new String("Boolean"), ordered_enterance_test_succeeded);

			break;
		/**
		 * UnplannedEnter: Updates the current number of visitors in a park when an
		 * unplanned entrance occurs, also inserts the visit into the database.
		 */
		case "UnplannedEnter":
			System.out.println("[UnplannedEnter|INFO]: UnplannedEnter enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			boolean unplanned_enter_test_succeeded = false;
			db_table = "parks";
			try {
				ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);
				String parkName_extracted = payload.get(0);
				String unplanned_visitors = payload.get(1);
				String visitor_id = payload.get(2);
				System.out.println("[UnplannedEnter|DEBUG]: payload is: " + payload);

				//////// MAAYAN PROVIDED

				if (checkUnplannedVisit(Integer.valueOf(unplanned_visitors), parkName_extracted)) {
					// Update number of visitors in the relevant park
					// prepare MySQL query
					PreparedStatement preparedStatement = db_con.prepareStatement(
							"UPDATE " + db_table + " SET currentVisitors = currentVisitors + ? WHERE parkName=?;");
					preparedStatement.setInt(1, Integer.parseInt(unplanned_visitors));
					preparedStatement.setString(2, parkName_extracted);
					int rowsAffected = preparedStatement.executeUpdate();
					if (rowsAffected > 0) {
						System.out.println("[UnplannedEnter|INFO]: updated parks, rows effected: " + rowsAffected);
						System.out.println("number of visitors added is: " + unplanned_visitors);
						unplanned_enter_test_succeeded = true;

					} else {
						System.out.println(
								"[UnplannedEnter|ERROR]:failed to update parks, rows effected: " + rowsAffected);
					}
				}
				// unplanned_enter_test_succeeded variable remain false if too much visitors ask
				// to come in
				if (unplanned_enter_test_succeeded) {
					// insert into visits table!
					String statement = "INSERT INTO visits (visitor_id, parkName, timeOfEntrence, numberOfVisitors, isGroup) VALUES (?, ?, ?, ?, 0)";
					ps = null;
					ps = db_con.prepareStatement(statement);
					ps.setString(1, visitor_id);
					ps.setString(2, parkName_extracted);
					ps.setString(3, LocalDateTime.now().format(f));
					ps.setInt(4, Integer.valueOf(unplanned_visitors));

					int rowsAffected = ps.executeUpdate();
					if (rowsAffected != 1) {
						System.out.println("[UnplannedEnter | ERROR]: couldnt insert visit into table!");
						unplanned_enter_test_succeeded = false;
						// query to revert number of current visitors in park, as entrance was not
						// completed
					} else
						System.out.println("inserted row into table");

				}

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[UnplannedEnter | ERROR]: Client sent payload for UnplannedEnter ep which is not an ArrayList<String>");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Response to client

			send_response(client, new String("UnplannedEnter"), new String("Boolean"), unplanned_enter_test_succeeded);

			/////

			break;

// ---------------------------------------------------------------------------------
		/**
		 * ExitRegistration: Registers the exit of visitors from the park, updating the
		 * current number of visitors and setting the exit time in the database.
		 */
		case "ExitRegistration":
			System.out.println("[ExitRegistration|INFO]: ExitRegistration enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			boolean exit_registration_test_succeeded = false;
			db_table = "parks";
			try {
				ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);
				String parkName_extracted = payload.get(0);
//					String exiting_visitors_extracted = payload.get(1);
				String visitor_id = payload.get(1);
				// get amount of visitors
				ps = null;
				ps = db_con.prepareStatement(
						"SELECT numberOfVisitors FROM visits WHERE parkName = ? AND visitor_id = ? AND timeOfExit IS NULL");
				ps.setString(1, parkName_extracted);
				ps.setString(2, visitor_id);

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					String exiting_visitors_extracted = String.valueOf(rs.getInt("numberOfVisitors"));
//						System.out.println("number of visitors exiting park is " + exiting_visitors_extracted);
					// ORENB update - don't allow negative update
					PreparedStatement get_current_visitors = db_con
							.prepareStatement("SELECT currentVisitors FROM " + db_table + " WHERE parkName=?");
					get_current_visitors.setString(1, parkName_extracted);
					ResultSet get_current_visitors_rs = get_current_visitors.executeQuery();
					if (get_current_visitors_rs.next()) { // ORNEB - found parkName that was provided

						// Check if ("Current Amount" - "Existing Amount") is not negative
						int current_visitors_from_db = get_current_visitors_rs.getInt("currentVisitors");
						if (current_visitors_from_db - Integer.valueOf(exiting_visitors_extracted) >= 0) {
							// prepare MySQL query - update parks currentVisitors
							PreparedStatement preparedStatement = db_con.prepareStatement("UPDATE " + db_table
									+ " SET currentVisitors = currentVisitors - ? WHERE parkName=?;");
							preparedStatement.setInt(1, Integer.parseInt(exiting_visitors_extracted));
							preparedStatement.setString(2, parkName_extracted);
							int rowsAffected = preparedStatement.executeUpdate();
							if (rowsAffected > 0) {
								System.out.println(
										"[ExitRegistration|INFO]: updated parks, rows effected: " + rowsAffected);
								exit_registration_test_succeeded = true;

							} else {
								System.out.println("[ExitRegistration|ERROR]:failed to update parks, rows effected: "
										+ rowsAffected);
							}

							// get visit id using visitor id
							String statement = "UPDATE visits SET timeOfExit = ? WHERE visitor_id = ? AND timeOfExit IS NULL";
							ps = null;
							ps = db_con.prepareStatement(statement);
							ps.setString(1, LocalDateTime.now().format(f));
							ps.setString(2, visitor_id);

							rowsAffected = ps.executeUpdate();
							if (rowsAffected != 1) {
								System.out.println("didnt update visits table");
								exit_registration_test_succeeded = false;
								send_response(client, endpoint, new String("ErrorString"),
										new String("exit time was not updated for visit!"));
								// here is supposed to be a way to revert the current visitors in park, because
								// exit did not succeed
							} else {
								System.out.println("exit time updated successfuly");
								exit_registration_test_succeeded = exit_registration_test_succeeded && true;
							}
						}

					}
				}

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[ExitRegistration | ERROR]: Client sent payload for ExitRegistration ep which is not an ArrayList<String>");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Response to client

			send_response(client, new String("ExitRegistration"), new String("Boolean"),
					exit_registration_test_succeeded);

			break;
		// ---------------------------------------------------------------------------------

		/**
		 * GuideRegistration: Registers a guide in the database if not already
		 * registered.
		 */
		case "GuideRegistration":
			System.out.println("[GuideRegistration|INFO]: GuideRegistration enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			boolean guide_register_test_succeeded = false;
			db_table = "guides";
			try {
				ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);

				// prepare MySQL query prepare
				// INSERT INTO `go_nature`.`guides` (`visitor_id`) VALUES (<{visitor_id: }>);

				prepared_statement = db_con.prepareStatement("SELECT * FROM " + db_table + " WHERE visitor_id=?;");
				prepared_statement.setString(1, payload.get(0));
				result_set = prepared_statement.executeQuery();

				// Check MySql Result
				if (!result_set.next()) { // ResultSet is empty
					// prepare MySQL query prepare
					prepared_statement = db_con
							.prepareStatement("INSERT INTO " + db_table + " (`visitor_id`) VALUES (?);");
					prepared_statement.setString(1, payload.get(0));
					int rowsAffected = prepared_statement.executeUpdate();

					if (rowsAffected > 0) {
						System.out.println("[GuideRegistration|INFO]: updated guilds, rows effected: " + rowsAffected);
						guide_register_test_succeeded = true;

					} else {
						System.out.println(
								"[GuideRegistration|ERROR]:failed to update guides, rows effected: " + rowsAffected);
					}

				} else {
					System.out.println("[GuideRegistration|ERROR]: Guide to register is alrady registered: " + payload);
				}

				// Catch problematic Payload
			} catch (ClassCastException e_clas) {
				System.out.println(
						"[GuideRegistration | ERROR]: Client sent payload for GuideRegistration ep which is not an ArrayList<String>");
				// ORENB_TODO: send client an error that he sent bad msg (not arrlist)
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Response to client

			send_response(client, new String("GuideRegistration"), new String("Boolean"),
					guide_register_test_succeeded);

			break;
		// ---------------------------------------------------------------------------------

		/**
		 * GetPrices: Retrieves the current prices or discounts from the database and
		 * sends them to the client.
		 */
		case "GetPrices":
			System.out.println("[GetPrices|INFO]: GetPrices enpoint trigered");
			// Response to client

			ArrayList<String> discount_arr = new ArrayList<>();
			for (Integer discount : discounts.values()) {
				discount_arr.add(discount.toString());
			}
			send_response(client, new String("GetPrices"), new String("ArrayList<String>"), discount_arr);

			break;

		// -----------------------------------------------------------------
		/**
		 * EnterWaitList: Inserts an order into the waitlist table in the database for
		 * visitors who couldn't enter the park immediately.
		 */
		case "EnterWaitList":
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;

			// get payload
			arr = (ArrayList<String>) arr_msg.get(2);
			// insert order in payload as waitlist
			try {
				db_table = "Orders";

				/// UPDATE TO SUPPORT SMS:
				ps = db_con.prepareStatement("INSERT INTO " + db_table
						+ " (`visitor_id`, `parkName`, `time_of_visit`, `visitor_number`, `visitor_email`, `visitor_phone`, `status`, `paid`, `reminderMsgSend`, `visitorConfirmedOrder`)"
						+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				ps.setString(1, arr.get(0));
				ps.setString(2, arr.get(1));
				ps.setString(3, arr.get(2));
				ps.setInt(4, Integer.valueOf(arr.get(3)));
				ps.setString(5, arr.get(4));
				ps.setString(6, arr.get(5));
				ps.setString(7, "WaitList"); // Status should be 'Active'
				ps.setBoolean(8, false); // paid
				ps.setBoolean(9, false); // reminderMsgSend
				ps.setBoolean(10, false); // visitorConfirmedOrder

				if (ps.executeUpdate() != 1) {
					System.out.println("[EnterWaitList | ERROR]: couldnt insert order to orders table");
					send_response(client, endpoint, new String("ErrorString"),
							new String("couldnt insert order into orders table as waitList"));
				} else {
					ps = null;
					ps = db_con.prepareStatement(
							"SELECT MAX(orderId) AS max FROM orders WHERE visitor_id = ? AND parkName = ? AND time_of_visit = ? AND visitor_number = ? AND visitor_email = ? AND visitor_phone = ? AND status = 'WaitList'");
					for (int i = 0; i < arr.size(); i++)
						ps.setString(i + 1, arr.get(i));
					ResultSet rs = ps.executeQuery();
					if (!rs.next()) {
						// couldnt find max?
						System.out.println("couldnt find max in db");
						send_response(client, endpoint, new String("ErrorString"),
								new String("couldnt find order id?"));
					}
					String ID = String.valueOf(rs.getInt("max"));

					/// OREN SMS DEVELOPING:
					clients_with_orders.put(ID, client);
					System.out.println("[EnterWaitList | INFO]: client:" + client
							+ " was added to clients_with_orders map with ORDERID: " + ID);

					send_response(client, endpoint, "String", new String(ID));
					System.out.println("[EnterWaitList | INFO]: sent client answear of true");
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		// ---------------------------------------------------------------------------------

		/** getPaidInAdvance: Checks if an order has been paid for in advance. */
		case "getPaidInAdvance":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			boolean getPaidInAdvance_test_succeeded = false;
			db_table = "orders";

			try {
				String getPaidInAdvance_order_id_extracted = (String) arr_msg.get(2);
				ps = db_con.prepareStatement("SELECT paid FROM " + db_table + " WHERE orderId=?;");
				ps.setString(1, getPaidInAdvance_order_id_extracted);

				result_set = ps.executeQuery();
				if (!result_set.next()) { // ResultSet is empty
					System.out.println("[" + endpoint + "|INFO]: ResultSet is empty - didnt not find the orderId: "
							+ getPaidInAdvance_order_id_extracted);
				} else {
					getPaidInAdvance_test_succeeded = result_set.getBoolean("paid");
				}

				send_response(client, endpoint, new String("Boolean"), getPaidInAdvance_test_succeeded);

			} catch (SQLException e) {
				System.out.println("[" + endpoint + " | ERROR]: sql error");
				e.printStackTrace();

			} catch (ClassCastException e_clas) {
				System.out.println("[" + endpoint + " | ERROR]: Client sent payload for " + endpoint
						+ " ep which is not an String");

			} catch (Exception e) {
				e.printStackTrace();

			}

			break;

		// ---------------------------------------------------------------------------------

		/**
		 * setPaidInAdvance: Updates the payment status of an order in the database to
		 * indicate payment in advance.
		 */
		case "setPaidInAdvance":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;

			db_table = "orders";

			try {
				ArrayList<String> extracted_arrlist = (ArrayList<String>) arr_msg.get(2);
				ps = db_con.prepareStatement("UPDATE " + db_table + " SET `paid` = ? WHERE `orderId` = ?;");
				ps.setString(1, extracted_arrlist.get(1));
				ps.setString(2, extracted_arrlist.get(0));

				int rowsAffected = ps.executeUpdate();
				if (rowsAffected != 1) {
					System.out.println(
							"[" + endpoint + " | ERROR]: couldn't update order status in " + db_table + " table");
					send_response(client, endpoint, "ErrorString", "Couldn't update order status in " + db_table);
				} else {
					send_response(client, endpoint, "Boolean", Boolean.TRUE);
					System.out.println("[" + endpoint + " | INFO]: sent client answer of true");
					return;
				}

			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}

			break;
//------------------------------------------------ Mostly for the use of department manager
		/**
		 * ParkCurrentParamsGet: Retrieves the current parameters (capacity, time
		 * constraints) of a park from the database.
		 */
		case "ParkCurrentParamsGet":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			db_table = "parks";

			try {
				String extracted_parkName = (String) arr_msg.get(2);
				ps = db_con.prepareStatement("SELECT `currentVisitors`,`capacity`,`diff`,`visitTimeInMinutes` FROM "
						+ db_table + " WHERE `parkName` = ?;");
				ps.setString(1, extracted_parkName);
				result_set = ps.executeQuery();

				if (!result_set.next()) { // ResultSet is empty
					System.out.println("[" + endpoint + "|INFO]: ResultSet is empty - didnt not find the parkName: "
							+ extracted_parkName);

					send_response(client, endpoint, new String("ErrorString"),
							new String("result set empty in the DB"));

					return;
				} else {
					ArrayList<String> response_arr = new ArrayList<>(Arrays.asList(result_set.getString("capacity"),
							result_set.getString("diff"), result_set.getString("visitTimeInMinutes"),
							result_set.getString("currentVisitors")));

					send_response(client, endpoint, new String("ArrayList<String>"), response_arr);

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
			break;

		// ------------------------------------------------ // Should be used by
		/**
		 * ParkCurrentParamsUpdate: Updates the current parameters of a park in the
		 * database.
		 */
		case "ParkCurrentParamsUpdate":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			boolean ParkCurrentParamsUpdate = false;
			db_table = "parks";

			try {
				ArrayList<String> extracted_arrlist = (ArrayList<String>) arr_msg.get(2);
				ps = db_con.prepareStatement("UPDATE " + db_table
						+ " SET `capacity` = ?, `diff` = ?, `visitTimeInMinutes` = ?, `newCapacity` = ?, `newDiff` = ?, `newVisitTimeInMinutes` = ?WHERE `parkName` = ?;");
				ps.setString(1, extracted_arrlist.get(1));
				ps.setString(2, extracted_arrlist.get(2));
				ps.setString(3, extracted_arrlist.get(3));
				// Also update the newValues to show there is no more reqest
				ps.setString(4, extracted_arrlist.get(1));
				ps.setString(5, extracted_arrlist.get(2));
				ps.setString(6, extracted_arrlist.get(3));
				ps.setString(7, extracted_arrlist.get(0));

				int rowsAffected = ps.executeUpdate();
				if (rowsAffected != 1) {
					System.out.println("[" + endpoint + " | ERROR]: couldn't update " + db_table + " table");

				} else {
					System.out.println("[" + endpoint + " | INFO]: sent client answer of true");
					ParkCurrentParamsUpdate = true;
				}
				send_response(client, endpoint, "Boolean", ParkCurrentParamsUpdate);

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			break;

		// ----------------- // Should be used by department manager to get new values
		/**
		 * ParkNewParamsGet: Retrieves the new parameters requested for a park from the
		 * database.
		 */
		case "ParkNewParamsGet":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			db_table = "parks";
			try {
				String extracted_parkName = (String) arr_msg.get(2);
				ps = db_con.prepareStatement("SELECT `newCapacity`,`newDiff`,`newVisitTimeInMinutes` FROM " + db_table
						+ " WHERE `parkName` = ?;");
				ps.setString(1, extracted_parkName);
				result_set = ps.executeQuery();

				if (!result_set.next()) { // ResultSet is empty
					System.out.println("[" + endpoint + "|INFO]: ResultSet is empty - didnt not find the parkName: "
							+ extracted_parkName);

					send_response(client, endpoint, new String("ErrorString"),
							new String("result set empty in the DB"));

					return;
				} else {
					ArrayList<String> response_arr = new ArrayList<>(Arrays.asList(result_set.getString("newCapacity"),
							result_set.getString("newDiff"), result_set.getString("newVisitTimeInMinutes")));

					send_response(client, endpoint, new String("ArrayList<String>"), response_arr);
					return;

				}

			} catch (Exception e) {
				e.printStackTrace();

			}
			break;

		// -----------------------------------------
		/**
		 * ParkNewParamsUpdate: Updates the new parameters of a park in the database.
		 */
		case "ParkNewParamsUpdate":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			ParkCurrentParamsUpdate = false;
			db_table = "parks";
			try {
				ArrayList<String> extracted_arrlist = (ArrayList<String>) arr_msg.get(2);
				ps = db_con.prepareStatement("UPDATE " + db_table
						+ " SET  `newCapacity` = ?, `newDiff` = ?, `newVisitTimeInMinutes` = ? WHERE `parkName` = ?;");
				ps.setString(1, extracted_arrlist.get(1));
				ps.setString(2, extracted_arrlist.get(2));
				ps.setString(3, extracted_arrlist.get(3));
				ps.setString(4, extracted_arrlist.get(0));

				int rowsAffected = ps.executeUpdate();
				if (rowsAffected != 1) {
					System.out.println("[" + endpoint + " | ERROR]: couldn't update " + db_table + " table");

				} else {
					System.out.println("[" + endpoint + " | INFO]: sent client answer of true");
					ParkCurrentParamsUpdate = true;
				}
				send_response(client, endpoint, "Boolean", ParkCurrentParamsUpdate);

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			break;

		// -------------------------------------------------------------------------------------
		/**
		 * ParkCheckIfApproveRequired: Checks if there are any new parameter requests
		 * pending approval for parks.
		 */
		case "ParkCheckIfApproveRequired":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			
			boolean ApproveRequired = false;
			db_table = "parks";
			try {
				String extracted_str = (String) arr_msg.get(2);
				ps = db_con.prepareStatement("SELECT * FROM " + db_table + " WHERE `parkName`=?;");
				ps.setString(1, extracted_str);
				
				result_set = ps.executeQuery();

				if (!result_set.next()) { // ResultSet is empty
					System.out.println("[" + endpoint + "|INFO]: ResultSet is empty");

					send_response(client, endpoint, new String("ErrorString"),
							new String("result set empty in the DB"));

					return;
				} else {
					if (!result_set.getString("capacity").equals(result_set.getString("newCapacity"))
							|| !result_set.getString("diff").equals(result_set.getString("newDiff"))
							|| !result_set.getString("visitTimeInMinutes")
									.equals(result_set.getString("newVisitTimeInMinutes"))) {
						ApproveRequired = true;
					}

					send_response(client, endpoint, new String("Boolean"), ApproveRequired);
					return;

				}

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// ------------------------------------------------
			/**
			 * AvilableSpaceGet: Retrieves the available space (current visitors and
			 * capacity) in a park from the database.
			 */
		case "AvilableSpaceGet":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			db_table = "parks";
			try {

				String extracted_parkName = (String) arr_msg.get(2);
				ps = db_con.prepareStatement(
						"SELECT `capacity`,`currentVisitors` FROM " + db_table + " WHERE `parkName` = ?;");
				ps.setString(1, extracted_parkName);
				result_set = ps.executeQuery();

				if (!result_set.next()) { // ResultSet is empty
					System.out.println("[" + endpoint + "|INFO]: ResultSet is empty - didnt not find the parkName: "
							+ extracted_parkName);

					send_response(client, endpoint, new String("ErrorString"),
							new String("result set empty in the DB"));

					return;
				} else {
					ArrayList<String> response_arr = new ArrayList<>(
							Arrays.asList(result_set.getString("capacity"), result_set.getString("currentVisitors")));

					send_response(client, endpoint, new String("ArrayList<String>"), response_arr);

					return;

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		// ---------------------------------- ADDING MAAYANs REPORTS HERE
		/**
		 * CreateVisitorsNumReport: Generates a report of the number of visitors,
		 * distinguishing between group and non-group visits, for a specified park
		 * within the current month.
		 */
		case "CreateVisitorsNumReport":
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			LocalDateTime current = LocalDateTime.now(), start = current.withDayOfMonth(1);// assuming last days of
																							// month are irrelevant
			start = start.withHour(9).withMinute(0).withSecond(0);
			String parkName = (String) arr_msg.get(2);
			int nonGroup = 0, group = 0;
			try {
				ps = null;
				ps = db_con.prepareStatement(
						"SELECT SUM(numberOfVisitors) AS sum FROM visits WHERE parkName = ? AND isGroup = ? AND timeOfEntrence BETWEEN ? AND ?");
				ps.setString(1, parkName);
				ps.setBoolean(2, false);// get non group amount
				ps.setString(3, start.format(f));
				ps.setString(4, current.format(f));
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) { // no non group visitors found
					nonGroup = 0;// precaution
				} else {
					nonGroup = rs.getInt("sum");
				}
				ps.setBoolean(2, true);// get group amount
				rs = ps.executeQuery();
				if (!rs.next()) {
					group = 0; // precaution
				} else {
					group = rs.getInt("sum");
				}

			} catch (SQLException e) {
				System.out.println("[" + endpoint + "_ep |ERROR ]: Failed executing query");
				e.printStackTrace();
			}
			ps = null;
			try {
				ps = db_con.prepareStatement(
						"INSERT INTO numberofvisitorsreport (month, year, parkName, amountOfNonGroup, amountOfGroup) VALUES (?, ?, ?, ?, ?) ");
				ps.setString(1, "" + current.getMonthValue());
				ps.setString(2, "" + current.getYear());
				ps.setString(3, parkName);
				ps.setInt(4, nonGroup);
				ps.setInt(5, group);
				int affectedRows = ps.executeUpdate();
				if (affectedRows != 1) { // couldnt update table??
					System.out.println(
							String.format("[%s | Error]: report could not be inserted into report table", endpoint));
					send_response(client, endpoint, new String("Boolean"), new Boolean(false));
					return;
				}
				// report was inserted into table
				send_response(client, endpoint, new String("Boolean"), new Boolean(true));
				System.out.println(String
						.format("[%s | INFO]: sent response of true to client, table updated succesfully", parkName));
			} catch (SQLException e) {
				System.out.println("[" + endpoint
						+ "_ep |ERROR ]: Failed executing query, user must have tried to create an existing report");

				send_response(client, endpoint, new String("Boolean"), new Boolean(false));

				e.printStackTrace();
			}
			break;

		// ---------------------------------------------------------------------------------

		/**
		 * GetVisitorsNumReport: Retrieves the number of visitors report for a specific
		 * park, month, and year, distinguishing between group and non-group visits.
		 */
		case "GetVisitorsNumReport":
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			ArrayList<String> payload = (ArrayList<String>) arr_msg.get(2);

			ps = null;
			try {
				ps = db_con.prepareStatement(
						"SELECT amountOfNonGroup, amountOfGroup FROM numberofvisitorsreport WHERE month = ? AND year = ? AND parkName = ?");
				ps.setString(1, payload.get(1));
				ps.setString(2, payload.get(2));
				ps.setString(3, payload.get(0));
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
					// report was not yet created
					System.out.println(String.format("[%s | INFO]: report requested does not exist", endpoint));
					send_response(client, endpoint, new String("String"), new String("null"));
					return;
				}
				nonGroup = rs.getInt("amountOfNonGroup");
				group = rs.getInt("amountOfGroup");
				ArrayList<String> response = new ArrayList<>(Arrays.asList("" + nonGroup, "" + group));
				send_response(client, endpoint, new String("ArrayList<String>"), response);
				System.out.println(String.format("[%s | INFO]: report information sent back to client", endpoint));
			} catch (SQLException e) {
				System.out.println("sql exception in " + endpoint);
				e.printStackTrace();
			}

			break;

		// ---------------------------------------------------------------------------------
		/**
		 * CreateDatesUsageReport: Triggers the creation of a usage report for a park
		 * based on dates.
		 */
		case "CreateDatesUsageReport":
			System.out.println("[CreateDatesUsageReport | INFO]: endpoint triggered!");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			parkName = (String) arr_msg.get(2);
			System.out.println("park name is " + parkName);
			boolean result = createUsageReport(parkName);
			System.out.println("returned!!!!!");
			System.out.println("result is: " + result);

			send_response(client, endpoint, new String("Boolean"), new Boolean(result));

			break;

		// ---------------------------------------------------------------------------------
		/**
		 * GetDatesUsageReport: Retrieves the dates usage report for a specific park,
		 * month, and year.
		 */
		case "GetDatesUsageReport":
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			arr = (ArrayList<String>) arr_msg.get(2);
			parkName = arr.get(0);
			String month = arr.get(1);
			String year = arr.get(2);
			ArrayList<String> response = getUsageReport(parkName, month, year);
			if (response == null)
				send_response(client, endpoint, new String("ArrayList<String>"), new String("null"));
			else
				send_response(client, endpoint, new String("ArrayList<String>"), response);

			return;

		// ---------------------------------------------------------------------------------
		/**
		 * CreateVisitsReport: Initiates the creation of a report for visits to a park
		 * on a specified day, month, and year.
		 */
		case "CreateVisitsReport":
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			arr = (ArrayList<String>) arr_msg.get(2);
			parkName = arr.get(0);
			String day = arr.get(1);
			month = arr.get(2);
			year = arr.get(3);
			result = createVisitsReport(parkName, day, month, year);
			System.out.println("result is " + result);

			send_response(client, endpoint, new String("Boolean"), new Boolean(result));

			break;

		// ---------------------------------------------------------------------------------
		/**
		 * ShowVisitsReport: Displays the visits report for a park on a particular day,
		 * month, and year.
		 */
		case "ShowVisitsReport":
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			arr = (ArrayList<String>) arr_msg.get(2);
			parkName = arr.get(0);
			day = arr.get(1);
			month = arr.get(2);
			year = arr.get(3);
			try {
				ArrayList<ArrayList<Integer>> visitsReportData = getVisitsReport(parkName, day, month, year);
				send_response(client, endpoint, new String("ArrayList<ArrayList<Integer>>"), visitsReportData);
			} catch (FileNotFoundException e) {
				System.out.println("file not found!, report was not created!");

				send_response(client, endpoint, new String("String"), new String("null"));
				e.printStackTrace();
			}

			break;

		// --------------------------------------------------------------------------------
		/**
		 * ShowCancellationReport: Generates a report on cancellation and missed orders
		 * for a park within a specified date range.
		 */
		case "ShowCancellationReport":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "ArrayList<String>", endpoint))
				return;
			try {
				// Define the result Nested ArrayList
				List<ArrayList<Integer>> statusCounts = new ArrayList<>();

				// Extract data from client pay-load
				ArrayList<String> from_and_to_extracted = (ArrayList<String>) arr_msg.get(2);
				String cancel_report_parkName = from_and_to_extracted.get(0);
				int dayFrom = Integer.valueOf(from_and_to_extracted.get(1));
				int monthFrom = Integer.valueOf(from_and_to_extracted.get(2));
				int yearFrom = Integer.valueOf(from_and_to_extracted.get(3));
				int dayTo = Integer.valueOf(from_and_to_extracted.get(4));
				int monthTo = Integer.valueOf(from_and_to_extracted.get(5));
				int yearTo = Integer.valueOf(from_and_to_extracted.get(6));

				// Define date objects (LocalDate instead of LocalDateTime to avoid overhead of
				// dealing with time)
				LocalDate startDate = LocalDate.of(yearFrom, monthFrom, dayFrom);
				LocalDate endDate = LocalDate.of(yearTo, monthTo, dayTo);
				LocalDate currentDate = startDate;

				// Get current time
				LocalDateTime currentTime = LocalDateTime.now();

				// Iterate over the time period provided by the client
				while (!currentDate.isAfter(endDate)) {

					// Combine data and time
					LocalDateTime startTime = LocalDateTime.of(currentDate, LocalTime.MIN);
					LocalDateTime endTime = LocalDateTime.of(currentDate, LocalTime.MAX);

					// Get count of canceled orders between startTime and endTime
					int cancelledCount = getCount(db_con, cancel_report_parkName, startTime, endTime, "Cancelled");

					// Get count of missed orders (Active orders which their time_of_visit already
					// passed ) between startTime and endTime
					int activePastCount = getActivePastCount(db_con, cancel_report_parkName, currentTime, startTime,
							endTime);

					// Update the result
					ArrayList<Integer> counts = new ArrayList<>();
					counts.add(cancelledCount);
					counts.add(activePastCount);
					statusCounts.add(counts);

					// Loop iteration
					currentDate = currentDate.plusDays(1);

				}

				// Calculate averages
				int totalCancelledCount = 0;
				int totalActivePastCount = 0;
				for (ArrayList<Integer> arrlist_curr : statusCounts) {
					totalCancelledCount += arrlist_curr.get(0);
					totalActivePastCount += arrlist_curr.get(1);
				}
				totalCancelledCount = totalCancelledCount / statusCounts.size();
				totalActivePastCount = totalActivePastCount / statusCounts.size();

				// Add averages to the head of the ArrayList
				ArrayList<Integer> avgs = new ArrayList<>(Arrays.asList(totalCancelledCount, totalActivePastCount));
				statusCounts.add(0, avgs);

				// Send result to client

				send_response(client, endpoint, new String("ArrayList<ArrayList<Integer>>"), statusCounts);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		// ------------------------------------------------------------------------
		/**
		 * OrderApprove: Handles the approval of an order by updating the database with
		 * the visitor's confirmation.
		 */
		case "OrderApprove":
			System.out.println("[" + endpoint + " |INFO]: " + endpoint + " enpoint trigered");
			if (!checkType(client, payload_type, "String", endpoint))
				return;
			boolean OrderApprove = false;
			db_table = "orders";
			try {
				String extracted_orderId = (String) arr_msg.get(2);
				ps = db_con.prepareStatement(
						"UPDATE " + db_table + " SET  `visitorConfirmedOrder` = true WHERE `orderId` = ?;");
				ps.setString(1, extracted_orderId);

				int rowsAffected = ps.executeUpdate();
				if (rowsAffected != 1) {
					System.out.println("[" + endpoint + " | ERROR]: couldn't update " + db_table + " table");

				} else {
					System.out.println("[" + endpoint + " | INFO]: sent client answer of true");
					OrderApprove = true;
				}
				send_response(client, endpoint, "Boolean", OrderApprove);

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			break;

		default:
			System.out.println("[handleMessageFromClient|info]: default enpoint");

			send_response(client, new String("DEFAULT"), new String("ErrorString"),
					new String("default endpoint triggered!"));
			break;
		}

		return;

	}

	
	/////////////////////// METHODS
	/**
	 * Retrieves the count of orders within a specified time frame and status for a given park.
	 * 
	 * @param connection   The database connection.
	 * @param parkName     The name of the park.
	 * @param startTime    The start time of the time frame.
	 * @param endTime      The end time of the time frame.
	 * @param status       The status of the orders to count.
	 * @return             The count of orders matching the criteria.
	 * @throws SQLException If a database access error occurs.
	 */
	private static int getCount(Connection connection, String parkName, LocalDateTime startTime, LocalDateTime endTime,
			String status) throws SQLException {
		String query = "SELECT COUNT(*) FROM orders WHERE parkName = ? "
				+ "AND status = ? AND time_of_visit >= ? AND time_of_visit <= ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, parkName);
			statement.setString(2, status);
			statement.setString(3, startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			statement.setString(4, endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
			return 0;
		}
	}

	/**
	 * Retrieves the count of active orders whose time of visit has already passed within a specified time frame for a given park.
	 * 
	 * @param connection     The database connection.
	 * @param parkName       The name of the park.
	 * @param currentTime    The current time.
	 * @param startTime      The start time of the time frame.
	 * @param endTime        The end time of the time frame.
	 * @return               The count of active orders whose time of visit has passed.
	 * @throws SQLException  If a database access error occurs.
	 */
	private static int getActivePastCount(Connection connection, String parkName, LocalDateTime currentTime,
			LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
		String query = "SELECT COUNT(*) FROM orders WHERE parkName = ? "
				+ "AND status = 'Active' AND time_of_visit >= ? AND time_of_visit <= ? " + "AND time_of_visit <= ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, parkName);
			statement.setString(2, startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			statement.setString(3, endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			statement.setString(4, currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
			return 0;
		}
	}

	/**
	 * check if currently amount of people in park + active orders for period visit
	 * time of park + amount of visitors wanting to enter park does not exceed park
	 * capacity
	 * 
	 * @param amount - amount of visitors wanting to enter park
	 * @return boolean - can enter park
	 * @throws SQLException
	 */
	private boolean checkUnplannedVisit(int amount, String parkName) throws SQLException {
		boolean result = false;

		int capacity = getParkCapacity(parkName, false); // get actual capacity of park
		int current = getCurrentVisitors(parkName); // get amount of people currently in park
		// get amount of orders planned to enter withing park visitTime
		LocalDateTime now = LocalDateTime.now();
		int time = getParkTime(parkName);// get visit time of park

		PreparedStatement ps;
		ResultSet rs;

		String statement = "SELECT SUM(visitor_number) AS sum FROM orders WHERE status = 'Active' AND time_of_visit BETWEEN ? AND ?";

		ps = db_con.prepareStatement(statement);
		ps.setString(1, now.format(f));
		ps.setString(2, now.plusMinutes(time).format(f));

		rs = ps.executeQuery();

		int sum;

		if (!rs.next()) {// no orders found
			sum = 0;
		} else
			sum = rs.getInt("sum");

		result = (capacity > current + sum + amount);// is capacity greater then amount of visitors wanting to enter
														// park plus amount of visitors currently in park and amount of
														// visitors in ordered visits for the expected visit time in
														// park

		return result;
	}

	/**
	 * method returns current visitors for specific park
	 * 
	 * @param parkName - name of park
	 * @return amount of visitors currently in park
	 * @throws SQLException
	 */
	private int getCurrentVisitors(String parkName) throws SQLException {
		int result = -1;

		PreparedStatement ps = db_con.prepareStatement("SELECT currentVisitors FROM parks WHERE parkName = ?");
		ps.setString(1, parkName);
		ResultSet rs = ps.executeQuery();
		rs.next();
		result = rs.getInt("currentVisitors");
		return result;
	}

	/**
	 * all orders ordered by guide are organised group orders, checking for a given
	 * orderId if that order was ordered by a group guide
	 * 
	 * @param orderId
	 * @return boolean - is the order organised group
	 * @throws SQLException
	 */

	private boolean isGroupOrder(String orderId) throws SQLException {
		ArrayList<String> order = getOrderFromId(Integer.valueOf(orderId));
		String visitor_id = order.get(0);
		PreparedStatement ps = db_con.prepareStatement("SELECT * FROM guides WHERE visitor_id = ?");
		ps.setString(1, visitor_id);
		ResultSet rs = ps.executeQuery();
		return rs.next(); // true if found - if not found rs is empty thus return false
	}

	/**
	 * gets specific report data from file saved on server side, organised to client
	 * request
	 * 
	 * @param parkName
	 * @param day
	 * @param month
	 * @param year
	 * @return
	 * @throws FileNotFoundException
	 */
	private ArrayList<ArrayList<Integer>> getVisitsReport(String parkName, String day, String month, String year)
			throws FileNotFoundException {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		String fileName = String.format("reports/visitsReport_%s_%s_%s_%s.txt", day, month, year,
				parkName.replace(' ', '_')); // all reports are created and handled using a specific format
		try (BufferedReader file = new BufferedReader(new FileReader(fileName))) { // file will be automatically closed
																					// when try ends
			String line;
			while ((line = file.readLine()) != null) { // every line of file in format "'number of non group', 'number
														// of group'"
				ArrayList<Integer> pair = new ArrayList<>();
				pair.add(Integer.valueOf(line.split(",")[0]));
				pair.add(Integer.valueOf(line.split(",")[1]));
				result.add(pair);
				System.out.println("added " + pair);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * creating visits report according to request from client, for day given - keep
	 * amount of visitors in park for every hour park is working (assuming 9-17)
	 * 
	 * @param parkName
	 * @param day
	 * @param month
	 * @param year
	 * @return
	 */
	private boolean createVisitsReport(String parkName, String day, String month, String year) {
		String filePath = String.format("reports/visitsReport_%s_%s_%s_%s.txt", day, month, year,
				parkName.replace(' ', '_'));
		PreparedStatement ps;
		ResultSet rs;
		LocalDateTime dayToCheck = LocalDateTime.of(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day),
				9, 0);
		int nonGroup = 0, group = 0;
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter(filePath));
			while (dayToCheck.getHour() <= 17) {
				ps = db_con.prepareStatement(
						"SELECT SUM(numberOfVisitors) AS sum FROM visits WHERE parkName = ? AND timeOfEntrence < ? AND timeOfExit >= ? AND isGroup = 0");
				ps.setString(1, parkName);
				ps.setString(3, dayToCheck.format(f));
				ps.setString(2, dayToCheck.plusHours(1).format(f));

				rs = ps.executeQuery();
				if (rs.next()) {
					nonGroup = rs.getInt("sum");
				} else
					nonGroup = 0;
				ps = null;
				ps = db_con.prepareStatement(
						"SELECT SUM(numberOfVisitors) AS sum FROM visits WHERE parkName = ? AND timeOfEntrence < ? AND timeOfExit >= ? AND isGroup = 1");
				ps.setString(1, parkName);
				ps.setString(3, dayToCheck.format(f));
				dayToCheck = dayToCheck.plusHours(1); // advance loop
				ps.setString(2, dayToCheck.format(f));
				rs = ps.executeQuery();
				if (rs.next()) {
					group = rs.getInt("sum");
				} else
					group = 0;
				file.write("" + group + "," + nonGroup + "\n");
			}
			System.out.println("finished writing day to file");
			file.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * read usage report from file to send to client
	 * 
	 * @param parkName
	 * @param month
	 * @param year
	 * @return
	 */
	private ArrayList<String> getUsageReport(String parkName, String month, String year) {
		ArrayList<String> result = new ArrayList<>();
		String fileName = String.format("reports/usageReport_%s_%s_%s.txt", month, year, parkName.replace(' ', '_'));
		Path path = Paths.get(fileName);
		if (!Files.exists(path))
			return null;

		try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = file.readLine()) != null) {
				result.add(line);
				System.out.println("added " + line);
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * create usage report for a specific park, according to requirements report is
	 * created at the end of every month for that month, finds all days of month
	 * that park was not full and save to text file on server side
	 * 
	 * @param parkName
	 * @return boolean - was report created
	 */
	private boolean createUsageReport(String parkName) {
		LocalDateTime end = LocalDateTime.now(), current = end.withDayOfMonth(1);
		System.out.println(current.format(f) + end.format(f));
		PreparedStatement ps;
		ResultSet rs;
		String filePath = String.format("reports/usageReport_%d_%d_%s.txt", current.getMonthValue(), current.getYear(),
				parkName.replace(' ', '_'));// all reports are created and handled using a specific format
		int capacity = getParkCapacity(parkName, false);

		try {

			BufferedWriter file = new BufferedWriter(new FileWriter(filePath));
			while (current.getDayOfMonth() <= end.getDayOfMonth() && current.getMonthValue() == end.getMonthValue()) {// loop iterates over days of month
				boolean wasFull = false;
				current = current.withHour(9).withMinute(0).withSecond(0); // park opens at 9
				while (current.getHour() < 17) { // loop iterates hours of day, park closes at 17
					ps = db_con.prepareStatement(
							"SELECT SUM(numberOfVisitors) AS sum FROM visits WHERE parkName = ? AND timeOfEntrence <= ? AND timeOfExit > ?");
					ps.setString(1, parkName);
					ps.setString(3, current.format(f));
					current = current.plusHours(1);
					ps.setString(2, current.format(f));
					// query explenation: if we want to check all visitors in park at hour x (were
					// in park between x and x+1) we know they entered park before x+1 and exited
					// the park after x
					rs = ps.executeQuery();
					if (rs.next()) {
						int sum = rs.getInt("sum");
						if (sum >= capacity) {// assuming that if at any given hour park was more than capacity, it was
												// full at this time(sum will include both people entering during that
												// hour and people exiting during that hour)
							wasFull = true; // raise flag
							break;// if there was an hour where park was full, day was full so not returned to
									// user, no need to chedk more times
						}
					}
				}
				System.out
						.println("[createUsageReport | INFO]: " + (wasFull ? "today was full" : "today was not full"));
				if (!wasFull)
					file.write(current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"); // if park did not
																									// fill up add to
																									// txt file of
																									// report data
				System.out.println(current.format(f));
				current = current.plusDays(1);// advance loop
			}
			file.close();
			return true;// report was created
		} catch (SQLException e) {
			System.out.println("[createUsageReport | ERROR]: couldnt execute query");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("[createUsageReport | ERROR]: couldnt create file");
			e.printStackTrace();
		}

		return false;

	}

	/**
	 * method that checks db that order is valid
	 * 
	 * @param arr - array list of string containing order parameters
	 * @return boolean - order is valid
	 */
	private boolean checkOrderTime(ArrayList<String> arr) {
		System.out.println("[checkOrderTime | INFO]: triggered!");
		try {
			int visitorNum = Integer.valueOf(arr.get(3));
			// get parks time of visit, and capacity of park
			PreparedStatement ps = db_con
					.prepareStatement("SELECT visitTimeInMinutes, capacity, diff FROM parks WHERE parkName = ?");
			ps.setString(1, arr.get(1));
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				// park was not found
				System.out.println("[checkOrderTime | ERROR]: park was not found - returning false");
				return false;
			}
			// found park
			int timeToAdd = rs.getInt("visitTimeInMinutes");
			int parkCapacity = rs.getInt("capacity");
			int capacityDiff = rs.getInt("diff");
			int actual = parkCapacity - capacityDiff;
			// System.out.println("Debug: actual capacity is: " + actual);

			// get amount of orders in timeframe - park's vistit time before order (to make
			// sure park is not full when order is placed, and during the order time
			String startTime = arr.get(2);
			ps = db_con.prepareStatement(
					"SELECT SUM(visitor_number) AS sum FROM orders WHERE parkName = ? AND status = 'Active' AND time_of_visit BETWEEN ? AND ?");
			ps.setString(1, arr.get(1));
			ps.setString(2, startTime);
			DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime endTime = LocalDateTime.parse(startTime, f);
			endTime = endTime.plusMinutes(timeToAdd);
			String endTimeFormatted = endTime.format(f);
			ps.setString(3, endTimeFormatted);
			rs = ps.executeQuery();
			int sum;
			if (!rs.next()) {
				sum = 0;
			} else
				sum = rs.getInt("sum");
			// System.out.println("Debug: sum came to " + sum);
			int availableSpace = actual - sum;
			boolean validOrder = (availableSpace - visitorNum) > 0; // if there is space for order in park
			LocalDateTime preStart = LocalDateTime.parse(startTime, f); // to check that park is not full for time of
																		// order
			preStart = preStart.minusMinutes(timeToAdd);
			String preStartFormatted = preStart.format(f);
			ps = db_con.prepareStatement(
					"SELECT SUM(visitor_number) AS sum FROM orders WHERE parkName = ? AND status = 'Active' AND time_of_visit BETWEEN ? AND ?");
			ps.setString(1, arr.get(1));
			ps.setString(2, preStartFormatted);
			ps.setString(3, startTime);
			rs = ps.executeQuery();
			if (!rs.next()) {
				sum = 0;
			} else
				sum = rs.getInt("sum");
			availableSpace = actual - sum;
			return validOrder && ((availableSpace - visitorNum) > 0);// both park not full at time of order and there is
																		// space for order
		} catch (SQLException e) {
			System.out.println("error in checkOrderTime(): ");
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * method to check that type of payload recieved from client matches type
	 * expected, send proper response to client
	 * 
	 * @param client
	 * @param payloadType
	 * @param expected
	 * @param ep
	 * @throws IOException
	 */
	private boolean checkType(ConnectionToClient client, String payloadType, String expected, String ep) {
		if (!payloadType.equals(expected)) {
			System.out.println(
					String.format("[rp | ERROR]: Client sent payload for %s ep which is not a %s", ep, payloadType));

			// send error to client
			send_response(client, ep, new String("ErrorString"), new String(
					String.format("Client asked %s end point but payload-type was not %s!", ep, payloadType)));

			return false;
		}
		return true;
	}

	/**
	 * method that returns all waitlisted orders affected by cancellation at
	 * orderTime for park parkName
	 * 
	 * @param orderTime
	 * @param parkName
	 * @return
	 */
	private ArrayList<ArrayList<String>> getWaitListedOrders(String orderTime, String parkName) {

		ArrayList<ArrayList<String>> arr = new ArrayList<>();
		// get capacity of park

		int visitTime = getParkTime(parkName);
		// get start and end times affected by orderTime cancellation
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime order = LocalDateTime.parse(orderTime, f), start = order.minusMinutes(visitTime),
				end = order.plusMinutes(visitTime);

		try {
			PreparedStatement ps = db_con.prepareStatement(
					"SELECT orderId FROM orders WHERE status = 'WaitList' AND time_of_visit BETWEEN ? AND ?");// get all
																												// orders
			ps.setString(1, start.format(f));
			ps.setString(2, end.format(f));
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {// no orders found
				System.out.println("[getWaitListedOrders | INFO]: no waitlisted orders found to check");
				return null;
			}
			do {// add all orders to array of orders
				int id = rs.getInt("orderId");
				arr.add(getOrderFromId(id));
			} while (rs.next());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return arr;// return array of orders

	}

	/**
	 * creates array of order data of given orderId
	 * 
	 * @param orderId
	 * @return
	 */
	private ArrayList<String> getOrderFromId(int orderId) {
		ArrayList<String> arr = new ArrayList<>();
		try {
			PreparedStatement ps = db_con.prepareStatement("SELECT * FROM orders WHERE orderId = ?");
			ps.setInt(1, orderId);
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				return null;
			arr.add(rs.getString("visitor_id"));
			arr.add(rs.getString("parkName"));
			arr.add(rs.getString("time_of_visit"));
			arr.add(String.valueOf(rs.getInt("visitor_number")));
			arr.add(rs.getString("visitor_email"));
			arr.add(rs.getString("visitor_phone"));
			arr.add(rs.getString("status"));
			arr.add(String.valueOf(orderId));
		} catch (SQLException e) {
			System.out.println("getOrderFromId: sql exception was thrown");
			e.printStackTrace();
		}
		return arr;
	}

	/**
	 * gets park capacity for given parkName, with diff meaning capacity for order
	 * or for entrance
	 * 
	 * @param parkName
	 * @param withDiff
	 * @return
	 */
	private int getParkCapacity(String parkName, boolean withDiff) {
		System.out.println("debug: getParkCapacity was called");
		int capacity, diff;
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = db_con.prepareStatement("SELECT capacity, diff FROM parks WHERE parkName = ?");
			ps.setString(1, parkName);
			rs = ps.executeQuery();
			if (!rs.next())
				return -1;
			capacity = rs.getInt("capacity");
			diff = rs.getInt("diff");
			if (withDiff) { // for order capacity
				// System.out.println("returned " + (capacity - diff));
				return capacity - diff;
			} else { // for entrance capacity
				// System.out.println("returned " + capacity);
				return capacity;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * method returns expected visit time for specific park
	 * 
	 * @param parkName - name of park (String)
	 * @return expected visit time of park in minutes
	 */
	private int getParkTime(String parkName) {
		int time;
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = db_con.prepareStatement("SELECT visitTimeInMinutes FROM parks WHERE parkName = ?");
			ps.setString(1, parkName);
			rs = ps.executeQuery();
			if (!rs.next())
				return -1;
			time = rs.getInt("visitTimeInMinutes");
			return time;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Cancels an order in the database.
	 * 
	 * @param orderId The ID of the order to be canceled.
	 * @return {@code true} if the order is successfully canceled, {@code false} otherwise.
	 * @throws SQLException If a database access error occurs or this method is called on a closed connection.
	 */
	public boolean cancelOrder(int orderId) throws SQLException {
		boolean result = false;
		PreparedStatement ps = db_con
				.prepareStatement("UPDATE `orders` SET `status` = 'Cancelled' WHERE `orderId` = ?");
		ps.setInt(1, orderId);
		int rowsAffected = ps.executeUpdate();

		// Cancellation result update check
		if (rowsAffected == 1) {
			result = true;
			advanceWaitList(orderId);
		}
		return result;
	}

	/**
	 * checks if order is now valid (after cancellation)
	 * 
	 * @param orderId
	 */
	private void advanceWaitList(int orderId) {
		ArrayList<String> order = getOrderFromId(orderId);
		System.out.println(order);
		String parkName = order.get(1);
		String orderTime = order.get(2);
		ArrayList<ArrayList<String>> affectedOrders = getWaitListedOrders(orderTime, parkName);
		if (affectedOrders == null)
			return;// no need to advance waitlist
		for (ArrayList<String> current : affectedOrders) {
			if (checkOrderTime(current)) {
				approveOrder(current.get(7));
			}
		}
	}

	/**
	 * changes order status to Active after 'user' approved
	 * 
	 * @param OrderId
	 */
	private void approveOrder(String OrderId) {
		try {
			PreparedStatement ps = db_con
					.prepareStatement("UPDATE `orders` SET `status` = 'Active' WHERE (`orderId` = ?);");
			ps.setString(1, OrderId);
			int rowsAffected = ps.executeUpdate();
			if (rowsAffected != 1)
				System.out.println("approveOrder didnt work??");
			System.out.println("assuming user has chosen to approve his waitlisted order, his order is now active");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	@Override
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
		//////////// Connect to Data Base ///////////////////
		// ORENB_TODO: DB should stay connected all the time?
		// ms_conn = new MysqlConnection(db_name, db_host, db_user, db_pass);
		System.out.println("[SERVER]: trying to connect to DataBase");
		this.db_con = MysqlConnection.connectDB();
		if (db_con != null) {
			System.out.println("[SERVER]: Succesfully connected DB");
		} else {
			System.out.println("[SERVER]: failed to connect DB");
		}

		// Initiate discounts
		discounts.put(new String("full_price"), new Integer(50));
		discounts.put(new String("discount_private_family_planned"), new Integer(15));
		discounts.put(new String("discount_private_family_unplanned"), new Integer(0));
		discounts.put(new String("discount_group_planned"), new Integer(25));
		discounts.put(new String("discount_group_unplanned"), new Integer(10));
		discounts.put(new String("discount_payment_in_advance"), new Integer(12));

		/////////////////////////////////////////////////////////////////

		/// Start SMS simulator thread
		OrderNotificationThread notificationThread = new OrderNotificationThread(this, db_con);
		notificationThread.start();

		// Call import module - special case: both src and dest tables are at the same
		// db.
		ImportSimulator is = new ImportSimulator(db_con, db_con);
		if (!is.importData()) {
			System.out.println("[SERVER]: import module failed");
		}

		// Create directory for maayan
		////////////////////////////////////////////////
		// create directory for reports

		String dirName = "reports";
		Path path = Paths.get(dirName);
		try {
			Files.createDirectories(path);
			System.out.println("[SERVER]: reports directory created successfully");
		} catch (IOException e) {
			System.out.println("[SERVER]: failed to create reports directory");
			e.printStackTrace();
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	@Override
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	
	/**
	 * Called when a client is connected to the server. 
	 * Adds the connected client to the GUI list.
	 * 
	 * @param client The connection to the client.
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		System.out.println("[clientConnected|INFO]: adding client to gui list");
		controller.addClient(client);
		return;
	}

}