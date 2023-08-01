package com.nolaria.sv.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * The Page Index class provides support for searching web pages given one or more keywords.
 * It serves as the API to the page_index table in the site_view database.
 * 
 * @author markjnorton@gmail.com
 */
public class PageIndex {
	private static String RESET_QUERY = "delete from page_index";
	private static Boolean INDEX_ALL_ENDABLED = false;

	/**
	 * This is a runnable entry point to provide support for indexing all pages
	 * and potentially test functions.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PageIndex pageIndex = new PageIndex();
			
			//	Index all pages.
			if (INDEX_ALL_ENDABLED)
				pageIndex.indexAll();
			else
				System.out.println("Index All is disabled.");
		}
		catch (PageException pe) {
			System.out.println("Exception - "+pe.getMessage()+" caused by "+pe.getCause());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Index the page given by extracting keywords and creating entries
	 * in the page_index table.
	 * 
	 * @param page
	 */
	public void index (PageId page) throws PageException {
		List<String> keywords = page.findKeywords();		
		
		Connection connector = RegistryConnector.getConnector();
		
		//	Create all of the queries.
		List<String> queries = new Vector<String>();
		for (String key : keywords) {
			if (key.length() > 40)	//	If the word is too long, just skip it.
				continue;
			StringBuffer qBuf = new StringBuffer();
			qBuf.append("insert into page_index values (");
			qBuf.append("'" + key + "', ");
			qBuf.append("'" + page.id + "'");
			qBuf.append(")");
			queries.add(qBuf.toString());
			//String query = qBuf.toString();
			//System.out.println(query);
		}					

		//	Loop over the queries and execute them.
		String query = null;
		try  {
			Statement stmt = connector.createStatement();
			for (int i=0; i<queries.size(); i++) {
				query = queries.get(i);
				stmt.executeUpdate(query);
			}
		}
		catch (SQLException sql) {
			System.out.println(query);
			throw new PageException(sql.getMessage(), sql.getCause());
		}
	}
	
	/**
	 * Index all non-archived pages.
	 * WARNING:  SiteView search will not work until indexing is completed.
	 * 
	 * @throws PageException
	 */
	public void indexAll() throws PageException {
		Date start = new Date();
		//System.out.println("Start: "+start);

		PageRegistry pr = new PageRegistry();
		List<String> ids = pr.getAllIds();
		
		this.reset();	//	Delete all records in page_index table.
		
		System.out.println("Pages to index: "+ids.size());
		
		//	Loop over pages and index them.
		int i=1;
		for (String id : ids) {
			System.out.println (i+". Processing: "+id);
			PageId page = pr.getPage(id);
			try {
				this.index(page);
			}
			catch (Exception ex) {
				//	Note that the exception is swallowed so that indexing can continue.
				System.out.println("**** Exception in indexAll(): "+ex.getMessage());
			}
			i++;
		}

		Date end = new Date();
		long elapsed = end.getTime() - start.getTime();
		System.out.println("Start: "+start);
		System.out.println("End: "+end);
		System.out.println("Duration: "+elapsed/1000L+" secs");
	}
	
	/**
	 * Reset page indexing by deleting all records in the page_index table.
	 * WARNING:  SiteView search will not work until indexing is restored.
	 * @throws PageException
	 */
	public void reset() throws PageException {
		Connection connector = RegistryConnector.getConnector();
		try  {
			Statement stmt = connector.createStatement();
			stmt.executeUpdate(RESET_QUERY);
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}

	}
	
	/**
	 * Search for pages containing the parameters passed>
	 * 
	 * @param parameters
	 * @return List of PageId
	 */
	public List<PageId> search(String parameters) throws PageException {
		List<PageId> pages = new Vector<PageId>();
		
		PageRegistry pr = new PageRegistry();
		Connection connector = RegistryConnector.getConnector();
		
		String query = "select * from page_index where word like '"+parameters+"'";
		
		try(Statement stmt = connector.createStatement())  {		
			ResultSet rs = stmt.executeQuery(query);
			rs.beforeFirst();
			
			// Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				String id = rs.getString("id");
				
				// String id, String site, String title, String file, String path
				//PageId page = new PageId(id, site, title, file, path);
				PageId page = pr.getPage(id);
				
				pages.add(page);
				}
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
		return pages;
	}
}
