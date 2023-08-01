package com.nolaria.sv.db;

/**
 * This is a simple class to contain the three key pieces of page information:
 * 
 * 	-	Title
 * 	-	Name
 * 	-	PID (page identifier)
 * 
 * Since this is really only used as a data structure, setters and getters are left out.
 * 
 * @author markj
 *
 */
public class PageInfo {
	public String title = null;		//	The page title.
	public String name = null;		//	The page name.
	public String pid = null;		//	The page identifier (UUID).

	/**
	 * Constructor given title, name, and pid.
	 * @param title
	 * @param name
	 * @param pid
	 */
	public PageInfo(String title, String name, String pid) {
		this.title = title;
		this.name = name;
		this.pid = pid;
	}
	
	/**
	 * Return a string version of the PageInfo object.  Used for debugging.
	 * @return page info string
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Page Information:\n");
		sb.append("\tTitle: "+this.title+"\n");
		sb.append("\tName: "+this.name+"\n");
		sb.append("\tPID: "+this.pid+"\n");
		
		return sb.toString();
	}

}
