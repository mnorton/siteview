/**
 * 
 */
package com.nolaria.sv;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.servlet.http.HttpServletRequest;

import com.nolaria.sv.db.*;

/**
 * The Page Id Framework is the base class of the Site Viewer app. It is
 * responsible for managing all of the panes that can appear for the current
 * request.
 * 
 * This framework implements the Page Id Model, which uses globally unique identifiers 
 * to specify a page.
 * 
 * @author Mark J. Norton - markjnorton@gmail.com
 *
 */
public class PageIdFramework {
	public static String DEFAULT_SITE = "nolaria";
	public static String DEFAULT_BANNER = "/media/customLogo.gif";

	private static final String DB_URL = "jdbc:mysql://localhost/site_view";
	private static final String CREDS = "?user=root&password=admin";

	private static SiteRegistry siteRegistry = null;
	private static PageRegistry pageRegistry = null;

	public String error = null;
	public HttpServletRequest request = null;
	
	public String siteName = null;
	public Site site = null;
	public String pageId = null;
	public PageId page = null;
	
	

	/**
	 * Constructor for the Page Id Framework given an HTTP servlet request.
	 * This class relies on a local database called site_view running an accessible with 
	 * previously established credentials.
	 * 
	 * @param req
	 */
	public PageIdFramework (HttpServletRequest req) throws Exception {
		this.request = req;
		
		//	Extract the site parameter, defaulting if need be.
		this.siteName = (String)request.getParameter("site");
		if (this.siteName == null)
			this.siteName = DEFAULT_SITE;
		
		//	Extract the page id parameter.  Show error if none provided.
		this.pageId = (String)request.getParameter("id");
		if (this.pageId == null)
			this.error ="A page identifier was not provided.";
		
		System.out.println ("Page request for: "+this.siteName+" - "+this.pageId);
		
		//	Open a database connection to access associated tables.
		Connection conn = null;
		try {
			//	Open the site and page registries.
			Class.forName("org.mariadb.jdbc.Driver");
			  
			//  Create the registry objects.
			conn = DriverManager.getConnection(DB_URL + CREDS);
			
			siteRegistry = new SiteRegistry(conn);
			pageRegistry = new PageRegistry(conn);
			
			this.site = siteRegistry.getSiteByName(this.siteName);
			if (this.site == null )
				this.error = "Site not found for: "+this.siteName;
			else
				System.out.println ("Site: "+site.toString());
			
			this.page = pageRegistry.getPage(this.pageId);
			if (this.page == null)
				this.error = "Page not found for: "+this.pageId;
			else
				System.out.println ("Page: "+this.page.toString());
			
		}
		catch (Exception ex) {
			this.error = ex.getMessage();
			ex.printStackTrace();
		}
		finally {
			conn.close();
		}
		
		
		
	}
	
	/**
	 * Creates the mark-up content for the banner pane.
	 * 
	 * @return HTML
	 */
	public String getBanner() {
		//	Show any error message instead of the banner.
		if (this.error != null)
			return "<h1>"+this.error+"</h1>";
				
		StringBuffer sb = new StringBuffer();
		
		//	TODO:  Create a means to find the banner image for any site.
		//	Add the banner logo.
		sb.append(
				"\t<a href=\"/"+this.siteName+"/home.html\"><img float=\"left\" src=\"/"+this.siteName+"/"+DEFAULT_BANNER+"\" width=\"500\"/></a>\n");
		//sb.append("&nbsp;&nbsp;&nbsp;&nbsp;\n");
		sb.append("\t<br>\n");
		
		//	TODO:  Add code to handle make page.

		return sb.toString();
	}
	
	/**
	 * Creates the mark-up for the content pane, which is an embedded iFrame.
	 * 
	 * @return HTML
	 */
	public String getContent() {
		return this.page.getIFrame();
	}
	
	public String getNavigation() {
		return "<h1>Navigation</h1>";
	}

	public String getFooter() {
		return "<h1>Footer</h1>";
	}

}
