/**
 * 
 */
package com.nolaria.sv.db;

/**
 * A site is a web site managed by Tomcat consisting of files below a site root folder
 * and a record in the site_regstry table.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class Site {
	public int id;			//	A numerical id for the site.
	public String name;		//	The site name.
	public String path;		//	Not used at this time.
	public String css;		//	Cascading style sheet name.
	
	/**
	 * Constructor given a name and path.
	 * @param name
	 * @param path
	 */
	public Site (String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	/**
	 * Constructor given an id, name and path.
	 * 
	 * @param id - Site identifier number.
	 * @param name - Site name.
	 * @param path - Path to site.
	 * @param css - Style sheet file name.
	 */
	public Site (int id, String name, String path, String css) {
		this.id = id;
		this.name = name;
		this.path = path;
		this.css = css;
	}

	/**
	 * Get the unique site identifier.
	 * @return site id
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Get the site name.
	 * @return name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the site path
	 * @return path
	 */
	public String getPath() {
		return this.path;
	}
	
	/**
	 * Get the CSS file name.
	 * @return css name.
	 */
	public String getCss() {
		return this.css;
	}

	/**
	 * Get the URL for the stylesheet.
	 * 
	 * @return style sheet url
	 */
	public String getCssUrl() {
		return SiteRegistry.LOCAL_HOST + "/"+this.name+"/"+this.css;
	}
	
	/**
	 * Return a description string of the Site object.
	 * return description string.
	 */
	public String toString() {
		return "Site: "+this.id+" named: "+this.name+" with path: "+this.path+" and stylesheet name of: "+this.css;
	}
	
	/**
	 * Return a formatted string to show the values of this Site object.
	 * @return formatted string.
	 */
	public String toStringPretty() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Site Record Values:\n");
		sb.append("\tid:   "+this.id+"\n");
		sb.append("\tname: "+this.name+"\n");
		sb.append("\tpath: "+this.path+"\n");
		sb.append("\tcss:  "+this.css+"\n");
		
		return sb.toString();
	}

}
