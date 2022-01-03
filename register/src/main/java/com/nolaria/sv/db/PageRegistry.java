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
 * The registry of all pages in a site.
 * 
 * @author markjnorton@gmail.com
 */
public class PageRegistry {
	private Connection connector =null;
	
	private static String PAGE_BY_ID_QUERY = "select site,name,file,path from page_registry where id=";
	private static String ALL_PAGES_QUERY = "select id,site,name,file,path from page_registry where archived!='T'";
	// insert into page_registry (id,site,name,file,path) values ('654b7d10-a431-4cec-9d1d-4262209c9b56','nolaria','Books','books.html','/home');
	private static String REGISTER_PAGE_QUERY = "insert into page_registry (id,site,name,file,path) values ";


	/**
	 * Constructor given a connector.
	 * @param connector
	 */
	public PageRegistry (Connection connector) {
		this.connector = connector;
	}
	
	/**
	 * Get a list of all registered pages.
	 * 
	 * @return List of Page
	 */
	public List<PageId> getAllPages()  throws SQLException {
		List<PageId> pages = new Vector<PageId>();
		
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
			PageId page = new PageId(id, site, name, file, path);
			
			pages.add(page);
		}
		rs.close();
		//stmt.close();

		return pages;
	}

	/**
	 * Get the page object for the id passed.
	 * 
	 * @param id
	 * @return a page or null if no page is found.
	 */
	public PageId getPage(String id) throws SQLException {
		PageId page = null;
		
		Statement stmt = this.connector.createStatement();
		ResultSet rs = stmt.executeQuery(PAGE_BY_ID_QUERY+"'"+id+"'");
		
		//	If there is no first, page is returned as null.
		if (rs.first()) {
			String site = rs.getString("site");
			String name = rs.getString("name");
			String file = rs.getString("file");
			String path = rs.getString("path");
			
			// String id, String site, String name, String file, String path
			page = new PageId(id, site, name, file, path);
		}
		
		rs.close();
		//stmt.close();
		
		return page;
	}
	
	/**
	 * Register a page given information about it.
	 * 
	 * @param id
	 * @param site
	 * @param name
	 * @param file
	 * @param path
	 * @throws SQLException
	 */
	public void registerPage(String id, String site, String name, String file, String path) throws SQLException {
		//	Escape apostrophes in the name.
		name = name.replace("'", "''");
		
		//	Assemble the insert query.
		String query = REGISTER_PAGE_QUERY + "('"+id+"','"+site+"','"+name+"','"+file+"','"+path+"')";
		//System.out.println(query);

		Statement stmt = this.connector.createStatement();;
		stmt.execute(query);
		//stmt.close();
	}
	
}
