package com.rbb.similarproductsreactive.exceptions;

public class ServiceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ServiceException(final String errorMessage, final Throwable cause) {
		super(errorMessage, cause);
	}
	
}
