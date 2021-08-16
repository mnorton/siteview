package com.nolaria.siteview.fix;

import java.lang.Exception;

/**
 * Used whenever there is a problem in fixing files.
 * 
 * @author markjnorton@gmail.com
 *
 */
public class FixException extends Exception {
	private static final long serialVersionUID = 6407241884286331135L;

	public FixException() {
		// TODO Auto-generated constructor stub
	}

	public FixException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public FixException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public FixException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public FixException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
