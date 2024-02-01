/**
 * 
 */
package com.nolaria.tools;

import java.util.List;
import java.util.UUID;

import com.nolaria.sv.db.*;

/**
 * This class contains tests of Util, SiteRegistry, PageRegistry.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class PageRegistryTest {
	private static int PAGE_CT = 5;
	private static final String BOOKS_ID = "654b7d10-a431-4cec-9d1d-4262209c9b56";
	private static final String YONTICA_ID = "f62b9661-d11e-4bed-b674-d04643bd7cdb";
	private SiteRegistry siteRegistery = new SiteRegistry();
	private PageRegistry pageRegistry = new PageRegistry();
	
	private String id = UUID.randomUUID().toString();
	private String site = "test";
	private String title = "Test Page";
	private String file = "test-page.html";
	private String path = "home";
	
	private String fullTestPageName = "D:/apache-tomcat-9.0.40/webapps/"+this.site+"/"+this.path+"/"+this.file;

	/**
	 *	Run a set of tests on the Util class, Site Registry and  Page Registry.
	 * 
	 * @param args
	 */
	public static void main(String[] args)  {
		PageRegistryTest test = new PageRegistryTest();
		
		Boolean doUtilTests = false;
		Boolean doSiteTests = false;
		Boolean doRegistryTests = false;
		
		if (doUtilTests) {
			System.out.println("\n-- UTIL TESTS --");
			test.testUtilExists();
			test.testUtilLoadFile();
			test.testUtilUpdateInfo();
		}
		if (doSiteTests) {
			System.out.println("\n-- SITE TESTS --");
			test.testGetAllSites();			
		}
		if (doRegistryTests) {
			System.out.println("\n-- PAGE TESTS --");
			test.testGetAllPages();
			test.testGetOnePage();
			test.testRegisterPage();
			test.testRegisterPageExists();
			test.testUpdatePage();
			test.testSoftDeletePage();
			test.testHardDeletePage();
		}
		
		test.testGetBodyContent();
	}
	
	/***********************************************************
	 *    Util Tests                                           *
	 **********************************************************/
	
	/**
	 * Test that a file exists.
	 * 
	 * @return true if test passes.
	 */		
	public boolean testUtilExists() {
		String bogusFileName = "/d/not-a-file.html";
		
		System.out.println ("\n1. File exists test:");
		
		//	Check for a file that doesn't exist.
		if (Util.fileExists(bogusFileName))
			System.out.println("\tBogus file should not exist, but does: "+bogusFileName);
		else
			System.out.println("\tBogus file should not exist, and does not: "+bogusFileName);
		
		//	Check for a file that should exist.
		if (Util.fileExists(this.fullTestPageName))
			System.out.println("\tTest file exists: "+this.fullTestPageName);
		else
			System.out.println("\tTest file should exist but does not: "+this.fullTestPageName);
		
		return true;
	}
	
	/**
	 * Test that a file can be loaded.
	 * 
	 * @return true if one was loaded.
	 */
	public boolean testUtilLoadFile() {
		System.out.println ("\n2. Load file:");
		
		//	Check to see if our test file exists. If not, text cannot be done.
		if (!Util.fileExists(this.fullTestPageName)) {
			System.out.println("\tTest file should exist but does not: "+this.fullTestPageName);
			return false;
		}
		
		//	Load the test file.
		String content = Util.loadFile(this.fullTestPageName);
		if (content != null)
			System.out.println("\tTest file loaded and has a size of: "+content.length());
		else
			System.out.println("\tTest file failed to load (returned null)");
		
		return true;
	}
	
	/**
	 * Test that the metadata in a file can be updated with new values.
	 * 
	 * @return true if update can be done.
	 */
	public boolean testUtilUpdateInfo() {
		System.out.println ("\n3. Update content metadata:");

		String newCss = "http://localhost:8080/test/red.css";
		String newTitle = "Longer Title Than Before";
		String fakeId = "uuid.fake";
		
		//	Check to see if our test file exists. If not, text cannot be done.
		if (!Util.fileExists(this.fullTestPageName)) {
			System.out.println("\tTest file should exist but does not: "+this.fullTestPageName);
			return false;
		}
		
		//	Load the test file.
		String content = Util.loadFile(this.fullTestPageName);
		if (content == null)
			System.out.println("\tTest file failed to load (returned null)");
		
		//	Update the metadata, etc.
		PageInfo info = new PageInfo(newTitle, this.file, fakeId);
		String updatedContent = Util.updateHeaderInfo(content, newCss, info);
		
		if (updatedContent == null) {
			System.out.println("\tContent was not updated (returned null)");
			return false;
		}
		else {
			System.out.println("\tContent was updated, new size is: "+updatedContent.length());
		}
		
		System.out.println("Updated content:\n"+updatedContent);
		return true;
	}

	
	/***********************************************************
	 *    Site Tests                                           *
	 **********************************************************/
	
	/**
	 * Get all site test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testGetAllSites() {
		try {
			//  Get the list of registered sites.
			List<Site> sites = siteRegistery.getSites();
		  
			//  Print them out.
			System.out.println("\n1. Registered sites test:");
			for (Site s : sites) {
				System.out.println("\t"+s.getName());
			}
			
			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (SiteException site) {
			System.out.println("Get All Sites failed: "+site.getCause().getMessage());
			return false;
		}
		
		return true;
	}
	
	
	/***********************************************************
	 *    Site Tests                                           *
	 **********************************************************/
	
	/**
	 * Get all pages test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testGetAllPages() {
		try {
			//  Get the list of registered pages.
			List<PageId> pages = pageRegistry.getAllPages();
			System.out.println("\n1. Registered pages test:");
			
			/*	This prints all pages, which can run to thousands.
			for (PageId pg : pages) {
				System.out.println("\t"+pg.toString());
				System.out.println("\tFile: "+pg.getFullFileName());
				System.out.println("\tURL: "+pg.getUrl());
				System.out.println();
			}
			*/
		  
			//	Show the first COUNT pages found.
			for (int i=0; i<PAGE_CT; i++) {
				PageId pg = pages.get(i);
				System.out.println("\t"+pg.toString());
				System.out.println("\tFile: "+pg.getFullFileName());
				System.out.println("\tURL: "+pg.getUrl());
				System.out.println();
			}			
			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Get All Pages failed: "+page.getCause().getMessage());
			return false;			
		}
		
		return true;
	}
	
	
	/**
	 * Get one page test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testGetOnePage() {
		try {
			//	Get a single page and show it.
			PageId page = pageRegistry.getPage(BOOKS_ID);
			System.out.println("\n2. Get one page test");
			System.out.println("Fetch the books page:");
			System.out.println("\t"+page.toString());			

			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Get One Page failed: "+page.getCause().getMessage());
			return false;			
		}

		return true;
	}
	
	
	/**
	 * Register a new page test.
	 * 
	 * @return true if the page was registered.
	 */
	public boolean testRegisterPage() {
		
		try {
			System.out.println("\n3. Register a page test:");
			this.pageRegistry.createPage(id, site, title, file, path);
			PageId page = this.pageRegistry.getPage(id);
			System.out.println(page.toStringPretty());
			System.out.println("\tPage titled "+title+" was registered with an id of "+id);
			
			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Register Page failed: "+page.getCause().getMessage());
			return false;			
		}

		return true;
	}

	/**
	 * Register a page that exists test.
	 * 
	 * @return true if the page already exists.
	 */
	public boolean testRegisterPageExists() {
		
		try {
			System.out.println("\n4. Register a page that exists test:");
			this.pageRegistry.createPage(id, site, title, file, path);
			System.out.println("\tPage titled "+title+" was registered when failure was expected.");
			
			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
			return false;
		}
		catch (PageException page) {
			System.out.println("\tRegister Page correctly failed: "+page.getCause().getMessage());
		}

		return true;
	}

	
	/**
	 * Update page test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testUpdatePage() {
		String newTitle ="New Test Title";
		//String newFile = "new-test-page.html";
		String newPath = "home";
		try {
			System.out.println("\n5. Update a page test:");
			System.out.println("\tUpdating page id: "+this.id);
			
			//  String id, String site, String title, String file, String path
			//this.pageRegistry.updatePage(id, site, newTitle, newFile, newPath);

			this.pageRegistry.updatePage(id, site, newTitle, this.file, newPath);
			System.out.println("\tPage was updated, new title is: "+newTitle);
			
			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			//System.out.println("Update Page failed: "+page.getCause().getMessage());
			System.out.println("Test Update Page failed due to an exception.");
			page.printStackTrace();
			return false;
		}

		return true;
	}
	
	
	/**
	 * Soft delete page test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testSoftDeletePage() {
		try {
			System.out.println("\n6. Soft delete page test:");
			this.pageRegistry.archive(id);
			System.out.println("\tPage was archived (soft delete).");

			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Soft Delete Page failed: "+page.getCause().getMessage());
			return false;			
		}

		return true;
	}

	
	/**
	 * Hard delete page test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testHardDeletePage() {
		try {
			System.out.println("\n7. Hard delete page test:");
			this.pageRegistry.delete(id);
			System.out.println("\tPage was completely deleted");

			//System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Hard Delete Page failed: "+page.getCause().getMessage());
			return false;			
		}

		return true;
	}
	
	/**
	 * Get the body content for this page.
	 * 
	 * @return true if able to get the content.
	 */
	public boolean testGetBodyContent() {
		try {
			//	Get all of the content
			PageId yonticaPage = this.pageRegistry.getPage(YONTICA_ID);
			if (yonticaPage == null) {
				System.out.println ("Unable to get page: "+YONTICA_ID);
				return false;
			}
			
			//	Extract just the body content
			String bodyContent = yonticaPage.getContentBody();
			if (bodyContent == null) {
				System.out.println ("Unable to body content for : "+yonticaPage.getTitle());
				return false;
			}
			
			//	Success!  Show the content.
			System.out.println(yonticaPage.toStringPretty());
			System.out.println(bodyContent);
		}
		catch (PageException pg) {
			pg.printStackTrace();
			return false;
		}
		return true;
	}
			
}
