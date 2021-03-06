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
	private static String PAGE_BY_ID_QUERY = "select site,title,file,path from page_registry where id=";
	private static String ALL_PAGES_QUERY = "select id,site,title,file,path from page_registry where archived!='T'";
	// insert into page_registry (id,site,title,file,path) values ('654b7d10-a431-4cec-9d1d-4262209c9b56','nolaria','Books','books.html','/home');
	private static String REGISTER_PAGE_QUERY = "insert into page_registry (id,site,title,file,path) values ";
	private static String PAGE_DELETE_BY_ID_QUERY = "delete from page_registry where id=";
		
	/**
	 * Get a list of all registered pages.
	 * 
	 * @return List of Page
	 * @throws PageException
	 */
	public List<PageId> getAllPages()  throws PageException {
		List<PageId> pages = new Vector<PageId>();
		
		//this.checkConnector("get all");
		
		Connection connector = RegistryConnector.getConnector();
		
		try(Statement stmt = connector.createStatement())  {		
			ResultSet rs = stmt.executeQuery(ALL_PAGES_QUERY);
			rs.beforeFirst();
			
			// Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				String id = rs.getString("id");
				String site = rs.getString("site");
				String title = rs.getString("title");
				String file = rs.getString("file");
				String path = rs.getString("path");
				
				// String id, String site, String title, String file, String path
				PageId page = new PageId(id, site, title, file, path);
				
				pages.add(page);
			}
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}

		return pages;
	}

	/**
	 * Get the page object for the id passed.
	 * 
	 * @param id
	 * @return a page or null if no page is found.
	 * @throws PageException
	 */
	public PageId getPage(String id) throws PageException {
		PageId page = null;
		
		//this.checkConnector("get single");
		
		Connection connector = RegistryConnector.getConnector();

		try(Statement stmt = connector.createStatement())  {		
			ResultSet rs = stmt.executeQuery(PAGE_BY_ID_QUERY+"'"+id+"'");
			
			//	If there is no first, page is returned as null.
			if (rs.first()) {
				String site = rs.getString("site");
				String title = rs.getString("title");
				String file = rs.getString("file");
				String path = rs.getString("path");
				
				// String id, String site, String title, String file, String path
				page = new PageId(id, site, title, file, path);
			}
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
		
		return page;
	}
	
	/**
	 * Register a page given information about it.
	 * 
	 * @param id
	 * @param site
	 * @param title
	 * @param file
	 * @param path
	 * @throws PageException
	 */
	public void registerPage(String id, String site, String title, String file, String path) throws PageException {
		//	Escape apostrophes in the title.
		title = title.replace("'", "''");
		
		//this.checkConnector("register");

		//	Assemble the insert query.
		String query = REGISTER_PAGE_QUERY + "('"+id+"','"+site+"','"+title+"','"+file+"','"+path+"')";
		//System.out.println(query);
		
		Connection connector = RegistryConnector.getConnector();

		try(Statement stmt = connector.createStatement())  {		
			stmt.execute(query);
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
	}
	
	/**
	 * This method always performs a soft delete by marking the archived field to true.
	 * 
	 * @param id of page to archive
	 * @throws PageException
	 */
	public void deletePage(String id) throws PageException {
		//this.checkConnector("delete");
		String query = PAGE_DELETE_BY_ID_QUERY +"'"+id+"'";
		
		Connection connector = RegistryConnector.getConnector();
		
		try(Statement stmt = connector.createStatement())  {		
			stmt.execute(query);
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}	
	}
	
	/**
	 * This method always performs a hard delete, removing the record associated with the page id passed.
	 * 
	 * @param id of page to delete
	 * @throws PageException
	 */
	public void hardDeletePage(String id) throws PageException {
		//	TODO:	Write this one.
	}
	
	/**
	 * This is a debug method to determine if the database connector is open/valid.
	 * 
	 * @param operation
	 * @return true if open/valid
	 * 
	 * @throws PageException
	 */
	/*
	private boolean checkConnector(String operation) throws PageException {
		try {
			if (!this.connector.isValid(1)) {
				//System.out.println ("Connector is valid on operation, "+operation);
				return true;
			}
			else {
				System.out.println ("Connector is not valid on operation, "+operation);
				return false;
			}
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}	
	}
	*/
}
