/**
 * 
 */
package com.nolaria.sv.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * This is a collection of methods commonly used to work with pages and sites.
 * All methods are static.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class Util {
	//public static String FILE_ROOT = "D:\\apache-tomcat-9.0.40\\webapps";

	
	/**
	 * Return a string that will indent HTML tags.  This is currently implemented using
	 * non-breaking spaces.  Later, this will be replaced by a DIV with inset border.
	 * @param level
	 * @return
	 */
	public static String indent(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++)
			sb.append("&nbsp;&nbsp;");
		return sb.toString();
	}

	public static String tabber(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++)
			sb.append("\t");
		return sb.toString();
	}

	/**
	 * Get a full file path without the file root.
	 * Note:  This used to be called extractRelativePath(), which it didn't.
	 * 
	 * @param path
	 * @return full path from the site root
	 */
	public static String extractRootPath (String path) {
		return path.substring(SiteRegistry.FILE_ROOT.length());
	}
	
	/**
	 * Given a file name with a full path, return the relative path from the site name node to the file node.
	 * 
	 * Example 1: D:/apache-tomcat-9.0.40/webapps/test/foo/bar/page.html
	 * Returns: foo/bar
	 * 
	 * Example 2: D:/apache-tomcat-9.0.40/webapps/test/page.html
	 * Returns an empty string.
	 * 
	 * @param siteName
	 * @param fileName
	 * @return
	 */
	public static String extractRelativePath(String siteName, String fileName) {
		String fromRootPageName = fileName.substring(fileName.indexOf(siteName)+siteName.length()+1, fileName.length());
		String[] parts = fromRootPageName.split("/");
		String relPath = "";
		if (parts.length > 1) {
			for (int i=0; i<parts.length-1; i++)
				relPath += "/" + parts[i];
		}
		if (relPath.length() > 1)
			relPath = relPath.substring(1, relPath.length());	// Remove first slash.
		return relPath;
	}
	
	/**
	 * Return true if the file name exists.
	 * This is a convenience method for use in test scripts.
	 * 
	 * @param filename
	 * @return true if the file name exists.
	 */
	public static Boolean fileExists (String filename) {
		File f = new File(filename);
		return f.exists();
	}

	/**
	 * Get the contents of a referenced file and return it.
	 * The reference is based on the FILE_ROOT, so a full file name doesn't need to be passed in.
	 * Currently, only PageRegistry.registerPage() uses this.
	 * 
	 * @param reference to a file in the web repo including relative path, "first/second/page.html".
	 * @return file contents
	 */
	public static String fetchContents(String reference) {
		String fn = SiteRegistry.FILE_ROOT+reference;
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
	 * Load the file given by fileName and return it as a String.
	 * 
	 * @param fileName
	 * @return contents of the file
	 */
	public static String loadFile (String fileName) {
		if (fileName == null) {
			System.out.println("Util.loadfile() - a null parameter was passed..");
			return null;
		}
		
		String content = null;
		File srcFile = new File (fileName);
		BufferedReader br = null;
		
		/* Load source file into memory.  */
		try {
			br = new BufferedReader(new FileReader(srcFile));

			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    content = sb.toString();
		}
		catch (IOException io) {
			System.out.println ("Exception when loading file: "+fileName);
			System.out.println (io.getMessage());
			System.out.println (io.getCause());
		}
		catch (Exception ex) {
			System.out.println ("Exception when loading file: "+fileName);
			ex.printStackTrace();
		}
		finally {
			try {
				if (br != null)
					br.close();
			}
			catch (IOException close) {
				System.out.println ("Error on closing file: "+fileName);
			}
		}

		return content;
	}
	
	/**
	 * Save the contents passed to the file named.
	 * 
	 * @param contents
	 * @param fileName
	 */
	public static void saveFile (String contents, String fileName) {
		if (contents == null || fileName == null) {
			System.out.println("Util.savefile() - a null parameter was passed..");
			return;
		}
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream (fileName);

			for (int i=0; i<contents.length(); i++) {
				int datum = (int)contents.charAt(i);
				out.write(datum);				
			}
		}
		catch (IOException io) {
			System.out.println ("Exception when saving file: "+fileName);
			System.out.println (io.getMessage());
			System.out.println (io.getCause());			
		}
		finally {
			try {
				if (out != null)
					out.close();
			}
			catch (IOException close) {
				System.out.println ("Error on closing file: "+fileName);
			}
		}		
	}

	/**
	 * Copy a file.  This will overwrite files that already exists, so be warned!
	 * This is the preferred way to copy files instead of loading and saving from memory.
	 * copyFile() copies binary files.  No strings involved
	 * 
	 * [Moved from Converter - Feb 2, 2024]
	 * 
	 * @param from full file path
	 * @param to full file path
	 * @return true if the file was copied.
	 */
	public static boolean copyFile(String from, String to) {
		boolean copyStatus = true;
		File fromFile = new File(from);
		File toFile = new File(to);
		
		//	Copy the file.
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(fromFile);
			out = new FileOutputStream(toFile);

			// Copy binary data from in to out.
			int datum = 0;
			while ((datum = in.read()) != -1) {
				out.write(datum);
			}
		} 
		catch (IOException io) {
			io.printStackTrace();
			copyStatus = false;
		}
		finally {
			// Close all files.
			try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			}
			catch (IOException io2) {
				io2.printStackTrace();
				copyStatus = false;
			}
		}
		
		copyStatus = true;
		return copyStatus;
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
	public static PageInfo getHeaderInfo(String pgContent) {
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
	 * Return the content of this page between the <BODY> and </BODY> Tags.
	 * 
	 * @param pgContent
	 * @return body content or the whole page content if tags cannot be found.
	 */
	public static String getBodyContent(String pgContent) {
		int bodyStart = pgContent.indexOf("<body>");
		bodyStart += "<body>".length();
		int bodyEnd = pgContent.indexOf("</body>");
		
		//	If we don't find the start or end of the body, return the whole content.
		if ((bodyStart == -1) || (bodyEnd == -1)) {
			return pgContent;
		}
		
		//	Extract the body content.
		String bodyContent = pgContent.substring(bodyStart, bodyEnd);
		
		return bodyContent;
	}
	
	/**
	 * Generate a new header section with the page information passed and update the content provided.
	 * 
	 * Note:  For backwards compatibility, the metadata are retained as follows:
	 * 	-	title
	 * 	-	name - the file name
	 * 	-	pid - the page id (UUID)
	 * 
	 * @param content - Current web page content
	 * @param cssFileName - CSS file name to be included in the header.
	 * @param pageInfo - Page title, file, and id.
	 * 
	 * @return updated page content or null on error.
	 */
	public static String updateHeaderInfo(String content, String cssFileName, PageInfo pageInfo) {
		//	Check the parameters passed are not null.
		if (content == null) {
			System.out.println("Util.updateHeaderInfo() - content string passed is null.");
			return content;
		}
		else if (cssFileName == null) {
			System.out.println("Util.updateHeaderInfo() - CSS file name string passed is null.");
			return content;			
		}
		else if (pageInfo == null) {
			System.out.println("Util.updateHeaderInfo() - page info object passed is null.");
			return content;
		}
		
		//	1.  Create a new header block with CSS and metadata from page info.
		StringBuffer fixedHeader = new StringBuffer();
		fixedHeader.append("<!DOCTYPE html>\n");
		fixedHeader.append("<html lang=\"en-us\">\n");
		fixedHeader.append("<head>\n");
		fixedHeader.append("\t<link rel=\"stylesheet\" href=\""+cssFileName+"\">\n");	//	Green is the nolaria theme.
		fixedHeader.append("\t<title>"+pageInfo.title+"</title>\n");
		fixedHeader.append("\t<meta name=\"title\" content=\""+pageInfo.title+"\" />\n");
		//fixedHeader.append("\t<meta name=\"name\" content=\""+pageInfo.file+"\" />\n");
		//fixedHeader.append("\t<meta name=\"pid\" content=\""+pageInfo.id+"\" />\n");
		fixedHeader.append("\t<meta name=\"file\" content=\""+pageInfo.file+"\" />\n");
		fixedHeader.append("\t<meta name=\"id\" content=\""+pageInfo.id+"\" />\n");

		fixedHeader.append("\t<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />\n");
		fixedHeader.append("\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
		fixedHeader.append("\t<meta http-equiv=\"Expires\" content=\"0\" />\n");

		fixedHeader.append("</head>\n");
				
		//	2.  Determine the offset of the start of content.
		int startOff = content.indexOf("</h1>");	//	This relies on the case being correct in the content file.
		if (startOff == -1) {
			//	Can't find </h1>, so look for <body> instead.
			startOff = content.indexOf("<body>");
			if (startOff == -1) {
				System.out.println("\nUtil.updateHeaderInfo() - Unable to update header info - can't find start if content.");
				return content;		//	Header is not updated.
			}
			else {
				//	Starting offset is found at BODY,
				fixedHeader.append("<body>\n");
				fixedHeader.append("\t<h1>"+pageInfo.title+"</h1>");			
			}
			//else - this is not needed, but preserved to show how it used to behave.
				//	Starting offset is found at BODY.
				//startOff += "<body>".length(); -- Causes the result to lack a <body> tag.
		}
		else {
			//	Starting offset is found at /H1.
			fixedHeader.append("<body>\n");
			fixedHeader.append("\t<h1>"+pageInfo.title+"</h1>");			
			startOff += "</h1>".length()+1;	//	The plus one consumes the old newline.
		}

		//System.out.println("\nFIXED HEADER:\n"+fixedHeader.toString());

		//	3.  Split old header off from the remaining content.
		String contentRemainder = content.substring(startOff, content.length());
		
		//System.out.println("\n\nCONTENT REMANDER: "+contentRemainder.substring(0, 20));
		
		//	4.  Merge the new header with content remainder to create the updated content.
		String updatedContent = fixedHeader.toString()+contentRemainder;
		
		//System.out.println("\nFIXED CONTENT:\n"+updatedContent);

		return updatedContent;
	}

	/**
	 * TODO:  Move this to the com.nolaria.sv.db.Util class
	 * Return a string where each word is capitalized.
	 * For example "pleasure dome 1" becomes "Pleasure Dome 1"
	 * 
	 * Because words are isolated by a space, dashed strings are treated as a single word.
	 * Thus "pleasure dome-1" becomes "Pleasure Dome-1"
	 * 
	 * @param title
	 * @return capitalized title
	 */
	public static String capitalize(String title) {
		//	Capitalize words.
		String[] parts = title.split(" ");
		String capTitle = "";
		for (String word : parts) {
			if (word.length() > 0) {
				char cap = Character.toUpperCase(word.charAt(0));
				String capWord = cap + word.substring(1, word.length());
				//System.out.println("\tCap: "+cap+" word: "+capWord);
				capTitle += capWord + " ";
			}
			else
				capTitle += " ";
		}
		capTitle = capTitle.trim();
		return capTitle;
	}
	
	/**	
	 * Search the content from the current position for a string given in goal and return it's offset.
	 * 
	 * Ex.  Util.lookAhead("<html> ...", 666, "<img")
	 *      <a href="image-ref.html">[<img] src="image-ref">...
	 *      ^ currPos                 ^ frame at newPos
	 *      
	 * @param content
	 * @param currPos
	 * @param goal
	 * @return
	 */
	public static int lookAhead(String content, int currPos, String goal) {
		int newPos = 0;
		int contentLen = content.length();
		int goalLen = goal.length();
		
		while ( (newPos <= contentLen) && (newPos <= currPos+500) ) {
			//	Exit if we reach the end of the content passed.
			if (newPos == contentLen)
				return -1;
			
			//	Exit if we've searched long enough.
			if (newPos == currPos+500)
				return -1;
			
			//	Snapshot the start of the newPos.
			String frame = content.substring(newPos, newPos+goalLen);
			
			//	if we have found what we are looking for, then return it
			if (frame.compareTo(goal) == 0)
				break;
		}
		return newPos;
	}
}
