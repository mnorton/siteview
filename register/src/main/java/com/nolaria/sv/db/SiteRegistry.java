/**
 * 
 */
package com.nolaria.sv.db;

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
	
	private static String ALL_SITES_QUERY = "select id,name,path from site_registry";
	private static String SITE_QUERY_NAME = "select id,path from site_registry where name=";
	private static String SITE_QUERY_ID = "select name,path from site_registry where id=";
	
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
			String id = rs.getString("id");
			String name = rs.getString("name");
			String path = rs.getString("path");
			
			Site site = new Site(id, name, path);
			
			sites.add(site);			
		}
		rs.close();
		stmt.close();
		
		return sites;
	}
	
	/**
	 * Return a Site object given the site name.
	 * 
	 * @param name of site
	 * @return Site object
	 * @throws SQLException
	 */
	public Site getSiteByName(String name) throws SQLException {
		Site site = null;

		Statement stmt = this.connector.createStatement();
		ResultSet rs = stmt.executeQuery(SITE_QUERY_NAME+"'"+name+"'");
		//rs.beforeFirst();
		
		//	If there is no first, site is returned as null.
		if (rs.first()) {
			String id = rs.getString("id");
			String path = rs.getString("path");
			site = new Site(id, name, path);
		}
		
		return site;
	}

	/**
	 * Return a Site object given the site id.
	 * 
	 * @param id of site
	 * @return Site object
	 * @throws SQLException
	 */
	public Site getSiteById(String id) throws SQLException {
		Site site = null;

		Statement stmt = this.connector.createStatement();
		ResultSet rs = stmt.executeQuery(SITE_QUERY_ID+"'"+id+"'");

		//	If there is no first, site is returned as null.
		if (rs.first()) {
			String name = rs.getString("name");
			String path = rs.getString("path");
			site = new Site(id, name, path);
		}

		return site;
	}

}
