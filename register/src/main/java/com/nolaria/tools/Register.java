/**
 * 
 */
package com.nolaria.tools;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;

import com.nolaria.sv.db.*;

/**
 * Scan a file tree for the DEFAULT_SITE and register any unregistered pages.
 * NOTE:  This class makes use of utility methods in com.nolaria.sv.db.Util.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class Register {
	private static final String DEFAULT_SITE = "nolaria";
	
	private static String ALLEGORY_ID = "7cbd34d1-72e3-43b9-b35d-4129ca489547";
	private static String ALTAMEK_ID = "4ca132aa-fcd0-4cd9-84b5-c5abaa10a58a";

	
	
	//	An instance of the application class.
	public static Register register = null;

	//public String FILE_ROOT = "D:\\apache-tomcat-9.0.40\\webapps";	//	Moved to the Util class.

	//	DB Connector, Site and Page registries.
	Connection conn = null;
	private SiteRegistry siteRegistry = null;
	private PageRegistry pageRegistry = null;
	
	//	Cached lists to speed up the scanning process.
	private List<PageId> allPages = null;
	private List<String> allPageNames = null;
	private List<String> failedRegistrations = null;
	
	public int registerCount = 0;

	/**
	 * Static main entry point.  Creates a Register object and starts the scan.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Register.register = new Register();
		  
		// Open a database connection to access associated tables.
		try {
			// Create the site and page registry objects.
			register.siteRegistry = new SiteRegistry();
			register.pageRegistry = new PageRegistry();
			
			// ===================  Test =============================
			//System.out.println("Page Registry Tests");
			//register.testSuccessiveGetPage();
			//register.testRegisterPage();

			// =================  Operations ==========================
			
			// Scan a site and register pages not already registered.
			register.scanAndRegister(DEFAULT_SITE);
			
			//	Scan a site and update page records with information from file metadata.
			register.scanAndUpdate(DEFAULT_SITE);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (register.conn != null)
				register.conn.close();
		}

	}

	/****************************************************************
	 *                    Operations                                *
	 ****************************************************************/
	
	/**
	 *	Scan the directory tree associated with the site name passed and register all
	 *	unregistered pages.
	 *
	 *	This creates a cached list of the file names of all registered pages.  There is a flaw in this
	 *	approach:  two pages might have the same file name, which would cause the second entry to throw 
	 *	a SQL exception (record already exists).  (This assumes the insert would fail, which it doesn't)
	 *
	 */
	public void scanAndRegister(String site) throws PageException {
		System.out.println("\n============  Register Pages ==============\n");
		System.out.println("Scanning this site: "+site);
		
		//	Get the list of all currently registered pages.  Speeds up checks for unregistered files.
		this.allPages = this.pageRegistry.getAllPages();
		this.allPageNames = new Vector<String>();
		for (PageId pg : allPages) {
			this.allPageNames.add(pg.getFile());
		}
		System.out.println("Registered pages: "+this.allPageNames.size());
		System.out.println();
		this.failedRegistrations = new Vector<String>();
		
		//for (String fn : this.allPageNames)
		//	System.out.println(fn);
		
		StringBuffer sb = new StringBuffer();
		directoryWalker(0, "\\"+site, sb);
		//System.out.println(sb.toString());
		
		System.out.println("Registration failures: ");
		if (this.failedRegistrations.size() == 0) {
			for (String fn : this.failedRegistrations)
				System.out.println("\t"+fn);
		}
		else
			System.out.println("\tnone");
	}

	/**
	 * Recurse over the directory tree generating text for each level.
	 * WARNING:  This method uses recursion!
	 * 
	 * @return nav content
	 */
	private void directoryWalker (int level, String relPath, StringBuffer sb) throws PageException {		
		//	Convert relative path to a full path.
		String dirPath = Util.FILE_ROOT + relPath;
		File dirFile = new File(dirPath);
				
		// Check for no files in this directory.
		File[] files = dirFile.listFiles();
		if (files == null || files.length == 0) {
			//sb.append("No files in path: " + dirPath + ".<br>");
			//sb.append("No files here.<br>\n");
			return;
		}
		
		TreeMap<String,File> fileList = new TreeMap<String,File>();
		TreeMap<String,File> dirList = new TreeMap<String,File>();

		//	Iterate over all files in this directory and sort them into maps.
		for (File f: files) {
			String name = f.getName();
			
			//	See if this file is a directory.
			if (f.isDirectory()) {
				dirList.put(name, f);
			}
			
			//	If not, it is a file.
			else {
				fileList.put(name, f);
			}
		}
		
		for (File f: files) {
			String name = f.getName();
			String relFilePath = Util.extractRelativePath(f.getPath());		//	Includes /sv/ at the start.
			relFilePath = relFilePath.replaceAll("\\\\", "/");
						
			//	See if this file is a directory.
			if (f.isDirectory()) {
				if (name.compareTo("media") != 0) {
					//sb.append(tabber(level)+relFilePath+".html\n");
					sb.append(Util.tabber(level)+this.register(relFilePath+".html")+"\n");
					directoryWalker(level+1, relFilePath, sb);
				}
			}
			
			//	If not, it is a file.
			else {				
				//	Filter out the style sheet, if it shows up.
				if ((name.compareTo("nolaria.css") == 0) || (name.compareTo("blue.css") == 0) || (name.compareTo("green.css") == 0))
						continue;
				
				//	If the name is not in the directory list, then add it.
				String[] parts = name.split("\\.");
				if (dirList.get(parts[0]) == null) {
					//sb.append(tabber(level)+relFilePath+"\n");
					sb.append(Util.tabber(level)+this.register(relFilePath)+"\n");
				}
			}

		}
	}
	
	/**
	 * Register the page at the relative file path passed.
	 * 
	 * @param relFilePath
	 * @return status string
	 */
	private String register(String relFilePath) throws PageException {
		String status = "???";
		
		//	The relPath has the site removed from it's front.
		String relPath = relFilePath;
		if (relPath.contains(DEFAULT_SITE)) {
			//relFilePath.replace(DEFAULT_SITE+"/", "");
			relPath = relPath.substring(DEFAULT_SITE.length()+1);
			//System.out.println(DEFAULT_SITE+" removed leaving: "+relPath);
		}
		//else
		//	System.out.println("Site not found in the relative path: "+relPath);
		relPath = relPath.substring(1);

		//	Remove the file name from path.
		String path = "";
		String parts[] = relPath.split("/");
		if (parts.length == 0)
			System.out.println("Rel path didn't split:  "+relPath);		
		for (int i=0; i<parts.length-1; i++)
			path += "/"+parts[i];
		if (path.length() != 0)
			path = path.substring(1);
		//System.out.println(path);
		
		//	Extract the file name.
		parts = relFilePath.split("/");
		if (parts.length == 0)
			System.out.println("Rel file path didn't split:  "+relFilePath);		
		String fileName = parts[parts.length-1];
		
		//	See if it is already registered.
		if (this.allPageNames.contains(fileName))
			status = "PREVIOUSLY";
		
		//	If not, it needs to be registered.
		else {
			String contents = Util.fetchContents(relFilePath);
			PageInfo info = Util.getHeaderInfo(contents);			
			
			//	Register the page.
			try {
				this.pageRegistry.createPage(info.pid, DEFAULT_SITE, info.title, fileName, path);
				status = "REGISTERED";
				this.registerCount++;
			}
			catch (PageException pg) {
				status = "FAILED";
				this.failedRegistrations.add(relFilePath);
				System.err.println(pg.getCause());
			}
		}
		
		return relFilePath + " --- "+status;
		//return fileName + " --- "+status;
	}

	/**
	 * Scan all records in the page_registry table and update them with info from the page contents.
	 * 
	 * @param site
	 * @throws PageException
	 */
	public void scanAndUpdate(String site) throws PageException {
		System.out.println("\n============  Update Pages ==============\n");
		System.out.println("Scanning this site: "+site);
		
		//	Get the list of all currently registered pages.  Speeds up checks for unregistered files.
		this.allPages = this.pageRegistry.getAllPages();

		System.out.println("Registered pages: "+this.allPages.size());
		System.out.println();

		int updates = 0;
		this.allPageNames = new Vector<String>();
		for (PageId pg : allPages) {
			//String fullPath = pg.getFullFileName();
			String relPath = "\\"+site+"\\"+pg.getPath()+"\\"+pg.getFile();
			String contents = Util.fetchContents(relPath);
			PageInfo headers = Util.getHeaderInfo(contents);
			
			boolean updateNeeded = false;
			
			//	See if the title needs to be updated.
			if (pg.getTitle() == null || headers.title == null || pg.getTitle().compareTo(headers.title) != 0) {
				System.out.println("\nTitle is different in: "+relPath);
				System.out.println("DB Title: ["+pg.getTitle()+"] vs "+ "File Title: ["+headers.title+"]");
				updateNeeded = true;
			}
			//	See if the name needs to be updated.
			/*
			if (pg.getFile() == null || headers.name == null || pg.getFile().compareTo(headers.name) != 0) {
				System.out.println("\nFile Name is different in: "+relPath);
				System.out.println("DB Name: "+pg.getFile()+" vs "+ "File Name: "+headers.name);
				updateNeeded = true;
			}
			*/

			//	Do the udpate, if needed.
			if (updateNeeded) {
				try {
					this.pageRegistry.updatePage(pg.getId(), site, headers.title, pg.getFile(), pg.getPath());
				}
				catch (PageException pe) {
					System.out.println("Error: "+pe.getCause());
				}
				updates++;
			}
		}

		System.out.println("Updates performed: "+updates);
		
	}
	
	/****************************************************************
	 *                    Test Operations                           *
	 ****************************************************************/

	/**
	 * Get two pages in a row from the Page Registry.
	 */
	@SuppressWarnings(value = { "unused" })
	public void testSuccessiveGetPage()  {
		
		try {
			PageId page1 = pageRegistry.getPage(ALLEGORY_ID);
			//System.out.println("Got the page called "+page1.getTitle());
			PageId page2 = pageRegistry.getPage(ALTAMEK_ID);
			//System.out.println("Got the page called "+page2.getTitle());
		}
		catch (PageException pg) {
			System.out.println("\ttestSuccessiveGetPage: FAILED");
			pg.printStackTrace();
		}
		
		System.out.println("\ttestSuccessiveGetPage:  PASS");
	}

	/**
	 * Get all and then attempt to register a new, test page.
	 * This attempts to reproduce a bug seen in PageIdFramework.createNewPage()
	 */
	@SuppressWarnings(value = { "unused" })
	public void testRegisterPage() {
		UUID randId = UUID.randomUUID();
		String pid = randId.toString();
		
		try {
			//	These happen in the PageIdFramework constructor.
			Site site = siteRegistry.getSiteByName(DEFAULT_SITE);
			
			PageId testPage = pageRegistry.getPage(pid);
			if (testPage != null)
				System.out.println("\tPage already exists for: "+pid);

			List<PageId> allPages = pageRegistry.getAllPages();
			
			//	These happen in createNewPage()
			pageRegistry.createPage(pid, DEFAULT_SITE, "TEST", "test.html", "/test");
			
			//	Delete the test page created.
			pageRegistry.deletePage(pid);
		}
		catch (PageException pg) {
			System.out.println("\ttestRegisterPage: FAILED");
			pg.printStackTrace();
		}
		System.out.println("\ttestRegisterPage:  PASS");	
	}
}
