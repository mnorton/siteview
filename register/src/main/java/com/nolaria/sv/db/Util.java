/**
 * 
 */
package com.nolaria.sv.db;

import java.io.BufferedReader;
import java.io.File;
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
	public static String FILE_ROOT = "D:\\apache-tomcat-9.0.40\\webapps";

	
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
	 * Extract the relative path from a full path.
	 * 
	 * @param path
	 * @return relative path
	 */
	public static String extractRelativePath (String path) {
		return path.substring(FILE_ROOT.length());
	}

	/**
	 * Get the contents of a referenced file and return it.
	 * 
	 * @param reference to a file in the web repo including relative path, "first/second/page.html".
	 * @return file contents
	 */
	public static String fetchContents(String reference) {
		String fn = FILE_ROOT+reference;
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
	 * Generate a new header section with the page information passed and update the content provided.
	 * 
	 * @param content - Current web page content
	 * @param cssFileName - CSS file name to be included in the header.
	 * @param pageInfo - Page title, name, and pid.
	 * 
	 * @return updated page content
	 */
	public static String updateHeaderInfo(String content, String cssFileName, PageInfo pageInfo) {
		//	Create fixed header block.
		StringBuffer fixedHeader = new StringBuffer();
		fixedHeader.append("<!DOCTYPE html>\n");
		fixedHeader.append("<html lang=\"en-us\">\n");
		fixedHeader.append("<head>\n");
		fixedHeader.append("\t<link rel=\"stylesheet\" href=\""+cssFileName+"\">\n");	//	Green is the nolaria theme.
		fixedHeader.append("\t<title>"+pageInfo.title+"</title>\n");
		fixedHeader.append("\t<meta name=\"title\" content=\""+pageInfo.title+"\" />\n");
		fixedHeader.append("\t<meta name=\"name\" content=\""+pageInfo.name+"\" />\n");
		fixedHeader.append("\t<meta name=\"pid\" content=\""+pageInfo.pid+"\" />\n");

		fixedHeader.append("\t<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />\n");
		fixedHeader.append("\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
		fixedHeader.append("\t<meta http-equiv=\"Expires\" content=\"0\" />\n");

		fixedHeader.append("</head>\n");
		
		// System.out.println("\n"+fixedHeader.toString());
		
		String contentRemainder = null;
		int startOff = content.indexOf("<body>");
		
		contentRemainder = content.substring(startOff, content.length());
						
		return fixedHeader+contentRemainder;
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
	
}
