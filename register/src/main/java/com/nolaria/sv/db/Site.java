/**
 * 
 */
package com.nolaria.sv.db;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;

/**
 * A site is a web site managed by Tomcat consisting of files below a site root folder
 * and a record in the site_regstry table.
 * 
 * 02/21/2024:  Added site status properties.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class Site {
	public int id = 0;					//	A numerical id for the site.
	public String name = null;			//	The site name.
	public String path = null;			//	Not used at this time.
	public String css = null;			//	Cascading style sheet name.
	public Properties status = new Properties();	//	Status properties of this site.
	
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
	 *  Load the properties from the site status file in the media directory.
	 *  Note:  Exceptions are swallowed, leaving status properties empty.
	 */
	public void loadStatus() {
		//	Check to see if properties are already loaded to avoid clobbering them.
		if (this.status.get("creation") != null)
			return;
		
		//	Otherwise, load the properties from the site status file in the media directory.
		try {
			String statusFileName = SiteRegistry.FILE_ROOT + "/" + this.getName() + "/" + 
					SiteRegistry.MEDIA_DIR_NAME + "/" + SiteRegistry.STATUS_FILE_NAME;
			//System.out.println("Site status file: "+statusFileName);
			Reader propReader = new FileReader(statusFileName);
			this.status.load(propReader);
		}
		catch (FileNotFoundException nf) {
			System.out.println("Site status properties file not found.");
		}
		catch (IOException io) {
			System.out.println("Cannot load site status properties file.");			
		}
	}
	
	/**
	 * Save the site status out to it's site status file, including any changes that may have been made.
	 * Note:  It's a good idea to save out the properties after every change.
	 * Note:  Exceptions are swallowed.
	 */
	public void saveStatus() {
		//	Set the update date/time.
		LocalDate.now();
		this.setStatus("site.updated", LocalDate.now()+" "+LocalTime.now());
		try {
			String statusFileName = SiteRegistry.FILE_ROOT + "/" + this.getName() + "/" + 
					SiteRegistry.MEDIA_DIR_NAME + "/" + SiteRegistry.STATUS_FILE_NAME;
			Writer propWriter = new FileWriter(statusFileName);
			this.status.store(propWriter, "");
		}
		catch (IOException io) {
			System.out.println("Cannot load site status properties file.");			
		}
		
	}
	
	/**
	 * Get the value of the site status property for the given key.
	 * An attempt is made to load the properties if they don't seem to be already loaded.
	 * 
	 * Defined status parameters include:
	 *     site.creation:  converted or new
	 *     site.created:  date/time
	 *     site.updated: date/time
	 *     site.converted.source: source of converted files.
	 *     fix.basic:  yes or no
	 *     fix.image: yes or no
	 *     fix.links: yes or no
	 * 
	 * @param key
	 * @return status property value or null
	 */
	public String getStatus(String key) {
		if (this.status == null)
			return null;
		String value = this.status.getProperty(key);
		if (value == null) {
			this.loadStatus();
			value = this.status.getProperty(key);
		}
		return value;
	}
	
	/**
	 * Set the value of a site status property given a key/value pair.
	 * This assumes the properties were previously loaded.
	 * 
	 * @param key - required
	 * @param value - defaults to empty string if null is passed
	 */
	public void setStatus(String key, String value) {
		if (key == null)
			return;
		if (value == null)
			value = "";
		
		this.status.put(key, value);
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
