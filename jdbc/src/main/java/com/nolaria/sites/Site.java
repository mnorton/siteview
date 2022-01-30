/**
 * 
 */
package com.nolaria.sites;

/**
 * Java bean for a Site.
 * 
 * @author markjnorton
 *
 */
public class Site {
	public String name;
	public String path;
	
	/**
	 * Constructor given a name and path.
	 * @param name
	 * @param path
	 */
	Site (String name, String path) {
		this.name = name;
		this.path = path;
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

}
