package com.nolaria.stieview.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import com.nolaria.sv.db.*;

/**
 * @author markjnorton@gmail.com
 *
 *	This is a utility application that takes path name pointer to the
 *	root of a file system containing HTML files down loaded from Google
 *	Sites. The app converts these files by extracting the core content
 *	and adding additional mark up to create a set of pages that can be
 *	viewed locally.
 *
 *	Updated in Jan. 2022 to include updates made in fixinplace.  Thus, most known 
 *	HTML issues have been fixed in the converter as well.
 */
public class Converter {
	// Constants.
	public static Converter app = new Converter();
	public static final String FileSep = "/";
	public static Boolean ContentOnly = true;	// True if only the content is to be copied.
	public static Boolean RunTests = false;		//	True if fix tests should be run.

	//	Used to determine file types.
	public static enum FileType {
		WEB, IMAGE, VIDEO, AUDIO, TEXT, XML, JSON, DIR, UNKNOWN
	};
	
	//	Fix types supported.  Add a new type when a fix-in-place pass is needed.
	public static enum FixType {BASIC, IMAGES, LINKS, UNKNOWN};
	
	// This is where all of the source pages live, to be converted.
	public static final String TAKEOUT_DIR = "D:/Google-Download/Takeout/ClassicSites";

	//	Registries
	public SiteRegistry siteRegistery = new SiteRegistry();
	public PageRegistry pageRegistry = new PageRegistry();
	
	//	Instance variables.
	public int fileCount = 0;

	/**
	 * Main entry point for the converter application.
	 * Three scenarios are possible:
	 * 
	 * 	1. Site doesn't exist:  create it and convert source files.
	 * 	2. Site exists and RunTests is true: just run the tests.
	 *  3. Site exists and RunTest is false:  do all fixes in place.
	 * 
	 * @param args[0]: TakeOut source folder
	 * @param args[1]: Site name name
	 * @param args[2]: CSS style file name to use.
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Takeout Converter");
		System.out.println("-----------------\n");
		
		//	Check to see if arguments are passed.  
		// These are defined on the svconverter project run configs.
		String sourceRootName = null;
		String siteName = null;
		String cssName = null;
		if (args.length == 3) {
			sourceRootName = args[0] + FileSep + args[1];
			siteName = args[1];
			cssName = args[2];
		}
		else {
			System.out.println ("Arguments missing: SOURCE_ROOT, CSS_NAME");
			return;		//	Exit the app.
		}

		//System.out.println("Source Root Name: "+app.sourceRootName);
		//System.out.println("CSS Name: "+app.cssName);

		//	Check to see if this site has already been created.
		Site site = app.siteRegistery.getSiteByName(siteName);
		if (site == null) {
			//	Scenario 1: Create site and convert files.
			System.out.println("\nConvert and fix start time: "+LocalTime.now());
			
			//	Convert from the home page.
			String homePageSourceName = sourceRootName+"/home.html";
			
			//	Create the site, initialize folders and files.
			String sitePath = "webapp/"+siteName;
			site = app.siteRegistery.createSite(siteName, sitePath, cssName, true);	

			app.convert(site, homePageSourceName);
			app.fixInPlace(site);

			System.out.println("\nConvert and fix end time: "+LocalTime.now());
		}
		else {
			if (site.getStatus("site.creation") == null)
				System.out.println("Site status is not available for: "+site.getName());
			
			if (RunTests) {
				//	Scenario 2: Run tests.
				//app.convertHomePage(site, homePageSourceName);
				// app.testFixBoundingBox (siteName);
				app.testFixImages(siteName);
			}
			else {
				//	Scenario 3:  Fix in place.
				System.out.println("\nFix start time: "+LocalTime.now());
				app.fixInPlace(site);
				System.out.println("\nFix end time: "+LocalTime.now());
			}	
		}
	}
	
	/**
	 * Convert the content of the source home page passed and write it to the appropriate place in the site passed.
	 * 
	 * There is some special case treatment of home pages.  If the source points to a 'home.html' file, the path
	 * is hard coded to an empty string (paths are relative to the root site folder) and the title is forced
	 * to be a capitalized form of the site name.
	 * 
	 * Note: this method was developed to test the conversion logic.  For full conversion, recursiveWalk() calls
	 * the convertPage() method.
	 * 
	 * @param site
	 * @param homePageSrcName
	 */
	public void convertHomePage(Site site, String homePageSourceName) {
		//	Check for null arguments.
		if ( (site == null) || (homePageSourceName == null)) {
			System.out.println("Null arguments were passed to convertHomePage()");
			return;
		}
		
		System.out.println("Source home page: "+homePageSourceName);
		
		//	Check that the source home page exists.
		File srcHomePage = new File(homePageSourceName);
		if (!srcHomePage.exists()) {
			System.out.println("Source home page doesn't exist at the path given.");
			return;
		}
		
		//	Extract the page file name.
		String[] parts = homePageSourceName.split("/");
		int partCount = parts.length;
		String pageFileName = parts[partCount-1];
		System.out.println("Page file name: "+pageFileName);
		
		//	If this is a home page, then special handling follows later.
		Boolean isHomePage = pageFileName.compareTo("home.html")==0;

		//	Create the destination home page.
		String homePageDestName = "";
		if (isHomePage) {
			homePageDestName = this.siteRegistery.getFileRoot()+"/"+site.getName()+"/"+site.getName()+".html";
		}
		else {
			//	Home page dest name needs to have a path inserted if not a home page.
			System.out.println("homePageDestName not handled for non-home pages.");
			return;
		}
		System.out.println("Destination home page: "+homePageDestName);
		
		//	Check to see if the destination home page already exists.
		File destHomePage = new File(homePageDestName);
		if (destHomePage.exists()) {
			System.out.println("Destination home page file already exists.");
			System.out.println("Deleting the file - the page record is not deleted.");
			destHomePage.delete();
		}
		
		System.out.println("Ready to convert and write the site home page.");
		
		//	Load the source content.
		String srcContent = Util.loadFile(homePageSourceName);
		if (srcContent == null) {
			System.out.println("Unable to load source content.");
			return;
		}
		
		//	If this is a home page, force the title to be the site name.
		String title = "";
		if (isHomePage) {
			title = site.getName();
			title = Util.capitalize(title);
			System.out.println("Title forced to be the site name: ["+title+"]");
		}
		else {
			title = this.extractTitle(srcContent);
			System.out.println("Title extracted from source: ["+title+"]");
		}

		//	Create the path and assign a UUID.
		String path = "";		//	No path if the file is at the site root.
		if (!isHomePage) {
			String[] srcParts = homePageSourceName.split("/");
			//	Ex:  D:/Google-Download/Takeout/ClassicSites/nolaria/altamek/allegory.html
			//	Splits into:  "D:", "Google-Download", "Takeout", "ClassicSites", "nolaria", altamek", "allegory.html"
			//	Path starts right after the site name (offset 4) and ends at length-2.
			//	This results in a path of "altamek".
			int pathStart = 0;
			int pathEnd = srcParts.length-2;
			for (String s : srcParts) {
				if (s.compareTo(site.getName()) == 0) {
					pathStart += 1;
					break;
				}
				else
					pathStart++;
			}
			for (int i=pathStart; i<=pathEnd; i++) {
				path += srcParts[i];
				if (i<pathEnd)
					path += "/";
			}
		}
		String uuid = UUID.randomUUID().toString();
		// String id, String site, String title, String file, String path, boolean archived
		PageId tempPage = new PageId(uuid, site.getName(), title, site.getName()+".html", path, false);
		
		//	Gather the document parts. 
		String headText = this.getHeaderText(site, tempPage);
		String bodyText = this.extractBody(srcContent);
		
		//	Fixes applied to content body only.
		bodyText = this.fixContent(bodyText);
		//System.out.println("\nFixed Body Content:"+bodyText+"\n");
		
		//	Unit them to create a converted, but not fully fixed content string.
		String convertedContent = headText + bodyText + "\n</body>\n</html>\n";
				
		//	Save the converted and fixed content to its destination location.
		Util.saveFile(convertedContent, homePageDestName);
		
		//	Register the page.
		if (isHomePage)
			//	In the case of the home page, force the home page file name to reflect the site name.
			pageFileName = site.getName() + ".html";
		try {
			this.pageRegistry.createPage(uuid, site.getName(), title, pageFileName, path);
		}
		catch (PageException pg) {
			System.out.println("Unable to register page: "+pageFileName+": "+pg.getMessage());
		}
	}
	

	/**
	 * Convert the files in TAKEOUT_DIR/dirName to a new set of SiteView pages based
	 * on the Page Ref Model in Tomcat.
	 * WARNING:  Uses recursion to walk the source content tree.
	 * 
	 * Note:  Updated to maintain site status.
	 * 
	 * @param srcName - full page to the source folder to convert.
	 * @param siteName - new site content is converted to.
	 */
	public void convert(Site site, String srcName) {		
		String srcPath = TAKEOUT_DIR + "/" + site.getName();
		System.out.println("Source files path: " + srcPath);
		String targetFilePath = SiteRegistry.FILE_ROOT + "/" + site.getName();
		System.out.println("Target directory path: " + targetFilePath+"\n");

		//	Recurse from the source root and convert all source files to the site destination.
		try {
			String convertedStatus = site.getStatus("site.creation");
			if ( (convertedStatus == null) || (convertedStatus.compareTo("converted")!=0) ) {
				System.out.println("\nConverting Source Files");
				this.recursiveWalk(0, srcPath, site, "");

				//	Add status information for this site.
				site.setStatus("site.creation", "converted");
				site.setStatus("site.created", LocalDate.now()+" "+LocalTime.now());
				site.setStatus("site.converted.source", srcPath);
				site.saveStatus();
			}
			else
				System.out.println("\nSource Files Previously Converted");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Apply any post conversion fixes.
	 * 
	 * @param site
	 */
	public void fixInPlace(Site site) {
		System.out.println("Fix in place site: "+site.getName());
		
		String rootName = SiteRegistry.FILE_ROOT + FileSep + site.getName();
		
		//	Recurse from the source root and convert all source files to the site destination.
		try {
			//	Check status and apply image fixes if needed.
			String fixImageStatus = site.getStatus("fix.images");
			//System.out.println("Fix image status: "+fixImageStatus);
			if ( (fixImageStatus == null) || (fixImageStatus.compareTo("yes")!=0) ) {
				System.out.println("\nFix Image Pass");
				this.recursiveFixWalk(0, FixType.IMAGES, site, rootName);
				site.setStatus("fix.images", "yes");
				site.saveStatus();
			}
			else
				System.out.println("\nImages Previously Fixed");
			
			//	Check status and apply links fixes if needed.
			String fixLinkStatus = site.getStatus("fix.links");
			//System.out.println("Fix image status: "+fixLinkStatus);
			if ( (fixLinkStatus == null) || (fixLinkStatus.compareTo("yes")!=0) ) {
				System.out.println("\nFix Links Pass");
				this.recursiveFixWalk(0, FixType.LINKS, site, rootName);
				site.setStatus("fix.links", "yes");
				site.saveStatus();
			}
			else
				System.out.println("\nLinks Previously Fixed");
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/****************************************************************************
	 * RECURSIVE TREE WALKERS
	 ****************************************************************************/
	
	/**
	 * Recursively walk a source file tree and convert files found.  Folders in the destination
	 * site are created as needed.  Media files are copied to the media folder under an extracted
	 * category.
	 * 
	 * WARNING: This method uses recursion to traverse a file system tree.
	 * 
	 * @param depth         - current depth of the tree walk.
	 * @param srcFilePath   - full file path to the current source file being processed.
	 * @param site			- the target site to convert files into.
	 * @param relTargetPath - directory path relative to the site root.
	 * @throws IOException
	 */
	public void recursiveWalk(int depth, String srcFilePath, Site site, String relTargetPath) throws IOException {

		File srcNode = new File(srcFilePath);
		String srcName = srcNode.getName();
		
		//	For debugging to avoid walking the whole tree.
		//if (depth >= 2)
		//	return;
		
		//	Handle a folder.
		if (srcNode.isDirectory()) {
			
			//	Skip recursion on page templates.  Not needed.
			if (srcName.compareTo("pagetemplates")==0)
				return;

			System.out.println(Util.tabber(depth)+depth+": Directory: "+srcFilePath);
			
			/*  I decided to just leave it in place and convert it normally.
			if (srcName.compareTo("home")==0)
				System.out.println(Util.tabber(depth)+"Special handling for HOME directory.");
			*/
			
			if (this.hasSubPages(srcFilePath)) {
				//	Create a target folder.
				String destFolderName = SiteRegistry.FILE_ROOT + relTargetPath + "/" + srcName;
				//System.out.println(Util.tabber(depth)+"Destination folder to create: "+destFolderName);
				File destFolderNode = new File(destFolderName);
				destFolderNode.mkdir();
			}
			
			String[] dirFiles = srcNode.list();
			depth++;
			for (String f : dirFiles) {
				String newRelPath = relTargetPath+"/"+srcName;
				this.recursiveWalk(depth, srcFilePath+"/"+f, site, newRelPath);
			}			
		}
		
		//	Handle a file.
		else {
			//System.out.println(Util.tabber(depth)+depth+": "+"Source File Path: "+srcFilePath);
			FileType type = this.getFileType(srcName);
			
			//	Convert a web page
			if (type == FileType.WEB) {
				//String destFileName = ROOT_PATH + FileSep + relTargetPath + FileSep + srcName; // Uses page node now.
				this.convertPage(depth, srcFilePath, site, relTargetPath);
			}
			
			//	Copy an image file.
			else if (type == FileType.IMAGE) {
				//	Check for graphic images to skip.  Might cause broken links.  TODO:  Reconsider this.
				if ((srcName.compareTo("container.gif")==0) || (srcName.compareTo("hd-bg.gif")==0) || (srcName.compareTo("tb-bg.gif")==0)  || 
						(srcName.compareTo("h1.gif")==0) || (srcName.compareTo("h2.gif")==0)  || (srcName.compareTo("h3.gif")==0) ) {
					//System.out.println(Util.tabber(depth)+"Unneeded media file skipped: "+srcName);
					return;
				}
				
				//	Check for the banner image.
				else if (srcName.compareTo("customLogo.gif")==0) {
					//System.out.println(Util.tabber(depth)+"Banner image handled specially: "+srcName);
					return;
				}
				else {
					this.convertMediaFile(depth, srcFilePath, site.getName());
				}
			}
			else {
				System.out.println(Util.tabber(depth)+"Unsupported file type: "+type);
			}
		}
	}
	
	/**
	 * Recursively walk a site file tree and apply fixes to each web file found.
	 * 
	 * WARNING: This method uses recursion to traverse a file system tree.	 * 
	 * 
	 * @param depth		- Recursion depth.
	 * @param fixType   - Type of fix to be applie in this pass.
	 * @param site		- Site being fixed.
	 * @param filePath	- Current file in the tree walk.
	 */
	public void recursiveFixWalk (int depth, FixType fixType, Site site, String filePath) {
		File fileNode = new File(filePath);
		String fileName = fileNode.getName();
				
		//	For debugging to avoid walking the whole tree.
		//if (depth >= 2)
		//	return;
		
		//	Handle a folder.
		if (fileNode.isDirectory()) {
			if (fileName.compareTo(SiteRegistry.MEDIA_DIR_NAME) == 0) {
				//System.out.println(Util.tabber(depth) + "Skipping the media folder.");
				return;
			}
			System.out.println(Util.tabber(depth) + depth + " - Directory: "+fileName);
			String[] dirFiles = fileNode.list();
			depth++;
			for (String f : dirFiles) {
				String newfilePath = filePath+FileSep+f;
				this.recursiveFixWalk(depth, fixType, site, newfilePath);
			}
		}
		else {
			FileType type = this.getFileType(fileName);
			
			//	Fix a web page
			if (type == FileType.WEB) {
				System.out.println("\n"+Util.tabber(depth) + depth+"- Page file to fix: "+ fileName);
				
				//	Check and apply fixes to images if needed.
				if (fixType == FixType.IMAGES) {
					String category = this.extractMediaCategory(site.getName(), filePath);
					//System.out.println(Util.tabber(depth) + "Media category: "+ category);
					this.fixImagesByRefs(depth, site.getName(), filePath, category);
				}
				
				//	Check and apply fixes to page references (links) if needed.
				else if(fixType == FixType.LINKS)
					this.fixLinks(depth, site.getName(), filePath);
				
				//	For safetly. Only valid fix types should be passed.
				else
					System.out.println(Util.tabber(depth) + "Unknown fix type: "+ fixType);
			}
		}			

	}

	/****************************************************************************
	 * FILE COPY METHODS
	 ****************************************************************************/
	
	/**
	 * Given a source (TakeOut) page in srcName, convert it and write to a location based
	 * on relTargetPath using the site passed.
	 * 
	 * @param depth - recursion depth is used to format console output.
	 * @param srcName - full path to source file.
	 * @param site - the site this page will be associated with.
	 * @param relTargetPath - folder path relative to the site root folder.
	 */
	public void convertPage(int depth, String srcName, Site site, String relTargetPath) {
		
		//	Check for null arguments.
		if ( (site == null) || (srcName == null) || (relTargetPath == null)) {
			System.out.println("Null arguments were passed to convertAndCopyPage()");
			return;
		}
		
		//System.out.println(Util.tabber(depth)+"Convert web page: "+srcName);
		String siteName = site.getName();
		
		//	Check that the source page exists.
		File srcFile = new File(srcName);
		if (!srcFile.exists()) {	//	Very unlikely, but WTF.
			System.out.println(Util.tabber(depth)+"Source page doesn't exist at the path given.");
			return;
		}
		
		//	Extract the page file name.
		String[] parts = srcName.split("/");
		int partCount = parts.length;
		String pageFileName = parts[partCount-1];
		//System.out.println("Page file name: "+pageFileName);
		
		//	Create the destination page.
		String destFileName = SiteRegistry.FILE_ROOT + FileSep + relTargetPath + FileSep + pageFileName; // Uses page node now.
		//System.out.println(Util.tabber(depth)+"Destination home page: "+destFileName);
		
		//	Load the source content.
		String srcContent = Util.loadFile(srcName);
		if (srcContent == null) {
			System.out.println(Util.tabber(depth)+"Unable to load source content.");
			return;
		}
		
		//	Get the page title from the content.
		String title = this.extractTitle(srcContent);

		//	Create the page path and assign a UUID.	
		//System.out.println("====> relTargetPath: "+relTargetPath+" less site name: "+siteName);
		String pagePath = relTargetPath.substring(siteName.length()+1,relTargetPath.length());
		if ( (pagePath.length() > 1) && (pagePath.charAt(0) == '/') )
			pagePath = pagePath.substring(1, pagePath.length());
		String uuid = UUID.randomUUID().toString();
		// String id, String site, String title, String file, String path, boolean archived
		PageId tempPage = new PageId(uuid, site.getName(), title, pageFileName, pagePath, false);
		
		//	Gather the document parts. 
		String headText = this.getHeaderText(site, tempPage);
		String bodyText = this.extractBody(srcContent);
		
		//	Fixes are applied to content body only.
		bodyText = this.fixContent(bodyText);
		//System.out.println("\nFixed Body Content:"+bodyText+"\n");
		
		//	Unit them to create a converted, but not fully fixed content string.
		String convertedContent = headText + bodyText + "\n</body>\n</html>\n";
				
		//	Save the converted and fixed content to its destination location.
		Util.saveFile(convertedContent, destFileName);
		
		//	Register the page.
		try {
			this.pageRegistry.createPage(uuid, siteName, title, pageFileName, pagePath);
			System.out.println(Util.tabber(depth)+"Converted page registered: "+pagePath+" - "+pageFileName);
		}
		catch (PageException pg) {
			System.out.println(Util.tabber(depth)+"Unable to register page: "+pageFileName+": "+pg.getMessage());
		}

	}
	
	/**
	 * Convert the media file given by srcName by extracting a media category name
	 * and copying to the media folder of this site in the category folder.
	 * 
	 * @param depth
	 * @param srcName
	 */
	public void convertMediaFile(int depth, String srcName, String site) {
		String[] parts = srcName.split("/");
		int ct = parts.length;
		if (ct < 2) {		//	Avoid indexing exceptions that are very unlikely.
			System.out.println(Util.tabber(depth)+"Unable to convert media file: "+srcName);
			return;
		}
		String mediaName = parts[ct-1];
		
		//	Get the category name
		String categoryName = this.extractMediaCategory(site, srcName);
		String destName = "";
		if (categoryName.length() > 0) {
			//	If the media category folder doesn't exist, then create it.
			String destCategoryName =  SiteRegistry.FILE_ROOT +FileSep + site + FileSep + SiteRegistry.MEDIA_DIR_NAME + FileSep + categoryName;
			System.out.println(Util.tabber(depth)+"Create category folder: "+destCategoryName);
			File destCategoryFile = new File(destCategoryName);
			destCategoryFile.mkdir();

			destName = SiteRegistry.FILE_ROOT +FileSep + site + FileSep +  SiteRegistry.MEDIA_DIR_NAME + FileSep + categoryName + FileSep + mediaName;
		}
		else
			destName = SiteRegistry.FILE_ROOT +FileSep + site + FileSep +  SiteRegistry.MEDIA_DIR_NAME + FileSep + mediaName;

		//	Handled the [v6 file name case.
		destName.replaceAll("\\[v6", "");
		
		//System.out.println(Util.tabber(depth)+"Copy media page from: "+srcName);
		//System.out.println(Util.tabber(depth)+"Category: "+categoryName);
		//System.out.println(Util.tabber(depth)+"Copy media page to: "+destName);
		
		try {
			Util.copyFile(srcName, destName);
		}
		catch (Exception ex) {
			System.out.println(Util.tabber(depth)+"Media file convert failed: "+srcName);
			System.out.println(Util.tabber(depth)+"Reason: "+ex.getMessage());
		}
	}

	
	/****************************************************************************
	 * FIX METHODS
	 ****************************************************************************/
	
	/**
	 * Apply known fixes to the document content passed.
	 * CAUTION:  the order of fixes matters.
	 * 
	 * @param content
	 * @return fixed content.
	 */
	public String fixContent(String content) {
		String temp = content;
		
		temp = this.fixBreakingDivs(temp);		//	Must be before fixLineEnds()
		temp = this.fixNonBreakSpace(temp);		//	Delete the funky Â character.
		temp = this.fixLineEnds(temp);			//	Add new lines after all HTML tags.
		temp = this.fixQuotes(temp);			//	Replace funky char with single or double quote.
		temp = this.fixJavascriptVoids(temp);	//	Delete bad image references		
		temp = this.fixBoundingBox(temp);		//	Remove bounding box around content.

		//temp = this.fixImages(temp);			//	Copies images and fixes links.

		//temp = this.fixLinks(temp);			//	Must be done in a separate pass over files.

		return temp;
	}
	
	/**
	 * Google Takeout uses the 'Â' as a kind of non-breaking space.  This deletes them.
	 * 
	 * @param content
	 * @return fixed content
	 */
	private String fixNonBreakSpace(String content) {
		//String fixedContent = content.replace("Â", "&nbsp;"); // Convert non-breaking spaces
		
		String temp = content.replace("Â", ""); 		// Remove non-breaking spaces
		return temp;
	}
	
	/**
	 * Google's Takeout content has been striped of white space to make the files smaller.
	 * That results in a file that's impossible to read.
	 * 
	 * Fix Line Ends solves this problem by:
	 * 
	 *   - Inserting a new line after every tag closing (>)
	 * 
	 * @param content
	 * @return fixed content
	 */
	private String fixLineEnds(String content) {
		String temp = content.replace(">", ">\n");;
		
		// The old way didn't catch all cases.
		// fixedContent = fixedContent.replace("/>", "/>\n");
		// fixedContent = fixedContent.replace("</", "\n</");		

		return temp;
	}


	/**
	 * Some images refer to "javascript:void(0);".  Deleted these as they only cause problems.
	 * 
	 * @param content
	 * @return fixed content
	 */
	private String fixJavascriptVoids(String content) {
		String temp = content;
		
		//	TODO:  Implementation goes here.
		
		return temp;
	}


	/**
	 * This fix replaces "<div><br></div>" with "<br><br>"
	 * It must be applied before fixLineEnds, else there will be new lines after the tags.
	 *  
	 * @param content
	 * @returns fixed content
	 */
	private String fixBreakingDivs(String content) {
		String temp = content.replaceAll("<div><br><//div>", "<br><br>");
		return temp;
	}
	
	/**
	 * In some cases there is a string of characters that represent a single or double quote.
	 * This fix replaces them with single or double quote characters.
	 * 
	 * @param content
	 * @return fixed content
	 */
	private String fixQuotes(String content) {
		String temp = content;
		
		//	TODO: Add the quote fix.
		
		return temp;
	}

	/**
	 * When Takeout was run, it put the content of all pages into a table with a single cell.
	 * Personally, I think this is ugly, so this method removes it.
	 * 
	 * See Converter.testFixBoundingBox() to verify.
	 * 
	 * @param content
	 * @return fixed content
	 */
	private String fixBoundingBox(String content) {
		String temp = content;
		
		//	Remove the top.
		int startOff = content.indexOf("<table xmlns");
		int endOff = content.indexOf("name-content-1\">");
		endOff += "name-content-1\">".length()+1;

		String first = content.substring(0, startOff);
		String last = content.substring(endOff, temp.length());
		//System.out.println("Page first part size: "+first.length()+", last part size: "+last.length());
		temp = first + last;
		
		//	Remove the bottom.
		startOff = endOff = 0;
		int tempOff = 0;
		while (temp.indexOf("</td>",startOff) != -1) {
			if ((tempOff = temp.indexOf("</td>",startOff+"</td>".length()))==-1)
				break;
			else
				startOff = tempOff;
			//System.out.println("startOff: "+startOff);
		}
	//	System.out.println("New startOff: "+startOff+" snip: ["+temp.substring(startOff, startOff+10)+"]");
		
		tempOff = 0;
		while (temp.indexOf("</table>",endOff) != -1) {
			if ((tempOff = temp.indexOf("</table>",endOff+"</table>".length()))==-1)
				break;
			else
				endOff = tempOff;
			//System.out.println("endOff: "+endOff);
		}
		//System.out.println("New endOff: "+endOff+" snip: ["+temp.substring(endOff, endOff+10)+"]");
		endOff += "</table>".length()+1;

				
		first = temp.substring(0, startOff);
		last = temp.substring(endOff, temp.length());
		//System.out.println("Page bottom part size: "+first.length()+", last part size: "+last.length());
		temp = first + last;
		
		return temp;
	}


	/**
	 * Fix images in a page by scanning for media file references by replacing
	 * relative file references with a URL to an image in the media folder with optional category.
	 * 
	 * @param depth - recursion depth used to indent any console messages.
	 * @param siteName - the site name
	 * @param pageName - full path to the web page to be fixed.
	 * @param category - media category to use, which may be empty but not null.
	 * @return fixed content
	 */
	private void fixImagesByRefs(int depth, String siteName, String pageName, String category) {
		if ( (siteName == null) || (pageName == null) || (category == null) ) {
			System.out.println("fixImagesbyRefs: null parameters were passed.");
			return;
		}
		
		//	Load the page content.  By default, the fixed content is the same.
		String content = Util.loadFile(pageName);
		if (content == null) {
			System.out.println(Util.tabber(depth)+"Unable to load page contents for: "+pageName);
			return;
		}
		String fixedContent = content;
		
		//	Extract the references to media files.
		List<String> refs = this.extractMediaRefs(content);
		//System.out.println(Util.tabber(depth)+"Refs to fix: "+refs.size());
		
		//	This is used to filter out references that are in the media folder.
		String mediaRoot = FileSep + siteName + FileSep + SiteRegistry.MEDIA_DIR_NAME;
		
		//	Iterate over the references found.
		for (String r : refs) {
			//	Check to see if this is reference to a file in the media folder, avoid over-fixing.
			if (r.indexOf(mediaRoot) == 0)
				continue;
			
			//	Check for the [v6 case and skip it.
			if (r.indexOf("[") != -1)
				continue;
			
			String[] parts = r.split("/");
			String mediaFileName = parts[parts.length-1];
			
			System.out.println(Util.tabber(depth)+"Fixing image refs to: "+r+" Category: "+category+" File: "+mediaFileName);

			//	Check for the JavaScript void case.  This results in a broken link, but at least it's not to a script.
			if (mediaFileName.compareTo("javascript:void(0);") == 0)
				mediaFileName = "javascript-void.png";
			
			//	Apply the fix
			String mediaTarget = "/"+siteName+"/"+SiteRegistry.MEDIA_DIR_NAME+"/"+mediaFileName;
			if (category.length() > 0)
				mediaTarget = "/"+siteName+"/"+SiteRegistry.MEDIA_DIR_NAME+"/"+category+"/"+mediaFileName;
			fixedContent = fixedContent.replaceAll(r, mediaTarget);
		}
		
		//	Save the fixed content out to the page file.
		Util.saveFile(fixedContent, pageName);
	}


	/**
	 * Fix all web page references (links).
	 * 
	 * Links generated by TakeOut fall into one of the following patters:
	 * 
	 *     Folder Traversal:  ../../projects/cold-frame.html
	 *     Relative to a Folder:  2011/2011-08-02-introduction.html
	 *     No Path:  starting-point.html
	 *     External:  http://creativecommons.org/licenses
	 * 
	 * Internal references need to be converted into the PageId model, which means
	 * finding a page ID.  Besides the GUID, a page can be uniquely identified by
	 * it's site, relative path, and file name.
	 * 
	 * The site name is passed in and the file is always the last node in the full
	 * file name.  The relative path, however, can be tricky to calculate.  All
	 * internal references are done relative to the current page (being fixed),
	 * so the path to that page is "adjusted" based on the above patterns to
	 * create a relative path that can be used to find a page record.
	 * 
	 * This implementation (Mar. 2, 2024) scans the content file linearly and fixes anchors when they are found.
	 * This replaces the older approach of looking for HREFs and globally replacing them in the
	 * content.  The latter approach doesn't fix problems in the anchor, such as frame targeting.
	 * 
	 * @param content
	 * @return fixed content string
	 */
	private void fixLinks(int depth, String siteName, String pageName) {
		if ( (siteName == null) || (pageName == null) ) {
			System.out.println("fixImagesbyRefs: null parameters were passed.");
			return;
		}
		
		//	Load the page content.  By default, the fixed content is the same.
		String content = Util.loadFile(pageName);
		if (content == null) {
			System.out.println(Util.tabber(depth)+"Unable to load page contents for: "+pageName);
			return;
		}
		
		//	Converted content is assembled in this string buffer.
		StringBuffer sb = new StringBuffer();
		
		String linkStart = "<a";
		String linkEnd = "</a>";
		
		//	Scan the page for anchors and fix them.
		for (int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);
			String remainingContent = content.substring(i);
			
			// Check for an anchor start.
			if (checkForTag(remainingContent, linkStart)) {
				
				// Find the end of the HREF.
				int end = remainingContent.indexOf(linkEnd);
				
				String link = remainingContent.substring(0, end+linkEnd.length());
				//System.out.println(Util.tabber(depth)+link);
				
				//	Fix the link.
				String fixedLink = this.fixAnchor(depth, siteName, pageName, link);
								
				//	Add it to the fixed content.
				sb.append(fixedLink);
				
				i += link.length();	// Skip over the current link start.
			}

			// Otherwise just copy the current character.
			else {
				sb.append(ch);
			}
		}
		
		//	Save the fixed content back out to the page file.
		String fixedContent = sb.toString();
		Util.saveFile(fixedContent, pageName);
	}
	
	/**
	 * A full anchor element is passed in srcAnchor.  The HREF and link text are parsed out by
	 * linearly walking the srcAnchor string using the following algorithm:
	 * 
	 *   1: Scan the source anchor string and parse out href and link text.
	 *   2: Skip to the start of an HEF in the source content.
	 *   3: Skip over the HREF parameter name.
	 *   4: Collect the HERF value.
	 *   5: Skip to the end of the anchor tag.
	 *   6: Skip over the end of the anchor tag.
	 *   7: Collect the link text.
	 * 
	 * This results in the HREF in srcHrefBuf (srcHref) and the link text in linkTextBuf (linkText).
	 * The HREF is then used to calculated the relative path to this page.
	 * Using site name, relative path, and the page file name, the page is looked up in the Page Registry
	 * If found, it is used to create a PageId URL, which is used to fix the HREF.
	 * If not found, the original source anchor is returned, which will result in a broken link to be fixed
	 * manually later.
	 * 
	 * In tests, a case came up where the anchor passed didn't contain an HREF.  This is legitimate in HTHML
	 * and is used to create a "page anchor".  A test was added for this case and the srcAnchor returned.
	 * 
	 * @param depth
	 * @param siteName
	 * @param pageName
	 * @param srcAnchor
	 * @return
	 */
	private String fixAnchor(int depth, String siteName, String pageName, String srcAnchor) {
		String hrefStart = "href=\"";
		
		//	Check for an anchor with no HREF.  Pathological, but it does happen.
		if (srcAnchor.indexOf(hrefStart) == -1)
			return srcAnchor;
		
		StringBuffer srcHrefBuf = new StringBuffer();		//	Collects the source HREF string.
		StringBuffer linkTextBuf = new StringBuffer();	//	Collects the link text string.
		
		//	1: Scan the source anchor string and parse out href and link text.
		int i = 0;
		char ch = srcAnchor.charAt(i);
		String remainingContent = srcAnchor.substring(i);
		
		//	2: Skip to the start of an HEF in the source content.
		while (i<srcAnchor.length() && !checkForTag(remainingContent, hrefStart)) {
			i++;
			ch = srcAnchor.charAt(i);
			remainingContent = srcAnchor.substring(i);
		}
		
		//	3: Skip over the HREF parameter name.
		i += hrefStart.length();
		ch = srcAnchor.charAt(i);
		
		//	4: Collect the HERF value.
		while (i<srcAnchor.length() && ch != '"') {
			srcHrefBuf.append(ch);
			i++;
			ch = srcAnchor.charAt(i);
		}
		
		//	5: Skip to the end of the anchor tag.
		while (i<srcAnchor.length() && ch != '>') {
			i++;
			ch = srcAnchor.charAt(i);
		}
		
		//	6: Skip over the end of the anchor tag.
		i++;
		if (ch == '\n')
			i++;
		ch = srcAnchor.charAt(i);
		
		//	7: Collect the link text.
		while (i<srcAnchor.length() && ch != '<') {
			linkTextBuf.append(ch);
			i++;
			ch = srcAnchor.charAt(i);
		}
		
		//	Convert the collected values into strings and trim them.
		String srcHref = srcHrefBuf.toString().trim();
		String linkText = linkTextBuf.toString().trim();
		
		//	Proposed simple fix, path is not adjusted.
		String fixedLink = "<a href=\""+srcHref+"\" target=\"_blank\" >"+linkText+"</a>\n";   
		
		//	Check for previously fixed references.  Prevent over fixing.
		if (srcHref.indexOf("localhost") == 0) {
			System.out.println(Util.tabber(depth)+"Previous fix: "+srcHref+" - "+linkText);
			return srcAnchor;	//	Avoid over fixing.
		}
		
		//	Check for external links.
		if (srcHref.indexOf("http") == 0) {
			//System.out.println(Util.tabber(depth)+"External fix: "+fixedLink);
			return fixedLink;			
		}

		//	Check for an image reference.
		FileType type = this.getFileType(srcHref);
		if (type == FileType.IMAGE) {
			//System.out.println(Util.tabber(depth)+"Image reference: "+srcHref+" - "+linkText);
			return srcAnchor;	//	Avoid over fixing.			
		}
		
		//	Get the fixed path.
		String newRelPath = this.fixPath(ch, siteName, pageName, srcHref);
		
		//	Get the web file name.
		String file = null;
		if (srcHref.indexOf("/") == -1)	//	It might not have a path.
			file = srcHref;
		else {
			String[] parts = srcHref.split("/");
			if (parts.length == 0)	//	It might be a leading slash.
				file = srcHref;
			else
				file = parts[parts.length-1];
		}

		//	Lookup the page using site/path/name combination.
		PageId page = null;
		try {
			page = this.pageRegistry.getPageByFile(siteName, newRelPath, file);
		}
		catch (PageException pg) {
			System.out.println(Util.tabber(depth)+"Can't find page for: "+siteName+" - "+newRelPath+" - "+file);
			System.out.println(Util.tabber(depth)+"No fix for: "+srcHref+" - "+linkText);
			return srcAnchor;	//	Returning the source makes this whole method non-destructive.
		}

		//  If a page wasn't found, we can't fix the reference. Should be caught in the exception.  This is for safety.
		if (page == null) {
			System.out.println(Util.tabber(depth)+"Can't find page for: "+siteName+" - "+newRelPath+" - "+file);
			System.out.println(Util.tabber(depth)+"No fix for: "+srcHref+" - "+linkText);
			return srcAnchor;	//	Returning the source makes this whole method non-destructive.
		}
		
		//	Get the Site View reference from the pae.
		String siteViewRef = page.getUrl();
		System.out.println(Util.tabber(depth)+"Fixed reference: "+siteViewRef);		
		
		//	Assemble the final link fix.
		fixedLink = "<a href=\""+siteViewRef+"\" target=\"_blank\" >"+linkText+"</a>\n";
		
		System.out.println(Util.tabber(depth)+"Fix needed: "+srcHref+" - "+linkText);
		System.out.println(Util.tabber(depth)+"Proposed fix: "+fixedLink);
					
		return fixedLink;
	}
	
	private String fixPath(int path, String siteName, String pageName, String ref) {
		
		//	Extract the relative path for this page.
		String relPath = Util.extractRelativePath(siteName, pageName);

		//	Split up the file name referenced.
		String[] parts = ref.split("/");
		
		//	If we have folder traversal characters, adjust the relative path.
		parts = ref.split("\\.\\.");
		String newRelPath = "";
		int traversalCt = parts.length-1;
		if (traversalCt > 0) {
			parts = relPath.split("/");
			for (int i=0; i<parts.length-traversalCt; i++)
				newRelPath += FileSep + parts[i];
			//System.out.println(Util.tabber(depth)+"New relative path: "+newRelPath);
		}
		else
			newRelPath = relPath;
		
		//	Move path nodes from the original reference (if any) to the relative path.
		parts = ref.split("/");
		for (int i=0; i<parts.length-1; i++) {
			//	Skip traversal nodes.
			if (parts[i].compareTo("..") == 0)
				continue;
			newRelPath += FileSep + parts[i];
		}
		
		//	Trim of leading file separators, if present.
		if (newRelPath.length()>0 && newRelPath.charAt(0) == '/')
			newRelPath = newRelPath.substring(1, newRelPath.length());
		
		return newRelPath;
	}
	/**
	 * Return true if a content tag is found.
	 * 
	 * @param content - remaining content string.
	 * @param tag     - to be tested for
	 * @return true if tag is found.
	 */
	private boolean checkForTag(String content, String tag) {
		// System.out.println("check for tag: content
		// len="+content.length()+"tag="+tag);
		if (content.length() < tag.length())
			return false;
		String candidate = content.substring(0, tag.length());
		// if ((content.charAt(1) == tag.charAt(1)) && tag.compareTo("<img ") == 0)
		// System.out.println("checkForTag(): candidate="+candidate+", tag="+tag);

		if (candidate.compareTo(tag) == 0)
			return true;
		else
			return false;
	}
	
	
	/****************************************************************************
	 * EXTRACT METHODS
	 ****************************************************************************/

	/**
	 * Extracts the page title and name from the <title> element in the source content header.
	 * Page name is the same as the original title.
	 * 
	 * Note:  Google TakeOut appends the site title to the end of the page title.  Code added to remove
	 * this on 2/19/2024.
	 * 
	 * New page title converts dashes to spaces and capitalizes words.
	 * 
	 * Examples to handle:
	 * 	1. place (no dashes) ==> Place
	 * 	2. place-1 (single dash) ==> Place 1
	 * 	3. place--1 (two dashes) ==> Place 1
	 * 	4. place - 1 (space dash space) ==> Place - 1
	 * 	5. place---1 (three dashers) ==> Place - 1
	 * 
	 * @param content of page
	 * @return a map containing title and name entries.
	 */
	private String extractTitle(String content) {
		String siteTitleKeyword = "\"siteTitle\":\"";
		
		//	Extract the site title.
		String siteTitle = "";	//	Default to an empty title.
		int siteTitleStart = 0;
		int siteTitleEnd = 0;
		siteTitleStart = content.indexOf(siteTitleKeyword);
		if (siteTitleStart != -1) {
			siteTitleStart += siteTitleKeyword.length();
			siteTitleEnd = content.indexOf("\"", siteTitleStart);
			//System.out.println("====> Site Title Start: "+ siteTitleStart + " Title: "+siteTitleEnd);
			if ( (siteTitleEnd != -1) && (siteTitleEnd-siteTitleStart > 1) ) {
				siteTitle = content.substring(siteTitleStart, siteTitleEnd);
				//System.out.println("====> Site Title: "+ siteTitle);
			}
		}
		
		//	Find the start and end of the TITLE element in the content.
		int titleStart = content.indexOf("<title>");
		int titleEnd = content.indexOf("</title>");

		//	Extract the page title from the title element.  If there is a site title, remove it.
		String title = content.substring(titleStart+"<title>".length(), titleEnd);
		if (siteTitle.length()>0) {
			//System.out.println("====> Site Title: "+ siteTitle + " Title: "+title);
			title = title.replace(" - "+siteTitle, "");
			//System.out.println("====> Fixed Title: "+ title);
		}
			//title = content.substring(0, titleEnd-siteTitle.length()-3);
		//title = content.substring(titleStart+"<title>".length(), titleEnd-siteTitle.length()-3);
			

		//	Fix dashes to make words in the title.
		title = title.replaceAll(" - ", " ");
		title = title.replaceAll("---", " ");
		title = title.replaceAll("--", " ");
		//title = title.replaceAll("-", " ");	//	Deletes all dashes from the title.
		
		title = Util.capitalize(title);
		
		return title;
	}
	
	/**
	 * Relying on the BODY tag doesn't work with Takeout content.  The content
	 * starts at DIV and ends at another.
	 * 
	 * @param content
	 * @return
	 */
	private String extractBody (String content) {
		String start = "<div id=\"sites-canvas-main-content\">";
		String end = "<div id=\"sites-canvas-bottom-panel\">";

		/* Find the start and end points. */
		int startOffset = content.indexOf(start);
		int endOffset = content.indexOf(end);

		/* Extract the body content. */
		String extractedContent = content.substring(startOffset, endOffset);
		return extractedContent;
	}
	
	/**
	 * Scam the content provided for references to media files, filtered by a file type of IMAGE.
	 * 
	 * @param content
	 * @return List of media reference strings.
	 */
	private List<String> extractMediaRefs(String content) {
		List<String> refs = new Vector<String>();

		//	Tag constants.
		String ANCHOR = "<a";
		String HREF = "href=\"";
		String IMAGE = "<img";
		String SRC = "src=\"";
				
		int tempOff = 0;
		int refStart = 0;
		int refEnd = 0;
		String refString = null;
		
		//	Scan for HREFs.
		while (tempOff != -1) {
			tempOff = content.indexOf(ANCHOR,tempOff);
			if (tempOff != -1) {
				//	Advance the offset past the currently found instance.
				tempOff += ANCHOR.length();
				
				//	Extract the reference string.
				refStart = content.indexOf(HREF, tempOff)+HREF.length();
				refEnd = content.indexOf("\"", refStart);
				refString = content.substring(refStart, refEnd);
				
				//	Filter by file type and add to the list of reference strings.
				FileType type = this.getFileType(refString);
				if (type == FileType.IMAGE)
					refs.add(refString);
			}
			else {
				tempOff = -1;	//	Signal the end of the scan.
				break;
			}
		}

		//	Scan for HREFs.
		while (tempOff != -1) {
			tempOff = content.indexOf(IMAGE,tempOff);
			if (tempOff != -1) {
				//	Advance the offset past the currently found instance.
				tempOff += IMAGE.length();
				
				//	Extract the reference string.
				refStart = content.indexOf(SRC, tempOff)+HREF.length();
				refEnd = content.indexOf("\"", refStart);
				refString = content.substring(refStart, refEnd);
				
				//	Filter by file type and add to the list of reference strings.
				FileType type = this.getFileType(refString);
				if (type == FileType.IMAGE)
					refs.add(refString);
			}
			else {
				tempOff = -1;	//	Signal the end of the scan.
				break;
			}
		}
		
		return refs;
	}
	
	
	/**
	 * Extract a media file category from a full path source file name.
	 * If the file name comes immediately after the site name, then return an empty string.
	 * Otherwise, return the path node name after the site name.
	 * 
	 * @param siteName
	 * @param srcName
	 * @return media category or empty string.
	 */
	public String extractMediaCategory(String siteName, String srcName) {
		//	Pull out the media category.
		String[]parts = srcName.split("/");
		int ct = parts.length;
		
		if (ct < 2)
			return "";
		
		//	Find the site name.
		int offset = 0;
		for (String part : parts) {
			if (part.compareTo(siteName)==0)
				break;
			else
				offset++;
		}
		
		//	Check for no site name found.
		if (offset == 0)
			return "";
		
		//	If the next part doesn't have a period, then it is a category name.
		if (parts[offset+1].indexOf('.') == -1)
			return parts[offset+1];

		return "";
	}
	
	
	/****************************************************************************
	 * UTILITY METHODS
	 ****************************************************************************/
	
	/**
	 * Generate a new header (HEAD) section given a PageId object
	 * 
	 * @param page
	 * @return header text
	 */
	private String getHeaderText(Site site, PageId page) {
		StringBuffer sb = new StringBuffer();
		
		// Add header block with title and meta tags.
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html lang=\"en-us\">\n");
		sb.append("<head>\n"); // Fixed from <header>
		//	This style sheet reference allows BlueGriffin to style text during edit.
		
		// TODO:  Fix hard coded CSS reference.
		//sb.append("\t<link rel=\"stylesheet\" href=\"/" + STYLE_SHEET_URL + "\">\n");
		//sb.append("\t<link rel=\"stylesheet\" href=\"http://localhost:8080/nolaria/green.css\">\n");
		String cssUrl = SiteRegistry.LOCAL_HOST+FileSep+site.getName()+FileSep+site.getCss();
		sb.append("\t<link rel=\"stylesheet\" href=\""+cssUrl+"\">\n");
		sb.append("\t<title>"+page.getTitle()+"</title>\n");

		// Add title and name meta tags.
		sb.append("\t<meta name=\"title\" content=\"" + page.getTitle() + "\" />\n");
		sb.append("\t<meta name=\"file\" content=\"" + page.getFile() + "\" />\n");
		sb.append("\t<meta name=\"id\" content=\"" + page.getId() + "\" />\n");
		
		sb.append("\t<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />\n");
		sb.append("\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
		sb.append("\t<meta http-equiv=\"Expires\" content=\"0\" />\n");

		sb.append("</head>\n"); // Fixed from </header>
		sb.append("<body>\n");
		sb.append("<h1>" + page.getTitle() + "</h1>\n");
		
		return sb.toString();
	}


	/****************************************************************************
	 * UTILITY METHODS
	 ****************************************************************************/

	/**
	 * TODO:  Move this to Util?
	 * 
	 * Accepts a file path and returns it's file type. File type is based on
	 * extension names. This assumes that a file name will only have a single period
	 * (.) in it.
	 * 
	 * @param path - full file path.
	 * @return a FileType
	 */
	public FileType getFileType(String path) {
		File pathFile = new File(path);
		String name = pathFile.getName();

		String parts[] = name.split("\\.");

		// No parts is an error.
		if (parts.length == 0)
			return FileType.UNKNOWN;

		// One part is a directory.
		if (parts.length == 1)
			return FileType.DIR;

		// Two parts means an extension was found.
		String extension = parts[1].toLowerCase();
		switch (extension) {
			// Check for web pages.
			case "html":
			case "htm":
				return FileType.WEB;
	
			// Check for text files.
			case "txt":
			case "md":
				return FileType.TEXT;
	
			// Check for an XM file.
			case "xml":
				return FileType.XML;
	
			// Check for a JSON file.
			case "jsn":
			case "json":
				return FileType.JSON;
	
			// Check for image files.
			case "gif":
			case "jpg":
			case "jpeg":
			case "png":
			case "ico":
				return FileType.IMAGE;
	
			// Check for video files.
			case "mov":
			case "mpg":
			case "mpg4":
				return FileType.VIDEO;
	
			// Check for audio files.
			case "mp3":
			case "wav":
				return FileType.AUDIO;
		}

		return FileType.UNKNOWN;
	}
	
	/**
	 * If the folder at the full source name passed has web pages within it, return true.
	 * @param srcName
	 * @return true if srcName has web pages in it.
	 */
	private boolean hasSubPages(String srcName) {
		File srcDir = new File(srcName);
		if (!srcDir.isDirectory())
			return false;
		
		File[] files = srcDir.listFiles();
		for (File f : files) {
			if (f.isFile()) {
				String fileName = f.getName();
				FileType type = this.getFileType(fileName);
				if (type == FileType.WEB)
					return true;
			}
		}
		return false;
	}

	/****************************************************************************
	 * TEST METHODS
	 ****************************************************************************/

	/**
	 * Test the fixBoundingBox() algorithm on the home page associated with the site name passed.
	 * Better to run this text against pages that have already been (partially) fixed.
	 * 
	 * @param siteName
	 * @return true if it works.
	 */
	private boolean testFixBoundingBox (String siteName)  {
		
		String homePageName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+siteName+".html";
		String srcContent = Util.loadFile(homePageName);
		if (srcContent == null) {
				System.out.println("Unable to open: "+homePageName);
				return false;
		}
		System.out.println("Source content length: "+srcContent.length());
		
		String fixedContent = this.fixBoundingBox(srcContent);
		System.out.println("\n\n"+fixedContent);
		
		return true;
	}
	
	/**
	 * Test the fixImages() algorithm on the home page associated with the site name passed.
	 * Better to run this text against pages that have already been (partially) fixed.
	 * 
	 * @param siteName
	 * @return true if it works.
	 */
	private boolean testFixImages (String siteName) {
		String fixedContent = null;
		String homePageName = SiteRegistry.FILE_ROOT+"/"+siteName+"/"+siteName+".html";

		//fixedContent = this.fixImages(srcContent);
		this.fixImagesByRefs(0, siteName, homePageName, "");
		
		System.out.println("\n\n"+fixedContent);

		return true;
	}
	
}
