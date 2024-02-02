package com.nolaria.stieview.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	public static Boolean ContentOnly = true; // True if only the content is to be copied.

	public static enum FileType {
		WEB, IMAGE, VIDEO, AUDIO, TEXT, XML, JSON, DIR, UNKNOWN
	};
	
	public static final String STYLE_SHEET_PATH = "d:\\dev\\siteview\\shared\\styles\\green.css";

	// This is where all of the source pages live, to be converted.
	public static final String TAKEOUT_DIR = "D:\\Google-Download\\Takeout\\ClassicSites";

	// Change these to be the names of the source and target site.
	public static final String SRC_SITE = "nolariaplanes";
	public static final String SRC_TITLE = " - nolaria-planes";
	public static final String TARGET_SITE = "planes";

	// Derived file paths and URLs based on the site name above.
	public static final String HOST = "http://localhost:8080";
	public static final String TOMCAT = "D:\\apache-tomcat-9.0.40";
	public static final String ROOT_PATH = TOMCAT + "\\webapps";
	public static final String STYLE_SHEET_URL = HOST + "/" + TARGET_SITE + "/" + TARGET_SITE + ".css";

	public static final String MEDIA_DIR_NAME = "media";
	
	public static final String TEST_FILE_SRC = "D:\\Google-Download\\Takeout\\ClassicSites\\nolariaplanes\\home.html";
	public static final String TEST_FILE_DEST = "D:\\apache-tomcat-9.0.40\\webapps\\planes\\home.html";
	
	//	Registries
	public SiteRegistry siteRegistery = new SiteRegistry();
	public PageRegistry pageRegistry = new PageRegistry();
	
	//	Instance variables.
	public String siteName = null;			//	Extracted from the sourceRootName.
	public int fileCount = 0;

	/**
	 * Main entry point for the converter application. NOTE: currently this is hard
	 * coded to a specific source web page and target directory. Later, this can be
	 * extracted from the args passed in.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Takeout Converter");
		System.out.println("-----------------\n");
		
		//	Check to see if arguments are passed.  These would be defined on the svconverter project.
		String sourceRootName = null;
		String cssName = null;
		if (args.length == 2) {
			sourceRootName = args[0];
			cssName = args[1];
		}
		else {
			System.out.println ("Arguments missing: SOURCE_ROOT, CSS_NAME");
			return;		//	Exit the app.
		}

		//System.out.println("Source Root Name: "+app.sourceRootName);
		//System.out.println("CSS Name: "+app.cssName);

		//	Extract the site name from the sourceRootName
		String[] parts = sourceRootName.split("/");
		int partCount = parts.length;
		app.siteName = parts[partCount-1];
		// System.out.println("Site to convert is named: "+app.siteName);
		
		//	Check to see if this site has already been created.
		Site site = app.siteRegistery.getSiteByName(app.siteName);
		if (site != null) {
			System.out.println("Site "+app.siteName+" exists, initialization was skipped.");
		}
		else {		
			System.out.println("\nReady to convert: "+app.siteName);
			
			//	Create the site, initialize folders and files, except for the home page.
			String sitePath = "webapp/"+app.siteName;
			site = app.siteRegistery.createSite(app.siteName, sitePath, cssName, false);	
		}

		//	Convert just the home page.
		String homePageSourceName = sourceRootName+"/home.html";
		app.convertPage(site, homePageSourceName);
		
	}
	
	/**
	 * Convert the content of the source page passed and write it to the appropriate place in the site passed.
	 * There is some special case treatment of home pages.  If the source points to a 'home.html' file, the path
	 * is hard coded to an empty string (paths are relative to the root site folder) and the title is forced
	 * to be a capitalized form of the site name.
	 * 
	 * @param site
	 * @param homePageSrcName
	 */
	public void convertPage(Site site, String homePageSourceName) {
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
				if (s.compareTo(siteName) == 0) {
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
		String headText = this.getHeaderText(tempPage);
		String bodyText = this.extractBody(srcContent);
		bodyText = this.fixLineEnds(bodyText);
		
		//	Unit them to create a converted, but not fully fixed content string.
		String convertedContent = headText + bodyText + "\n</body>\n</html>\n";
		
		//	Apply all other fixes.
		convertedContent = this.fixContent(convertedContent);
		//System.out.println("\n"+convertedContent);
		
		//	Save the converted and fixed content to its destination location.
		Util.saveFile(convertedContent, homePageDestName);
		
		//	Register the page.
		if (isHomePage)
			//	In the case of the home page, force the home page file name to reflect the site name.
			pageFileName = siteName + ".html";
		try {
			this.pageRegistry.createPage(uuid, siteName, title, pageFileName, path);
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
	 * @param srcName - full page to the source folder to convert.
	 * @param siteName - new site content is converted to.
	 */
	public void convert(String srcName, String siteName) {
		String srcPath = TAKEOUT_DIR + "\\" + srcName;
		System.out.println("Source files path: " + srcPath);
		String targetFilePath = ROOT_PATH + "\\" + siteName;
		System.out.println("Target directory path: " + targetFilePath);

		File srcDirFile = new File(srcPath);
		String[] rootFiles = srcDirFile.list();
		System.out.println("Files in the source root: " + rootFiles.length);

		try {
			for (String rootFile : rootFiles) {
				if (rootFile.indexOf("html") >= 0) {
					// System.out.println ("rescursiveWalk
					// (1,"+srcPath+"\\"+rootFile+","+siteName+");");
					this.recursiveWalk(1, srcPath + "\\" + rootFile, siteName);
				}
			}
		} catch (IOException io) {
			io.printStackTrace();
		}

	}

	/**
	 * This version of convert is used to convert a whole file system starting from
	 * a given root web page. It works as follows:
	 * <ol>
	 * <li>Convert the file passed into target directory given by global.</li>
	 * <li>Determine if a corresponding src directory exists.</li>
	 * <li>If so, iterate over all files in that directory, recursing on each.
	 * </ol>
	 * 
	 * WARNING: This method uses recursion to traverse a file system tree.
	 * TODO:  Update to use convert()
	 * 
	 * @param depth         - current depth of the tree walk. Can be used for
	 *                      indentation if needed.
	 * @param filePath      - full file path to the current file being processed.
	 * @param relTargetPath - directory path relative to the root tomcat dir. No
	 *                      leading or following slashes.
	 * @throws IOException
	 */
	public void recursiveWalk(int depth, String filePath, String relTargetPath) throws IOException {

		File srcNode = new File(filePath);
		String srcName = srcNode.getName();

		//System.out.println(indent(depth) + "Copy and Convert: " + srcName + " to: " + relTargetPath);

		String[] sParts = srcName.split("\\.");
		String correspondingDirName = "";
		if (sParts.length == 2) {
			correspondingDirName = sParts[0];
			//System.out.println(indent(depth) + "Corresponding directory name: " + correspondingDirName);
		} else {
			//System.out.println(indent(depth) + "Unable to split source file name: " + srcName);
			return;
		}

		if (srcNode.isFile() == true) {
			FileType type = getFileType(filePath);
			//System.out.println(indent(depth) + "File Type: " + type);

			// Handle the file based on file type.
			switch (type) {
			case WEB:
				// Convert and copy the web page.
				System.out.println(Util.indent(depth) + "Copy and Convert Page: " + srcName + " to: " + relTargetPath);

				this.convertAndCopy (filePath, relTargetPath);
				this.fileCount++;

				// See if there is a corresponding directory for the web page in the current
				// directory.
				String[] parts = filePath.split("\\.");
				String correspondingDirPath = null;
				if (parts.length == 2) {
					correspondingDirPath = parts[0];

					File correspondingDirFile = new File(correspondingDirPath);
					if (correspondingDirFile.isDirectory()) {
						// Corresponding directory was found.
						File[] files = correspondingDirFile.listFiles();
						// System.out.println(indent(depth)+"Files to handle: "+files.length);
						// System.out.println(indent(depth)+relTargetPath+FileSep+correspondingDirName);
						for (int j = 0; j < files.length; j++) {
							File file = files[j];
							// Recurse on files in the corresponding directory.
							recursiveWalk(depth + 1, file.getAbsolutePath(),
									relTargetPath + FileSep + correspondingDirName);
						}
					}
					// else
					// System.out.println(indent(depth)+"No corresponding directory:
					// "+correspondingDirName);
				} else {
					//System.out.println(indent(depth) + "Unable to split source file path: " + filePath);
					return;
				}
				break;

			case TEXT:
			case IMAGE:
			case XML:
			case JSON:
			case AUDIO:
			case VIDEO:
				//System.out.println (indent(depth)+"Copy image: "+srcName+" to: "+relTargetPath);
				// copyFile (filePath, relTargetPath);
				// copyFile (filePath, rootName+FileSep+"media");
				String targetFileName = ROOT_PATH+"\\"+TARGET_SITE+"\\"+MEDIA_DIR_NAME+"\\"+srcName;
				System.out.println(Util.indent(depth)+"Copy image from "+filePath+" to: "+targetFileName);
				Util.copyFile(filePath, targetFileName);
				
				//System.out.println (indent(depth)+"Copy image relative: "+relTargetPath+" name: "+srcName+" media: "+MEDIA_DIR_NAME);				
				//////this.copyFileRelative (filePath, rootName, MEDIA_DIR_NAME);
				//this.copyFileRelative (relTargetPath, srcName, MEDIA_DIR_NAME);
				this.fileCount++;

				break;

			default:
				System.out.println(Util.indent(depth) + "Unknown file type: " + type + " for " + srcName);
				return;
			}
		} else
			System.out.println(Util.indent(depth) + "Node to process is not a file:  " + srcNode.getName());
	}

	/****************************************************************************
	 * FILE COPY METHODS
	 ****************************************************************************/

	/**
	 * Takes a full path to a source web page, converts it, and copies to a target
	 * directory.
	 * 
	 * @param srcPath       - full source file.
	 * @param relTargetPath - target directory relative to Tomcat root.
	 * @throws IOException
	 */
	public void convertAndCopy(String srcPath, String relTargetPath) throws IOException {
		File srcFile = new File(srcPath);
		String srcName = srcFile.getName();
		String srcContent = null;

		//System.out.println("convertAndCopy: "+srcName+" to: "+relTargetPath);

		/* Check for the existence of the relative target directory in Tomcat. */
		String rootDirPath = ROOT_PATH + FileSep + relTargetPath;
		File rootDir = new File(rootDirPath);
		if (!rootDir.exists()) {
			// System.out.println("Creating directory: "+rootDirPath);
			rootDir.mkdir();
		} else {
			/* This is where a recursive delete could go. */
			// System.out.println("Directory already exists: "+rootDirPath);
		}

		/* Load source file into memory. */
		BufferedReader br = new BufferedReader(new FileReader(srcFile));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			srcContent = sb.toString();
		} finally {
			br.close();
		}

		StringBuffer sb = new StringBuffer();

		// Extract title and name.
		Map<String, String> properties = extractNames(srcContent);
		properties.put("name", srcName.substring(0, srcName.indexOf(".")));
		
		// System.out.println("Page title: ["+properties.get("title")+"] name:
		// ["+properties.get("name")+"]");

		// Add header text, if any.
		sb.append(headerText(properties));

		// Convert the content.
		sb.append(convert(srcContent));

		// Add footer text, if any.
		sb.append(footerText(properties));

		String targetContent = sb.toString();

		/* Write the extracted content to target file. */
		String tgFilePath = ROOT_PATH + FileSep + relTargetPath + FileSep + srcName; // Uses page node now.
		File tgFile = new File(tgFilePath);
		System.out.println("Target file to write: " + tgFilePath + " Exists:" + tgFile.exists());
		if (!tgFile.exists()) {
			// System.out.println("Target file to create: "+tgFilePath);
			tgFile.createNewFile();
		}
		PrintWriter pw = new PrintWriter(tgFile);
		pw.print(targetContent);
		pw.close();
	}

	/**
	 * Takes a full path to a file and copies to a target directory.
	 * 
	 * @param srcPath       to source file.
	 * @param relTargetPath - target directory relative to Tomcat root.
	 * @param subDirectory  - usually the media folder, but could be another
	 *                      sub-folder.
	 * @throws IOException
	 */
	public void copyFileRelative(String srcPath, String relTargetPath, String subDirectory) throws IOException {
		File srcFile = new File(srcPath);
		String srcName = srcFile.getName();
		String targetDir = ROOT_PATH + FileSep + relTargetPath + FileSep + subDirectory;
		File targetDirFile = new File(targetDir);
		if (!targetDirFile.exists())
			targetDirFile.mkdir();
		// String fullTargetPath = rootPath+FileSep+relTargetPath+FileSep+srcName; //
		// Uses media node now.
		String fullTargetPath = ROOT_PATH + FileSep + TARGET_SITE + FileSep + subDirectory + FileSep + srcName; // Uses
																												// media
																												// node
																												// now.
		File tgFile = new File(fullTargetPath);
		// System.out.println("copyFile from "+srcName+" to "+fullTargetPath);

		// Check for duplicate media file name.
		if (tgFile.exists()) {
			System.out.println("WARNING:  File " + srcName + " already exists in the media directory!");
			return;
		}

		// System.out.println("copyFile: "+srcName+" to: "+relTargetPath);

		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(tgFile);

			// Copy binary data from in to out.
			int datum = 0;
			while ((datum = in.read()) != -1) {
				out.write(datum);
			}
		} finally {
			// Close all files.
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}
	
	/****************************************************************************
	 * CONTENT METHODS
	 ****************************************************************************/

	/**
	 * @deprecated in favor of convertPage()
	 * The standard converter method.
	 * 
	 * Currently, this uses a static file system path to the set of pages to be
	 * converted. Converted documents are written into the webapp folder of Tomcat
	 * using the TOMCAT_HOME environment variable. Any existing pages in the target
	 * location are deleted.
	 * 
	 * @param content - the full source file content
	 * @throws IOException;
	 */
	public String convert(String content) throws IOException {
		String start = "<div id=\"sites-canvas-main-content\">";
		String end = "<div id=\"sites-canvas-bottom-panel\">";

		/* Extract content. */
		int startOffset = content.indexOf(start);
		int endOffset = content.indexOf(end);

		/* Extract content and fix it up. */
		String targetContent = content.substring(startOffset, endOffset);
		targetContent = targetContent.replace("/>", "/>\n"); // Insert return after closing element.
		targetContent = targetContent.replace("</", "\n</"); // Insert return after closing element.
		// System.out.println("First nbsp: "+targetContent.indexOf("Â"));
		targetContent = targetContent.replace("Â", "&nbsp;"); // Convert non-breaking spaces.

		targetContent = this.fixLinks(targetContent);
		targetContent = this.fixImages(targetContent);

		return targetContent;
	}
	
	
	/**
	 * Apply known fixes to the document content passed.
	 * 
	 * @param content
	 * @return fixed content.
	 */
	public String fixContent(String content) {
		String temp = content;
		
		temp = this.fixNonBreakSpace(content);
		//temp = this.fixLinks(temp);
		//temp = this.fixImages(temp);
		
		return temp;
	}
	
	/**
	 * Google Takeout uses the 'Â' as a kind of non-breaking space.  This fix converts those
	 * characters into an HTML character symbol, &nbsp;
	 * @param content
	 * @return
	 */
	private String fixNonBreakSpace(String content) {
		//String fixedContent = content.replace("Â", "&nbsp;"); // Convert non-breaking spaces
		String fixedContent = content.replace("Â", ""); 		// Remove non-breaking spaces
		return fixedContent;
	}
	
	/**
	 * Google's Takeout content has been striped of white space to make the files smaller.
	 * That results in a file that's impossible to read.
	 * 
	 * Fix Line Ends solves this problem by:
	 * 
	 *   - Inserting a new line after closing element.
	 *   - Inserting a new line before closing element.
	 * 
	 * @param content
	 * @return
	 */
	private String fixLineEnds(String content) {
		String fixedContent = content;
		
		fixedContent = fixedContent.replace("/>", "/>\n");
		fixedContent = fixedContent.replace("</", "\n</");		
		
		return fixedContent;
	}

	/**
	 * Fix all web page or image references.
	 * 
	 * @param content
	 * @return fixed content string
	 */
	private String fixLinks(String content) {
		StringBuffer sb = new StringBuffer();
		
		String hrefStart = "<a href=\"";
		String hrefEnd = "\">";
		
		for (int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);
			String remainingContent = content.substring(i);

			// Check for an image.
			if (checkForTag(remainingContent, hrefStart)) {
				// Find the end of the HREF.
				int end = remainingContent.indexOf(hrefEnd);
				String href = remainingContent.substring(hrefStart.length(), end);
				//System.out.println("Link HREF found: "+href);
				
				String link = "<a target=\"_parent\" href=\"/sv?ref="+href+"&site="+TARGET_SITE+">";
				//System.out.println("\tReplacement link:" + link);
				
				sb.append(link);
				i += hrefStart.length();	// Skip over the current link start.
			}

			// Otherwise just copy the current character.
			else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Fix all image references in an IMG tag.<br>
	 * <br>
	 * Algorithm:<br>
	 * 1. Scan for the string "<img".<br>
	 * 2. When found, extract the whole IMG element.<br>
	 * 3. Fix the media reference in the src attribute.<br>
	 * 4. Append the fixed string to the string buffer.<br>
	 * 5. Advance the index by the length of the fixed IMG element.<br>
	 * 
	 * @param content
	 * @return fixed content string
	 */
	private String fixImages(String content) {
		StringBuffer fixedContent = new StringBuffer();

		// Check for the start of an IMG element.
		for (int i = 0; i < content.length(); i++) {
			char currentChar = content.charAt(i);

			if ((Character.toLowerCase(content.charAt(i)) == '<')
					&& (Character.toLowerCase(content.charAt(i + 1)) == 'i')
					&& (Character.toLowerCase(content.charAt(i + 2)) == 'm')
					&& (Character.toLowerCase(content.charAt(i + 3)) == 'g')) {

				System.out.println("Image found.");

				// Extract the IMG element.
				StringBuffer imageBuf = new StringBuffer();
				int j = i;
				while ((Character.toLowerCase(content.charAt(j)) != '>') && (j < content.length())) { // Second clause
																										// prevents
																										// running off
																										// the end.
					imageBuf.append(content.charAt(j));
					j++;
				}
				imageBuf.append('>');
				String imageElement = imageBuf.toString();

				// System.out.println("---------- Image element:");
				// System.out.println("---------- "+imageElement);

				// Extract the src parameter.
				int srcOff = imageElement.indexOf("src=");
				srcOff = srcOff + "src=\"".length(); // Skip over the src parameter to the start of the value.
				String tempStr = imageElement.substring(srcOff, imageElement.length());
				int quoteOff = tempStr.indexOf("\""); // Find the closing quote.
				String srcParameter = imageElement.substring(srcOff, srcOff + quoteOff); // Extract the value of the src
																							// parameter.

				// System.out.println("---------- srcParameter value: "+srcParameter);

				// Create the reference fix. The URL prefix is added to make the images viewable
				// in BlueGriffin.
				int slashOff = srcParameter.indexOf("/");
				
				//	TODO:  Fix hard coded site name.
				//String fixedRef = HOST + FileSep + TARGET_SITE + "/" + MEDIA_DIR_NAME + "/"
				String fixedRef = HOST + FileSep + "nolaria" + "/" + MEDIA_DIR_NAME + "/"

						+ srcParameter.substring(slashOff + 1, srcParameter.length());

				// System.out.println("---------- Fixed Ref: "+fixedRef);

				// Assemble the parts to make the fix.
				String firstPart = imageElement.substring(0, srcOff);
				String middlePart = fixedRef;
				String endPart = imageElement.substring(imageElement.indexOf(srcParameter) + srcParameter.length(),
						imageElement.length());
				String repairedElement = firstPart + middlePart + endPart;

				// System.out.println("---------- firstPart: "+firstPart);
				// System.out.println("---------- middlePart: "+middlePart);
				// System.out.println("---------- endPart: "+endPart);

				// System.out.println("---------- Repaired image element:");
				// System.out.println("---------- "+repairedElement);

				// Append the fixed image to the fixed content.
				fixedContent.append(repairedElement);

				// Advance the character pointer past the old image element.
				i += imageElement.length();
			} else {
				// Since this is not the start of an image element, just append the current
				// character.
				fixedContent.append(currentChar);
			}

		}

		// System.out.println("----------");
		// System.out.println(fixedContent);
		// System.out.println("----------");

		// Don't return the converted content until we know it works.
		return fixedContent.toString();

		// return content;
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

	/**
	 * @deprecated in favor of extractNames(), below
	 * 
	 * Google Sites had both a page title and name. In most cases, these are the
	 * same, but pages may have a different title than name. This method extracts
	 * the title and name from meta tags in the source content (before conversion).
	 * 
	 * Examples:
	 * 
	 * <meta name="title" content="Phase_3_Project" />
	 * <meta itemprop="name" content="Phase_3_Project" />
	 * 
	 * NOTE: titles and names were coming up with a suffix, such as"- nolaria-d".
	 * Changed code to truncate at the first dash. However, a title might contain a
	 * dash, which could screw up the name.
	 * 
	 * @param content
	 * @return Map of title, name.
	 */
	public Map<String, String> extractNamesOld(String content) {
		Map<String, String> names = new HashMap<String, String>();

		// Extract the page title
		int titleOff = content.indexOf("<meta name=\"title\"");
		// System.out.println("Title string: "+content.substring(titleOff,
		// titleOff+60));

		if (titleOff != -1) {

			// Scan for third quote mark.
			for (int i = 0; i < 3; i++) {
				while (content.charAt(titleOff) != '\"') {
					// System.out.println("Scan position: "+content.substring(titleOff,
					// titleOff+60));

					// Check for a close tag character and break if found.
					if (content.charAt(titleOff) == '>')
						break;

					// Otherwise, keep scanning.
					else {
						titleOff++;
					}
					// Check for a close tag character and break if found.
					if (content.charAt(titleOff) == '>')
						break;
				}
				titleOff++; // Skip over the quote mark.
			}
			// System.out.println("Title string: "+content.substring(titleOff,
			// titleOff+50));
			int endQuoteOff = titleOff;
			while (content.charAt(endQuoteOff) != '-') {
				if (content.charAt(endQuoteOff) == '>') {
					endQuoteOff = titleOff;
					break;
				}
				endQuoteOff++;
			}

			// TODO: Check for a single character title here and fix it from the page name.

			if (endQuoteOff - titleOff > 0) {
				String title = content.substring(titleOff, endQuoteOff - 1);
				names.put("title", title);
			} else {
				System.out.println("Final quote was not found: " + (endQuoteOff - titleOff));
			}
		}

		// Extract the page name
		int nameOff = content.indexOf("<meta itemprop=\"name\"");
		if (nameOff != -1) {

			// Scan for third quote mark.
			for (int i = 0; i < 3; i++) {
				while (content.charAt(nameOff) != '\"') {
					// System.out.println("Scan position: "+content.substring(nameOff,
					// titleOff+60));

					// Check for a close tag character and break if found.
					if (content.charAt(nameOff) == '>')
						break;

					// Otherwise, keep scanning.
					else {
						nameOff++;
					}
					// Check for a close tag character and break if found.
					if (content.charAt(nameOff) == '>')
						break;
				}
				nameOff++;
			}
			// System.out.println("Name string: "+content.substring(nameOff, nameOff+50));
			int endQuoteOff = nameOff;
			while (content.charAt(endQuoteOff) != '-') {
				if (content.charAt(endQuoteOff) == '>') {
					endQuoteOff = nameOff;
					break;
				}
				endQuoteOff++;
			}
			if (endQuoteOff - nameOff > 0) {
				String name = content.substring(nameOff, endQuoteOff - 1);
				names.put("name", name);
			} else {
				System.out.println("Final quote was not found: " + (endQuoteOff - nameOff));
			}
		}

		return names;
	}
	
	/**
	 * Extracts the page title and name from the <title> element in the source content header.
	 * Page name is the same as the original title.
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
	public Map<String, String> extractNames(String content) {
		Map<String, String> properties = new HashMap<String, String>();

		//	Extract the name from the title element.
		int titleStart = content.indexOf("<title>");
		int titleEnd = content.indexOf("</title>");
		String title = content.substring(titleStart+"<title>".length(), titleEnd);
		System.out.print("Raw page title: "+title);

		//	Strip off the site title.
		title = title.replaceAll(SRC_TITLE, "");

		//	Fix dashes to make words in the title.
		title = title.replaceAll(" - ", " ");
		title = title.replaceAll("---", " ");
		title = title.replaceAll("--", " ");
		title = title.replaceAll("-", " ");
		
		
		System.out.println(" == Final Title: "+title);

		//System.out.println("Final title: "+capTitle);

		title = Util.capitalize(title);
		properties.put("title", title);
		
		return properties;
	}

	/**
	 * Extracts the page title and name from the <title> element in the source content header.
	 * Page name is the same as the original title.
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
	public String extractTitle(String content) {
		//	Extract the name from the title element.
		int titleStart = content.indexOf("<title>");
		int titleEnd = content.indexOf("</title>");
		String title = content.substring(titleStart+"<title>".length(), titleEnd);

		//	Strip off the site title.
		//title = title.replaceAll(SRC_TITLE, "");

		//	Fix dashes to make words in the title.
		title = title.replaceAll(" - ", " ");
		title = title.replaceAll("---", " ");
		title = title.replaceAll("--", " ");
		title = title.replaceAll("-", " ");
		
		title = Util.capitalize(title);
		
		return title;
	}
	
	/**
	 * Relying on the BODY tag doesn't work with Takeout content.  The content
	 * starts at DIV and ends at another.
	 * @param content
	 * @return
	 */
	public String extractBody (String content) {
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
	 * @deprecated in favor of Util.updateHeaderInfo()
	 * Generate header text to be included in output file. Text generation is
	 * dependent on the contentOnly flag.
	 * 
	 * @param properties
	 * @return header text
	 */
	public String headerText(Map<String, String> properties) {
		StringBuffer sb = new StringBuffer();
		String title = properties.get("title");
		String name = properties.get("name");
		String pid = UUID.randomUUID().toString();

		// Generate text for a content only output.
		if (ContentOnly) {
			// Add header block with title and meta tags.
			sb.append("<!DOCTYPE html>\n");
			sb.append("<html lang=\"en-us\">\n");
			sb.append("<head>\n"); // Fixed from <header>
			//	This style sheet reference allows BlueGriffin to style text during edit.
			
			// TODO:  Fix hard coded CSS reference.
			//sb.append("\t<link rel=\"stylesheet\" href=\"/" + STYLE_SHEET_URL + "\">\n");
			sb.append("\t<link rel=\"stylesheet\" href=\"http://localhost:8080/nolaria/green.css\">\n");
			sb.append("\t<title>"+title+"</title>\n");

			// Add title and name meta tags.
			sb.append("\t<meta name=\"title\" content=\"" + title + "\" />\n");
			sb.append("\t<meta name=\"name\" content=\"" + name + "\" />\n");
			sb.append("\t<meta name=\"pid\" content=\"" + pid + "\" />\n");
			
			sb.append("\t<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />\n");
			sb.append("\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
			sb.append("\t<meta http-equiv=\"Expires\" content=\"0\" />\n");

			sb.append("</head>\n"); // Fixed from </header>
			sb.append("<body>\n");
			sb.append("<h1>" + title + "</h1>\n");
		}

		// Generate text for a static web page.
		else {
			// Add header block with title and meta tags.
			sb.append("<!DOCTYPE html>\n");
			sb.append("<html lang=\"en-us\"");
			sb.append("<head>\n"); // Fixed from <header>
			sb.append("\t<link rel=\"stylesheet\" href=\"/" + STYLE_SHEET_URL + "\">\n"); // This allows BlueGriffin to
																							// see the style sheet.
			sb.append("\t<title>" + title + "</title>\n");
			sb.append("\t<meta name=\"title\" content=\"" + title + "\" />\n");
			sb.append("\t<meta name=\"name\" content=\"" + name + "\" />\n");
			sb.append("\t<meta name=\"pid\" content=\"" + pid + "\" />\n");

			sb.append("\t<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />\n");
			sb.append("\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n");
			sb.append("\t<meta http-equiv=\"Expires\" content=\"0\" />\n");

			// This is covered in the style sheet, so not needed.
			// sb.append("\t<style>\n");
			// sb.append("\tbody {\n");
			// sb.append("\t\tcolor: black;\n");
			// sb.append("\t\tfont-family: Arial;\n");
			// sb.append("\t}\n");
			// sb.append("\t</style>\n");

			sb.append("</head>\n"); // Fixed from </header>
			sb.append("<body>\n");
			sb.append("<h1>" + title + "</h1>\n");
		}

		return sb.toString();
	}
	
	public String getHeaderText(PageId page) {
		StringBuffer sb = new StringBuffer();
		
		// Add header block with title and meta tags.
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html lang=\"en-us\">\n");
		sb.append("<head>\n"); // Fixed from <header>
		//	This style sheet reference allows BlueGriffin to style text during edit.
		
		// TODO:  Fix hard coded CSS reference.
		//sb.append("\t<link rel=\"stylesheet\" href=\"/" + STYLE_SHEET_URL + "\">\n");
		sb.append("\t<link rel=\"stylesheet\" href=\"http://localhost:8080/nolaria/green.css\">\n");
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

	/**
	 * Generate footer text to included in the output file. Text generation is
	 * dependent on the contentOnly flag.
	 * 
	 * @param properties
	 * @return footer text
	 */
	public String footerText(Map<String, String> properties) {
		StringBuffer sb = new StringBuffer();

		// Generate text for a content only output.
		if (ContentOnly) {
			// No footer text is generated.
		}

		// Generate text for a static web page.
		else {
			sb.append("</body>\n</html>\n");
		}

		return sb.toString();

	}

	/****************************************************************************
	 * UTILITY METHODS
	 ****************************************************************************/

	/**
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
	 * @deprecated in favor of Util.tabber();
	 * Return a string with a set of tabs equal to the depth passed.
	 * 
	 * @param depth
	 * @return tab string
	 */
	/*
	public String indent(int depth) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < depth; i++)
			sb.append("\t");
		return sb.toString();
	}
	*/
	
	/**
	 * @deprecated in favor of Util.capitalize()
	 * Return a string where each word is capitalized.
	 * For example "pleasure dome 1" becomes "Pleasure Dome 1"
	 * 
	 * Because words are isolated by a space, dashed strings are treated as a single word.
	 * Thus "pleasure dome-1" becomes "Pleasure Dome-1"
	 * 
	 * @param title
	 * @return capitalized title
	 */
	/*
	public String capitalize(String title) {
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
	*/
}
