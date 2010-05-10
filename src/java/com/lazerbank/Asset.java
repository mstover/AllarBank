package com.lazerbank;

import java.io.IOException;
import java.util.concurrent.Future;


public interface Asset extends Comparable<Asset>{
	
	/**
	 * Returns true if folder exists and has assets within.  
	 * Returns false if folder does not exist or is empty.
	 * @return
	 */
	public Catalog catalog() throws IOException,MagicException;
	
	public String getPath();

}
