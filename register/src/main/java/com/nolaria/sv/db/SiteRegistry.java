/**
 * 
 */
package com.nolaria.sv.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
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
	public Site createSite (String name, String path, String css, Boolean createHome) throws SiteException {
		
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
		
		//	Initialize the directories and files.
		if (site != null)
			this.setUpFiles(site, createHome);
		else
			throw new SiteException("Final site object was null.");
		
		if (createHome)
			System.out.println("createSite() -Site registered and initialized with home page: "+site.getId()+": "+site.getName());
		else
			System.out.println("createSite() - Site registered and initialized with no home page: "+site.getId()+": "+site.getName());
		
		return site;
	}
	
	/**
	 * Create the site root folder and required folders/files in it.  A flag is passed
	 * to indicate when a stubbed home page should be created (createHome = true).
	 * 
	 * @param site - a registered site object.
	 * @param createHome - if true, create a home page
	 */
	private void setUpFiles (Site site, Boolean createHome) {
		//	Passing a null site is a no-no.
		if (site == null) {
			System.out.println("setUpFiles() - site passed as null, no setup done.");
			return;
		}
		
		//	The file root is where all websites and webapps live on Tomcat.
		String fileRootName = this.getFileRoot();
		System.out.println("File root name: "+fileRootName);
		
		//	The site root is the folder where the managed site lives.
		String siteRootName = fileRootName + "/" +site.getName();
		System.out.println("Site root name: "+siteRootName);
		
		//	Create the root folder, if it doesn't exist.
		File rootFolder = new File(siteRootName);
		if (rootFolder.exists())
			System.out.println("Site root folder already exists.  Skipping this step.");
		else {
			if (rootFolder.mkdir())
				System.out.println("Site root folder created.");
			else {
				System.out.println("Site root folder was not created.");
				return;
			}
		}
		
		//	The site folder is where the content lives.
		String siteFolderName = siteRootName + "/" +site.getName();
		System.out.println("Site folder name: "+siteFolderName);

		//	Create the content folder if it doesn't already exist.
		File siteFolder = new File(siteFolderName);
		if (siteFolder.exists())
			System.out.println("Site content folder already exists.  Skipping this step.");
		else {
			if (siteFolder.mkdir())
				System.out.println("Site content folder created.");
			else {
				System.out.println("Site content folder was not created.");
				return;
			}
		}

		// The media folder is where pictures (etc) are kept for the new site.
		String mediaFolderName = siteRootName + "/media";
		System.out.println("Media folder name: "+mediaFolderName);
		
		//	Create the media folder if it doesn't exist.
		File mediaFolder = new File(mediaFolderName);
		if (mediaFolder.exists())
			System.out.println("Media  folder already exists.  Skipping this step.");
		else {
			if (mediaFolder.mkdir())
				System.out.println("Media folder created.");
			else {
				System.out.println("Media folder was not created.");
				return;
			}
		}
		
		//	Create an empty (ghost) home page.
		if (createHome) {
			//	The home page is in the site root folder, next to the site content folder
			String homePageName = siteRootName +"/"+site.getName()+".html";
			System.out.println("Home page name: "+homePageName);
			
			//	Create the home page and save it.
			File homePage = new File(homePageName);
			if (!homePage.exists()) {
				StringBuffer sb = new StringBuffer();
				sb.append("<!DOCTYPE html>\n");
				sb.append("<html lang=\"en-us\">\n");
				sb.append("<head>\n");
				sb.append("</head>\n");
				sb.append("<body>\n");
				sb.append("</body>\n");
				sb.append("</html>\n");
	
				String content = sb.toString();
				//System.out.println("\nStubbed content: \n"+content+"\n");
				
				String pgTitle = Util.capitalize(site.getName());
				String pgFile = site.getName()+".html";
				String pgId = UUID.randomUUID().toString();
				PageInfo pi = new PageInfo(pgTitle, pgFile, pgId);
				content = Util.updateHeaderInfo(content, site.getCss(), pi);
				//System.out.println("\nUpdated content: \n"+content+"\n");
				
				//	Write the new page out.
				Util.saveFile(content, homePageName);					
			}
			else
				System.out.println("Site page already exists.");
		}

		//	Copy the CSS file from the shared repo.
		String css = site.getCss();
		String cssSrcName = SiteRegistry.FILE_REPO+"/"+css;
		String cssDestName = siteRootName+"/"+css;
		System.out.println("CSS source file name: "+cssSrcName);
		System.out.println("CSS dest file name: "+cssDestName);

		//String cssContent = Util.loadFile(cssSrcName);
		Util.copyFile(cssSrcName, cssDestName);
		System.out.println("The "+css+" file was copied to the new site.");
	}

}
