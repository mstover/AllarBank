package com.lazerbank;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ConversionTimeoutException extends BadConversionException {
	private static final long serialVersionUID = 1;
	
	private boolean isFatal = false;

	public void setFatal(boolean isFatal) {
		this.isFatal = isFatal;
	}

	public ConversionTimeoutException() {
		super();
	}

	public ConversionTimeoutException(String message, File image, InputStream stdout, InputStream stderr) {
		super(message, image, stdout, stderr);
		
	}
	
	protected boolean isFatal()
	{
		return isFatal;
	}

}
