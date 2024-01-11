package com.nolaria.sv.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
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
	private static String DEINDEX_QUERY = "delete from page_index where id='";
	private static Boolean INDEX_ALL_ENABLED = true;

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
			if (INDEX_ALL_ENABLED)
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
	 * 1/7/2024:  Fixed a problem where null ids were getting inserted into the index.
	 * 
	 * @param page
	 */
	public void index (PageId page) throws PageException {
		List<String> keywords = page.findKeywords();		
		
		Connection connector = RegistryConnector.getConnector();
		
		//	Check for a null page id.
		if (page.id == null)
			return;			//	Fixes a problem where null ids were getting inserted into the index.
		
		//	Create all of the queries.
		//List<String> queries = new Vector<String>();
		
		//	Create a bulk insert query for all indexed words on this page.
		StringBuffer qBuf = new StringBuffer();
		qBuf.append("insert into page_index values ");
		for (String key : keywords) {
			//	Filter out junk words.
			char c = key.charAt(0);
			if ((key.length() < 3) || (key.length() > 40))	//	If the word is too short or too long, just skip it.
				continue;
			if (!Character.isAlphabetic(c))
				continue;
			
			// TODO: changing this to a bulk insert will probably speed up indexing.
			// TODO:  INSERT INTO tbl_name (a,b,c) VALUES(1,2,3),(4,5,6),(7,8,9);
			//StringBuffer qBuf = new StringBuffer();
			//qBuf.append("insert into page_index values (");
			qBuf.append("(");
			qBuf.append("'" + key + "',");
			qBuf.append("'" + page.id + "'");
			qBuf.append(")");
			//queries.add(qBuf.toString());
			//String query = qBuf.toString();
			//System.out.println(query);
		}					

		//	Loop over the queries and execute them.
		String query = null;
		try  {
			Statement stmt = connector.createStatement();
			stmt.executeQuery(qBuf.toString());
			
			/*
			for (int i=0; i<queries.size(); i++) {
				query = queries.get(i);
				stmt.executeUpdate(query);
			}
			*/
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
	 * Deindex a page by deleting all index records having the page ID passed.
	 * 
	 * @param pageId of page to deindex.
	 * @throws PageException
	 */
	public void deindex(String id) throws PageException {
		Connection connector = RegistryConnector.getConnector();
		try  {
			Statement stmt = connector.createStatement();
			stmt.executeUpdate(DEINDEX_QUERY+id+"'");
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
		System.out.println ("Page with an id of "+id+" was deindexed.");
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
		
		System.out.println("Searching for "+parameters);
		
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
				
				if (page == null ) {
					//	This can happen if records are deleted without re-indexing.
					System.out.println("Page not found for id of: "+id);
					continue;
				}
				else
					pages.add(page);
				}
		}
		catch (SQLException sql) {
			throw new PageException(sql.getMessage(), sql.getCause());
		}
		
		//	Sort the results on path.
		if (pages == null || pages.size() ==0)
			throw new PageException("No pages were found for "+parameters);
		Collections.sort(pages);
		
		return pages;
	}
}
