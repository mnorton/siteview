package com.nolaria.sv.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

/**
 * The Page Index class provides support for searching web pages given one or more keywords.
 * It serves as the API to the page_index table in the site_view database.
 * 
 * @author markjnorton@gmail.com
 */
public class PageIndex {

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
			StringBuffer qBuf = new StringBuffer();
			qBuf.append("insert into page_index values (");
			qBuf.append("'" + key + "', ");
			qBuf.append("'" + page.id + "', ");
			qBuf.append("'" + page.title + "', ");
			qBuf.append("'" + page.path + "'");
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
}
