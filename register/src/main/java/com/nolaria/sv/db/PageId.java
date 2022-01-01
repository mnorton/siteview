/**
 * 
 */
package com.nolaria.sv.db;

import java.io.File;

/**
 * 
 * Data for a web page in the Page Id Model.  In this model, references
 * to a page is done using it's identifier, for example:
 * 
 * http://localhost:8080/sv?site=nolaria&id=961d30bb-c47b-4908-9762-d5918d477319
 * 
 * @author markjnorton
 *
 */
public class PageId {
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
	public PageId (String id, String site, String name, String file, String path) {
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
	public String getPath() {
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
			sb.append(File.separator+this.getPath()+File.separator);
		sb.append(this.file);
		
		return sb.toString();
	}
	
	/**
	 * Return the URL for this page using the Page Id model.  Such as:
	 * http://localhost:8080/sv?site=nolaria&id=961d30bb-c47b-4908-9762-d5918d477319
	 * @return URL string
	 */
	public String getUrl() {
		return "http://localhost:8080/sv?site="+this.getSite()+"&id="+this.getId();
	}
	
	/**
	 * Return the direct URL for this page.  This reference points to an HTML file
	 * managed by TomCat, such as:
	 * http://localhost:8080/nolaria/home.html
	 * 
	 * @return URL string
	 */
	private String getDirectUrl() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("http://localhost:8080/");
		sb.append(this.site+"/");
		if (this.path.length() == 0) {
			sb.append(this.getSite());
			sb.append("/");
		}
		else {
			sb.append(this.getPath());
			sb.append("/");
		}
		sb.append(this.getFile());
		
		//System.out.println("Direct URL - Path: "+this.getPath()+" - File: "+this.getFile());
		
		return sb.toString();

	}
	
	/**
	 * Get HTML mark-up to embed this page in an iFrame.
	 * @return iFrame mark-up
	 */
	public String getIFrame() {
		//	This is the mark-up that puts an iFrame into the content pane.
		return "\t<iframe src='"+this.getDirectUrl()+"' title='"+this.getName()+"'></iframe>\n";
	}

	/**
	 * Create a description string for this page and return it.
	 * returns description string.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(name + ": ");
		sb.append("is identified by: "+id+", ");
		sb.append("in site: "+site);
		//sb.append("located in: "+this.getFullFileName());
		sb.append(" with path: "+this.getPath());
		
		return sb.toString();
	}

}
