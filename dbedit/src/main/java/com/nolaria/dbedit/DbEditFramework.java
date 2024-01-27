package com.nolaria.dbedit;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.nolaria.sv.db.PageException;
import com.nolaria.sv.db.PageId;
import com.nolaria.sv.db.PageRegistry;
import com.nolaria.sv.db.SiteRegistry;

public class DbEditFramework {

	//	Database APIs.
	private static SiteRegistry siteRegistry = new SiteRegistry();;
	private static PageRegistry pageRegistry = new PageRegistry();

	public String error = null;
	public HttpServletRequest request = null;

	public DbEditFramework (HttpServletRequest req) {
		this.request = req;
		
	}
	
	/**
	 * Return the HTML of the main body.
	 * 
	 * @return HTML string.
	 */
	public String getBody() {
		String op = request.getParameter("op");
		
		//	Show page content depending on the op parameter.
		if (op == null) {
			return this.getIdentifierForm();
		}
		else if (op.compareTo("show") == 0) {
			return this.getShowForm();
		}
		else if (op.compareTo("update") == 0) {
			this.doUpdate();
			return this.getIdentifierForm();			
		}
		else {
			return "<b></b>Unknown operation, op: "+op+"</br/>";
		}
	}
	
	/**
	 * Return the HTML for the show form.
	 * 
	 * @return HTML
	 */
	public String getIdentifierForm() {
		StringBuffer sb = new StringBuffer();
		
		//	Display error message if present.
		if (this.error != null)
			sb.append(this.error);
		
		sb.append("");
		sb.append("<form id=\"show-form\" method=\"get\" action=\"/dbedit\">\n");		
		sb.append("\t<input type=\"hidden\" name=\"op\" value=\"show\">\n");

		sb.append("\t<table>\n");
		sb.append("\t\t<tr>\n");

		sb.append("\t\t\t<td><label for=\"id-field\"><b>Identifier:&nbsp;</b></label></td>\n");
		sb.append("\t\t\t<td><input type=\"text\" size=\"34\" id=\"id-field\" name=\"page-id\" /></td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t\t<tr>\n");

		sb.append("\t\t\t<td>&nbsp;</td>\n");
		sb.append("\t\t\t<td><button type=\"submit\" form=\"show-form\"><b>Get Values</b></button> \n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t</table>\n");

		sb.append("\t</form>\n");
		
		return sb.toString();
	}
	
	/**
	 * Return the HTML for the update form.
	 * 
	 * @return HTML
	 */
	public String getShowForm() {
		StringBuffer sb = new StringBuffer();
		
		//	Variables used to display field values.
		PageId page = null;
		String id = this.request.getParameter("page-id");
		String title = "";
		String path = "";
		String file = "";
		String archived = "";

		//	If no id is found, display an empty form.
		if (id == null) {
			this.error = "<br>Page identifier not found.<b><br/>";
			id = "";
			
			System.out.println("Page identifier not found.");
		}
		//	If we have one, get the page and extract field values.
		else {
			try {
				page = DbEditFramework.pageRegistry.getPage(id);
				title = page.getTitle();
				path = page.getPath();
				file = page.getFile();
				if (page.getArchive())
					archived = "T";
				else
					archived =  "F";
				
				System.out.println(page.toString());
			}
			catch (PageException pg) {
				this.error = "<br>Page record not found.<b><br/>";
				pg.printStackTrace();
			}
		}
		
		//	Display error message if present.
		if (this.error != null)
			sb.append(this.error);
		
		sb.append("<form id=\"update-form\" method=\"get\" action=\"/dbedit\">\n");
		sb.append("\t<input type=\"hidden\" name=\"op\" value=\"update\">\n");
		sb.append("\t<table>\n");
		sb.append("\t\t<tr>\n");
		
		sb.append("\t\t\t<td><label for=\"id-field\"><b>Identifier:&nbsp;</b></label></td>\n");
		sb.append("\t\t\t<td><input type=\"text\" size=\"34\" id=\"id-field\" name=\"page-id\" value=\""+id+"\" /></td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t\t<tr>\n");
		
		sb.append("\t\t\t<td><label for=\"title-field\"><b>Title:&nbsp;</b></label></td>\n");
		sb.append("\t\t\t<td><input type=\"text\" size=\"34\" id=\"title-field\" name=\"page-title\" value=\""+title+"\" /></td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t\t<tr>\n");
		
		sb.append("\t\t\t<td><label for=\"path-field\"><b>Path:&nbsp;</b></label></td>\n");
		sb.append("\t\t\t<td><input type=\"text\" size=\"34\" id=\"path-field\" name=\"page-path\" value=\""+path+"\" /></td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t\t<tr>\n");
		
		sb.append("\t\t\t<td><label for=\"file-field\"><b>File:&nbsp;</b></label></td>\n");
		sb.append("\t\t\t<td><input type=\"text\" size=\"34\" id=\"file-field\" name=\"page-file\" value=\""+file+"\" /></td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t\t<tr>\n");
		
		sb.append("\t\t\t<td><label for=\"archive-field\"><b>Archived:&nbsp;</b></label></td>\n");
		sb.append("\t\t\t<td><input type=\"text\" size=\"1\" id=\"archive-field\" name=\"page-archive\" value=\""+archived+"\" /></td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t\t<tr>\n");
		
		sb.append("\t\t\t<td>&nbsp;</td>\n");
		sb.append("\t\t\t<td><button type=\"submit\" form=\"update-form\"><b>Update</b></button</td>\n");
		
		sb.append("\t\t</tr>\n");
		sb.append("\t</table>\n");
		sb.append("\t</form>\n");
		
		return sb.toString();
	}
	
	/**
	 * Handle the update here.
	 */
	public void doUpdate() {
		
		String id = request.getParameter("page-id");
		String site = "nolaria";
		String title = request.getParameter("page-title");
		String path = request.getParameter("page-path");
		String file = request.getParameter("page-file");
		String archived = request.getParameter("page-archive");
		Boolean archivedFlag = false;
		if (archived.compareTo("T")==0)
			archivedFlag = true;
		
		// String id, String site, String name, String file, String path, boolean archived
		PageId existingPage = null;
		try {
			existingPage = DbEditFramework.pageRegistry.getPage(id);
		}
		catch (PageException pg) {
			System.out.println("Cannot find the existing page: "+id);
			this.error = "Cannot find the existing page: "+id;
			return;		//	No update is performed.
		}
		System.out.println("Existing Page\n"+existingPage.toStringPretty());
		PageId updatedPage = new PageId(id, existingPage.getSite(), title, file, existingPage.getPath(), archivedFlag);
		System.out.println("Update Page\n"+updatedPage.toStringPretty());
		
		//	Check that new file name exists.
		String fullFileName = updatedPage.getFullFileName();
		File f = new File(fullFileName);
		if (!f.exists()) {
			//	Restore existing file name.  This allows other updates to occur.
			//updatedPage.file = existingPage.getFile();
			System.out.println("Doesn't exist: "+fullFileName);
			this.error = "<b>New file name does not exist</b><br/>";
			return;
		}
		else
			System.out.println("Exists: "+fullFileName);
			
		
		//	Make the update.
		try {
			//	This will only update the identified record with a new title or file.  Other values are forced to be the same.
			DbEditFramework.pageRegistry.updatePage(id, existingPage.getSite(), title, file, existingPage.getPath());
			System.out.println("Page ["+id+"] was updated to title: "+title+" and file: "+file+".");
		}
		catch (PageException pg) {
			System.out.println("Unable to update page: "+id);
			this.error = "Unable to update page: "+id;	
			pg.printStackTrace();
		}
	}
}
