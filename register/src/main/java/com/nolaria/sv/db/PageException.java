package com.nolaria.sv.db;

/**
 * A generic exception used by the Site View DB classes.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class PageException extends Exception {

	/**
	 *	Gotta include this or you get a warning.
	 */
	private static final long serialVersionUID = 623895690237549L;

	/**
	 * Create a page exception with a message.
	 * 
	 * @param msg
	 */
	public PageException(String msg) {
		super(msg);
	}

	/**
	 * Create a page exception with a message and a cause.
	 * 
	 * @param msg
	 * @param cause
	 */
	public PageException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
