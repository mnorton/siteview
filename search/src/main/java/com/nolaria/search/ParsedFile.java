/**
 * 
 */
package com.nolaria.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This simple class holds information from a parsed HTML web page.
 * This follows the conventions of a Java Pea, so it doesn't have getters and setters.
 * 
 * @author markjnorton@gmail.com
 */
public class ParsedFile {
	public String pid = null;
	public String title = null;
	public Map<String,IndexableString> strings = new HashMap<String,IndexableString>();
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("PID: "+this.pid+"\n");
		sb.append("Title: "+this.title+"\n");
		sb.append("\n");
		
		Set<String> keys = this.strings.keySet();
		for (String key : keys) {
			IndexableString str = this.strings.get(key);
			int level = str.getLevel();
			sb.append(level+"| "+str.getTag()+": "+str.getStr()+"\n");
		}
		sb.append("\nIndexable strings found: "+this.strings.size());
		
		return sb.toString();
	}
}
