package com.lazerbank;

import strategiclibrary.util.Files;


public class MissingDigitalAsset extends DigitalAsset {


	String primary,path;
	
	public MissingDigitalAsset()
	{
		super("","no_such_image.ppp");
	}
	
	public void setPrimary(String p) {
		primary = p;
	}
	
	public void setPath(String p)
	{
		path = p;
	}

	public String getPath() {
		return path;
	}

	public String getPrimary() {
		return primary;
	}
	
	public String getName()
	{
		if(primary == null) return "";
		return Files.hackOffExtension(primary);
	}

	@Override
	public void delete() {
		super.delete();
		deleteConvertedVersions();
	}

	@Override
	protected void moveAsset(String dirname) {
	}

}
