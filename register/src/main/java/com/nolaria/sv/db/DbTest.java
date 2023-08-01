/**
 * 
 */
package com.nolaria.sv.db;

import java.util.Collections;
import java.util.List;

/**
 * @author markj
 *
 */
public class DbTest {
	public static final String TEST_PAGE_ID = "7cbd34d1-72e3-43b9-b35d-4129ca489547";

	/**
	 * A test suite for the Nolaria DB classes.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DbTest dbTest = new DbTest();
			
			//dbTest.testFindKeywords();
			//dbTest.testIndexing();
			
			/*
			dbTest.testSearch("susan");
			dbTest.testSearch("du-%");
			dbTest.testSearch("% %");
			dbTest.testSearch("libary");
			dbTest.testSearch("library");
			dbTest.testSearch("libraries");
			dbTest.testSearch("~%");
			dbTest.testSearch("poison");
			dbTest.testSearch("?modern");
			dbTest.testSearch("jack scratch");
			*/
			
			dbTest.testResultsFormat("libraries");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * A test to find keywords in a test page.
	 * @throws PageException
	 */
	public void testFindKeywords() throws PageException {
		PageRegistry pr = new PageRegistry();
		PageId page = pr.getPage(DbTest.TEST_PAGE_ID);
		System.out.println("Got the page: "+page.getTitle());
		List<String> keywords = page.findKeywords();
		
		System.out.println("Keywords found:");
		for (String key : keywords)
			System.out.println("\t"+key);
		
	}
	
	/**
	 * A test to index the keywords found on a page.
	 * @throws PageException
	 */
	public void testIndexing() throws PageException {
		PageRegistry pr = new PageRegistry();
		PageIndex pi = new PageIndex();
		
		PageId page = pr.getPage(DbTest.TEST_PAGE_ID);
		pi.index(page);
		System.out.println("Page titled: "+page.title+" was indexed");
	}
	
	/**
	 * A test of the search function.
	 * @param parameters
	 */
	public void testSearch(String parameters) throws PageException {
		PageIndex pi = new PageIndex();
		
		List<PageId> pages = pi.search(parameters);
		System.out.println("Search on ["+parameters+"] returned "+pages.size()+" records");
	}
	
	/**
	 * Test of simple formatted output for parameters.
	 * @param parameters
	 * @throws PageException
	 */
	public void testResultsFormat(String parameters)  throws PageException {
		PageIndex pi = new PageIndex();
		
		List<PageId> pages = pi.search(parameters);
		System.out.println("Search on ["+parameters+"] returned "+pages.size()+" records");
		
		Collections.sort(pages);
		
		int limit = 20;
		int i = 0;
		for (PageId page : pages) {
			String expandoPath = page.path.replaceAll("/", " > ");
			System.out.println("\t"+expandoPath+" == "+page.title);
			i++;
			if (i>limit) break;
		}
		
	}

}
