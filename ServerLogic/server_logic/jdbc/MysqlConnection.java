/**
 * This class provides methods to establish a connection to a MySQL database.
 */
package jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class MysqlConnection {
	 /**
     * Establishes a connection to the MySQL database.
     * 
     * @return A Connection object representing the database connection, or null if the connection fails.
     */
	public static Connection connectDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
			return null;
		}

		try {
			//Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/go_nature?serverTimezone=IST", "root", "Aa123456");
			
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/go_nature?serverTimezone=IST","Anna Garmash","Aa123456!");
			System.out.println("SQL connection succeed");
			return conn;
		} catch (SQLException ex) {/* handle any errors */
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			return null;
		}

	}

}
