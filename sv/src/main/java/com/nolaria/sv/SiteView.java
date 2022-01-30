/**
 * 
 */
package com.nolaria.sv;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServlet;

/**
 * Should this be deprecated?
 * 
 * @author Mark Norton
 *
 */
public class SiteView extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public void init() {
		System.out.println("========  SITE VIEW STARTED  ========");
	}
	
	/**
	 * Handle all GET requests for this servlet.
	 * @throws IOException 
	 * 
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String contextPath = req.getContextPath();
		System.out.println("SITE VIEW GET "+contextPath);
		
		PrintWriter pw=resp.getWriter();
		pw.println("<h1>Viewer</h1>");
		pw.close();
		
		//HttpServletResponse resp = new HttpServletResponse();
	}

}
