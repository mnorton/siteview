/**
 * 
 */
package com.nolaria.sv.db;

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
			dbTest.testIndexing();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void testFindKeywords() throws PageException {
		PageRegistry pr = new PageRegistry();
		PageId page = pr.getPage(DbTest.TEST_PAGE_ID);
		System.out.println("Got the page: "+page.getTitle());
		List<String> keywords = page.findKeywords();
		
		System.out.println("Keywords found:");
		for (String key : keywords)
			System.out.println("\t"+key);
		
	}
	
	public void testIndexing() throws PageException {
		PageRegistry pr = new PageRegistry();
		PageIndex pi = new PageIndex();
		
		PageId page = pr.getPage(DbTest.TEST_PAGE_ID);
		pi.index(page);
		
	}

}
