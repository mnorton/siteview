/**
 * 
 */
package com.nolaria.tools;

import java.util.List;
import java.util.UUID;

import com.nolaria.sv.db.*;

/**
 * This class contains tests of the Page and Site Registries.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class RegistryTest {
	private static int PAGE_CT = 5;
	private static final String BOOKS_ID = "654b7d10-a431-4cec-9d1d-4262209c9b56";
	private SiteRegistry siteRegistery = new SiteRegistry();
	private PageRegistry pageRegistry = new PageRegistry();
	
	private String id = UUID.randomUUID().toString();
	private String site = "test-site";
	private String title = "Test Page";
	private String file = "test-page.html";
	private String path = "test";

	/**
	 *	Run a test of tests on the Site and Page registries.
	 * 
	 * @param args
	 */
	public static void main(String[] args)  {
		RegistryTest test = new RegistryTest();
		
		test.testGetAllSites();
		test.testGetAllPages();
		test.testGetOnePage();
		test.testRegisterPage();
		test.testRegisterPageExists();
		test.testUpdatePage();
		test.testSoftDeletePage();
		test.testHardDeletePage();
	}
	
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
			System.out.println("\nRegistered sites test:");
			for (Site s : sites) {
				System.out.println("\t"+s.getName());
			}
			
			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Get All Sites failed: "+page.getCause().getMessage());
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Get all pages test.
	 * 
	 * @return true if test passes.
	 */
	public boolean testGetAllPages() {
		try {
			//  Get the list of registered pages.
			List<PageId> pages = pageRegistry.getAllPages();
			System.out.println("\nRegistered pages test:");
			
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
			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
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
			System.out.println("\nGet one page test");
			System.out.println("Fetch the books page:");
			System.out.println("\t"+page.toString());			

			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
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
			System.out.println("\nRegister a page test:");
			this.pageRegistry.createPage(id, site, title, file, path);
			System.out.println("\tPage titled "+title+" was registered with an id of "+id);
			
			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
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
			System.out.println("\nRegister a page that exists test:");
			this.pageRegistry.createPage(id, site, title, file, path);
			System.out.println("\tPage titled "+title+" was registered when failure was expected.");
			
			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
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
		String newFile = "new-test-page.html";
		String newPath = "/home";
		try {
			System.out.println("\nUpdate a page test:");
			this.pageRegistry.updatePage(id, site, newTitle, newFile, newPath);
			System.out.println("\tPage was updated, new title is: "+newTitle+", new file is: "+newFile);
			
			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Update Page failed: "+page.getCause().getMessage());
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
			System.out.println("\nSoft delete page test:");
			this.pageRegistry.archive(id);
			System.out.println("\tPage was archived instead of deleted.");

			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
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
			System.out.println("\nHard delete page test:");
			this.pageRegistry.delete(id);
			System.out.println("\tPage was completely deleted");

			System.out.println(RegistryConnector.isValid()?"CONNECTOR VALID":"CONNECTOR INVALID");
		}
		catch (PageException page) {
			System.out.println("Hard Delete Page failed: "+page.getCause().getMessage());
			return false;			
		}

		return true;
	}
}
