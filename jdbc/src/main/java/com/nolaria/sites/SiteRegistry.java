/**
 * 
 */
package com.nolaria.sites;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

/**
 * The SiteRegistry is used to manage site data.
 * 
 * @author markjnorton
 *
 */
public class SiteRegistry {
	private Connection connector =null;
	
	private static String ALL_SITES_QUERY = "select name,path from site_registry";
	
	/**
	 * Constructor given a JDBC connector.
	 * 
	 * @param connector
	 */
	public SiteRegistry(Connection connector) {
		this.connector = connector;
	}
	
	/**
	 * Get the list of registered sites.
	 * 
	 * @return List of sites
	 * @throws SQLException
	 */
	public List<Site> getSites() throws SQLException {
		Vector<Site> sites = new Vector<Site>();
		
		Statement stmt = this.connector.createStatement();
		ResultSet rs = stmt.executeQuery(ALL_SITES_QUERY);
		rs.beforeFirst();

		// Extract data from result set
		while (rs.next()) {
			// Retrieve by column name
			String name = rs.getString("name");
			String path = rs.getString("path");
			
			Site site = new Site(name, path);
			
			sites.add(site);			
		}
		rs.close();
		stmt.close();
		
		return sites;
	}
}
