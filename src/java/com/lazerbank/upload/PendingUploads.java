package com.lazerbank.upload;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;

import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

@CoinjemaObject(type="upload")
public class PendingUploads implements Iterable<UploadPackage>{

	
	public PendingUploads()
	{
		
	}
	
	public Iterator<UploadPackage> iterator()
	{
		return new PackageIterator(uploadsPending);
	}
	
	class PackageIterator implements Iterator<UploadPackage>
	{
		File packageDir;
		File[] packages;
		int count = 0;
		
		public PackageIterator(File dir)
		{
			packageDir = dir;
			packages = packageDir.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					return pathname.exists() && pathname.isFile() && (pathname.getName().endsWith(".zip") || pathname.getName().endsWith(".properties"));
				}
				
			});
		}

		public boolean hasNext() {
			return count < packages.length && nextPack() != null;
		}

		private UploadPackage nextPack() {
			UploadPackage pack = null;
			while(pack == null)
			{
				try {
					if(packages[count].getName().endsWith(".zip"))
						pack = new UploadPackage(packages[count]);
					else pack = new MoveProperties(packages[count]);
				}catch(Exception e)
				{
					count++;
					if(count >= packages.length) return null;
				}
			}
			return pack;
		}
		
		public UploadPackage next()
		{
			UploadPackage nextPack = nextPack();
			count++;
			return nextPack;
		}

		public void remove() {
			
		}
		
	}
	
	
	
	File uploadsPending;
	@CoinjemaDependency(method="watchedFolder")
	public void setUploadsPending(String watchFolder) {
		this.uploadsPending = new File(watchFolder);
	}

}
