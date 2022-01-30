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
 * @author markjnorton
 */
public class PageRegistry {
	private Connection connector =null;
	
	private static String PAGE_BY_ID_QUERY = "select site,name,file,path from page_registry where id=";
	private static String ALL_PAGES_QUERY = "select id,site,name,file,path from page_registry where archived!='T'";

	/**
	 * Constructor given a connector.
	 * @param connector
	 */
	public PageRegistry (Connection connector) {
		this.connector = connector;
	}
	
	/**
	 * Get the page object for the id passed.
	 * 
	 * @param id
	 * @return a page or null if no page is found.
	 */
	public Page getPage(String id) throws Exception {
		Page page = null;
		
		Statement stmt = this.connector.createStatement();
		ResultSet rs = stmt.executeQuery(PAGE_BY_ID_QUERY+"'"+id+"'");
		
		rs.beforeFirst();
		if (!rs.next()) {
			//	Handle the page not found situation.  TODO:  Create a custom exception.
			return  null;
		}
		
		String site = rs.getString("site");
		String name = rs.getString("name");
		String file = rs.getString("file");
		String path = rs.getString("path");
		
		// String id, String site, String name, String file, String path
		page = new Page(id, site, name, file, path);
		rs.close();
		stmt.close();
		
		return page;
	}
	
	/**
	 * Get a list of all registered pages.
	 * 
	 * @return List of Page
	 */
	public List<Page> getAllPages()  throws SQLException {
		List<Page> pages = new Vector<Page>();
		
		Statement stmt = this.connector.createStatement();
		ResultSet rs = stmt.executeQuery(ALL_PAGES_QUERY);
		rs.beforeFirst();
		
		// Extract data from result set
		while (rs.next()) {
			// Retrieve by column name
			String id = rs.getString("id");
			String site = rs.getString("site");
			String name = rs.getString("name");
			String file = rs.getString("file");
			String path = rs.getString("path");
			
			// String id, String site, String name, String file, String path
			Page page = new Page(id, site, name, file, path);
			
			pages.add(page);
		}
		rs.close();
		stmt.close();

		return pages;
	}
}
