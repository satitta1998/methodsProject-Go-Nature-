/**
 * Represents an order made by a visitor for a park visit.
 * An order contains information such as park name, order number,
 * time of visit, number of visitors, visitor's contact details, etc.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Order implements Serializable {
	private String parkName;
	private String orderNumber;
	private String timeOfVisit;
	private String numberOfVisitors;
	private String telephoneNumber;
	private String email;
	private String visitorID;

    /**
     * Constructor for creating an Order object with specified parameters.
     * @param parkName The name of the park associated with the order.
     * @param orderNumber The unique order number.
     * @param timeOfVisit The time of the scheduled visit.
     * @param numberOfVisitors The number of visitors included in the order.
     * @param telephoneNumber The telephone number of the visitor.
     * @param email The email address of the visitor.
     * @param visitorID The ID of the visitor.
     */
	//constructor
	public Order(String parkName, String orderNumber, String timeOfVisit, String numberOfVisitors, String telephoneNumber, String email, String visitorID) {
		this.parkName = parkName;
		this.orderNumber = orderNumber;
		this.timeOfVisit = timeOfVisit;
		this.numberOfVisitors = numberOfVisitors;
		this.telephoneNumber = telephoneNumber;
		this.email = email;
		this.visitorID = visitorID;
	}
	
    /**
     * Constructor for creating an Order object from an ArrayList.
     * The ArrayList should contain order information in the following order:
     * [orderNumber, parkName, timeOfVisit, numberOfVisitors, email, telephoneNumber, visitorID].
     * @param arr The ArrayList containing order information.
     */
	//constructor
	public Order(ArrayList<String> arr) {
		this.orderNumber = arr.get(0);
		this.parkName = arr.get(1);
		this.timeOfVisit = arr.get(2);
		this.numberOfVisitors = arr.get(3);
		this.email = arr.get(4);
		this.telephoneNumber = arr.get(5);
		this.visitorID = arr.get(6);
	}
	
	/**
	 * Retrieves the name of the park associated with the order.
	 * @return The name of the park.
	 */
	//get park name
	public String getParkName() {
		return parkName;
	}
	
	/**
	 * Sets the name of the park associated with the order.
	 * @param parkName The name of the park to be set.
	 */
	//set park name
	public void setParkName(String parkName) {
		this.parkName = parkName;
	}
	
	/**
	 * Retrieves the order number.
	 * @return The order number.
	 */
	//get order number
	public String getOrderNumber() {
		return orderNumber;
	}
	
	/**
	 * Sets the order number.
	 * @param orderNumber The order number to be set.
	 */
	//set order number
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	/**
	 * Retrieves the time of visit for the order.
	 * @return The time of visit.
	 */
	//get time of visit
	public String getTimeOfVisit() {
		return timeOfVisit;
	}

	/**
	 * Sets the time of visit for the order.
	 * @param timeOfVisit The time of visit to be set.
	 */
	//set time of visit
	public void setTimeOfVisit(String timeOfVisit) {
		this.timeOfVisit = timeOfVisit;
	}

	/**
	 * Retrieves the number of visitors included in the order.
	 * @return The number of visitors.
	 */
	//get number of visitors
	public String getNumberOfVisitors() {
		return numberOfVisitors;
	}

	/**
	 * Sets the number of visitors included in the order.
	 * @param numberOfVisitors The number of visitors to be set.
	 */
	//set number of visitors
	public void setNumberOfVisitors(String numberOfVisitors) {
		this.numberOfVisitors = numberOfVisitors;
	}

	/**
	 * Retrieves the telephone number of the visitor.
	 * @return The telephone number.
	 */
	//get phone number
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	/**
	 * Sets the telephone number of the visitor.
	 * @param telephoneNumber The telephone number to be set.
	 */
	//set phone number
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	/**
	 * Retrieves the email address of the visitor.
	 * @return The email address.
	 */
	//get email
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email address of the visitor.
	 * @param email The email address to be set.
	 */
	//set email
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Retrieves the ID of the visitor.
	 * @return The visitor ID.
	 */
	//get visitor ID
	public String getVisitorID() {
		return visitorID;
	}
	
	/**
	 * Sets the ID of the visitor.
	 * @param visitorID The visitor ID to be set.
	 */
	//set visitor ID
	public void setVisitorID(String visitorID) {
		this.visitorID = visitorID;
	}


    /**
     * Converts the Order object to a formatted string.
     * @return A formatted string representation of the Order object.
     */
	public String toString(){
		return String.format("%s %s %s %s %s %s %s\n",parkName, orderNumber, timeOfVisit, numberOfVisitors, telephoneNumber, email, visitorID);
	}
	
    /**
     * Converts the Order object to an ArrayList.
     * @return An ArrayList containing order information.
     */
	public ArrayList<String> toArrayList() {
		return new ArrayList<String>(Arrays.asList(parkName, orderNumber, timeOfVisit, numberOfVisitors, telephoneNumber, email, visitorID));
	}
	
    /**
     * Sets all fields of the Order object from the given ArrayList.
     * The ArrayList should contain order information in the following order:
     * [orderNumber, parkName, timeOfVisit, numberOfVisitors, email, telephoneNumber, visitorID].
     * @param arr The ArrayList containing order information.
     */
	//set all fields to order from given array
	public void setAllFields(ArrayList<String> arr) {
		this.orderNumber = arr.get(0);
		this.parkName = arr.get(1);
		this.timeOfVisit = arr.get(2);
		this.numberOfVisitors = arr.get(3);
		this.email = arr.get(4);
		this.telephoneNumber = arr.get(5);
		this.visitorID = arr.get(6);
	}
}