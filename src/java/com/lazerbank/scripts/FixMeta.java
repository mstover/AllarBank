package com.lazerbank.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FixMeta {

	public FixMeta() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader("/home/mikes/Documents/lazer_ideas/filenames.csv"));
		String line = in.readLine();
		Set<String> filenames = new HashSet<String>();
		while(line != null)
		{
			filenames.add(cleanLine(line));
			line = in.readLine();
		}
		in.close();
		List<String> metadata = new ArrayList<String>();
		in = new BufferedReader(new FileReader("/home/mikes/Documents/lazer_ideas/irwin_obsolete_keys.csv"));
		line = in.readLine();
		String headers = line;
		line = in.readLine();
		while(line != null)
		{
			String filename = getFilename(line);
			if(filenames.contains(filename))
				metadata.add(line);
			line = in.readLine();
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/mikes/Documents/lazer_ideas/irwin_obsolete_keys_cleaned.csv"));
		out.write(headers);
		out.write("\n");
		for(String outLine : metadata)
		{
			out.write(outLine);
			out.write("\n");
		}
		out.close();

	}
	
	private static String cleanLine(String line)
	{
		if(line.startsWith("\"")) line = line.substring(1);
		if(line.endsWith("\"")) line = line.substring(0,line.length()-1);
		return line;
	}
	
	private static String getFilename(String line)
	{
		int index = line.indexOf("\"Irwin/");
		String filename = line.substring(0,index);
		index = filename.lastIndexOf("\"");
		filename = filename.substring(0,index);
		index = filename.lastIndexOf("\"");
		filename = filename.substring(index+1);
		return filename;
		
	}

}
