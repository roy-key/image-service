package db;

import org.apache.log4j.Logger;

import java.sql.*;

public class ConfigDB {

	protected static final Logger _logger = Logger.getRootLogger();

	private String driver = "com.mysql.jdbc.Driver";

	private String dbAddress;

	private String username;

	private String password;

	private Connection connection;

	// counts opened statements
	private int statementsCounter = 0;

	/*
	 * public ConfigDB() {
	 * 
	 * }
	 */

	public ConfigDB(String DBAddress, String userName, String password, String driver) {
		try {

			Class.forName(driver).newInstance();
			setDBAddress(DBAddress);
			setUsername(userName);
			setPassword(password);
			setDriver(driver);
			setConnection();

		}
		catch (Exception e) {
			_logger.error("Failed to load SQL driver: " + e.getMessage() + " : " + e.getStackTrace());
		}
	}

	private void setConnection() {
		try {
			connection = DriverManager.getConnection(dbAddress, username, password);
		}
		catch (SQLException e) {
			_logger.error("Failed to create connection to mSQL: " + e.getLocalizedMessage());
		}
	}

	/**
	 * counts opened Statements should be used in pair with closeStatement().
	 * 
	 * @return Statement
	 */
	public Statement createStatement( ) {
		try {
			if (connection == null || connection.isClosed()) {
				setConnection();
			}
			if (connection != null) {
				statementsCounter++;
				return connection.createStatement( );
			}
			else
				throw new NullPointerException("Trying access null - connection!");
		}
		catch (Exception e) {
			_logger.error("Failed to create statement: " + e.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * closes all passed objects decrements opened statements. when number of opened statements goes to 0, closes connection.
	 * should be used in pair with createStatement()
	 * 
	 * @param st
	 * @param rs
	 */
	public void closeStatement(Statement st, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			statementsCounter--;
			if (statementsCounter <= 0 && connection != null) {
				connection.close();
			}
		}
		catch (SQLException e) {
			_logger.error("Failed to close statement: " + e.getLocalizedMessage());
		}
	}

	/**
	 * @param _DBAddress
	 *            the _DBAddress to set
	 */
	public void setDBAddress(String _DBAddress) {
		this.dbAddress = _DBAddress;
	}

	/**
	 * @return the _DBAddress
	 */
	public String getDBAddress() {
		return dbAddress;
	}

	/**
	 * @param _username
	 *            the _username to set
	 */
	public void setUsername(String _username) {
		this.username = _username;
	}

	/**
	 * @return the _username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param _password
	 *            the _password to set
	 */
	public void setPassword(String _password) {
		this.password = _password;
	}

	/**
	 * @return the _password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	

}
