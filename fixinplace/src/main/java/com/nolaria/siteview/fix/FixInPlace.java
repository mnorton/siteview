/**
 * 
 */
package com.nolaria.siteview.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * This application is a follow on to the SV Converter.  It is intended to fix any problems
 * that might be left over from conversion.
 * 
 * @author markjnorton@gmail.com
 */
public class FixInPlace {
	public static FixInPlace app = new FixInPlace();
	
	//	Fix one file
	public static String testInputFile = "C:/Users/markj/Documents/Personal/SiteViewer/alberg.html";
	public static String testOutputFile = "C:/Users/markj/Documents/Personal/SiteViewer/alberg-fixed.html";
	
	// Fix all files
	public static String rootFolder = "C:/apache-tomcat-9.0.40/webapps/nolaria";
	
	//  Analysis files.
	public static String bugDataFile = "C:/Users/markj/Documents/Personal/SiteViewer/bug-data.csv";
	
	public int fileCt = 0;	//	Bit of a hack using a global, but whatever, I'm lazy.
	

	/**
	 * Since this application isn't intended to be run in isolation (runs in Eclipse), fixes and analysis
	 * not being performed are commented out.  Bit of hack, but expedient.
	 * 
	 * @param args - not used.
	 */
	public static void main(String[] args) {
		//app.fixOneFile(testInputFile, testOutputFile);	//	Just fix the specified file - used for testing.
		//app.fixAllFiles(rootFolder, testOutputFile);	//	Fix all files starting at the root specified and save to test file.
		app.fixAllFiles(rootFolder, null);			//	Fix all files starting at the root specified and save to real file.
		//app.analyzeAllFiles(rootFolder);				//	Collect bug statistics starting at the root specified.
	}

	/**
	 * Apply fixes to a single file.  This is mostly used for testing purposes, but could be used
	 * for one-off fixing a specific file.
	 * 
	 * @param fileName - full path to the page to be fixed.
	 * @param testOutputFile - path to output file or null if this is not a test.
	 */
	public void fixOneFile (String fileName, String testOutputFile) {
		//	Get the file's contents.
		String content = this.loadFile(fileName);
		
		//	Get the page name.  Used to fix titles.
		File contentFile = new File(fileName);
		String pageName = contentFile.getName();		
		pageName = pageName.substring(0, pageName.indexOf('.'));
		//System.out.println("File to be fixed:  "+fileName+" - Page name: "+pageName);
		
		//	Fix problems.  See comments in method, below.
		try {
			//String fixedContent = this.fixHeader1(content, pageName);	//	Fixes 1
			String fixedContent = this.fixHeader2(content, pageName);	//	Fixes 2
			
			//System.out.println("\n"+fixedContent);
			
			//	Save out the fixed results.
			if (testOutputFile != null)
				this.saveFile(fixedContent, testOutputFile);	//	 Use this one to test a fix.
			else
				this.saveFile(fixedContent, fileName);			//	Use this one to fix for real.
		}
		catch (FixException fe) {
			System.out.println("Fix Exception: "+fe.getMessage());
		}
		System.out.println("File got fixed: "+fileName);
	}

	/**
	 * Fix all files starting at the root folder provided.
	 * 
	 * @param rootFolder
	 */
	public void fixAllFiles(String rootFolder, String testFileOutput) {
		this.recursiveFixWalk(0, rootFolder, testFileOutput);
		System.out.println("Files encountered: "+this.fileCt);
	}

	/**
	 * Walk all files staring at the root file provided and collect bug data.
	 * Results in a CSV file that can be imported into Excel.
	 * 
	 * @param rootFolder
	 */
	public void analyzeAllFiles(String rootFolder) {
		StringBuffer sb = new StringBuffer();
		//	Add column names.
		sb.append("File Name,Header Style,svTitle,Header,Title,Class\n");
		
		//	Walk the files and collect bug data.
		File rootFile = new File(rootFolder);		
		this.recursiveAnalysisWalker(0, rootFile, sb);
		
		//	Add sum equations
		sb.append("\n");
		sb.append("File Count:,"+this.fileCt+"\n");
		sb.append("sytleBug:,"+"=SUM(B1:B"+this.fileCt+")\n");
		sb.append("svTitleBug:,"+"=SUM(C1:C"+this.fileCt+")\n");
		sb.append("headerBug:,"+"=SUM(D1:D"+this.fileCt+")\n");
		sb.append("titleBug:,"+"=SUM(E1:E"+this.fileCt+")\n");
		sb.append("classBug:,"+"=SUM(F1:F"+this.fileCt+")\n");
		
		//	Save the results
		//System.out.println(sb.toString());
		this.saveFile(sb.toString(), bugDataFile);
	}
	
	

	
	/****************************************************************************
	 *		DIRECTORY WALKERS	
	 ****************************************************************************/

	/**
	 * Recursively walk all files and fix them.
	 * 
	 * @param depth - Current depth of directory tree.
	 * @param filePath - Current file or directory.
	 * @param relTargetPath
	 * @throws IOException
	 */
	public void recursiveFixWalk(int depth, String fileName, String testFileOutput) {
		File file = new File(fileName);
		
		//	Check for media folder and skip it.
		if (file.getName().compareTo("media") == 0)
			return;
		
		//	Check for a CSS file and skip it.
		if (file.getName().indexOf(".css") == 0)
			return;
		
		//	If this is a directory, recurse to lower level.
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File newFile : files) {
				this.recursiveFixWalk(depth+1, newFile.getPath(), testOutputFile);
			}
		}
		
		//	Otherwise, fix this file.
		else {
			this.fixOneFile(fileName, null);
			this.fileCt++;
		}

	}
	
	/**
	 * Recursively walk all files and collect bug statistics.
	 * 
	 * @param depth
	 * @param filePath
	 */
	public void recursiveAnalysisWalker (int depth, File file, StringBuffer sb) {
		System.out.println("File to analyze:  "+file.getPath());
		
		//	Check for media folder.
		if (file.getName().compareTo("media") == 0)
			return;
		
		//	If this is a directory, recurse to lower level.
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File newFile : files) {
				this.recursiveAnalysisWalker(depth+1, newFile, sb);
			}
		}
		
		//	Otherwise, collect statistics on this file.
		else {
			this.analyzeFile(file, sb);
		}
	}

	
	/****************************************************************************
	 *		FIX METHODS	
	 ****************************************************************************/

	/**
	 * This fixes the following:
	 * 
	 * 	- Strip off unneeded styling in the HEAD section.
	 *	- Remove svTitle in the HTML tag.  It is not needed.
	 *  - Change HEADER to HEAD.
	 *	- Fix pages with a dash in the name, such as, be-01---wainwright.html.  Google generates a single letter title.
	 *		- Fix title
	 * 		- Fix meta title
	 * 		- Fix meta name
	 * 		- Retain PID
	 * 	- Fix the H2 in the page content.
	 * 
	 * This is done by extracting the title, page, and PID and regenerating the HEAD block
	 * 
	 * @param content
	 * @return repaired content
	 * @throws FixException if HEADER is not found, which implies an already fixed file.
	 */
	public String fixHeader1(String content, String pageName) throws FixException{
		int endHeaderIndex = content.indexOf("</header>");
		if (endHeaderIndex == -1) {
			throw new FixException("Unable to find the closing HEADER element on page:"+pageName);
		}
		
		//	Extract the header block and get page information.
		String headerBlock = content.substring(0, endHeaderIndex);
		PageInfo info = this.getHeaderInfo(headerBlock);
		
		
		//System.out.println("Title: "+title);
		//System.out.println("Name: "+name);
		//System.out.println("PID: "+pid);
		
		//	Test for the single letter case and correct if detected.
		if (info.title.length() == 1) {
			info.title = pageName.replace("-", " ");
			
			//	Capitalize the title.  More involved that it should be.
			if (info.title.length() > 2) {
				StringBuffer newTitle = new StringBuffer();
				String[] parts = info.title.split(" ");
				
				

				for (String part : parts) {
					String caps = part.toUpperCase();
					if (part.length()>0)
						part = caps.substring(0, 1) + part.substring(1, part.length());
					newTitle.append(part+" ");
				}
				info.title = newTitle.toString().trim();
				System.out.println("Fixed title capitalized:  "+info.title);
			}
			else {
				info.title = info.title.toUpperCase();
			}
		}
		
		//	Create fixed header block.
		StringBuffer fixedHeader = new StringBuffer();
		fixedHeader.append("<!DOCTYPE html>\n");
		fixedHeader.append("<html lang=\"en-us\">\n");
		fixedHeader.append("<head>\n");
		fixedHeader.append("\t<link rel=\"stylesheet\" href=\"file:///C:/Web/green.css\">\n");
		fixedHeader.append("\t<title>"+info.title+"</title>\n");
		fixedHeader.append("\t<meta name=\"title\" content=\""+info.title+"\" />\n");
		fixedHeader.append("\t<meta name=\"name\" content=\""+info.name+"\" />\n");
		fixedHeader.append("\t<meta name=\"pid\" content=\""+info.pid+"\" />\n");
		fixedHeader.append("</head>\n");
		
		// System.out.println("\n"+fixedHeader.toString());
		
		String contentRemainder = null;
		int startOff = content.indexOf("</h1>");
		
		/*	If there is not H1 block in theb body, these other tests might be able to figure out the start.
		if (startOff == -1) {
			startOff = content.indexOf("<body");
			fixedHeader.append("");
		}
		if (startOff == -1) {
			startOff = content.indexOf("</header>");
		}
		if (startOff == -1) {
			startOff = content.indexOf("</head>");
		}
		*/
		contentRemainder = content.substring(startOff+"</h1>".length(), content.length());
		
		StringBuffer fixedContent = new StringBuffer(fixedHeader);
		fixedContent.append("<body>\n");
		fixedContent.append("<h1>"+info.title+"</h1>");
		fixedContent.append(contentRemainder);
				
		return fixedContent.toString();
	}
	
	/**
	 * This assumes that fixHeader1 has already been run and )that HEADER has been converted to HEAD.
	 * The following is fixed:
	 * 
	 * 	-	Change style sheet link to http://localhost:8080/nolaria/green.css
	 * 	-	Add meta tags to prevent caching of page.
	 * 
	 * @param content of this web page
	 * @param pageName - file name with no path
	 * @return repaired content
	 * @throws FixException if HEAD is not found, which implies an already fixed file.
	 */
	public String fixHeader2(String content, String pageName) throws FixException{
		int endHeaderIndex = content.indexOf("</head>");
		if (endHeaderIndex == -1) {
			throw new FixException("Unable to find the closing HEAD element on page:"+pageName);
		}
		
		//	Extract the header block and get page information.
		String headerBlock = content.substring(0, endHeaderIndex);
		PageInfo info = this.getHeaderInfo(headerBlock);
		
		//System.out.println("Title: "+info.title);
		//System.out.println("Name: "+info.name);
		//System.out.println("PID: "+info.pid);
				
		//	Create fixed header block.
		StringBuffer fixedHeader = new StringBuffer();
		fixedHeader.append("<!DOCTYPE html>\n");
		fixedHeader.append("<html lang=\"en-us\">\n");
		fixedHeader.append("<head>\n");
		fixedHeader.append("\t<link rel=\"stylesheet\" href=\"http://localhost:8080/nolaria/green.css\">\n");
		fixedHeader.append("\t<title>"+info.title+"</title>\n");
		fixedHeader.append("\t<meta name=\"title\" content=\""+info.title+"\" />\n");
		fixedHeader.append("\t<meta name=\"name\" content=\""+info.name+"\" />\n");
		fixedHeader.append("\t<meta name=\"pid\" content=\""+info.pid+"\" />\n");

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
	 * Analyze the file passed for bugs.
	 * 
	 * @param file
	 * @param sb
	 */
	public void analyzeFile(File file, StringBuffer sb) {
		String filePath = file.getPath();
		String styleBug = "0";
		String svTitleBug = "0";
		String headerBug = "0";
		String titleBug = "0";
		String classBug = "0";
		
		//	Load the contents of the file to analyze.
		String content = this.loadFile(filePath);
		if (content == null) {
			System.out.println("Unable to load file: "+filePath);
			return;
		}
		
		//	styleBug checks for extra STYLE tag in header.
		int styleBugOffset = content.indexOf("<style>");
		if (styleBugOffset >= 0) styleBug = "1";
		
		//	svTitleBug checks for svTitle in the HTML element.
		int svTitleBugOffset = content.indexOf("<style>");
		if (svTitleBugOffset >= 0) svTitleBug = "1";
		
		//	headerBug checks for HEADER instead of HEAD.
		int headerBugOffset = content.indexOf("<style>");
		if (headerBugOffset >= 0) headerBug = "1";
		
		//	titleBug checks for single character titles.
		int startTitleOffset = content.indexOf("<title>");
		if (startTitleOffset != -1) {
			int endTitleOffset = content.indexOf("</title>", startTitleOffset+1);
			String title = content.substring(startTitleOffset+"<title>".length(), endTitleOffset);
			if (title.length()==1) titleBug = "1";
		}

		//	classBug checks for the presence of class attributes that are no longer needed>
		int classeBugOffset = content.indexOf("<style>");
		if (classeBugOffset >= 0) classBug = "1";
		
		//	Add stats for this file.
		sb.append(filePath+","+styleBug+","+svTitleBug+","+headerBug+","+titleBug+","+classBug+","+"\n");
		this.fileCt += 1;
	}
	
	/****************************************************************************
	 *		UTILITY METHODS	
	 ****************************************************************************/

	/**
	 * This utility method takes the header block of content and extracts key page data including:
	 * 
	 * 	-	Title
	 * 	-	Name
	 * 	-	PID (page identifier)
	 * 
	 * @param headerBlock
	 * @return PageInfo object
	 */
	public PageInfo getHeaderInfo(String headerBlock) {
		String title = null;	//	The page title.
		String name = null;		//	The page name.
		String pid = null;		//	The page identifier (UUID).
				
		//  Extract the title.
		int startTitleOffset = headerBlock.indexOf("<title>");
		int endTitleOffset = headerBlock.indexOf("</title>", startTitleOffset+1);
		title = headerBlock.substring(startTitleOffset+"<title>".length(), endTitleOffset);
		
		//	Extract the name.
		String nameStart = "name=\"name\" content=\"";
		int startNameOffset = headerBlock.indexOf(nameStart);
		int endNameOffset = headerBlock.indexOf("\"", startNameOffset+nameStart.length());
		name = headerBlock.substring(startNameOffset+nameStart.length(), endNameOffset);
		
		//	Extract the page identifier.
		String idStart = "name=\"pid\" content=\"";
		int startPidOffset = headerBlock.indexOf(idStart);
		int endPidOffset = headerBlock.indexOf("\"", startPidOffset+idStart.length());
		pid = headerBlock.substring(startPidOffset+idStart.length(), endPidOffset);
		
		return new PageInfo(title,name,pid);		
	}
	
	/**
	 * Load the file given by fileName and return it as a String.
	 * 
	 * @param fileName
	 * @return contents of the file
	 */
	public String loadFile (String fileName) {
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
	public void saveFile (String contents, String fileName) {
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
	 * Return a string with a set of tabs equal to the depth passed.
	 * 
	 * @param depth
	 * @return tab string
	 */
	public String indent(int depth) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < depth; i++)
			sb.append("\t");
		return sb.toString();
	}
}
