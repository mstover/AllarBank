package com.lazerbank.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import strategiclibrary.util.Files;

import com.lazerinc.ecommerce.DatabaseUtilities;
import com.lazerinc.ecommerce.ProductFamily;

@CoinjemaObject
public class MoveProperties extends UploadPackage {
	Properties properties;
	File origFile;
	
	DatabaseUtilities dbutil;

	public MoveProperties(File propertyFile) throws IOException {
		origFile = propertyFile;
		properties = new Properties();
		properties.load(new FileInputStream(propertyFile));
	}

	@Override
	public boolean copyFilesTo(File toDir) throws IOException {
		String path = properties.getProperty("PATHNAME");
		return moveFiles(toDir,path);
	}
	
	private boolean moveFiles(File toDir,String path)
	{
		String absoluteDir = toDir.getAbsoluteFile().getAbsolutePath();
		System.out.println("todir = " + absoluteDir);
		String familyDirName = path.substring(0,path.indexOf("/"));
		System.out.println("Family dir = " + familyDirName);
		String startDir = absoluteDir.substring(0,absoluteDir.indexOf("/"+familyDirName+"/"));
		File currentDir = new File(startDir,path);
		File original = new File(new File(currentDir,"_originals"),properties.getProperty("FILENAME"));
		File origDest = new File(new File(toDir,"_originals"),properties.getProperty("FILENAME"));
		if(!original.exists() || origDest.exists()) return false;
		origDest.getParentFile().mkdirs();
		System.out.println("Moving files from " + original.getPath() + " to " + origDest.getPath());
		original.renameTo(origDest);
		ProductFamily family = dbutil.getProductFamily(properties.getProperty("family"));
		family.moveDerivatives(Files.hackOffExtension(properties.getProperty("FILENAME")), toDir, currentDir);
		return true;
	}

	@Override
	public void delete() {
		origFile.delete();
	}

	@Override
	public Properties getAdditionalMeta(String productPrimary) {
		return null;
	}

	@Override
	public Properties getMeta() {
		return properties;
	}

	@Override
	public String getName() {
		return properties.getProperty("FILENAME");
	}

	@Override
	public String getProductFamily() {
		return properties.getProperty("family");
	}

	@CoinjemaDependency(alias="dbutil")
	public void setDbutil(DatabaseUtilities dbutil) {
		this.dbutil = dbutil;
	}

}
