package com.nolaria.sv;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle all Editor methods.
 * 
 * @author markjnorton@gmail.com
 */
public class Editor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public void init() {
		System.out.println("========  EDITOR STARTED  ========");
	}
	
	/**
	 * Handle all GET requests for this servlet.
	 * 
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		String contextPath = req.getContextPath();
		System.out.println("EDIT GET "+contextPath);
	}

}
