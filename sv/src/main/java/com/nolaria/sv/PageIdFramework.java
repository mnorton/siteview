/**
 * 
 */
package com.nolaria.sv;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Properties;
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
		
		//	Create the Page and Site Registries.
		siteRegistry = new SiteRegistry();
		pageRegistry = new PageRegistry();
		
		System.out.println("\n============================ Site Viewer =============================\n");
		
		this.site = siteRegistry.getSiteByName(this.siteName);
		if (this.site == null )
			this.error = "Site not found for: "+this.siteName;
		else
			System.out.println ("Site: "+site.toString());
		
		this.page = pageRegistry.getPage(this.pageId);
		if (this.page == null) {
			System.out.println("Page not found for:  "+this.pageId);
			this.error = "Page not found for: "+this.pageId;
			return;
		}
		else
			System.out.println ("Page: "+this.page.toString());
		
		//	Create a map of filenames to pages.  Allows lookup of pages by file name from directory walk.
		List<PageId> pageList = pageRegistry.getAllPages();
		for (PageId lookupPage : pageList) {
			String key = lookupPage.getSite() + "/" + lookupPage.getPath() + "/" + lookupPage.getFile();
			this.pages.put(key, lookupPage);
		}
		
		//	Some debugging logic for the page lookup table.
		/*
		System.out.println("Page lookup table size: "+this.pages.size());
		String testPageName = "nolaria//ab-secmil.html";
		PageId testPage = this.pages.get(testPageName);
		if (testPage == null)
			System.out.println("Unable to lookup "+testPageName);
		else
			System.out.println("Lookup for "+testPageName+" gives an id of: "+testPage.getId());
		
		System.out.println();
		*/		
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
		
		//	Check for a new page request.
		String newTitle = this.request.getParameter("new-title");
		if ( (newTitle != null) && (newTitle.length() > 0) )
			this.createNewPage(newTitle);

		
		//	TODO:  Create a means to find the banner image for any site.
		//	Add the banner logo.
		sb.append("\t<a href=\"http://localhost:8080/sv/?site=nolaria&id=961d30bb-c47b-4908-9762-d5918d477319\"><img float=\"left\" src=\"/"+this.siteName+"/"+DEFAULT_BANNER+"\" width=\"500\"/></a>\n");
		//sb.append("&nbsp;&nbsp;&nbsp;&nbsp;\n");
		sb.append("\t<br>\n");
		
		//	Show full path name of this page.
		//sb.append("\t<div style=\"font-size: 12pt\"><b>" + this.page.getFullPath() + "</b></div><br>\n");
		
		//	New Page Form.
		sb.append("\t<br><div>\n");
		sb.append("\t<form id=\"new-page-form\" method=\"get\" action=\"/sv\">\n");

		sb.append("\t\t<input type=\"hidden\" name=\"site\" value=\""+this.page.getSite()+"\">\n");
		sb.append("\t\t<input type=\"hidden\" name=\"id\" value=\""+this.page.getId()+"\">\n");

		sb.append("\t\t<span style=\\\"color: yellow;\\\"><button type=\"submit\" form=\"new-page-form\">\n");
		sb.append("\t\t\t<b>New Page</b></button></span>&nbsp;&nbsp;\n");
		sb.append("\t\t</span>&nbsp;&nbsp;\n");
		sb.append("\t\t<label for=\"new-title\"><b>Title:</b></label>\n");
		sb.append("\t\t<input type=\"text\" id=\"new-title\" name=\"new-title\">\n");

		sb.append("\t\t&nbsp;&nbsp;<a target=\"_blank\" href=\"" + this.page.getDirectUrl() + "\"/>\n");
		sb.append("\t\t<button type=\"button\">\n");
		sb.append("\t\t\t<b>Print</b>\n");
		sb.append("\t\t</button>\n");
		sb.append("\t\t</a>\n");

		sb.append("\t</form>\n");		
		sb.append("\t</div>\n");

		return sb.toString();
	}
	
	/**
	 * Creates the mark-up for the content pane, which is an embedded iFrame.
	 * 
	 * @return HTML
	 */
	public String getContent() {
		if (this.page == null)
			return "<h1>Page object is null</h1>\n";
		return this.page.getIFrame();
	}
	
	/**
	 * Creates the mark-up for the navigation pane using a drop down style.
	 * 
	 * @return HTML
	 */
	public String getNavigation() {
		if (this.page == null)
			return "<h1>Page object is null</h1>\n";
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
		//System.out.println("Level: " + level + ", Rel Path:  ["+relPath+"]");
		
		//	Check for a missing page.
		if (this.page == null) {
			System.out.println("directoryWalkerDrop:  Page object is missing.");
			return;
		}
		
		//	Check for a missing path.
		String pagePath = this.page.getPath();
		if (pagePath == null) {
			sb.append("\tPath is null for page: "+page.toString()+"<br>\n");
			return;			
		}
		
		//	Otherwise, split it.  If the path is empty, relParts[0] will empty, which is okay.
		String[] relParts = this.page.getPath().split("/");
		
		//	Convert relative path to a full path.
		String dirPath = FILE_ROOT + relPath;
		if (relPath.length() == 0)
			dirPath = FILE_ROOT+"/"+this.siteName;
		File dirFile = new File(dirPath);
		
		//System.out.println("Page path: "+relPath);
		//System.out.println("dirFile: "+dirPath);
		
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
			String relFilePath = Util.extractRelativePath(f.getPath());
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
					
					//	The path is used to create a lookup key.
					relParts = relFilePath.split("/");
					String path = "";
					for (String pt : relParts) {
						//	Skip the site name.
						if (pt.compareTo(this.siteName) == 0)
							continue;
						//	Skip the directory name.
						if (pt.compareTo(name) == 0)
							continue;
						
						path += "/" + pt;
					}
					//	Fix up the path.
					int off = 0;
					for (int i=0; i<path.length(); i++) {
						if (path.charAt(i) != '/')
							break;
						else
							off++;
					}
					path = path.substring(off, path.length());
					
					//	Look up the page by it's filename.  If not found, add error message and continue.
					String key = this.siteName + "/" + path + "/" + fn;
					PageId foundPage = this.pages.get(key);
					if (foundPage == null) {
						//System.out.println("Directory page not found for "+key);
						System.out.println("Directory page not found for "+this.siteName+" - "+path+" - "+fn);
						sb.append(Util.tabber(level)+"Directory page not found for "+key+"<br>");
						continue;
					}

					//	Set the check flag (dropped down).
					String pathParts[] = this.page.getPath().split("/");
					String checked = "";
					if (level < pathParts.length) {
						if (pathParts[level].compareTo(name) == 0)
							checked = " checked=\"true\"";
						//else
						//	System.out.println("Page path ["+this.page.getPath()+"] at level "+(level)+":  ["+pathParts[level]+"] is not the same as ["+name+"]");
					}
					
					sb.append(Util.tabber(level)+"<li>\n");
					sb.append(Util.tabber(level)+"<input type=\"checkbox\" id=\""+randId+"\""+checked+"/>\n");
					sb.append(Util.tabber(level)+"<label for=\""+randId+"\">");
					//sb.append(Util.indent(level)+"<a href='/sv?site="+this.siteName+"&id="+foundPage.getId()+"'>"+name+"</a>");
					String title = foundPage.title;
					sb.append(Util.indent(level)+"<a href='/sv?site="+this.siteName+"&id="+foundPage.getId()+"'>"+title+"</a>");
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
				//if ((name.compareTo("nolaria.css") == 0) || (name.compareTo("blue.css") == 0) )
				//		continue;

				String fn = name;
				
				//	The path is used to create a lookup key.
				relParts = relFilePath.split("/");
				String path = "";
				for (String pt : relParts) {
					//	Skip the site name.
					if (pt.compareTo(this.siteName) == 0)
						continue;
					//	Skip the directory name.
					if (pt.compareTo(fn) == 0)
						continue;
					
					path += "/" + pt;
				}
				//	Fix up the path.
				int off = 0;
				for (int i=0; i<path.length(); i++) {
					if (path.charAt(i) != '/')
						break;
					else
						off++;
				}
				path = path.substring(off, path.length());


				//	Look up the page by it's filename.  If not found, add error message and continue.
				String key = this.siteName + "/" + path + "/" + fn;
				PageId foundPage = this.pages.get(key);
				if (foundPage == null) {
					System.out.println("File page not found for "+this.siteName+" - "+path+" - "+fn);
					//System.out.println("\tKey not found: "+key);
					sb.append(Util.tabber(level)+"File page not found for "+key+"<br>");
					continue;
				}

				//	If the name is not in the directory list, then add it.
				String[] parts = name.split("\\.");
				if (dirList.get(parts[0]) == null) {
					//sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+"'>"+name+"</a><br>\n");
					sb.append(Util.tabber(level)+"<li><span>");
					//sb.append(Util.indent(level)+"<a href='/sv?site="+this.siteName+"&id="+foundPage.getId()+"'>"+name+"</a>");
					String title = foundPage.title;
					sb.append(Util.indent(level)+"<a href='/sv?site="+this.siteName+"&id="+foundPage.getId()+"'>"+title+"</a>");
					sb.append("</span></li>\n");
				}
			}

		}
	}

	
	/************************************************************************
	 *                   Request Processors                                 *
	 ***********************************************************************/

	/**
	 * Create a new page from the title passed in the directory specified by ref.
	 * Note:  the page to be created as a path of the current path plus a node taken from the current file name.
	 * 
	 * @param ref - directory path
	 * @param newTitle - title of the new page.
	 */
	public void createNewPage(String newTitle) {
		
		String path = this.page.getPath();
		
		String currentPageFile = this.page.getFile();
		String node = currentPageFile.substring(0, currentPageFile.indexOf(".html"));
		//String file = newTitle.replaceAll(" ", "-").toLowerCase() + node +".html";
		String file = newTitle.replaceAll(" ", "-").toLowerCase() +".html";
		String pid = UUID.randomUUID().toString();
		
		//	The target path is the path of the current page, plus the directory it will be created in.
		String targetPath = path+"/"+node;
		if (targetPath.charAt(0) == '/')
			targetPath = targetPath.substring(1);

		System.out.println("New page to create: "+newTitle+ " on path:"+targetPath+" in file: "+file);

		//	Register the new page.
		try {
			pageRegistry.registerPage(pid, this.siteName, newTitle, file, targetPath);
		}
		catch (PageException pg) {
			System.out.println("Unable to create page "+newTitle);
			System.out.println("SQL Error:  "+pg.getMessage());
			return;
		}

		//	Create the HTML content of the new page.
		StringBuffer content = new StringBuffer();
		content.append("<!DOCTYPE html>\n");
		content.append("<html lang=\"en-us\">\n");
		
		//	Add HEAD content.
		content.append("<head>\n");
		content.append("\t<link rel=\"stylesheet\" href=\"http://localhost:8080/nolaria/green.css\">\n");
		content.append("\t<title>"+newTitle+"</title>\n");
		content.append("\t<meta name=\"title\" content=\""+newTitle+"\" />\n");
		content.append("\t<meta name=\"name\" content=\""+file+"\" />\n");
		content.append("\t<meta name=\"pid\" content=\""+pid+"\" />\n");

		content.append("\t<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />\n");
		content.append("\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
		content.append("\t<meta http-equiv=\"Expires\" content=\"0\" />\n");
		content.append("</head>\n");

		//	Add BODY content.
		content.append("<body>\n");
		content.append("\t<h1>"+newTitle+"</h1>\n");
		content.append("</body>\n");

		content.append("</html>\n");
		
		//	Extract the path part of the page reference.
		/*
		int extentionOffest = ref.indexOf(".html");
		String path = "";
		if (extentionOffest != -1)
			path = ref.substring(0, extentionOffest);
		*/
		
		//	Make a folder to how the new page, if needed.
		String dirName = FILE_ROOT+"\\"+this.siteName;
		if (path.length() > 0)
			dirName += "\\" + path + "\\" + node;
		else
			dirName += "\\" + node;
		File dirFile = new File(dirName);
		if (!dirFile.exists()) {
			//if (dirFile.mkdir() == true)
				System.out.println("Created a directory called: "+dirFile);
		}
		
		//	Save the contents out to the new page file.
		String fileName = dirName+"\\"+file;
		Util.saveFile(content.toString(), fileName);
		
		System.out.println("Created a page called: "+newTitle+" in a name of: "+file+" with a PID of: "+pid);
		System.out.println("Save new contents to: "+fileName);
	}
}
