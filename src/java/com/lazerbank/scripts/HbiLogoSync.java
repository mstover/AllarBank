package com.lazerbank.scripts;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import strategiclibrary.util.Files;

public class HbiLogoSync {

	static File topDir;

	static String logosLib = "HBI_Logo_Library";
	
	static Map<String,String> libraryNames = new HashMap<String,String>();
	
	static {
		libraryNames.put("C9 by Champion","C9 by Champion");
		libraryNames.put("Champion Apparel","Champion Apparel");
		libraryNames.put("Duofold","Duofold");
		libraryNames.put("Hanes Casualwear","Hanes Casualwear");
		libraryNames.put("Hanes Hosiery","Hanes Hosiery");
		libraryNames.put("Hanes_Magalog","Hanes_Magalog");
		libraryNames.put("Hanes Sleepwear","Hanes Sleepwear");
		libraryNames.put("Hanes_Slush","Hanes_Slush");
		libraryNames.put("Hanes_Slush","Hanes Socks");
		libraryNames.put("Hanes Ultimate","Hanes Ultimate");
		libraryNames.put("Hanes Underwear","Hanes Underwear");
		libraryNames.put("HB_Champion_Team","HB_Champion_Team");
		libraryNames.put("HP-OB","HP-OB");
		libraryNames.put("IA_Bali Scanbank","Bali");
		libraryNames.put("IA_Barely There Scanbank","Barely There");
		libraryNames.put("IA_Hanes Scanbank","Hanes");
		libraryNames.put("IA_JMS Scanbank","JMS");
		libraryNames.put("IA_Playtex Scanbank","Playtex");
		libraryNames.put("IA_Wonderbra Scanbank","Wonderbra");
		libraryNames.put("Leggs","Leggs");
		libraryNames.put("Polo Ralph Lauren","Polo Ralph Lauren");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		topDir = new File(args[0]);
		doDirectory(new File(topDir, "C9 by Champion"));
		doDirectory(new File(topDir, "Champion Apparel"));
		doDirectory(new File(topDir, "Duofold"));
		doDirectory(new File(topDir, "Hanes Casualwear"));
		doDirectory(new File(topDir, "Hanes Hosiery"));
		doDirectory(new File(topDir, "Hanes_Magalog"));
		doDirectory(new File(topDir, "Hanes Sleepwear"));
		doDirectory(new File(topDir, "Hanes_Slush"));
		doDirectory(new File(topDir, "Hanes Socks"));
		doDirectory(new File(topDir, "Hanes Ultimate"));
		doDirectory(new File(topDir, "Hanes Underwear"));
		doDirectory(new File(topDir, "HB_Champion_Team"));
		doDirectory(new File(topDir, "HP-OB"));
		doDirectory(new File(topDir, "IA_Bali Scanbank"));
		doDirectory(new File(topDir, "IA_Barely There Scanbank"));
		doDirectory(new File(topDir, "IA_Hanes Scanbank"));
		doDirectory(new File(topDir, "IA_JMS Scanbank"));
		doDirectory(new File(topDir, "IA_Playtex Scanbank"));
		doDirectory(new File(topDir, "IA_Wonderbra Scanbank"));
		doDirectory(new File(topDir, "Leggs"));
		doDirectory(new File(topDir, "Polo Ralph Lauren"));

	}

	private static void doDirectory(File dir) {
		if (dir.getName().equals("_originals")
				&& dir.getPath().matches(".+\\/[lL]ogos?\\/.+")) {
			moveOriginals(dir);
		} else {
			dir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					if (f.exists() && f.isDirectory()) {
						doDirectory(f);
						return true;
					}
					return false;
				}
			});
		}
	}

	private static void moveOriginals(File dir) {
		System.out.println("Move files from " + dir.getAbsolutePath());
		File[] origFiles = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.exists() && !f.isDirectory()
						&& !f.getName().toLowerCase().startsWith(".")) {
					return true;
				} else
					return false;
			}
		});
		File toDir = new File(new File(topDir,logosLib),chopPath(dir));
		for (File origFile : origFiles) {
			File destFile = new File(toDir,origFile.getName());
			if (!destFile.exists()) {
				System.out.println("Moving file: " + origFile.getName() + " to " + destFile.getPath());
				try {
					destFile.getParentFile().mkdirs();
					Files.copyFile(origFile, destFile);
				} catch (Exception e) {

				}
			}
		}
	}

	private static String chopPath(File dir) {
		String n = dir.getPath();
		if(n.startsWith(topDir.getPath()))
			n = n.substring(topDir.getPath().length());
		if(n.startsWith("/")) n = n.substring(1);
		String familyDir = n.substring(0,n.indexOf("/"));
		n = n.substring(familyDir.length()+1);
		familyDir = libraryNames.get(familyDir);
		if(n.startsWith("/")) n = n.substring(1);
		String archive = n.substring(0,n.indexOf("/"));
		n = n.substring(archive.length()+1);
		n = archive + "/" + familyDir + "/" + n;
		return n;
	}

}
