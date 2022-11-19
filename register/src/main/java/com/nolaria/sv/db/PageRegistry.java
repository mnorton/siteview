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
	 * Create a page given information about it and register it.
	 * 
	 * @param id
	 * @param site
	 * @param title
	 * @param file
	 * @param path
	 * @throws PageException
	 */
	public void createPage(String id, String site, String title, String file, String path) throws PageException {
		//	Escape apostrophes in the title.
		title = title.replace("'", "''");
		
		//this.checkConnector("register");
		
		// TODO:  Add a check to see if the page is already registered.

		//	Assemble the insert query.
		String query = REGISTER_PAGE_QUERY + "('"+id+"','"+site+"','"+title+"','"+file+"','"+path+"')";
		//System.out.println(query);
		System.out.println("Registered: "+file+": "+id);
		
		Connection connector = RegistryConnector.getConnector();

		try(Statement stmt = connector.createStatement())  {		
			stmt.execute(query);
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
	}
	
	/**
	 * Register a missing page.  This method is used when a page is found in the file system
	 * but is not present in the page registry.  The file is expected to have:
	 *     <meta name="title" content="TITLE">
	 *     <meta name="name" content="FILE NAME">
	 *     <meta name="pid" content="UUID">
	 *
	 * If these are not present, a PageException is thrown.
	 * In theory, they could be defaulted, but it would mean updating the content as well
	 * as the page_registry table.
	 *
	 * @param filename
	 * @throws PageException
	 */
	public void registerPage(String site, String relFilePath) throws PageException {
		//System.out.println("Page to register: "+relFilePath);
				
		//	The relPath has the site removed from it's front.
		String relPath = relFilePath;
		if (relPath.contains(site)) {
			relPath = relPath.substring(site.length()+1);
		}
		relPath = relPath.substring(1);

		//	Remove the file name from the path.
		String path = "";
		String parts[] = null;
		
		parts = relPath.split("/");
		if (parts.length == 0)
			System.out.println("Rel path didn't split:  "+relPath);		
		for (int i=0; i<parts.length-1; i++)
			path += "/"+parts[i];
		if (path.length() != 0)
			path = path.substring(1);
		
		//	Extract the file name.
		parts = relFilePath.split("/");
		if (parts.length == 0)
			System.out.println("Rel file path didn't split:  "+relFilePath);		
		String fileName = parts[parts.length-1];
				
		//	Extract metadata from the page file.
		String contents = Util.fetchContents(relFilePath);
		PageInfo info = Util.getHeaderInfo(contents);			
			
		//	Register the page.  This will throw PageException if the page is already registered.
		this.createPage(info.pid, site, info.title, fileName, path);		
	}

	/**
	 * Update the site, title, file, and path of the page given by id.
	 * Note:  old values must be passed if not changed.
	 * 
	 * @param id of the page to update
	 * @param site - new site name.
	 * @param title - new title name.
	 * @param file - new file name.
	 * @param path - new path.
	 * @throws PageException
	 */
	public void updatePage(String id, String site, String title, String file, String path) throws PageException {
		//	Escape apostrophes in the title.
		title = title.replace("'", "''");
		
		//this.checkConnector("register");

		//	Assemble the update query.
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE page_registry SET ");
		sb.append("site='"+site);
		sb.append("', title='"+title);
		sb.append("', file='"+file);
		sb.append("', path='"+path);
		sb.append("' where id='"+id+"';");
		String updatePageQuery = sb.toString();
		
		//System.out.println("Page Update Query:  "+updatePageQuery);
		
		Connection connector = RegistryConnector.getConnector();

		try(Statement stmt = connector.createStatement())  {		
			stmt.execute(updatePageQuery);
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
		//this.checkConnector("soft delete");
		//String query = PAGE_DELETE_BY_ID_QUERY +"'"+id+"'";
		String query = "UPDATE page_registry SET archived='T' where id='"+id+"';";
		
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
		//this.checkConnector("hard delete");
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
