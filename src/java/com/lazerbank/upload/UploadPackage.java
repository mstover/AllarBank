package com.lazerbank.upload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import strategiclibrary.util.Files;

public class UploadPackage{
	
	ZipFile zipPackage;
	File underlying;
	Properties meta;
	Map<String,Properties> additionalMetaData;
	String productFamily;
	String path;
	
	public UploadPackage()
	{
		
	}
	
	public UploadPackage(File zipPackage) throws IOException
	{
		underlying = zipPackage;
		this.zipPackage = new ZipFile(zipPackage);
		meta = readCategories();
		additionalMetaData = readAdditionalMetaData();
		System.out.println("additional meta = " + additionalMetaData);
		productFamily = meta.getProperty("family");
		meta.remove("family");
	}
	
	public String getProductFamily() {
		return productFamily;
	}

	public Properties getMeta() {
		return meta;
	}
	
	public Properties getAdditionalMeta(String productPrimary)
	{
		if(additionalMetaData.containsKey(productPrimary))
		{
			return additionalMetaData.get(productPrimary);
		}
		else return null;
	}
	
	
	public void delete()
	{
		try {
			zipPackage.close();
		} catch(Exception e){}
		underlying.delete();
	}
	
	public String getName()
	{
		return zipPackage.getName();
	}
	
	private Map<String,Properties> readAdditionalMetaData() throws IOException
	{
		Map<String,Properties> additionalMeta = new HashMap<String,Properties>();
		if(zipPackage.getEntry("additional.meta") != null)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(zipPackage.getInputStream(zipPackage.getEntry("additional.meta"))));
			String delimiter = in.readLine();
			String[] headers = in.readLine().split(delimiter);
			String line = null;
			while((line = in.readLine()) != null)
			{
				String[] values = line.split(delimiter);
				additionalMeta.put(values[0].trim(),generateProperties(headers,values));
			}
		}
		return additionalMeta;
	}
	
	private Properties generateProperties(String[] keys,String[] values)
	{
		Properties props = new Properties();
		for(int i = 1;i < keys.length;i++)
		{
			if(values.length > i)
				props.setProperty(keys[i].trim(), values[i].trim());
			else props.setProperty(keys[i].trim(), "N/A");
		}
		return props;
	}

	private Properties readCategories() throws IOException
	{
			Properties cats = new Properties();
			InputStream in = zipPackage.getInputStream(zipPackage.getEntry("imagedata.properties"));
			cats.load(in);
			in.close();
			Properties fixedCats = new Properties();
			for(Object raw : cats.keySet())
			{
				String key = ((String)raw).replaceAll("_"," ");
				fixedCats.setProperty(key,cats.getProperty((String)raw));
			}
			return fixedCats;
	}
	
	public boolean copyFilesTo(File toDir) throws IOException
	{
		Enumeration<? extends ZipEntry> entries = zipPackage.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = entries.nextElement();
			if(entry.getName().endsWith(".meta") || entry.getName().endsWith(".properties")) continue;
			BufferedInputStream in = new BufferedInputStream(zipPackage.getInputStream(entry));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(toDir,entry.getName())));
			try {
				Files.copy(in,out);
				Properties additionalMeta = getAdditionalMeta(entry.getName());
				if(additionalMeta != null) additionalMeta.store(new FileOutputStream(new File(toDir,entry.getName()+".properties")), "");
			}finally {
				try {
					if(in != null)
						in.close();
				} finally {
					if(out != null)
						out.close();
				}
			}
		}
		return true;
	}
}
