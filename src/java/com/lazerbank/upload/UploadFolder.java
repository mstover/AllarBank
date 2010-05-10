package com.lazerbank.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.coinjema.context.CoinjemaContext;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import com.lazerbank.CatalogedFolder;

@CoinjemaObject(type="asset")
public class UploadFolder extends CatalogedFolder {

	Properties categories;
	List<String> extraCategories;
	
	float matchStrength = 0;

	public UploadFolder(File d) {
		super(d);
	}

	public UploadFolder(String root, File d, CoinjemaContext cc) {
		super(root, d, cc);
	}

	public UploadFolder(String root, File d) {
		super(root, d);
	}

	public UploadFolder(File d, CoinjemaContext cc) {
		super(d, cc);
	}
	
	public UploadFolder findFamilyMatch(String family)
	{
		if (getFamily() != null && !getFamily().getTableName().equals(family))
			return null;
		else if(getFamily() != null && getFamily().getTableName().equals(family)) return this;
		for (File subDir : getDirectory().listFiles()) {
			if (subDir.isFile())
				continue;
			if (subDir.getName().startsWith("_"))
				continue;
			if (!subDir.exists())
				continue;
			UploadFolder subFolder = new UploadFolder(getRoot(),subDir,
					new CoinjemaContext(subDir.getName())).findFamilyMatch(family);
			if(subFolder != null) return subFolder;
		}
		return null;
	}

	public UploadFolder findMatch(String family, Properties otherCategories,
			float minMatchStrength) {
		if (getFamily() != null && !getFamily().getTableName().equals(family))
			return null;
		matchStrength = calcMatchStrength(otherCategories);
		float bestMatchStrength = Math.max(matchStrength, minMatchStrength);
		UploadFolder ret = matchStrength == bestMatchStrength ? this : null;
		if(bestMatchStrength == 1F) return ret;
		for (File subDir : getDirectory().listFiles()) {
			if (subDir.isFile())
				continue;
			if (subDir.getName().startsWith("_"))
				continue;
			if (!subDir.exists())
				continue;
			UploadFolder subFolder = new UploadFolder(getRoot(),subDir,
					new CoinjemaContext(subDir.getName())).findMatch(family,
					otherCategories, bestMatchStrength);
			ret = subFolder != null ? subFolder : ret;
			if(ret != null) bestMatchStrength = ret.getMatchStrength();
		}
		return ret;
	}

	/**
	 * It's a potential match of otherCategories is a superset of this folders categories -
	 * ie, the folder won't be giving the file any metadata not asserted in the otherCategories.
	 * 
	 * It's a partial match if the otherCategories contains metadata not found in the folder.  The strength
	 * of the match is the percentage of otherCategories matched by the folder's categories.
	 * @param otherCategories
	 * @return
	 */
	private float calcMatchStrength(Properties otherCategories) {
		int size = otherCategories.size();
		int numMatches = 0;
		for (Object key : categories.keySet()) {
			if (!otherCategories.containsKey(key)
					|| !categories.get(key).equals(otherCategories.get(key)))
				return 0;
			else
				numMatches++;
		}
		getLog().info("Num matches = " + numMatches);
		return (float) ((float)numMatches / (float)size);
	}

	public float getMatchStrength() {
		return matchStrength;
	}

	public void setMatchStrength(float matchStrength) {
		this.matchStrength = matchStrength;
	}

	public Properties getCategories() {
		return categories;
	}

	@CoinjemaDependency(method="category")
	public void setCategories(Properties rawCats) {
		if(extraCategories == null) extraCategories = new ArrayList<String>();
		else return;
		this.categories = new Properties();
		for(Object key : rawCats.keySet())
		{
			String skey = ((String) key).replaceAll("_", " ");
			String value = rawCats.getProperty((String) key);
			if (value.trim().matches("\\.+")) {
				String[] pathElements = getPath().split("/");
				int len = value.trim().length();
				if (pathElements.length >= len) {
					value = pathElements[len - 1];
				} else
					value = null;
			}
			if (value != null && !"null".equals(value))
				categories.setProperty((String) skey, value.trim());
			else
			{
				value = rawCats.getProperty((String) key);
				if (value.trim().matches("\\.+")) {
					extraCategories.add(value.trim()+skey);
				}
			}
		}
		if(extraCategories != null)
		{
			Collections.sort(extraCategories,Collections.reverseOrder());
			for(int i = 0;i < extraCategories.size();i++)
			{
				extraCategories.set(i,extraCategories.get(i).replaceAll("\\.+",""));
			}
		}		
	}
	
	public boolean copyFiles(UploadPackage pack) throws IOException
	{
		StringBuffer newDir = new StringBuffer();
		for(String dir : getExtraDirs(pack.getMeta()))
		{
			newDir.append(dir).append("/");
		}
		File toDir = getDirectory();
		if(newDir.length() > 0)
		{
			toDir = new File(toDir,newDir.toString());
			toDir.mkdirs();
		}
		return pack.copyFilesTo(toDir);
	}
	
	public Collection<String> getExtraDirs(Properties otherCategories)
	{
		List<String> newDirs = new LinkedList<String>();
		if(otherCategories.containsKey("path")) return newDirs;
		for(String cat : extraCategories)
		{
			if(otherCategories.containsKey(cat))
			{
				newDirs.add(otherCategories.getProperty(cat).trim());
			}
		}
		return newDirs;
	}

	

}
