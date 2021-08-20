/**
 * 
 */
package com.nolaria.sv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
//import java.lang.System.Logger.Level;
import java.util.UUID;

import com.nolaria.sv.PageFramework.FileType;

/**
 * A page models a web page given by a reference:  a relative path plus a page name with an HTML or HTML extension.
 * The Page model has been extended to include ref based information and meta data element in the HEAD of the contents.
 * Pane management happens in PageFramework.
 * 
 * @author Mark J. Norton - markjorton@gmail.com
 *
 */
public class Page {	
	String FileRoot = null;			//	The root file path to the site.
	String ref = "";				//	Full reference to this page: relative path and page name.
	String relPath = "";			//	The relative path
	String[] relPathNodes = null;	//	The list of directory nodes in the relative path.  Page name is not included.
	
	String pageName = null;			//	The page file name, such as page.html.  Extension is included.
	String pageTitle = null;		//	The page title.
	String pageId = null;			//	The page identifier.
	String content = null;			//	Contents of the page.

	//String mediaRef = "media";		//	The media node ref.  So far, the media directory is flat, so only one file node.

	
	/**
	 * Page constructor given a reference.
	 * A reference consists of zero or more directory nodes plus a page name with extension.
	 * These notes are separated by a FILE_SEPARATOR.
	 * 
	 * If the page reference is null, it defaults to an empty string.
	 * 
	 * @param ref
	 */
	public Page(String ref, String root) {
		//	Save the page reference.
		this.FileRoot = root;
		if (ref != null) {
			this.ref = ref;
		}
		
		// Extract page name.
		String[] nodes = ref.split("/");
		int nodeCt = nodes.length;		
		this.pageName = nodes[nodeCt-1];
		
		// Build the relative path.
		for (int i=0; i<nodeCt-1; i++) {
			this.relPath = this.relPath + nodes[i] + "/";
		}
		
		//	Extract page information from the header.
		PageInfo info = this.getHeaderInfo(this.getContent());
		this.pageTitle = info.title;
		if (info.pid == null)
			this.pageId = UUID.randomUUID().toString();
		else
			this.pageId = info.pid;
		//System.out.println(info.toString());
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
	 * Get the full file name paths for this page.
	 * 
	 * @return full path string
	 */
	public String getFullPath() {
		//return PageFramework.FILE_ROOT + this.getRelPath() + this.getRef();
		return this.FileRoot + this.getRef();
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
		/*	This approach is out of date
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
		}  */
		
		//	If there is no page title, then create one from the page name.
		if (this.pageTitle == null) {
			this.pageTitle = this.getPageName();
			this.pageTitle = this.pageTitle.substring(0, this.pageTitle.indexOf(".html"));
		}
		
		return this.pageTitle;
	}
	
	
	/**
	 * Get the page identifier.
	 * @return
	 */
	public String getPageId() {
		return this.getPageId();
	}

	/**
	 * Get the contents of the file associated with the page..
	 * 
	 * @return content text
	 */
	public String getContent() {
		//	Returned cached page contents, if available.  Commented out section puts content into the generated page.
		if (this.content == null)
			this.content = fetchContents(this.ref);

		return this.content;		
	}
	
	/**
	 * Get HTML markup to embed this page in an iFrame.
	 * @return iFrame markup
	 */
	public String getIFrame() {
		//	This is the markup that puts an iFrame into the content pane.
		return "\t<iframe src=\"/"+PageFramework.SITE_NODE+"/"+this.ref+"\" title=\""+this.pageTitle+"\"></iframe>\n";
		
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
		String fn = this.FileRoot+reference;
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
	
	/**
	 * This utility method takes the page content and extracts key page data including:
	 * 
	 * 	-	Title
	 * 	-	Name
	 * 	-	PID (page identifier)
	 * 
	 * If any of these data are not found, null is return in that field.
	 * 
	 * @param headerBlock
	 * @return PageInfo object
	 */
	protected PageInfo getHeaderInfo(String pgContent) {
		String title = null;	//	The page title.
		String name = null;		//	The page name.
		String pid = null;		//	The page identifier (UUID).
				
		//  Extract the title.
		int startTitleOffset = pgContent.indexOf("<title>");
		int endTitleOffset = pgContent.indexOf("</title>", startTitleOffset+1);
		if (startTitleOffset > 0 && endTitleOffset > 0)
			title = pgContent.substring(startTitleOffset+"<title>".length(), endTitleOffset);
		
		//	Extract the name.
		String nameStart = "name=\"name\" content=\"";
		int startNameOffset = pgContent.indexOf(nameStart);
		int endNameOffset = pgContent.indexOf("\"", startNameOffset+nameStart.length());
		if (startNameOffset > 0 && endNameOffset > 0)
			name = pgContent.substring(startNameOffset+nameStart.length(), endNameOffset);
		
		//	Extract the page identifier.
		String idStart = "name=\"pid\" content=\"";
		int startPidOffset = pgContent.indexOf(idStart);
		int endPidOffset = pgContent.indexOf("\"", startPidOffset+idStart.length());
		if (startPidOffset > 0 && endPidOffset > 0)
			pid = pgContent.substring(startPidOffset+idStart.length(), endPidOffset);
		
		return new PageInfo(title,name,pid);	
	}
	
	/**
	 * Returns a formatted string with all page information.  This is intended for debugging purposes.
	 * @return page infor string
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Information associated with: "+this.pageTitle+"\n");
		sb.append("\tPath Info:\n");
		sb.append("\t\tref: "+this.ref+"\n");
		sb.append("\t\trelPath: "+this.relPath+"\n");
		//sb.append("\t\trelPathNodes: "+this.relPathNodes.toString()+"\n");
		sb.append("\tMeta Data: "+"\n");
		sb.append("\t\tpageName: "+this.pageName+"\n");
		sb.append("\t\tpageTitle: "+this.pageTitle+"\n");
		sb.append("\t\tpagePid: "+this.pageId+"\n");
		//sb.append("\tmediaRef: "+this.mediaRef+"\n");
				
		return sb.toString();
	}
}
