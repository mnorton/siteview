/**
 * 
 */
package com.nolaria.sv;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The Page Framework is the base class of the Site Viewer app. It is
 * responsible for managing all of the panes that can appear for the current
 * request.
 * 
 * @author Mark J. Norton - markjnorton@gmail.com
 *
 */
public class PageFramework {
	public static enum FileType {
		UNKNOWN, DIRECTORY, HTML, TEXT, IMAGE, AUDIO, VIDEO
	};

	public static String SLASH = "%2F";
	public static int MAX_TITLE_LENGTH = 150;
	public static int NAV_LINES = 100;
	public static int MAX_NAV_DEPTH = 100;
	public static String SITE_NODE = "nolaria";
	public static String FILE_ROOT = "C:\\apache-tomcat-9.0.40\\webapps\\"+SITE_NODE+"\\";

	public Page page = null;		//	The page referenced.
	public static Logger logger = System.getLogger("sv");

	/**
	 * Constructor given a page reference.
	 * 
	 * @param ref
	 */
	public PageFramework(String site, String ref) {
		// this.ref = ref;

		//	Add the site name to file root.
		if (site == null) site="norberg3";
		//if (FILE_ROOT.indexOf(site) == -1)
		//	PageFramework.FILE_ROOT += site+"\\";
		
		System.out.println("File Root: "+FILE_ROOT);
		
		// Create a page model.
		this.page = new Page(ref);
		PageFramework.logger.log(Level.INFO, "Requested Page Reference: " + this.page.getRef());
	}

	/**
	 * Get the page associated with this request.
	 * 
	 * @return page
	 */
	public Page getPage() {
		return this.page;
	}

	/**
	 * Get the contents of the banner pane.
	 * 
	 * @return content of the banner pane.
	 */
	public String getBanner() {
		StringBuffer sb = new StringBuffer();

		sb.append(
				"<a href=\"/"+SITE_NODE+"/home.html\"><img float=\"left\" src=\"/"+SITE_NODE+"/media/customLogo.gif\" width=\"500\"/></a>\n");
		//sb.append("&nbsp;&nbsp;&nbsp;&nbsp;\n");
		sb.append("<br>\n");
		//sb.append("<span style=\"font-size: 16pt\"><b>" + this.page.getPageTitle() + "</b></span><br><br><br>\n");
		
		//	This version shows the full file path, that can be used for editing.
		sb.append("<span style=\"font-size: 16pt\"><b>" + this.page.getFullPath() + "</b></span><br><br><br>\n");

		return sb.toString();
	}

	/**
	 * Get the contents of the file and show them. This method can be used to
	 * support file processing before being display, though none right now.
	 * 
	 * @param reference
	 * @return content text
	 */
	public String getContent() {
		return this.page.getContent();
	}

	
	/**
	 * Get the contents of the navigation frame.
	 * 
	 * @return navigation text.
	 */
	public String getNavigation () {
		return this.getDropNavigation();		//	Nested drop down navigation tree.
		//return this.getFullNavigation();		//	Full navigation shows all directories and files.
		//return this.getTwoLevelNavigation();	//  Original navigation: this level and the next.
	}
	
	/**
	 * Generate contents for navigation from the current level plus one level down.
	 * 
	 * @return navigation text.
	 */
	private String getTwoLevelNavigation() {
		StringBuffer sb = new StringBuffer();

		// Add title and link to Home.
		sb.append("\t\t<h2>Site Navigation</h2>\n");
		sb.append("\t<a href='/sv?ref=home.html'>Home</a><br><br>\n");

		// Create the directory path and File object.
		String dirPath = null;
		String relPath = this.page.getRelPath();
		if (relPath.length() > 0)
			dirPath = FILE_ROOT + relPath;
		else
			dirPath = FILE_ROOT;
		File dirFile = new File(dirPath);

		// Check for no files in this directory.
		File[] files = dirFile.listFiles();
		if (files == null || files.length == 0) {
			//sb.append("No files in path: " + dirPath + ".<br>");
			sb.append("No files here.<br>\n");
			return sb.toString();
		}

		// Scan files in this directory.
		for (File f : files) {
			String name = f.getName();
			if (name == null)
				name = "UNKONWN";
			if (f.isDirectory()) {
				// Show directory files.
				sb.append("\t\t" + name + ":<br>\n");
				File[] subFiles = f.listFiles();

				// Show files in sub directory.
				for (File sf : subFiles) {
					String subName = sf.getName();

					// This will be the full path.
					String subPath = sf.getPath();

					// Extract root and pages node to get relative sub-path.
					String subRelPath = subPath.substring(FILE_ROOT.length(),
							subPath.indexOf(subName) - 1);
					subRelPath = subRelPath.replace("\\", SLASH) + SLASH; // Escape the back slashes.

					if (subName == null)
						subName = "UNKNOWN";
					if (sf.isDirectory()) {
						sb.append("\t\t&nbsp;&nbsp;&nbsp;&nbsp;" + subName + ":<br>\n");
					} else {
						if (subName.contains(".html")) {
							sb.append("\t\t&nbsp;&nbsp;&nbsp;&nbsp;<a href='/sv?ref=" + subRelPath + subName + "'>"
									+ subName + "</a><br>\n");
						}
					}
				}
			} else {
				// Show this file.
				sb.append("\t<a href='/sv?ref=" + name + "'>" + name + "</a><br>\n");
			}
		}

		return sb.toString();
	}

	/**
	 * Get the text for full navigation.  This shows all directories starting from the root, indented by level.
	 * 
	 * @return full navigation text.
	 */
	private String getFullNavigation() {
		StringBuffer sb = new StringBuffer();
		this.directoryWalker(0, "", sb);
		
		//	Pad out the nav column to make a full page, at minimum.
		String[] parts = sb.toString().split("\\n");
		if (parts.length < NAV_LINES) {
			for (int i=0; i<NAV_LINES-parts.length; i++)
				sb.append("\t<br>\n");
		}
		return sb.toString();
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
	 * Recurse over the directory tree generating text for each level.
	 * WARNING:  This method uses recursion!
	 * 
	 * @return nav content
	 */
	private void directoryWalker (int level, String relPath, StringBuffer sb) {
		//PageFramework.logger.log(Level.INFO, "Level: " + level + ", Rel Path:  ["+relPath+"]");
		
		//	Convert relative path to a full path.
		String dirPath = FILE_ROOT + relPath;
		File dirFile = new File(dirPath);
		
		// Check for no files in this directory.
		File[] files = dirFile.listFiles();
		if (files == null || files.length == 0) {
			//sb.append("No files in path: " + dirPath + ".<br>");
			sb.append("No files here.<br>\n");
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
		
		for (File f: files) {
			String name = f.getName();
			//String relFilePath = this.extractRelativePath(f.getPath()+"/"+relPath);
			String relFilePath = this.extractRelativePath(f.getPath());		//	Includes /sv/ at the start.
			relFilePath = relFilePath.replaceAll("\\\\", "/");
			//String relPagePath = relFilePath.substring("/sv".length());
			//PageFramework.logger.log(Level.INFO, "SCAN Level: " + level + ", Rel File Path:  "+relFilePath+", Name: "+name);
			
			//	See if this file is a directory.
			if (f.isDirectory()) {
				//PageFramework.logger.log(Level.INFO, "Directory found: " + level + ", Rel Path:  "+relFilePath);
				if (name.compareTo("media") != 0) {
					String fn = name+".html";
					PageFramework.logger.log(Level.INFO, "Directory found: " + level + ", Rel File Path:  ["+relFilePath+"], Name: "+name+", File name: "+fn);
					//sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+fn+"'>"+name+"</a><br>\n");
					//sb.append(directoryWalker(level++, relFilePath, sb.toString()));
					if (level == 0)
						sb.append("<br>"+indent(level)+"<b><a href='/sv?ref="+relFilePath+".html'>"+name+"</a></b><br>\n");
					else
						sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+".html'>"+name+"</a><br>\n");
					directoryWalker(level+1, relFilePath, sb);
				}
			}
			
			//	If not, it is a file.
			else {
				//PageFramework.logger.log(Level.INFO, "File found: " + level + ", Rel Path:  "+relFilePath);
				//	Look up the file and see if it is a directory, skip if so.
				//String pageName = name.substring(0, name.indexOf(".html"));
				//String relFilePath = relPath.substring("/sv/".length());
				
				//	Filter out the style sheet, if it shows up.
				if (name.compareTo("nolaria.css") == 0)
						continue;
				
				//	If the name is not in the directory list, then add it.
				String[] parts = name.split("\\.");
				if (dirList.get(parts[0]) == null) {
					//PageFramework.logger.log(Level.INFO, "File found: " + level + ", Rel Path:  ["+relPath+"], Name: "+name);
					sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+"'>"+name+"</a><br>\n");
				}
			}

		}
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
		
		//	Convert relative path to a full path.
		String dirPath = FILE_ROOT + relPath;
		File dirFile = new File(dirPath);
		
		// Check for no files in this directory.
		File[] files = dirFile.listFiles();
		if (files == null || files.length == 0) {
			//sb.append("No files in path: " + dirPath + ".<br>");
			sb.append("No files here.<br>\n");
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
		
		for (File f: files) {
			String name = f.getName();
			String relFilePath = this.extractRelativePath(f.getPath());		//	Includes /sv/ at the start.
			relFilePath = relFilePath.replaceAll("\\\\", "/");
			
			//	This id is used for the drop down controls.  Using page names led to duplicate ids that prevented some folders from dropping down.
			String randId = UUID.randomUUID().toString();
			
			//	See if this file is a directory.
			if (f.isDirectory()) {
				//PageFramework.logger.log(Level.INFO, "Directory found: " + level + ", Rel Path:  "+relFilePath);
				if (name.compareTo("media") != 0) {
					//String fn = name+".html";
					//sb.append(indent(level)+"<b><a href='/sv?ref="+relFilePath+".html'>"+name+"</a></b><br>\n");
					
					sb.append(tabber(level)+"<li>\n");
					sb.append(tabber(level)+"<input type=\"checkbox\" id=\""+randId+"\"/>\n");
					sb.append(tabber(level)+"<label for=\""+randId+"\">");
					sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+".html'>"+name+"</a>");
					sb.append("</label>\n");
					sb.append(tabber(level)+"<ul>\n");

					//	Recurse into the directory.
					if (level < MAX_NAV_DEPTH) {
						directoryWalkerDrop(level+1, relFilePath, sb);
					}

					sb.append(tabber(level)+"</ul>\n");
					sb.append(tabber(level)+"</li>\n");
				}
			}
			
			//	If not, it is a file.
			else {
				//	Filter out the style sheet, if it shows up.
				if (name.compareTo("nolaria.css") == 0)
						continue;
				
				//	If the name is not in the directory list, then add it.
				String[] parts = name.split("\\.");
				if (dirList.get(parts[0]) == null) {
					//sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+"'>"+name+"</a><br>\n");
					sb.append(tabber(level)+"<li><span>");
					sb.append(indent(level)+"<a href='/sv?ref="+relFilePath+"'>"+name+"</a>");
					sb.append("</span></li>\n");
				}
			}

		}
	}
	
	//  TODO:  remove this once confidence in navigation is restored.
	private String getNavigationOld() {
		StringBuffer sb = new StringBuffer();

		// String[] parts = this.ref.split("\\/");

		// Add title and link to Home.
		sb.append("\t\t<h2>Site Navigation</h2>\n");
		// sb.append("\tXX ref: "+this.ref+"<br>\n");
		// sb.append("\tXX split ref ct: "+parts.length+"<br>\n");
		// sb.append("\tXX relPath: "+this.relPath+"<br>\n");
		// sb.append("\tXX ===============<br><br>\n");
		sb.append("\t<a href='/sv?ref=home.html'>Home</a><br><br>\n");

		// Create File objects.
		String dirPath = null;
		String relPath = this.page.getRelPath();
		if (relPath.length() > 0)
			dirPath = FILE_ROOT + relPath;
		else
			dirPath = FILE_ROOT;
		File dirFile = new File(dirPath);

		// this.logger.log(Level.INFO,"Navigation from directory: "+dirPath);
		// sb.append("\tXX dirPath: "+dirPath+"<br><br>\n");

		// Scan files in this directory.
		File[] files = dirFile.listFiles();
		if (files == null || files.length == 0) {
			sb.append("No files in path: " + dirPath + ".<br>");
			return sb.toString();
		}
		for (File f : files) {
			String name = f.getName();
			if (name == null)
				name = "UNKONWN";
			if (f.isDirectory()) {
				// Show directory files.
				sb.append("\t\t" + name + ":<br>\n");
				File[] subFiles = f.listFiles();
				// sb.append("\tDirectory Files: "+subFiles.length+"<br>\n");

				// Show files in sub directory.
				for (File sf : subFiles) {
					String subName = sf.getName();

					// This will be the full path.
					String subPath = sf.getPath();

					// Extract root and pages node to get relative sub-path.
					String subRelPath = subPath.substring(FILE_ROOT.length(),
							subPath.indexOf(subName) - 1);
					// subRelPath =subRelPath.replace("/", SLASH) + SLASH; // Escape the slashes.
					subRelPath = subRelPath.replace("\\", SLASH) + SLASH; // Escape the back slashes.

					if (subName == null)
						subName = "UNKNOWN";
					if (sf.isDirectory()) {
						sb.append("\t\t&nbsp;&nbsp;&nbsp;&nbsp;" + subName + ":<br>\n");
					} else {
						// sb.append("\t\t&nbsp;&nbsp;&nbsp;&nbsp;Sub File: "+subName+" -
						// "+this.getFileType(subName)+"<br>\n");
						if (subName.contains(".html")) {
							sb.append("\t\t&nbsp;&nbsp;&nbsp;&nbsp;<a href='/sv?ref=" + subRelPath + subName + "'>"
									+ subName + "</a><br>\n");
						}
					}
				}
			} else {
				// Show this file.
				sb.append("\t<a href='/sv?ref=" + name + "'>" + name + "</a><br>\n");
			}
		}

		return sb.toString();

	}

	/**
	 * 
	 * @return page reference.
	 */
	public String getRef() { 
		return this.page.getRef();
		}

	/**
	 * 
	 * @return page name.
	 */
	public String getPageName() { 
		return this.page.getPageName(); 
		}

	/**
	 * 
	 * @return page title.
	 */
	public String getPageTitle() {
		return this.page.getPageTitle();
	}

	/************************************************************************
	 *                   Utility Methods                                    *
	 ***********************************************************************/
	
	/**
	 * Return a string that will indent HTML tags.  This is currently implemented using
	 * non-breaking spaces.  Later, this will be replaced by a DIV with inset border.
	 * @param level
	 * @return
	 */
	private String indent(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++)
			sb.append("&nbsp;&nbsp;");
		return sb.toString();
	}

	private String tabber(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++)
			sb.append("\t");
		return sb.toString();
	}

	/**
	 * Extract the relative path from a full path.
	 * 
	 * @param path
	 * @return relative path
	 */
	private String extractRelativePath (String path) {
		return path.substring(FILE_ROOT.length());
	}

}
