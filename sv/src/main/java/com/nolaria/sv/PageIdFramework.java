/**
 * 
 */
package com.nolaria.sv;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

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
	public static int MAX_NAV_DEPTH = 100;
	public static String FILE_ROOT = "D:\\apache-tomcat-9.0.40\\webapps";
	
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
	
	public Map<String,PageId> pages = new HashMap<String,PageId>();
	

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
			
			System.out.println("\n============================ Site Viewer =============================\n");
			
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
			
			//	Create a map of filenames to pages.  Allows lookup of pages by file name from directory walk.
			List<PageId> pageList = pageRegistry.getAllPages();
			for (PageId page : pageList) {
				this.pages.put(page.getFile(), page);
			}
			
			//	Some debugging logic for the page lookup table.
			/*
			System.out.println("Page lookup table size: "+this.pages.size());
			String testPageName = "ab-secmil.html";
			PageId testPage = this.pages.get(testPageName);
			if (testPage == null)
				System.out.println("Unable to lookup "+testPageName);
			else
				System.out.println("Lookup for "+testPageName+" gives an id of: "+testPage.getId());
			
			System.out.println();
			*/
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
	
	/**
	 * Creates the mark-up for the navigation pane using a drop down style.
	 * 
	 * @return HTML
	 */
	public String getNavigation() {
		return this.getDropNavigation();
	}

	public String getFooter() {
		return "<h1>Footer</h1>";
	}
	
	/**
	 * Get the text for full navigation using drop down icons.
	 * 
	 * @return full drop down navigation text.
	 */
	private String getDropNavigation() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<div data-ui-css-component=\"treeview\"><ul>\n");
		this.directoryWalkerDrop(0, "", sb);
		sb.append("</ul></div>\n");
		
		return sb.toString();		
	}

	/**
	 * Recurse over the directory tree generating text for each folder level.
	 * This version generates HTML that uses the drop down navigation controls in CSS.
	 * 
	 * WARNING:  This method uses recursion!
	 * 
	 * @return drop down nav content
	 */
	private void directoryWalkerDrop (int level, String relPath, StringBuffer sb) {
		//PageFramework.logger.log(Level.INFO, "Level: " + level + ", Rel Path:  ["+relPath+"]");
		
		String[] relParts = this.page.getPath().split("/");
		
		//	Convert relative path to a full path.
		String dirPath = FILE_ROOT + relPath;
		if (relPath.length() == 0)
			dirPath = FILE_ROOT+"/"+this.siteName;
		File dirFile = new File(dirPath);
		
		System.out.println("Page path: "+this.page.getPath());
		System.out.println("dirFile: "+dirFile);
		
		// Check for no files in this directory.
		File[] files = dirFile.listFiles();
		if (files == null || files.length == 0) {
			sb.append("No files in path: " + dirPath + ".<br>");
			//sb.append("No files here.<br>\n");
			return;
		}
		
		TreeMap<String,File> fileList = new TreeMap<String,File>();
		TreeMap<String,File> dirList = new TreeMap<String,File>();		

		//	Iterate over all files in this directory and sort them into maps.
		for (File f: files) {
			String name = f.getName();
						
			//	See if this file is a directory.
			if (f.isDirectory()) {
				//PageFramework.logger.log(Level.INFO, "Directory name added to list: " +name);
				dirList.put(name, f);
			}
			
			//	If not, it is a file.
			else {
				fileList.put(name, f);
			}
		}
		
		//	Iterate over the files and generate navigation content.
		for (File f: files) {
			String name = f.getName();
			String relFilePath = Util.extractRelativePath(f.getPath());		//	Includes /sv/ at the start.
			relFilePath = relFilePath.replaceAll("\\\\", "/");

			//	Check for and skip style sheets.
			if (name.indexOf(".css") >= 0)
				continue;

			//	This id is used for the drop down controls.  Using page names led to duplicate ids that prevented some folders from dropping down.
			String randId = UUID.randomUUID().toString();
			
			//	See if this file is a directory.
			if (f.isDirectory()) {
				//System.out.println("Recursion level: "+level+" relFilePath: "+relFilePath+" name: "+name);
				if (name.compareTo("media") != 0) {
					String fn = name+".html";

					//	Look up the page by it's filename.  If not found, add error message and continue.
					PageId page = this.pages.get(fn);
					if (page == null) {
						System.out.println("Directory page not found for "+relFilePath+" - "+fn);
						sb.append(Util.tabber(level)+"Directory page not found for "+relFilePath+" - "+fn+"<br>");
						continue;
					}

					//	Set the check flag (dropped down).
					String checked = "";
					if (level < relParts.length)
						if (relParts[level].compareTo(name) == 0)
							checked = " checked=\"true\"";
						else
							System.out.println(relParts[level]+" is not the same as "+name);
					
					sb.append(Util.tabber(level)+"<li>\n");
					sb.append(Util.tabber(level)+"<input type=\"checkbox\" id=\""+randId+"\""+checked+"/>\n");
					sb.append(Util.tabber(level)+"<label for=\""+randId+"\">");
					sb.append(Util.indent(level)+"<a href='/sv?site="+this.siteName+"&id="+page.getId()+"'>"+name+"</a>");
					sb.append("</label>\n");
					sb.append(Util.tabber(level)+"<ul>\n");

					//	Recurse into the directory.
					if (level < MAX_NAV_DEPTH) {
						directoryWalkerDrop(level+1, relFilePath, sb);
					}

					sb.append(Util.tabber(level)+"</ul>\n");
					sb.append(Util.tabber(level)+"</li>\n");
				}
			}
			
			//	If not, it is a file.
			else {
				//	Filter out the style sheet, if it shows up.
				if ((name.compareTo("nolaria.css") == 0) || (name.compareTo("blue.css") == 0) )
						continue;

				String fn = name;

				//	Look up the page by it's filename.  If not found, add error message and continue.
				PageId page = this.pages.get(fn);
				if (page == null) {
					System.out.println("File page not found for "+relFilePath+" - "+fn);
					sb.append(Util.tabber(level)+"File page not found for "+relFilePath+" - "+fn+"<br>");
					continue;
				}

				//	If the name is not in the directory list, then add it.
				String[] parts = name.split("\\.");
				if (dirList.get(parts[0]) == null) {
					//sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+"'>"+name+"</a><br>\n");
					sb.append(Util.tabber(level)+"<li><span>");
					sb.append(Util.indent(level)+"<a href='/sv?site="+this.siteName+"&id="+page.getId()+"'>"+name+"</a>");
					//sb.append(Util.indent(level)+"<a href='/sv?ref="+relFilePath+"'>"+name+"</a>");
					sb.append("</span></li>\n");
				}
			}

		}
	}

}
