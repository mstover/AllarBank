package com.lazerbank;

import java.io.File;
import java.io.InputStream;

public class NoSuchImageException extends MagicException {
	private static final long serialVersionUID = 1;

	public NoSuchImageException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NoSuchImageException(String message, File image, InputStream stdout, InputStream stderr) {
		super(message, image, stdout, stderr);
		// TODO Auto-generated constructor stub
	}

}
