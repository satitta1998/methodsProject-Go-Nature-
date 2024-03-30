/**
 * The PriceGenerator class calculates the final price for a visit based on various parameters.
 */
package entity;

import java.util.ArrayList;

import client.ChatClient;
import client.ClientUI;

public class PriceGenerator {
	
	private Double finalPrice;
	private Integer fullPrice;
	private Integer discountPrivateFamilyPlanned;
	private Integer discountPrivateFamilyUnplanned;
	private Integer discountGroupPlanned;
	private Integer discountGroupUnplanned;
	private Integer discountPaymentInAdvance;

	private Boolean isGuidedGroup;
	private Integer visitorsNumber;
	private Boolean isPaidInAdvance;
	private Boolean isPlannedVisit;
	
    /**
     * Retrieves the final calculated price.
     * @return The final price.
     */
	//getters
	public Double getFinalPrice() {
		return finalPrice;
	}
    /**
     * Retrieves the full price before any discounts.
     * @return The full price.
     */
	public Integer getFullPrice() {
		return fullPrice;
	}
    /**
     * Retrieves the discount for planned private or family visits.
     * @return The discount for planned private or family visits.
     */
	public Integer getDiscountPrivateFamilyPlanned() {
		return discountPrivateFamilyPlanned;
	}
    /**
     * Retrieves the discount for unplanned private or family visits.
     * @return The discount for unplanned private or family visits.
     */
	public Integer getDiscountPrivateFamilyUnplanned() {
		return discountPrivateFamilyUnplanned;
	}
    /**
     * Retrieves the discount for planned group visits.
     * @return The discount for planned group visits.
     */
	public Integer getDiscountGroupPlanned() {
		return discountGroupPlanned;
	}
    /**
     * Retrieves the discount for unplanned group visits.
     * @return The discount for unplanned group visits.
     */
	public Integer getDiscountGroupUnplanned() {
		return discountGroupUnplanned;
	}
    /**
     * Retrieves the discount for payment made in advance.
     * @return The discount for payment made in advance.
     */
	public Integer getDiscountPaymentInAdvance() {
		return discountPaymentInAdvance;
	}
    /**
     * Retrieves whether the visit is for a guided group.
     * @return True if the visit is for a guided group, otherwise false.
     */
	public Boolean getIsGuidedGroup() {
		return isGuidedGroup;
	}
    /**
     * Retrieves the number of visitors included in the visit.
     * @return The number of visitors.
     */
	public Integer getVisitorsNumber() {
		return visitorsNumber;
	}
    /**
     * Retrieves whether the visit is paid in advance.
     * @return True if the visit is paid in advance, otherwise false.
     */
	public Boolean getIsPaidInAdvance() {
		return isPaidInAdvance;
	}
    /**
     * Retrieves whether the visit is planned.
     * @return True if the visit is planned, otherwise false.
     */
	public Boolean getIsPlannedVisit() {
		return isPlannedVisit;
	}
	
    /**
     * Sets whether the group is guided.
     * @param isGuidedGroup True if the group is guided, otherwise false.
     */
	//setters
	public void setIsGuidedGroup(Boolean isGuidedGroup) {
		this.isGuidedGroup = isGuidedGroup;
	}
	
    /**
     * Sets the number of visitors for the visit.
     * @param visitorsNumber The number of visitors.
     */
	public void setVisitorsNumber(Integer visitorsNumber) {
		this.visitorsNumber = visitorsNumber;
	}
	
    /**
     * Sets whether the visit is paid in advance.
     * @param isPaidInAdvance True if the visit is paid in advance, otherwise false.
     */
	public void setIsPaidInAdvance(Boolean isPaidInAdvance) {
		this.isPaidInAdvance = isPaidInAdvance;
	}
	
    /**
     * Sets whether the visit is planned.
     * @param isPlannedVisit True if the visit is planned, otherwise false.
     */
	public void setIsPlannedVisit(Boolean isPlannedVisit) {
		this.isPlannedVisit = isPlannedVisit;
	}
	
    /**
     * Generates the final price for the visit based on the provided parameters.
     * @return The calculated final price.
     */
	//public method for generating the final price
	public Double generateFinalPrice() {
		try {
			//to set the prices
			setPrices();
			
			if(isPlannedVisit) {
				//planned visit
				if(isGuidedGroup) {
					//planned guided group
					if(isPaidInAdvance) {
						//planned guided group paid in advance
						finalPrice = (fullPrice*(1-(((double)discountGroupPlanned+(double)discountPaymentInAdvance)/100)))*(visitorsNumber-1);
						return finalPrice;
					}else {
						//planned guided group not paid in advance
						finalPrice = (fullPrice*(1-((double)discountGroupPlanned/100)))*(visitorsNumber-1);
						return finalPrice;
					}
				}else {
					//planned private or family visit
					finalPrice = (fullPrice*(1-((double)discountPrivateFamilyPlanned/100)))*visitorsNumber;
					return finalPrice;
				}
			}else {
				//unplanned visit
				if(isGuidedGroup) {
					//unplanned guided group
					finalPrice = (fullPrice*(1-((double)discountGroupUnplanned/100)))*visitorsNumber;
					return finalPrice;
				}else {
					//unplanned private or family visit
					finalPrice = (fullPrice*(1-((double)discountPrivateFamilyUnplanned/100)))*visitorsNumber;
					return finalPrice;
				}
			}
		} catch (Exception e) {
			System.out.println("Error in PriceGenerator: generateFinalPrice");
			System.out.println(e.getMessage());
		}
		return finalPrice;
	}
		
	
	//private method that get and the prices from DB and set them
	private void setPrices() {
		try {
			//1. get the prices from DB
			ArrayList<Object> arrmsg = new ArrayList<Object>();
			arrmsg.add(new String("GetPrices"));
			arrmsg.add(new String("GetPrices"));
			arrmsg.add(new String("GetPrices"));
			ClientUI.chat.accept(arrmsg);	
			
			//2. set the prices
			
			if (ChatClient.dataFromServer.get(0).equals("null"))
				throw new NullPointerException("No prices returned from DB");
			
			//"ArrayList<Integer>", {"0-full_price", "1-discount_private_family_planned", "2-discount_private_family_unplanned", "3-discount_group_planned", "4-discount_group_unplanned", "5-discount_payment_in_advance" }
			fullPrice = Integer.parseInt(ChatClient.dataFromServer.get(0));
			discountPrivateFamilyPlanned = Integer.parseInt(ChatClient.dataFromServer.get(1));
			discountPrivateFamilyUnplanned = Integer.parseInt(ChatClient.dataFromServer.get(2));
			discountGroupPlanned = Integer.parseInt(ChatClient.dataFromServer.get(3));
			discountGroupUnplanned = Integer.parseInt(ChatClient.dataFromServer.get(4));
			discountPaymentInAdvance = Integer.parseInt(ChatClient.dataFromServer.get(5));
			
		}catch (NullPointerException e) {
			System.out.println(e.getMessage());
		}catch (Exception e) {
			System.out.println("Error in PriceGenerator: setPrices");
			System.out.println(e.getMessage());
		}
	}//END OF setPrices
	

}
