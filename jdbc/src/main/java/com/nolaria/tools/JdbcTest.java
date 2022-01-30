package com.nolaria.tools;
import java.sql.*;
import java.util.List;

import com.nolaria.sites.Page;
import com.nolaria.sites.PageRegistry;
import com.nolaria.sites.Site;
import com.nolaria.sites.SiteRegistry;
//import org.mariadb.jdbc.*;

public class JdbcTest {
	  private static final String DB_URL = "jdbc:mysql://localhost/site_view";
	  private static final String CREDS = "?user=root&password=admin";
	  private static final String HOME_ID = "961d30bb-c47b-4908-9762-d5918d477319";
	  private static final String BOOKS_ID = "654b7d10-a431-4cec-9d1d-4262209c9b56";
	  
	  private static SiteRegistry siteRegistery = null;
	  private static PageRegistry pageRegistry = null;

	  public static void main(String[] args) throws Exception {
		  // Open a connection
		  Class.forName("org.mariadb.jdbc.Driver");
		  
		  //  Create the registry objects.
		  Connection conn = DriverManager.getConnection(DB_URL + CREDS);
		  siteRegistery = new SiteRegistry(conn);
		  pageRegistry = new PageRegistry(conn);
		  
		  //  Get the list of registered sites.
		  List<Site> sites = siteRegistery.getSites();
		  
		  //  Print them out.
		  System.out.println("Registered sites:");
		  for (Site s : sites) {
			  System.out.println("\t"+s.getName());
		  }			  

		  //  Get the list of registered pages.
		  List<Page> pages = pageRegistry.getAllPages();
		  System.out.println("\nRegistered pages:");
		  for (Page pg : pages) {
			  System.out.println("\t"+pg.toString());
			  System.out.println("\tFile: "+pg.getFullFileName());
			  System.out.println("\tURL: "+pg.getUrl());
			  System.out.println();
		  }
		  
		  Page page = pageRegistry.getPage(BOOKS_ID);
		  System.out.println("\nGet the books page:");
		  System.out.println("\t"+page.toString());
		  
		  conn.close();
	  }

}
