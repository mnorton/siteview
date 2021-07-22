/**
 * 
 */
package com.nolaria.sv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger.Level;

import com.nolaria.sv.PageFramework.FileType;

/**
 * A page models a web page given by a reference:  a relative path plus a page name with an HTML or HTML extension.
 * This model is only concerned with page metadata (such as ref and title), and the contents of the page.
 * Pane management happens in PageFramework.
 * 
 * @author Mark J. Norton - markjorton@gmail.com
 *
 */
public class Page {
	
	String ref = null;				//	Full reference to this page: relative path and page name.
	String relPath = "";			//	The relative path
	String[] relPathNodes = null;	//	The list of directory nodes in the relative path.  Page name is not included.
	String pageName = null;			//	The page name.
	String pageTitle = null;		//	The page title (which may be different than the name.
	String content = null;			//	Contents of the page.

	String pagesRef = null;			//	The pages node plus ref.
	String mediaRef = null;			//	The media node ref.  So far, the media directory is flat, so only one file node.

	
	/**
	 * Page constructor given a reference.
	 * A reference consists of zero or more directory nodes plus a page name with extension.
	 * These notes are separated by a FILE_SEPARATOR.
	 * 
	 * @param ref
	 */
	public Page(String ref) {
		//	Refer should never be passed as null, but just in case ...
		if (ref == null) {
			PageFramework.logger.log(Level.ERROR, "Null reference was passed to Page constructor.");
			ref = "";
		}
		this.ref = ref;
		//this.pagesRef  = "pages/" + ref;
		this.pagesRef = ref;
		this.mediaRef = "media";
		
		//LogHandler.logger.info("Passed ref:  "+ref);
		String[] nodes = ref.split("/");
		int nodeCt = nodes.length;
		
		// Extract page name.
		this.pageName = nodes[nodeCt-1];
		//LogHandler.logger.info("Page name:  "+this.pageName);
		
		// Build the relative path.
		for (int i=0; i<nodeCt-1; i++) {
			this.relPath = this.relPath + nodes[i] + "/";
		}
	}

	/**
	 * 
	 * @return page reference.
	 */
	public String getRef() {
		return this.ref;
	}

	/**
	 * Get the relative path for this page.
	 * 
	 * @return relative path string
	 */
	public String getRelPath() {
		return this.relPath;
	}
	
	/**
	 * Get the array of relative path nodes.
	 * If the array length is 0, ref is at the root.
	 * 
	 * @return array of relative path nodes
	 */
	public String[] getRelPathNodes() {
		if (this.relPathNodes == null) {
			this.relPathNodes = ref.split("/");
			
			// int nodeCt = this.relPathNodes.length;
		}
		return this.relPathNodes;
	}
	
	/**
	 * 
	 * @return page name.
	 */
	public String getPageName() {
		return this.pageName;
	}
	
	/**
	 * 
	 * @return page title.
	 */
	public String getPageTitle() {
		if (this.pageTitle == null) {
			String contents = this.getContent();
			int offset = contents.indexOf("svTitle=\"");
			
			//  Check for title not found and default to page name.
			if (offset == -1) {
				this.pageTitle = this.pageName;
				//this.pageTitle = "UNKNOWN";
				return this.pageTitle;
			}
			else {
				//	Extract the page title using MAX_TITLE_LENGTH as a safety boundary.
				offset += "svTitle=\"".length();	//	Advance past the attribute.

				StringBuffer sb = new StringBuffer();
				for (int i=offset; i<offset+PageFramework.MAX_TITLE_LENGTH; i++) {
					if (contents.charAt(i) == '"')
						break;
					else
						sb.append(contents.charAt(i));
				}
				this.pageTitle=sb.toString();
				PageFramework.logger.log(Level.INFO, "Page title found: "+this.pageTitle);
			}	
		}
		return this.pageTitle;
	}

	/**
	 * Get the contents of the file and show them.
	 * This method can be used to support file processing before being display, though no such processing is done right now.
	 * 
	 * @param reference
	 * @return content text
	 */
	public String getContent() {
		//LogHandler.logger.info("Content file to load: "+this.ref);
		
		//	Returned cached page contents, if available.  Commented out section puts content into the generated page.
		/* if (this.content == null)
			this.content = fetchContents(this.pagesRef);
		return this.content;  */
		
		//	This puts an iFrame into the content pane.
		return "\t<iframe src=\"/"+PageFramework.SITE_NODE+"/"+this.pagesRef+"\" title=\""+this.pageTitle+"\"></iframe>\n";
	}

	
	/**************************************************************************************
	 *                                 Private Classes                                    *         
	 **************************************************************************************/
	
	
	/**
	 * Get the contents of a referenced file and return it.
	 * 
	 * @param reference to a file in the web repo including relative path, "first/second/page.html".
	 * @return file contents
	 */
	protected String fetchContents(String reference) {
		String fn = PageFramework.FILE_ROOT+reference;
		StringBuffer sb = new StringBuffer();
		
		FileReader in = null;
		File refFile = new File(fn);
		/*
		if (refFile.exists() == false) {
			sb.append("Referenced file, "+fn+" doesn't exist.<br>");
			return sb.toString();
		}
		*/
			
		try {
			in = new FileReader(refFile);
			
			//	Read the file referenced into a string buffer.
			int ch = 0;
			while ((ch =in.read()) != -1) {
				sb.append((char)ch);
			}
			in.close();
		}
		catch (IOException io) {
			//sb.delete(0, sb.length());
			sb.append("Error referencing: "+reference+"<br>");
			sb.append("Message: " + io.getMessage()+"<br>");
			sb.append("Cause: " + io.getCause());
			io.printStackTrace();
		}
				
		return sb.toString();
	}
	
	/**
	 * Extract the file extension from the page name passed.
	 * 
	 * @param name
	 * @return extension String
	 */
	protected String getPageNameExtension (String name) {
		//  Check for null or empty.
		if (name == null || name.length() == 0)
			return "";
		
		String[] s = name.split(".");
		
		//  Split should never return 0, but if it does ...
		if (s.length == 0)
			return "";
		
		//	A directory will have no extension.
		else if (s.length == 1)
			return s[0];
		
		//	Return the file extension.  Note that if the name has two dots, the first node will be returned.
		return s[1];
	}
	
	/**
	 * Determine what file type the name is based on extensions.
	 * 
	 * @param name
	 * @return FileType
	 */
	protected FileType getFileType (String name) {
		//  Check for null or empty.
		if (name == null || name.length() == 0)
			return FileType.UNKNOWN;
		
		String[] s = name.split(".");
		
		//  Split should never return 0, but if it does ...
		if (s.length == 0)
			return FileType.UNKNOWN;
		
		//	A directory will have no extension.
		else if (s.length == 1)
			return FileType.DIRECTORY;
		
		//	Check file extension.
		else if (s.length == 2) {
			String ext = s[1];
			switch (ext) {
				case "jpg":
				case "png":
					return FileType.IMAGE;
				case "html":
				case "htm":
					return FileType.HTML;
				case "txt":
					return FileType.TEXT;
				default:
					return FileType.UNKNOWN;
			}
		}

		//	Too many periods!
		else if (s.length > 2)
			return FileType.DIRECTORY;

		return FileType.UNKNOWN;
	}
}
