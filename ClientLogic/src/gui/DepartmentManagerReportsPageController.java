/**
 * The DepartmentManagerReportsPageController class controls the user interface for generating reports
 * in the department manager reports page. It provides functionality to select report types, choose dates,
 * and create reports.
 */
package gui;

import java.awt.Label;
import java.util.ArrayList;
import entity.NextPage;
import entity.SecondPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;

public class DepartmentManagerReportsPageController {

    /**
     * Enum representing different types of reports available for generation.
     */
	public enum ReportsTypes {

		ORDER_CANCELLATION("The total number of oreders that are cancelled report"),
		TOTAL_VISITORS_NUMBER("The total number of visitors report - segmented according to the type of visit");

		private final String description;

		ReportsTypes(String description) {
			this.description = description;
		}

        /**
         * Gets the description of the report type.
         * 
         * @return The description of the report type.
         */
		public String getDescription() {
			return description;
		}
	}

	@FXML
	private Button btnBack;

	@FXML
	private Button btnCreateReport;

	@FXML
	private ComboBox<String> cmbReportType, cmbDay, cmbMonth, cmbYear, cmbDayTo, cmbMonthTo, cmbYearTo;

	@FXML
	private Text txtError, fromTxt, toTxt;

	private String parkName;

    /**
     * Loads data for the department manager reports page, including the available report types,
     * days, months, and years.
     * 
     * @param parkName The name of the park for which reports are being generated.
     */
	public void loadData(String parkName) {
		try {

			cmbDay.setVisible(false);
			cmbMonth.setVisible(false);
			cmbYear.setVisible(false);
			cmbDayTo.setVisible(false);
			cmbMonthTo.setVisible(false);
			cmbYearTo.setVisible(false);

			// 1. Load park name
			this.parkName = parkName;
			
			// 2. Load reports types

			if (parkName.equals("All parks")) {
				this.cmbReportType.getItems().addAll(ReportsTypes.ORDER_CANCELLATION.getDescription());
			}else {
				this.cmbReportType.getItems().addAll(ReportsTypes.TOTAL_VISITORS_NUMBER.getDescription(),
					ReportsTypes.ORDER_CANCELLATION.getDescription());
			}
			// 3. Load days
			ArrayList<String> day = new ArrayList<>();
			for (int i = 1; i <= 31; i++) {
				day.add("" + i);
			}
			this.cmbDay.getItems().addAll(day);
			this.cmbDayTo.getItems().addAll(day);

			// 4. Load months
			ArrayList<String> months = new ArrayList<>();
			for (int i = 1; i < 13; i++) {
				months.add("" + i);
			}
			this.cmbMonth.getItems().addAll(months);
			this.cmbMonthTo.getItems().addAll(months);

			// 5. Load years
			ArrayList<String> years = new ArrayList<>();
			for (int i = 2020; i < 2041; i++) {
				years.add("" + i);
			}
			this.cmbYear.getItems().addAll(years);
			this.cmbYearTo.getItems().addAll(years);
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerReportsPageController: loadData");
			System.out.println(e.getMessage());
		}

	}

    /**
     * Handles the event when a report type is selected, showing relevant ComboBoxes and labels
     * based on the selected report type.
     * 
     * @param event The ActionEvent triggered by selecting a report type.
     */
	@FXML
	void pressReport(ActionEvent event) {
		try {
			ReportsTypes selectedReportType = null;
			// Get report type
			String reportType = this.cmbReportType.getValue();

			if (reportType != null) {
				for (ReportsTypes type : ReportsTypes.values()) {
					if (type.getDescription().equals(reportType)) {
						selectedReportType = type;
						break;
					}
				}
			}
			// Show relevant combo box and label
			if (selectedReportType != null) {
				switch (selectedReportType) {
				case TOTAL_VISITORS_NUMBER: {
					cmbDay.setVisible(true);
					cmbMonth.setVisible(true);
					cmbYear.setVisible(true);
					fromTxt.setText("Select date:");
					cmbDayTo.setVisible(false);
					cmbMonthTo.setVisible(false);
					cmbYearTo.setVisible(false);
					toTxt.setText("");
					break;
				}
				case ORDER_CANCELLATION: {
					cmbDay.setVisible(true);
					cmbMonth.setVisible(true);
					cmbYear.setVisible(true);
					fromTxt.setText("Select date from:");
					cmbDayTo.setVisible(true);
					cmbMonthTo.setVisible(true);
					cmbYearTo.setVisible(true);
					toTxt.setText("To:");
					break;
				}
				default: {
					System.out.println("No such report type");
					break;
				}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerReportsPageController: pressReport");
			System.out.println(e.getMessage());
		}
	}

    /**
     * Handles the event when the "Create report" button is pressed, initiating the creation of a report
     * based on the selected report type and parameters.
     * 
     * @param event The ActionEvent triggered by pressing the "Create report" button.
     */
	// Event for "Create report" button
	@FXML
	void pressCreateReportBtn(ActionEvent event) {
		try {
			ReportsTypes selectedReportType = null;
			// Get report type, month and year
			String reportType = this.cmbReportType.getValue();
			String day = this.cmbDay.getValue();
			String dayTo = this.cmbDayTo.getValue();
			String month = this.cmbMonth.getValue();
			String monthTo = this.cmbMonthTo.getValue();
			String yearTo = this.cmbYearTo.getValue();
			String year = this.cmbYear.getValue();

			if (reportType != null) {
				for (ReportsTypes type : ReportsTypes.values()) {
					if (type.getDescription().equals(reportType)) {
						selectedReportType = type;
						break;
					}
				}
			} else {
				throw new IllegalArgumentException("You have to select report type.");
			}

			if (selectedReportType != null) {
				switch (selectedReportType) {
				case TOTAL_VISITORS_NUMBER: {

					// if((day.isEmpty()) || (month.isEmpty()) || (year.isEmpty()))
					if ((day == null) || (month == null) || (year == null))
						throw new IllegalArgumentException("You have to select the day, month and year.");

					// Open report page
					ArrayList<String> dataForReport = new ArrayList<>();
					dataForReport.add(parkName);
					dataForReport.add(day);
					dataForReport.add(month);
					dataForReport.add(year);

					this.txtError.setText("");
					SecondPage page = new SecondPage(event, "/gui/VisitingReport.fxml", "", "VisitingReportController",
							"pressBackBtn", dataForReport);
					page.openSecondPage();
					break;
				}
				case ORDER_CANCELLATION: {

					// if((day.isEmpty()) || (month.isEmpty()) || (year.isEmpty()))
					if ((day == null) || (month == null) || (year == null) || (dayTo == null) || (monthTo == null)
							|| (yearTo == null))
						throw new IllegalArgumentException("You have to select the day, month and year.");

					// Open report page
					ArrayList<String> dataForReport = new ArrayList<>();
					dataForReport.add(parkName);
					dataForReport.add(day);
					dataForReport.add(month);
					dataForReport.add(year);
					dataForReport.add(dayTo);
					dataForReport.add(monthTo);
					dataForReport.add(yearTo);

					this.txtError.setText("");
					SecondPage page = new SecondPage(event, "/gui/CancellationReport.fxml", "",
							"CancellationReportController", "pressBackBtn", dataForReport);
					page.openSecondPage();
					break;
				}
				default: {
					System.out.println("No such report type");
					break;
				}
				}
			}
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			this.txtError.setText(e.getMessage());
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerReportsPageController: pressCreateReportBtn");
			System.out.println(e.getMessage());
		}

	}

    /**
     * Handles the event when the "Back" button is pressed, navigating back to the department manager menu.
     * 
     * @param event The ActionEvent triggered by pressing the "Back" button.
     */
	// Event for "Back" button
	@FXML
	void pressBackBtn(ActionEvent event) {
		try {
			NextPage page = new NextPage(event, "/gui/DepartmentManagerMenu.fxml", "Department Manager Menu",
					"DepartmentManagerMenuController", "pressBackBtn", parkName);
			page.Next();
		} catch (Exception e) {
			System.out.println("Error in DepartmentManagerReportsPageController: pressBackBtn");
			System.out.println(e.getMessage());
		}
	}

}
