package com.lazerbank.scripts;

import java.io.File;
import java.io.FileFilter;

import strategiclibrary.util.Files;

public class ReorgLowRes {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File(args[0]);
		doDirectory(dir);

	}
	
	private static void doDirectory(File dir)
	{
		System.out.println("Doing dir: " + dir.getAbsolutePath());
		if(dir.getName().equals("Thumbnails") || dir.getName().equals("Viewfiles"))
			Files.deleteTree(dir);
		else if(dir.getName().equals("Originals"))
		{
			doOriginalsDir(dir);
			Files.deleteTree(dir);
		}
		else
		{
			dir.listFiles(new FileFilter() {
				public boolean accept(File f)
				{
					if(f.exists() && f.isDirectory())
					{
						doDirectory(f);
						return true;
					}
					return false;
				}
			});
		}
	}
	
	private static void doOriginalsDir(File orig)
	{
		System.out.println("In Originals: " + orig.getAbsolutePath());
		File[] realOriginalsDir = orig.listFiles(new FileFilter() {
				public boolean accept(File f)
				{
					if(f.exists() && f.isDirectory() && f.getName().indexOf(".") == -1)
					{
						return true;
					}
					else return false;
				}
			});
		File parent = orig.getParentFile();
		moveOriginals(parent,orig);
		if(realOriginalsDir != null && realOriginalsDir.length >= 1)
		{
			for(File realOrigDir : realOriginalsDir)
			{
				moveOriginals(parent, realOrigDir);
			}
		}
	}

	private static void moveOriginals(File parent, File realOrigDir) {
		System.out.println("Move files from " + realOrigDir.getAbsolutePath());
		File[] origFiles = realOrigDir.listFiles(new FileFilter() {
			public boolean accept(File f)
			{
				if(f.exists() && !f.isDirectory() && !f.getName().toLowerCase().startsWith("."))
				{
					return true;
				}
				else return false;
			}
		});
		for(File origFile : origFiles)
		{
			System.out.println("Moving file: " + origFile.getName());
			origFile.renameTo(new File(parent,origFile.getName()));
		}
	}

}
