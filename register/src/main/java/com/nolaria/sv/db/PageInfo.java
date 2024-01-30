package com.nolaria.sv.db;

/**
 * This is a simple class to contain the three key pieces of page information:
 * 
 * 	-	title
 * 	-	file
 * 	-	id (page identifier)
 * 
 * Since this is really only used as a data structure, setters and getters are left out.
 * Jan. 27, 2024:  Refactored by changing name to file and pid to id.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class PageInfo {
	public String title = null;		//	The page title.
	public String file = null;		//	The page file name.
	public String id = null;		//	The page identifier (UUID).

	/**
	 * Constructor given title, file, and id.
	 * @param title
	 * @param file (used to be name)
	 * @param id (used to be pid)
	 */
	public PageInfo(String title, String file, String id) {
		this.title = title;
		this.file = file;
		this.id = id;
	}
	
	/**
	 * Return a string version of the PageInfo object.  Used for debugging.
	 * @return page info string
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Page Information:\n");
		sb.append("\tTitle: "+this.title+"\n");
		sb.append("\tFile: "+this.file+"\n");
		sb.append("\tID: "+this.id+"\n");
		
		return sb.toString();
	}

}
