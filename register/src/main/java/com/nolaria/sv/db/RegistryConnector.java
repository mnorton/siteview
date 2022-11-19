/**
 * 
 */
package com.nolaria.sv.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class manages a JDBC database connector for use with the Site and Page registries.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class RegistryConnector {
	private static final String DB_URL = "jdbc:mysql://localhost/site_view";
	private static final String CREDS = "?user=root&password=admin";

	private static Connection connector = null;
	
	/**
	 * Get a JDBC connector if one has not been created.  Also check to see if the
	 * current connector is valid and open.  If not, re-create it.
	 * 
	 * @return a JDBC Connection
	 * @throws PageException
	 */
	public static Connection getConnector() throws PageException {
		
		try {

			//	See if the connector has not been created.
			if (RegistryConnector.connector == null) {
				System.out.println ("Creating a new connector.");
				
				//	Create one.
				RegistryConnector.connector = RegistryConnector.newConnector();
			}
		
			//	See if the connector is valid/open.
			else if (!RegistryConnector.connector.isValid(1)) {
				System.out.println ("Connector invalid, creating a new one.");
				
				//	Replacing the current should for a reclaim of the old connector when GC happens.
				RegistryConnector.connector = RegistryConnector.newConnector();
			}
		
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
		
		return RegistryConnector.connector;
	}
	
	
	/**
	 * Return true if the current connector is valid.
	 * 
	 * @return true if valid
	 * @throws PageException
	 */
	public static boolean isValid() throws PageException {
		try {

			//	See if the connector has not been created.
			if (RegistryConnector.connector == null) {
				System.out.println ("Connector is null.");
				return false;
			}
		
			//	See if the connector is valid/open.
			else if (!RegistryConnector.connector.isValid(1)) {
				System.out.println ("Connector is invalid");
				return false;
			}
		
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
		
		return true;
		
	}
	
	/**
	 * Create a new connector.
	 * 
	 * @return a JDBC Connection
	 * @throws PageException
	 */
	@SuppressWarnings("unused")
	private static Connection newConnector() throws PageException {
		Connection conn = null;
		
		try {
			//	Open the site and page registries.
			Class.forName("org.mariadb.jdbc.Driver");
			  
			//  Create the registry objects.
			conn = DriverManager.getConnection(DB_URL + CREDS);
		}
		catch (ClassNotFoundException noClass) {
			throw new PageException(noClass.getMessage(), noClass.getCause());						
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());			
		}
		
		return conn;
	}
}
