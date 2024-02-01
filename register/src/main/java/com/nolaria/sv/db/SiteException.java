package com.nolaria.sv.db;

/**
 * A generic exception used by the Site View DB classes for site registry exceptions.
 * 
 * @author markjnorton@gmail.colm
 *
 */
public class SiteException extends Exception {
	private static final long serialVersionUID = 623812340237540L;
	
	public SiteException (String msg) {
		super(msg);
	}

	public SiteException (String msg, Throwable cause) {
		super(msg, cause);
	}
}
