/**
 * 
 */
package com.nolaria.sv.db;

/**
 * Java bean for a Site.
 * 
 * @author markjnorton
 *
 */
public class Site {
	public String id;
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
	 * Constructor given an id, name and path.
	 * 
	 * @param id - Site identifier number.
	 * @param name - Site name.
	 * @param path - Path to site.
	 */
	Site (String id, String name, String path) {
		this.id = id;
		this.name = name;
		this.path = path;
	}

	/**
	 * Get the unique site identifier.
	 * @return site id
	 */
	public String getId() {
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
	 * Return a description string of the Site object.
	 * return description string.
	 */
	public String toString() {
		return "Site: "+this.id+" named: "+this.name+" with path: "+this.path;
	}

}
