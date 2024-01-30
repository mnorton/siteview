/**
 * 
 */
package com.nolaria.siteview.fix;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import com.nolaria.sv.db.*;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.DocumentType;

import org.jsoup.nodes.*;
import org.jsoup.select.*;

//import sun.nio.cs.ext.TIS_620;

/**
 * This application is a follow on to the SV Converter.  It is intended to fix any problems
 * that might be left over from conversion.
 * 
 * @author markjnorton@gmail.com
 */
public class FixInPlace {
	public static FixInPlace app = new FixInPlace();
	
	//	Fix one file
	//public static String testInputFile = "C:/Users/markj/Documents/Personal/SiteViewer/alberg.html";
	public static String testInputFile = "D:/Personal/SiteView/people.html";
	//public static String testOutputFile = "C:/Users/markj/Documents/Personal/SiteViewer/alberg-fixed.html";
	public static String testOutputFile = "D:/Personal/SiteView/people.html";
	
	// Fix all files
	//public static String rootFolder = "C:/apache-tomcat-9.0.40/webapps/nolaria";
	public static String rootFolder = "D:/apache-tomcat-9.0.40/webapps/nolaria";
	public static String mediaFolder = "D:/apache-tomcat-9.0.40/webapps/nolaria/media/";
	
	//  Analysis files.
	public static String bugDataFile = "C:/Users/markj/Documents/Personal/SiteViewer/bug-data.csv";
	
	//	Fix codes are used by the generic recursivefileNameFix() tree walker to identify which fix to apply.
	//  FUZZY_EXACT: Single hit on title match.
	//  FUZZY_FIRST: Multiple hits on title match, first one is used.
	public static enum Fix_Code {FUZZY_EXACT, FUZZY_FIRST};
	
	//  CSS file to use when generating a header.
	public static String cssGreen = "http://localhost:8080/nolaria/green.css";
	public static String cssBlue = "http://localhost:8080/nolaria/blue.css";
	
	//	This can be used were needed.
	public static PageRegistry registry = new PageRegistry();	//	Page registry database methods.
	public static Connection connector = null;					//	A database connector, in case one is needed.
	
	public int fileCt = 0;	//	Bit of a hack using a global, but whatever, I'm lazy.
	

	/**
	 * Since this application isn't intended to be run in isolation (runs in Eclipse), fixes and analysis
	 * not being performed are commented out.  Bit of hack, but expedient.
	 * 
	 * @param args - not used.
	 */
	public static void main(String[] args) {
		try {
			connector = RegistryConnector.getConnector();	//	Initialize a DB connector for general use.
			
			//app.fixOneFile(testInputFile, testOutputFile);	//	Just fix the specified file - used for testing.
			//app.fixAllFiles(rootFolder, testOutputFile);	//	Fix all files starting at the root specified and save to test file.
			//app.fixAllFiles(rootFolder, null);			//	Fix all files starting at the root specified and save to real file.
			//app.analyzeAllFiles(rootFolder);				//	Collect bug statistics starting at the root specified.
			
			//File rootFile = new File(rootFolder);
			//app.recursiveImageWalker(0, rootFile);		//	Fix image references for the Page ID model.
			
			//String testFileName = "D:\\apache-tomcat-9.0.40/webapps/nolaria/aurelia/uvarfestin/gunderstaad/gunderstaad-journal.html";
			//File testFile = new File(testFileName);
			//app.checkForMissingImages(0, testFile);
			
			// This was the start of an effort to fix page embedded page links, but I don't think I got it working.
			//File testFile = new File(testInputFile);
			//app.fixLinks(0, testFile);//	Fix embedded links to use the Page ID model.
			
			//	Fix all records to restore their file names.  - 1/15/2024
			//app.fixFileNamesByRecords();
			
			//	These fixes are for the oka-03.html database disaster.
			//app.fixFileNamesByFileInfo(root);
			app.fixRecordsUsingFuzzyMatch(Fix_Code.FUZZY_EXACT);
			//app.fixRecordsUsingFuzzyMatch(Fix_Code.FUZZY_FIRST);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Apply fixes to a single file.  This is mostly used for testing purposes, but could be used
	 * for one-off fixing a specific file.
	 * 
	 * @param fileName - full path to the page to be fixed.
	 * @param testOutputFile - path to output file or null if this is not a test.
	 * @throws PageException 
	 */
	public void fixOneFile (String fileName, String testOutputFile) throws PageException {
		//	Get the file's contents.
		//String content = FixInPlace.util.loadFile(fileName);
		
		//	Get the page name.  Used to fix titles.
		File contentFile = new File(fileName);
		String pageName = contentFile.getName();		
		pageName = pageName.substring(0, pageName.indexOf('.'));
		//System.out.println("File to be fixed:  "+fileName+" - Page name: "+pageName);
		
		//	Fix problems.  See comments in method, below.
		try {
			//String fixedContent = this.fixHeader1(content, pageName);	//	Fixes 1
			String fixedContent = this.fixHeader2(fileName, pageName);	//	Fixes 2
			
			//System.out.println("\n"+fixedContent);
			
			//	Save out the fixed results.
			if (testOutputFile != null)
				Util.saveFile(fixedContent, testOutputFile);	//	 Use this one to test a fix.
			else
				Util.saveFile(fixedContent, fileName);			//	Use this one to fix for real.
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
		Util.saveFile(sb.toString(), bugDataFile);
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
			try {
				this.fixOneFile(fileName, null);
				this.fileCt++;
			}
			catch (PageException pg) {
				System.out.println(pg.getMessage() + "Skip this file: "+fileName);
			}
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

	/**
	 * Recursively walk all files and identify pages with references to missing images.
	 * 
	 * @param depth
	 * @param file
	 */
	public void recursiveImageWalker (int depth, File file) {
		//System.out.println("File to scan:  "+file.getPath());
		
		//	Check for media folder.
		if (file.getName().compareTo("media") == 0)
			return;
		
		//	If this is a directory, recurse to lower level.
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File newFile : files) {
				this.recursiveImageWalker(depth+1, newFile);
			}
		}
		
		//	Otherwise, collect statistics on this file.
		else {
			this.checkForMissingImages(depth, file);
		}
	}

	/**
	 * Recursively walk all files and fix file names in page records using page header info.
	 * 
	 * @param depth
	 * @param file
	 */
	public void recursiveFileNameFixer (int depth, FixInPlace.Fix_Code fixCode, File file) {
		//System.out.println("File to scan:  "+file.getPath());
		
		//	Check for folders (media) or files (*.css) to skip.
		String fn = file.getName();
		if (fn.compareTo("media") == 0)
			return;
		//	Check for a css file.
		if (fn.indexOf(".css") != -1)
			return;
		
		//	If this is a directory, recurse to lower level.
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File newFile : files) {
				this.recursiveFileNameFixer(depth+1, fixCode, newFile);
				
			}
		}
		
		//	Otherwise, see if this file needs fixing and fix it.
		else {
			switch (fixCode) {
			case FUZZY_EXACT:	//	Single hit on fuzzy match to title.
				this.fixIfFuzzyExact(file);
			case FUZZY_FIRST:	//	Multiple hits on fuzzy match to titles, use the first.
			default:
				System.out.println("No fix applied to: "+fn);
			}
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
	 * 01/15/2024: Refactored to take a file name and call getHeaderInfo(filename).
	 * 
	 * @param filename
	 * @param pagename
	 * @return repaired content
	 * @throws FixException if HEADER is not found, which implies an already fixed file.
	 * @throws PageException 
	 */
	public String fixHeader1(String filename, String pageName) throws FixException, PageException{
		PageInfo info = this.getHeaderInfo(filename);
		String content = Util.loadFile(filename);
				
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
		fixedHeader.append("\t<meta name=\"name\" content=\""+info.file+"\" />\n");
		fixedHeader.append("\t<meta name=\"pid\" content=\""+info.id+"\" />\n");
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
	 * 01/15/2024: Refactored to take a file name and call getHeaderInfo(filename).
	 * 
	 * @param filename of this web page
	 * @param pageName - file name with no path
	 * 
	 * @return repaired content
	 * @throws FixException if HEAD is not found, which implies an already fixed file.
	 * @throws PageException 
	 */
	public String fixHeader2(String filename, String pageName) throws FixException, PageException{
		PageInfo info = this.getHeaderInfo(filename);
		String content = Util.loadFile(filename);

		
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
		fixedHeader.append("\t<meta name=\"name\" content=\""+info.file+"\" />\n");
		fixedHeader.append("\t<meta name=\"pid\" content=\""+info.id+"\" />\n");

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
		String content = Util.loadFile(filePath);
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
	
	/**
	 * Scan the file passed for IMG tags.  Check references to ensure that the image file is present in the media folder.
	 * 
	 * @param file
	 */
	public void checkForMissingImages(int depth, File file) {
		//String tag = "<img border=\"0\" src=\"";
		String tag = "src=\"";
		String fileName = file.getPath();
		int imageRefsFound = 0;
		
		String filePath = file.getPath();

		String content = Util.loadFile(filePath);
		if (content == null) {
			// System.out.println("Unable to load file: "+filePath);
			return;
		}

		int contentLen = content.length();
		int offset = 0;
		
		//	Scan the file contents for IMG tags.
		while (offset <= contentLen) {
			//System.out.println(indent(depth+1)+"Pointer at: "+offset);
			
			//	Look for the next (or first) IMG tag.
			int imageLoc = content.indexOf(tag, offset);
			if (imageLoc == -1) {
				//System.out.println(indent(depth+1)+"Image tag was not found");
				break;	//	Didn't find one, so we are done.
			}
			else {
				imageLoc += tag.length();
				//System.out.println(indent(depth+1) + "Found image at: "+imageLoc);
			}
			
			//	Found one, so print location and refs.
			String imageUrl = content.substring(imageLoc, content.indexOf("\"", imageLoc));
			imageRefsFound++;
						
			//	Print the candidate URL.
			//System.out.println(indent(depth+1) + "[" + imageRefsFound + "] " + imageUrl);
			//System.out.println(indent(depth+1) +  "Length: "+imageUrl.length() + " - At Location: "+imageLoc);			
			
			try {
				URL img = new URL(imageUrl);
				
				String imageName =img.getFile();
				String imagePath = FixInPlace.mediaFolder;
				File imageFile = new File(imagePath);
				if (!imageFile.exists()) {
					//	If this is the first image found, print the name of the file being scanned.
					if (imageRefsFound == 1)
						System.out.println(indent(depth)+fileName);

					System.out.println(indent(depth+1) + imageName);
				}
				
			}
			catch (MalformedURLException ex) {
				//	If this is the first image found, print the name of the file being scanned.
				System.out.println(fileName);

				if (imageUrl.length() == 0)
					System.out.println("\tMalformed URL: Empty");
				else
					System.out.println("\tMalformed URL: "+imageUrl);
			}
						
			offset = imageLoc + imageUrl.length() + 1;
			//System.out.println(indent(depth+1) + "New Offset: " + offset);
		}
		
	}
	
	/**
	 * With the migration to the Page ID model, all links embedded in pages are broken.  They currently
	 * have a URL this is path based.  This method finds all links in a page and updates them to to use the Page ID
	 * associated with that page.  This requires a database lookup using a select with path and file name.
	 * 
	 * @param depth
	 * @param file
	 * @throws Exception
	 */
	public void fixLinks(int depth, File file) throws Exception {
		System.out.println("Fixing links in: "+file.getName());
		
		/*  The standard Xerces DOM parser won't work on HTML files.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();		
		Document doc = parser.parse(file);		
		DocumentType docType = doc.getDoctype();
		System.out.println ("Document Type: "+docType.getName());
		*/
		
		//	Parse the HTML document.
		String htmlStr = Util.loadFile(testInputFile);
		org.jsoup.parser.Parser parser = org.jsoup.parser.Parser.htmlParser();
		Document doc = parser.parseInput(htmlStr, "file:///D:/Personal/SiteView/people.html");
		String baseUri = doc.baseUri();
		String pageTitle = doc.title();
		System.out.println("Document Base URI: "+baseUri+"\n");
		
		//	Find all of the HREFs.
		System.out.println("Links found in: "+pageTitle);
		Element root = doc.body();
		Elements links = root.getElementsByTag("a");
		for (Element link : links) {
			//Node n = (Node) link;
			String linkUrl = link.attr("abs:href");
			System.out.println("\t"+linkUrl);
			
			//PageId pg = registry.getPageByFileName();
		}
	}
	
	/**
	 * On Jan. 15, 2024 I did a silly thing.  I set every file name to oka-03.html.
	 * This method attempts to undo that damage by attempting to extract the file name from the page title
	 * and restoring it in the database.
	 */
	public void fixFileNamesByRecords() throws PageException {		
		List<PageId> pages = registry.getAllPages();
		
		System.out.println("Starting restoration of file names in "+pages.size()+" records.\n");
		
		//	Iterate over all pages and look for ones that we can't make file names for.
		int broken = 0;
		int fixed = 0;
		for (PageId page : pages) {
			if (page.getFile().compareTo("oka-03.html") == 0 ) {
				String fixedTitle = this.titleToFileName(page.getTitle());
				String fileName = fixedTitle+".html";
				String fullFileName = rootFolder+"/"+page.getPath()+"/"+fileName;
				File f = new File(fullFileName);
				if (f.exists()) {
					// String id, String site, String title, String file, String path
					registry.updatePage(page.getId(), page.getSite(), page.getTitle(), fileName, page.getPath());
					fixed++;
				}
				else {
					//System.out.println(page.getTitle()+" => "+fileName);
					//System.out.println(page.getTitle()+" => "+fullFileName);
					//System.out.println(fullFileName);
					broken++;
				}
			}
		}
		System.out.println("\nFixable: "+fixed+" Broken: "+broken);	
	}
	
	/**
	 * Walk the entire file tree.  Each file is opened and the PageInfo is extracted.
	 * A filter/fix is performed based on the fix code passed.
	 * 
	 * @param fixCode
	 * @param file
	 */
	public void fixRecordsUsingFuzzyMatch(Fix_Code fixCode) {
		System.out.println("Starting restoration of file names using file metadata.\n");
		
		File rootFile = new File(rootFolder);
		this.fileCt = 0;
		this.recursiveFileNameFixer(0, fixCode, rootFile);
		System.out.println("Pages that can be fixed using fuzzy match: "+this.fileCt);
	}
	
	/**
	 * This is one of the fixes called from recursiveFileNameFixer() which walks the file tree and attempts to apply fixes
	 * based on a fixCode.
	 * 
	 * Using the file passed, get its metadata from the file contents.
	 * Do a fuzzy (wild card) query using a title extracted from the file name against it's current title.
	 * If there is only a single hit, update the record 
	 * 
	 */
	public void fixIfFuzzyExact(File file) {
		String fullFileName = file.getAbsolutePath();
		try {
			String content = Util.loadFile(fullFileName);
			PageInfo meta = Util.getHeaderInfo(content);
			if (meta == null) {
				System.out.println("Unable to extract file info for: "+fullFileName);
				return;
			}
			else {
				PageId page = registry.getPage(meta.id);
				if (page == null)
					return;
				
				//	See if this is one of the broken ones.
				if (page.getFile().compareTo("oka-03.html") == 0) {						
					List<String> ids = new Vector<String>();
					String path = page.getPath();
					String title = page.getTitle();
					title = title.replace("'", "");
					String fuzzyQuery = "select id from page_registry where path='"+path+"' and title like '"+title+"%'";
					try(Statement stmt = connector.createStatement())  {		
						ResultSet rs = stmt.executeQuery(fuzzyQuery);
						rs.beforeFirst();
						
						// Extract data from result set
						while (rs.next()) {
							// Retrieve by column name
							String id = rs.getString("id");
							ids.add(id);
						}
					}
					catch (SQLException sql) {
						//	Swallow the exception or recursion will halt.
						System.out.println(sql.getMessage()+" - "+sql.getCause());
					}
					
					// Report how we did.
					if (ids.size() == 0) {
						//	No match, no joy.
						return;
					}
					else if (ids.size() == 1) {
						//	Single match, yes!
						String newId = ids.get(0);
						
						//	Check that the new id matches the page Id in the metadata.
						if (newId.compareTo(page.getId()) == 0) {
							String name = file.getName();
							String newTitle = this.fileNameToTitle(name);
							
							//System.out.println("["+title+"] ==> ["+newTitle+"] -- ["+file.getName()+"] -- "+newId);
							System.out.println(page.getUpdateQuery());
							
							//	Fix things up.
							//  updatePage(String id, String site, String title, String file, String path)
							/*
							FixInPlace.registry.updatePage(newId, page.getSite(), newTitle, name, page.getPath());
							Util.updateHeaderInfo(content, fullFileName, meta);
							Util.saveFile(content, fullFileName);
							*/
							
							this.fileCt++;
						}
					}
					else {
						// System.out.println("Found "+ids.size()+" matches on title of "+title);
					}
				}
			}
		}
		catch (PageException pg) {
			System.out.println (pg.getMessage() + " skiping file: "+fullFileName);
		}
	}

	/**
	 * This is one of the fixes called from recursiveFileNameFixer() which walks the file tree and attempts to apply fixes
	 * based on a fixCode.
	 * 
	 * Using the file passed, get its metadata from the file contents.
	 * Do a fuzzy (wild card) query using a title extracted from the file name against it's current title.
	 * If there multiple hits, use the first one found to update the record 
	 * 
	 */
	public void fixIfFuzzyFirst(File file) {
		//	To be imlemented.
	}

	
	/****************************************************************************
	 *		UTILITY METHODS	
	 ****************************************************************************/

	/**
	 * @deprecated
	 * This utility method takes the file name of a page file, loads its contents and
	 * extracts header metadata including:
	 * 
	 * 	-	Title
	 * 	-	Name
	 * 	-	PID (page identifier)
	 * 
	 * 01/15/2024: Refactored to take a file name.
	 *
	 * @param headerBlock
	 * @return PageInfo object or full if an error happened.
	 */
	public PageInfo getHeaderInfo(String filename) {
		String title = null;	//	The page title.
		String name = null;		//	The page name.
		String pid = null;		//	The page identifier (UUID).
		
		if (filename == null) {
			System.out.println("getHeaderInfo: filename passed is null.");
			return null;
		}
		
		//	Load the file contents.
		String headerBlock = Util.loadFile(filename);
				
		//  Extract the title.
		int startTitleOffset = headerBlock.indexOf("<title>");
		if (startTitleOffset == -1) {
			System.out.println("getHeaderInfo: start of title not found.");
			return null;
		}
		int endTitleOffset = headerBlock.indexOf("</title>", startTitleOffset+1);
		if (endTitleOffset == -1) {
			System.out.println("getHeaderInfo: end of title not found.");
			return null;			
		}
		title = headerBlock.substring(startTitleOffset+"<title>".length(), endTitleOffset);
		
		//	Extract the name.
		String nameStart = "name=\"name\" content=\"";
		int startNameOffset = headerBlock.indexOf(nameStart);
		if (startNameOffset == -1) {
			System.out.println("getHeaderInfo: start of name not found.");
			return null;
		}
		int endNameOffset = headerBlock.indexOf("\"", startNameOffset+nameStart.length());
		if (endNameOffset == -1) {
			System.out.println("getHeaderInfo: end of name not found.");
			return null;			
		}
		name = headerBlock.substring(startNameOffset+nameStart.length(), endNameOffset);
		
		//	Extract the page identifier.
		String idStart = "name=\"pid\" content=\"";
		int startPidOffset = headerBlock.indexOf(idStart);
		if (startPidOffset == -1) {
			System.out.println("getHeaderInfo: start of pid not found.");
			return null;
		}
		int endPidOffset = headerBlock.indexOf("\"", startPidOffset+idStart.length());
		if (endPidOffset == -1) {
			System.out.println("getHeaderInfo: end of pid not found.");
			return null;			
		}
		pid = headerBlock.substring(startPidOffset+idStart.length(), endPidOffset);
		
		return new PageInfo(title,name,pid);		
	}
	
	/**
	 * Convert the page title passed to a file name by:
	 * 1.  Converting text to lower case
	 * 2.  Converting spaces to dash.
	 * 
	 * @param title
	 * @return file
	 */
	public String titleToFileName(String title) {
		String file = title.toLowerCase();
		file = file.replace(' ', '-');
		file = file.replace('/', '-');
		file = file.replace('.', '+');
		file = file.replace(",", "");
		file = file.replace("'", "-");
		file = file.replace("+", "");
		file = file.replace("_", "");
		file = file.replace(':', '-');
		file = file.replace("&amp;", "-");
		return file;
	}
	
	/**
	 * This method creates a page title given a file name.  For the most part, this is used
	 * to correct the truncated titles that happened in an earlier phase of conversion.  It
	 * does this in two steps:
	 * 
	 * 	1. Remove the file extension (usually .html)
	 *  2. Replace --- with -
	 *  3. Use the Util.captizalize() method for final title.
	 *  
	 *  "00---things-to-do.html" becomes "00 - Things To Do"
	 *      but
	 *  "city-02.html" becomes "City 02"
	 *      can't be helped because dash means two things: a dash or a space.
	 * 
	 * @param fileName
	 * @return Extracted title
	 */
	public String fileNameToTitle(String fileName) {
		int off = fileName.indexOf('.');
		String title = fileName.substring(0, off);
		title = title.replace("---", " + ");	//  Stub the three dash case.
		title = title.replace('-',' ');			//	All dashes to spaces
		title = title.replace('+', '-');		//	Restore the three dash case.
		
		//	Capitalize the first letter.
		char firstLetter = title.charAt(0);
		char firstCapitalized = Character.toUpperCase(firstLetter);
		title = firstCapitalized + title.substring(1, title.length());
		
		title = Util.capitalize(title);
		
		return title;
	}
	
	/**
	 * Load the file given by fileName and return it as a String.
	 * 
	 * @param fileName
	 * @return contents of the file
	 */
	/*  DEPRECATED - use FixInPlace.util.loadFile() instead.
	public String loadFile (String fileName) {
		String content = null;
		File srcFile = new File (fileName);
		BufferedReader br = null;
		
		// Load source file into memory.
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
*/
	
	/**
	 * Save the contents passed to the file named.
	 * 
	 * @param contents
	 * @param fileName
	 */
	/*  DEPRECATED - use FixInPlace.util.saveFile() instead.
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
	*/
	
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
