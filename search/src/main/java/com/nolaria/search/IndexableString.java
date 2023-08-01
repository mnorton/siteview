/**
 * 
 */
package com.nolaria.search;

/**
 * This class is a wrapper for a string which is a candidate for Lucene indexing.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class IndexableString {
	public int level = 0;
	public String tag = null;
	public String str = null;
	
	/**
	 * Create a new IndexableString given level, tag, and str.
	 * 
	 * @param level
	 * @param tag
	 * @param str
	 * @throws Exception
	 */
	public IndexableString(int level, String tag, String str) throws Exception {
		if (tag == null)
			throw new Exception("Tag cannot be null.");
		if (str == null)
			throw new Exception("Indexable string cannot be null.");
		
		this.level = level;
		this.tag = tag;
		this.str = str;
	}
	
	/**
	 * Return the element level.
	 * @return level
	 */
	public int getLevel() {
		return this.level;
	}
	
	/**
	 * Return the element tag.
	 * @return tag
	 */
	public String getTag() {
		return this.tag;
	}
	
	/**
	 * Return the string part.
	 * @return str
	 */
	public String getStr() {
		return this.str;
	}
	
	/**
	 * Set the string part of an indexable string.
	 */
	public void setStr(String s) {
		this.str = s;
	}
	
	/**
	 * Returning a pretty form of the indexable string.
	 * @return pretty string
	 */
	public String toString() {
		return this.tag + ","+this.level + ": "+this.str;
	}
}
