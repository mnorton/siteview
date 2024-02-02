package com.nolaria.tools;

import java.util.List;
import java.util.Vector;

import com.nolaria.sv.db.PageId;
import com.nolaria.sv.db.Util;

/**
 * Page Node allows a tree structure to be built that contains PageId objects
 * as values.  This is used by the Scaffolder to build out stubbed files from records
 * in the page_registry table.
 * 
 * Nodes have a name the corresponds to a level in a PageId path.
 * 
 * @author markjnorton@gmail.com
 */
public class PageNode {
	String name;
	private PageId page;
	private List<PageNode>children;
	
	/**
	 * Constructor given a PageId object.
	 * 
	 * @param page
	 */
	public PageNode (String name, PageId page) {
		this.page = page;
		this.children = new Vector<PageNode>();
	}
	
	/**
	 * 
	 * @return node name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * 
	 * @return the page in this node
	 */
	public PageId getPage() {
		return this.page;
	}
	
	/**
	 * 
	 * @return the list of child nodes.
	 */
	public List<PageNode> getChildren() {
		return this.children;
	}
	
	/**
	 * 
	 * @return true if this node has children
	 */
	public boolean hasChildren() {
		if (this.children.size() == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * Add a node to this node's children list.
	 * @param node
	 */
	public void addChild(PageNode node) {
		this.children.add(node);
	}
	
	/**
	 * 
	 * @return Simplifed version of page data.
	 */
	public String toString() {
		String str = this.page.getTitle()+": "+this.page.getPath()+" - "+this.page.getFile();
		return str;
	}
	
	public String toString (int depth) {
		String tabStr = Util.tabber(depth);
		return tabStr + this.page.toString();
	}
	
	
	
}
