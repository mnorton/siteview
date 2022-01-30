/**
 * 
 */
package com.nolaria.sites;

import java.io.File;

/**
 * This is the Page java bean.
 * It provides a class for a web page in the Page Id Model.  In this model, references
 * to a page is done using it's identifier, for example:
 * 
 * http://localhost:8080/sv?ref=home/books.html
 * 
 * @author markjnorton
 *
 */
public class Page {
	public String id;
	public String site;
	public String name;
	public String file;
	public String path;
	
	/**
	 * Constructor given all values.
	 * 
	 * @param id
	 * @param site
	 * @param name
	 * @param file
	 * @param path
	 */
	public Page (String id, String site, String name, String file, String path) {
		this.id = id;		//	A UUID for the page.
		this.site = site;	//	Web site associated with this page.
		this.name = name;	//	Web page title.
		this.file = file;	//	File name.
		this.path = path;	//	Path to file name relative to Tomcat webapps dir.
	}

	/**
	 * Get the unique identifier of this page.
	 * @return page identifier string
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Get the site name of this page.
	 * @return site name string
	 */
	public String getSite() {
		return this.site;
	}

	/**
	 * Get the name (title) of this page.
	 * @return name string
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the file name of this page.
	 * @return file name string
	 */
	public String getFile() {
		return this.file;
	}

	/**
	 * returns the directory this file is contained in.
	 * @return directory path string
	 */
	public String getDir() {
		return this.path;
	}
	
	/**
	 * Returns the file name of the page.
	 * @return file name string
	 */
	public String getFullFileName() {
		StringBuffer sb = new StringBuffer();
		
		String tomcat = System.getenv("CATALINA_HOME");
		sb.append(tomcat+File.separator);
		sb.append("webapps"+File.separator);
		sb.append(this.site);
		if (this.path.compareTo("") == 0)
			sb.append(File.separator);
		else
			sb.append(File.separator+this.path+File.separator);
		sb.append(this.file);
		
		return sb.toString();
	}
	
	public String getUrl() {
		// http://localhost:8080/sv?ref=home.html
		return "http://localhost:8080/sv/"+this.id;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(name + ": ");
		sb.append("is identified by: "+id+", ");
		sb.append("in site: "+site);
		//sb.append("located in: "+this.getFullFileName());
		
		return sb.toString();
	}

}
