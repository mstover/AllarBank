package com.lazerbank.scripts;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import strategiclibrary.util.Files;

public class ReorgHighRes {
	static File lowresDir, highresDir, toDir;

	static Stack<String> directories = new Stack<String>();
	static boolean colorOrg = false;
	static PrintWriter notes;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		lowresDir = new File(args[0]);
		highresDir = new File(args[1]);
		toDir = new File(args[2]);
		if(args.length == 4)
			colorOrg = true;
		toDir.mkdirs();
		notes = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(toDir,"notes.txt"))));
		notes.write("Low Res Files Without High Res");
		doDirectory(lowresDir);
		notes.close();
		

	}

	private static void doDirectory(File dir) {
		File[] subdirs = dir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				if (pathname.isDirectory() && !pathname.getName().equals("Thumbnails")
						&& !pathname.getName().equals("Viewfiles"))
					return true;
				return false;
			}

		});
		for (File subdir : subdirs) {
			if (subdir.getName().equals("Originals")) {
				doOriginalsDir(subdir);
			} else {
				directories.push(subdir.getName());
				doDirectory(subdir);
				directories.pop();
			}
		}
	}

	private static void doOriginalsDir(File orig) {
		File[] realOriginalsDir = orig.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.exists() && f.isDirectory()
						&& f.getName().indexOf(".") == -1) {
					return true;
				} else
					return false;
			}
		});
		copyOriginals(orig);
		if (realOriginalsDir != null && realOriginalsDir.length >= 1) {
			for (File realOrigDir : realOriginalsDir) {
				copyOriginals(realOrigDir);
			}
		}
	}

	private static void copyOriginals(File lowresDirectory) {
		File[] origFiles = lowresDirectory.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.exists() && !f.isDirectory()
						&& !f.getName().toLowerCase().startsWith(".")) {
					return true;
				} else
					return false;
			}
		});
		for (File lowResFile : origFiles) {
			File[] highResFiles = findHighRes(lowResFile);
			try {
			if (highResFiles == null || highResFiles.length == 0) {
				System.out.println("Copying low-res file: " + lowResFile.getName());
				appendToNotes(lowResFile.getPath());
				Files.copyFile(lowResFile, createNewFile(lowResFile));
			} else {
				for (File highResFile : highResFiles) {
					File newFile = createNewFile(highResFile);
					if (!newFile.exists())
					{
						System.out.println("Copying high-res file: " + highResFile.getName() + " to " + newFile.getPath());
						Files.copyFile(highResFile, newFile);
					}
				}
			}
			}catch(IOException e)
			{
				System.out.println("Failed to copy file: " + lowResFile.getPath());
				e.printStackTrace();
			}
		}
	}
	
	private static void appendToNotes(String note)
	{
		notes.write(note);
		notes.write("\n");
	}

	private static File[] findHighRes(File lowResFile) {
		String assetName = Files.hackOffExtension(lowResFile);
		List<File> filesFound = new LinkedList<File>();
		findFilesWithName(highresDir, assetName, filesFound);
		return filesFound.toArray(new File[filesFound.size()]);
	}

	private static void findFilesWithName(File dir, String name,
			List<File> filesFound) {
		for (File f : dir.listFiles()) {
			if (f.isFile()) {
				if (name.equals(Files.hackOffExtension(f)))
				{
					filesFound.add(f);
				}
			} else if (f.isDirectory())
				findFilesWithName(f, name, filesFound);
		}
	}

	private static File createNewFile(File highResFile) {
		String color = colorOrg ? getColorType(highResFile) : null;
		File newDir = new File(toDir, createNewPath(color));
		newDir.mkdirs();
		return new File(newDir,highResFile.getName());
	}

	private static String getColorType(File highResFile) {
		String[] dirNames = highResFile.getPath().split("/");
		String color = "RGB";
		for (String name : dirNames) {
			name = name.toUpperCase();
			if (name.equals("IMAGES-CMYK") || name.equals("CMYK"))
				return "CMYK";
			else if (name.equals("IMAGES-GRAYSCALE")
					|| name.equals("GRAYSCALE"))
				return "GRAYSCALE";
		}
		return color;
	}

	private static String createNewPath(String colorType) {
		StringBuffer path = new StringBuffer();
		for (String name : directories) {
			path.append(name).append("/");
		}
		if(colorOrg)
			path.insert(0, colorType+"/");
		return path.toString();
	}
}
