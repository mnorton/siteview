package com.nolaria.stieview.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author markjnorton@gmail.com
 *
 *         This is a utility application that takes path name pointer to the
 *         root of a file system containing HTML files down loaded from Google
 *         Sites. The app converts these files by extracting the core content
 *         and adding additional mark up to create a set of pages that can be
 *         viewed locally.
 */
public class Converter {
	// Constants.
	public static Converter app = new Converter();
	public static final String FileSep = "/";
	public static Boolean ContentOnly = false; // True if only the content is to be copied.

	public static enum FileType {
		WEB, IMAGE, VIDEO, AUDIO, TEXT, XML, JSON, DIR, UNKNOWN
	};
	
	public static final String STYLE_SHEET_PATH = "d:\\dev\\siteview\\shared\\styles\\green.css";

	// This is where all of the source pages live, to be converted.
	public static final String TAKEOUT_DIR = "D:\\Google\\Takeout";

	// Change these to be the names of the source and target site.
	public static final String SRC_SITE = "nolariaplanes";
	public static final String TARGET_SITE = "planes";

	// Derived file paths and URLs based on the site name above.
	public static final String HOST = "http://localhost:8080";
	public static final String TOMCAT = "D:\\apache-tomcat-9.0.40";
	public static final String ROOT_PATH = TOMCAT + "\\webapps";
	public static final String STYLE_SHEET_URL = HOST + "/" + TARGET_SITE + "/" + TARGET_SITE + ".css";

	public static final String MEDIA_DIR_NAME = "media";

	/**
	 * Main entry point for the converter application. NOTE: currently this is hard
	 * coded to a specific source web page and target directory. Later, this can be
	 * extracted from the args passed in.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * This is the code that would accep src and target site names. if (args.lenght
		 * == 0 || args.length > 2) {
		 * System.out.println("Usage: Starting at: "+srcPath+"  "+rootName+"\n");
		 * return; } else { SRC_SITE = args[0]; TARGET_SITE = args[1]; }
		 */

		// app.convert("nolariadd", "nolaria");
		app.prepareTargetSite(TARGET_SITE);
		//app.convert(SRC_SITE, TARGET_SITE);

	}

	/**
	 * Prepare the target site by creating directories and copying the style sheet.
	 * 
	 * @param siteName
	 */
	public void prepareTargetSite(String siteName) {
		String targetFilePath = ROOT_PATH +"\\"+siteName;
		String mediaFilePath = targetFilePath+MEDIA_DIR_NAME;

		//	Create the target site directory.
		File rootDirFile = new File(targetFilePath);
		if (!rootDirFile.exists()) {
			rootDirFile.mkdir();
		}

		//	Create the media directory
		File mediaDirFile = new File(mediaFilePath);
		if (!mediaDirFile.exists()) {
			mediaDirFile.mkdir();
		}
		
		//	Copy the style sheet.
		String targetStylePath = targetFilePath + "\\" + siteName + ".css";
		try {
			this.copyFile(STYLE_SHEET_PATH, targetStylePath);
		}
		catch (IOException io) {
			System.out.println("Unable to copy "+STYLE_SHEET_PATH+" to "+targetStylePath);
			System.out.println(io.getMessage());
		}
		
		System.out.println(siteName+ " was prepared successfully.");
	}

	/**
	 * Convert the files in TAKEOUT_DIR/dirName to a new set of SiteView pages based
	 * on the Page Ref Model in Tomcat.
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

		System.out.println(indent(depth) + "Copy and Convert: " + srcName + " to: " + relTargetPath);

		String[] sParts = srcName.split("\\.");
		String correspondingDirName = "";
		if (sParts.length == 2) {
			correspondingDirName = sParts[0];
			System.out.println(indent(depth) + "Corresponding directory name: " + correspondingDirName);
		} else {
			System.out.println(indent(depth) + "Unable to split source file name: " + srcName);
			return;
		}

		if (srcNode.isFile() == true) {
			FileType type = getFileType(filePath);
			System.out.println(indent(depth) + "File Type: " + type);

			// Handle the file based on file type.
			switch (type) {
			case WEB:
				// Convert and copy the web page.
				System.out.println(indent(depth) + "Copy and Convert Page: " + srcName + " to: " + relTargetPath);

				////////// convertAndCopy (filePath, relTargetPath);

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
					System.out.println(indent(depth) + "Unable to split source file path: " + filePath);
					return;
				}
				break;

			case TEXT:
			case IMAGE:
			case XML:
			case JSON:
			case AUDIO:
			case VIDEO:
				// System.out.println (indent(depth)+"Copy: "+srcName+" to: "+relTargetPath);
				// copyFile (filePath, relTargetPath);
				// copyFile (filePath, rootName+FileSep+"media");

				////////// copyFileRelative (filePath, rootName, app.mediaDirName);

				break;

			default:
				System.out.println(indent(depth) + "Unknown file type: " + type);
				return;
			}
		} else
			System.out.println(indent(depth) + "Node to process is not a file:  " + srcNode.getName());
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

		// System.out.println("convertAndCopy: "+srcName+" to: "+relTargetPath);

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
		// TODO: Check for single characgter titles and fix them.
		Map<String, String> properties = extractNames(srcContent);
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
	
	/**
	 * Copy a file.  This will overwrite files that already exists, so be warned!
	 * 
	 * @param from full file path
	 * @param to full file path
	 * @throws IOException so that calling code can handle it.
	 */
	public void copyFile(String from, String to) throws IOException {
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
		// System.out.println("First nbsp: "+targetContent.indexOf("�"));
		targetContent = targetContent.replace("�", "&nbsp;"); // Convert non-breaking spaces.

		targetContent = this.fixReferences(targetContent);
		targetContent = this.fixImages(targetContent);

		return targetContent;
	}

	/**
	 * Fix all web page or image references.
	 * 
	 * @param content
	 * @return fixed content string
	 */
	private String fixReferences(String content) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < content.length(); i++) {
			char ch = content.charAt(i);
			String remainingContent = content.substring(i);

			// Check for an image.
			if (checkForTag(remainingContent, "<img ") || checkForTag(remainingContent, "<IMG ")) {
				// System.out.println("Reference found.");
				sb.append(ch);
			}

			// Otherwise just copy the current character.
			else {
				sb.append(ch);
			}
		}
		return content;
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
				String fixedRef = HOST + FileSep + TARGET_SITE + "/" + MEDIA_DIR_NAME + "/"
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
	public Map<String, String> extractNames(String content) {
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

			// Add title and name meta tags.
			sb.append("<meta name=\"title\" content=\"" + title + "\" />\n");
			sb.append("<meta name=\"name\" content=\"" + name + "\" />\n");
			sb.append("<meta name=\"pid\" content=\"" + pid + "\" />\n");
		}

		// Generate text for a static web page.
		else {
			// Add header block with title and meta tags.
			sb.append("<!DOCTYPE html>\n");
			sb.append("<html svTitle=\"" + title + "\" lang=\"en-us\">\n");
			sb.append("<head>\n"); // Fixed from <header>
			sb.append("\t<link rel=\"stylesheet\" href=\"/" + STYLE_SHEET_URL + "\">\n"); // This allows BlueGriffin to
																							// see the style sheet.
			sb.append("\t<title>" + title + "</title>\n");
			sb.append("\t<meta name=\"title\" content=\"" + title + "\" />\n");
			sb.append("\t<meta name=\"name\" content=\"" + name + "\" />\n");
			sb.append("\t<meta name=\"pid\" content=\"" + pid + "\" />\n");

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

		// One part is a directgory.
		if (parts.length == 1)
			return FileType.DIR;

		// Two parts means an extension was found.
		String extension = parts[1];
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
	 * Return a string with a set of tabs equal to the depth passed.
	 * 
	 * @param depth
	 * @return tab string
	 */
	public String indent(int depth) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < depth; i++)
			sb.append("\t");
		return sb.toString();
	}
}
