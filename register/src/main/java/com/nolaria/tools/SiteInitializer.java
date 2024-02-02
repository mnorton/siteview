package com.nolaria.tools;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.nolaria.sv.db.*;


/**
 * A small application that initializes a site and registers it.
 * 
 * @author markjnorton@gmail.com
 */
public class SiteInitializer {
	public static String CSS_DEFAULT = "green.css";
	public SiteRegistry siteRegistery = new SiteRegistry();
	public PageRegistry pageRegistry = new PageRegistry();

	/**
	 * Main entry point for the Site Initializer tool.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Site Initializer Tool");
		System.out.println("---------------------\n");
		
		try {
			SiteInitializer app = new SiteInitializer();
			String css = "blue.css";	//	Change this if a different one is desired.
			app.createSite("aleum", css);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Register the site name passed and create all objects within it.
	 * 
	 * @param siteName
	 * @throws PageException 
	 */
	public void createSite(String siteName, String css) throws SiteException {
		String path = "webapps/"+siteName;
		
		Site site = null;
		//	Check to see if this site is already registered.
		site = this.siteRegistery.getSiteByName(siteName);
		if (site != null) {
			System.out.println("The site "+siteName+" is already registered.");
			return;
		}
		
		//	Register the new site and initialize files, including the home page.
		site = this.siteRegistery.createSite(siteName, path, css, true);
		if (site == null) {
			System.out.println("Unable to register the site.");
			return;
		}
		else
			System.out.println("\nSite was registered: \n"+site.toStringPretty()+"\n");
		
		//	Create a fake site object.
		//site = new Site(1000,siteName,path,css);
		//this.setUpFiles(site, css);	//	Handled by SiteRegister.createSite() now.
		
		List<Site> sites = this.siteRegistery.getSites();
		System.out.println("\nExisting sites:");
		for (Site s : sites) {
			System.out.println("\t"+s.toString());
		}			
	}
	
	/**
	 * Create the site root folder and required folders/files in it.
	 * 
	 * @param siteName
	 */
	private void setUpFiles (Site site, String css) {
		//	The file root is where all websites and webapps live on Tomcat.
		String fileRootName = this.siteRegistery.getFileRoot();
		System.out.println("File root name: "+fileRootName);
		
		//	The site root is the folder where the managed site lives.
		String siteRootName = fileRootName + "/" +site.getName();
		System.out.println("Site root name: "+siteRootName);
		
		//	Create the root folder, if it doesn't exist.
		File rootFolder = new File(siteRootName);
		if (rootFolder.exists())
			System.out.println("Site root folder already exists.  Skipping this step.");
		else {
			if (rootFolder.mkdir())
				System.out.println("Site root folder created.");
			else {
				System.out.println("Site root folder was not created.");
				return;
			}
		}
		
		//	The site folder is where the content lives.
		String siteFolderName = siteRootName + "/" +site.getName();
		System.out.println("Site folder name: "+siteFolderName);

		//	Create the content folder if it doesn't already exist.
		File siteFolder = new File(siteFolderName);
		if (siteFolder.exists())
			System.out.println("Site content folder already exists.  Skipping this step.");
		else {
			if (siteFolder.mkdir())
				System.out.println("Site content folder created.");
			else {
				System.out.println("Site content folder was not created.");
				return;
			}
		}

		// The media folder is where pictures (etc) are kept for the new site.
		String mediaFolderName = siteRootName + "/media";
		System.out.println("Media folder name: "+mediaFolderName);
		
		//	Create the media folder if it doesn't exist.
		File mediaFolder = new File(mediaFolderName);
		if (mediaFolder.exists())
			System.out.println("Media  folder already exists.  Skipping this step.");
		else {
			if (mediaFolder.mkdir())
				System.out.println("Media folder created.");
			else {
				System.out.println("Media folder was not created.");
				return;
			}
		}
		
		//	The home page is in the site root folder, next to the site content folder
		String homePageName = siteRootName +"/"+site.getName()+".html";
		System.out.println("Home page name: "+homePageName);
		
		//	Create the home page and save it.
		File homePage = new File(homePageName);
		if (!homePage.exists()) {
			StringBuffer sb = new StringBuffer();
			sb.append("<!DOCTYPE html>\n");
			sb.append("<html lang=\"en-us\">\n");
			sb.append("<head>\n");
			sb.append("</head>\n");
			sb.append("<body>\n");
			sb.append("</body>\n");
			sb.append("</html>\n");

			String content = sb.toString();
			//System.out.println("\nStubbed content: \n"+content+"\n");
			
			String pgTitle = Util.capitalize(site.getName());
			String pgFile = site.getName()+".html";
			String pgId = UUID.randomUUID().toString();
			PageInfo pi = new PageInfo(pgTitle, pgFile, pgId);
			content = Util.updateHeaderInfo(content, site.getCss(), pi);
			//System.out.println("\nUpdated content: \n"+content+"\n");
			
			//	Write the new page out.
			Util.saveFile(content, homePageName);
				
			//	Register the page.
			try {
				this.pageRegistry.createPage(pgId, site.getName(), pgTitle, pgFile, pgId);
			}
			catch (PageException pg) {
				System.out.println("Unable to register the site page:  "+pg.getCause());
			}
		}
		else
			System.out.println("Site page already exists.");

		//	Copy the CSS file from the shared repo.
		String cssSrcName = SiteRegistry.FILE_REPO+"/"+css;
		String cssDestName = siteRootName+"/"+css;
		System.out.println("CSS source file name: "+cssSrcName);
		System.out.println("CSS dest file name: "+cssDestName);

		String cssContent = Util.loadFile(cssSrcName);
		if (cssContent != null) {
			Util.saveFile(cssContent, cssDestName);
			System.out.println("The "+css+" file was copied to the new site.");
		}
		else {
			System.out.println("Unable to copy CSS file.");
			return;
		}
	}
}
