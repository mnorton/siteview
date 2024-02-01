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
	public static String LOCAL_HOST = "http://localhost:8080";
	public static String FILE_ROOT = "D:/apache-tomcat-9.0.40/webapps";
	public static String FILE_REPO = "D:/dev/siteview/shared/styles";
	
	private static String ALL_SITES_QUERY = "select * from site_registry";
	private static String SITE_QUERY_NAME = "select * from site_registry where name=";
	private static String SITE_QUERY_ID = "select * from site_registry where id=";
	private static String REGISTER_SITE_QUERY = "insert into site_registry (name,path,css) values ";

	/**
	 * Get the file root.
	 * @return the file root.
	 */
	public String getFileRoot() {
		return SiteRegistry.FILE_ROOT;
	}
	
	/**
	 * Get the list of registered sites.
	 * 
	 * @return List of sites
	 * @throws SiteException
	 */
	public List<Site> getSites() throws SiteException {
		Vector<Site> sites = new Vector<Site>();
		
		//	Get a db connector
		Connection connector = null;
		try {
			connector = RegistryConnector.getConnector();
		}
		catch (PageException pg) {
			throw new SiteException("Exception getting a database connector - "+pg.getMessage());
		}
		
		try(Statement stmt = connector.createStatement())  {		
			ResultSet rs = stmt.executeQuery(ALL_SITES_QUERY);
			rs.beforeFirst();
	
			// Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				@SuppressWarnings("unused")
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String path = rs.getString("path");
				String css = rs.getString("css");
				
				Site site = new Site(id, name, path, css);
				
				sites.add(site);			
			}
		}
		catch (SQLException sql) {
			throw new SiteException(sql.getMessage(), sql.getCause());
		}	
		
		return sites;
	}
	
	/**
	 * Return a Site object given the site name.
	 * 
	 * @param name of site
	 * @return Site object or null
	 * @throws SiteException
	 */
	public Site getSiteByName(String name) throws SiteException {
		Site site = null;

		//	Get a db connector
		Connection connector = null;
		try {
			connector = RegistryConnector.getConnector();
		}
		catch (PageException pg) {
			throw new SiteException("Exception getting a database connector - "+pg.getMessage());
		}

		try(Statement stmt = connector.createStatement())  {		
			ResultSet rs = stmt.executeQuery(SITE_QUERY_NAME+"'"+name+"'");
				//	If there is no first, site is returned as null.
				if (rs.first()) {
					int id = rs.getInt("id");
					String path = rs.getString("path");
					String css = rs.getString("css");
					site = new Site(id, name, path, css);
				}
		}
		catch (SQLException sql) {
			throw new SiteException(sql.getMessage(), sql.getCause());
		}	
		
		return site;
	}

	/**
	 * Return a Site object given the site id.
	 * 
	 * @param id of site
	 * @return Site object
	 * @throws SiteException
	 */
	public Site getSiteById(int id) throws SiteException {
		Site site = null;

		//	Get a db connector
		Connection connector = null;
		try {
			connector = RegistryConnector.getConnector();
		}
		catch (PageException pg) {
			throw new SiteException("Exception getting a database connector - "+pg.getMessage());
		}

		try(Statement stmt = connector.createStatement())  {		
			ResultSet rs = stmt.executeQuery(SITE_QUERY_ID+"'"+id+"'");
	
			//	If there is no first, site is returned as null.
			if (rs.first()) {
				String name = rs.getString("name");
				String path = rs.getString("path");
				String css = rs.getString("css");
				site = new Site(id, name, path, css);
			}
		}
		catch (SQLException sql) {
			throw new SiteException(sql.getMessage(), sql.getCause());
		}	

		return site;
	}
	
	/**
	 * Create a new site by registering it.
	 * 
	 * @param name
	 * @param path
	 * @param css
	 * @return new Site object.
	 * @throws SiteException
	 */
	public Site createSite (String name, String path, String css) throws SiteException {
		
		//	Check for null values.
		if ( (name == null) || (path == null) || (css == null) )
			throw new SiteException("Null values passed to create site.");
		
		//	Check to see if this site already is registered.
		Site site = this.getSiteByName(name);
		
		//	Get a db connector
		Connection connector = null;
		try {
			connector = RegistryConnector.getConnector();
		}
		catch (PageException pg) {
			throw new SiteException("Exception getting a database connector - "+pg.getMessage());
		}

		//	Assemble the insert query.
		String query = REGISTER_SITE_QUERY + "('"+name+"','"+path+"','"+css+"')";
		//System.out.println(query);
		System.out.println("Site registered: "+name+": "+path);
		
		//	Insert a new Site record.
		try(Statement stmt = connector.createStatement())  {	
			stmt.execute(query);
		}
		catch (SQLException sql) {
			throw new SiteException(sql.getMessage(), sql.getCause());
		}
		
		//	Get the most recently create index.
		try(Statement stmt = connector.createStatement())  {
			query = "select last_insert_id() as 'index' from site_registry limit 1";
			ResultSet rs = stmt.executeQuery(query);
			
			//	If there is no first, site is returned as null.
			if (rs.first()) {
				int index = rs.getInt("index");
				site = new Site(index, name, path, css);
			}
			
		}
		catch (SQLException sql) {
			throw new SiteException(sql.getMessage(), sql.getCause());
		}

		//	Don't return null, better to throw an exception.
		if (site == null)
			throw new SiteException("Final site object was null.");
		
		return site;
	}

}
