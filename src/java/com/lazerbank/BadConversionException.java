package com.lazerbank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BadConversionException extends MagicException {
	private static final long serialVersionUID = 1;

	public BadConversionException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BadConversionException(String message, File image, InputStream stdout, InputStream stderr) {
		super(message, image, stdout, stderr);
		// TODO Auto-generated constructor stub
	}

	

}
