package com.nolaria.tools;

import com.nolaria.sv.db.PageException;
import com.nolaria.sv.db.Site;
import com.nolaria.sv.db.SiteRegistry;

public class SiteRegistryTest {

	/**
	 * Entry point for testing the Site Registry.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws PageException {
		// TODO Auto-generated method stub

		SiteRegistry siteRegistry = new SiteRegistry();
		
		Site site = siteRegistry.getSiteByName("nolaria");
		System.out.println(site.toStringPretty());
		
		String styleUrl = site.getCssUrl();
		System.out.println("Stylesheet URL: "+styleUrl);
		
	}

}
