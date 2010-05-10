package com.lazerbank.scripts;

import java.io.File;
import java.io.FileFilter;

import strategiclibrary.util.Files;

public class ReCatalog {

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
		if(dir.getName().equals("_originals") || dir.getName().equals("_invalid_originals"))
		{
			moveOriginals(dir);
		}
		else if(dir.getName().equals("_jpg") || dir.getName().equals("_eps") || dir.getName().equals("_png")
				 || dir.getName().equals("_web") || dir.getName().equals("_thumb"))
		{
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

	private static void moveOriginals(File dir) {
		File parent = dir.getParentFile();
		System.out.println("Move files from " + dir.getAbsolutePath());
		File[] origFiles = dir.listFiles(new FileFilter() {
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
