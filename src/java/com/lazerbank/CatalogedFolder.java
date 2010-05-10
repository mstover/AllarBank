package com.lazerbank;

import static com.lazerbank.DigitalAsset.INVALID;
import static com.lazerbank.DigitalAsset.ORIGINALS;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaContext;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import com.lazerinc.category.ProductField;
import com.lazerinc.category.ProtectedField;
import com.lazerinc.ecommerce.CommerceProduct;
import com.lazerinc.ecommerce.DatabaseUtilities;
import com.lazerinc.ecommerce.ProductFamily;

@CoinjemaObject(type = "asset")
public class CatalogedFolder implements Asset, Comparable<Asset> {
	File directory;

	String root;

	Lazerweb repository;

	Set<String> acceptedExtensions;

	Logger log;

	ProductFamily family;

	DatabaseUtilities dbutil;

	Set<String> ignoredDirs = new HashSet<String>();

	String[] protectedCategories;

	boolean processingFolder;

	public CatalogedFolder(File d) {
		directory = d;
		root = directory.getAbsolutePath();
		processingFolder = false;
		protectedCategories = new String[0];
	}

	public CatalogedFolder(String root, File d) {
		directory = d;
		this.root = root;
		processingFolder = false;
		protectedCategories = new String[0];
	}

	public CatalogedFolder(String root, File d, CoinjemaContext cc) {
		directory = d;
		this.root = root;
		processingFolder = false;
		protectedCategories = new String[0];
	}

	/**
	 * Create a subfolder with a coinjema context.
	 * 
	 * @param d
	 * @param cc
	 */
	public CatalogedFolder(File d, CoinjemaContext cc) {
		directory = d;
		root = directory.getAbsolutePath();
		protectedCategories = new String[0];
	}

	private void updateProtectedFields() {
		for (String fieldName : protectedCategories) {
			ProductField f = family.getField(fieldName);
			if (f == null) {
				f = new ProtectedField(family.getTableName(), fieldName, 0, 0);
				family.createField(f, dbutil.getAdmin());
			} else if (f.getType() != ProductField.PROTECTED) {
				log.debug("Make sure " + fieldName + " is a protected field");
				f.setType(ProductField.PROTECTED);
				family.updateField(f, dbutil.getAdmin());
			}
		}
	}

	public boolean collect(Catalog catalog) {
		log.debug("directory name = " + directory.getName()
				+ " ignored dirs = " + ignoredDirs);
		if (directory != null && directory.isDirectory()) {
			if (!ignoredDirs.contains(directory.getName())) {
				updateProtectedFields();
				log.debug("Cataloging folder " + getPath());
				Collection<Asset> assets = getAssets();
				if (assets == null || assets.size() == 0)
					deleteIgnoredDirs();
				for (Asset a : assets) {
					if (a instanceof CatalogedFolder) {
						catalog.addExisting(a);
						if (family != null) {
							((CatalogedFolder) a).collect(catalog);
						} else {
							((CatalogedFolder) a).catalog().waitFor();
						}
					} else if (family != null) {
						catalog.addExisting(a);
						if (!((DigitalAsset) a).exists()) {
							catalog.forAddition((DigitalAsset) a);
						} else {
							catalog.forUpdate((DigitalAsset) a);
						}
					}
				}
				if (family != null) {
					for (DigitalAsset a : repository.getMissingAssets(family,
							getPath(), catalog.getExisting(), processingFolder)) {
						a.setPathRoot("");
						catalog.forRemoval(a);
					}
					removeConversions(new HashSet<Asset>(assets));
					if (processingFolder) {
						catalog.processCollectedAssets();
						catalog.clear();
					}
				}
			}
		}
		return true;
	}

	/**
	 * Process for cataloging a folder and all images within it and all
	 * sub-folders within.
	 */
	public Catalog catalog() {
		if (family != null) {
			family.refresh();
			family.getProducts(dbutil.getAdmin());
		}
		processingFolder = true;
		Catalog c = new Catalog(family);
		collect(c);
		return c;
	}

	private void removeConversions(Set<Asset> existing) {
		File[] conversionDirs = directory.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return (f.isDirectory() && f.exists()
						&& f.getName().startsWith("_")
						&& !f.getName().equals(ORIGINALS) && !f.getName()
						.equals(INVALID));
			}
		});
		for (File dir : conversionDirs) {
			boolean isEmpty = true;
			for (File convertedImage : dir.listFiles()) {
				if (convertedImage.exists()) {
					if (convertedImage.isDirectory())
						delete(convertedImage);
					else {
						DigitalAsset asset = new DigitalAsset(root, this,
								convertedImage);
						if (!existing.contains(asset))
							convertedImage.delete();
						else
							isEmpty = false;
					}
				}
			}
			if (isEmpty)
				delete(dir);
		}
	}

	public String getPath() {
		String path = directory.getAbsolutePath().substring(root.length())
				+ "/";
		if (path.length() > 1 && path.startsWith("/"))
			path = path.substring(1);
		return path;
	}

	/**
	 * When a folder is found to be empty, delete all processed image folders
	 * (those that begin with "_").
	 * 
	 */
	private void deleteIgnoredDirs() {
		delete(directory);
	}

	private void delete(File dir) {
		dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.exists() && f.getName().startsWith("_")) {
					delete(f);
					f.delete();
				}
				return false;
			}
		});
	}

	/**
	 * Get a list of digital assets in the folder and sub-folders that may
	 * contain digital assets.
	 * 
	 * @return
	 */
	protected LinkedList<Asset> getAssets() {
		final LinkedList<Asset> assets = new LinkedList<Asset>();
		final CatalogedFolder parent = this;
		directory.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (!f.getName().startsWith("_") && f.exists()) {
					if (f.isDirectory()) {
						CatalogedFolder cf = new CatalogedFolder(root, f,
								new CoinjemaContext(f.getName()));
						assets.add(cf);
					} else {
						if (isDigitalAsset(f)) {
							assets.add(new DigitalAsset(root, parent, f));
						}
					}
					return true;
				} else if (f.getName().equals(DigitalAsset.ORIGINALS)
						&& f.isDirectory()) {
					addCatalogedAssets(f, assets);
				}
				return false;
			}
		});
		Collections.sort(assets);
		return assets;
	}

	private void addCatalogedAssets(File originals,
			final LinkedList<Asset> assets) {
		final CatalogedFolder parent = this;
		originals.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (!f.getName().startsWith("_") && f.exists()
						&& !f.isDirectory()) {
					if (isDigitalAsset(f)) {
						assets
								.add(new PreCatalogedDigitalAsset(root, parent,
										f));
					}
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Is the file an image or just a configuration file? Only images are
	 * assets.
	 * 
	 * @param f
	 * @return
	 */
	private boolean isDigitalAsset(File f) {
		return acceptedExtensions.contains(getExtension(f));
	}

	private String getExtension(File f) {
		int index = f.getName().lastIndexOf(".");
		if (index > -1 && index < f.getName().length() - 1)
			return f.getName().substring(index + 1);
		else
			return "";
	}

	public int compareTo(Asset o) {
		if (o instanceof CatalogedFolder) {
			return directory.getName().compareTo(
					((CatalogedFolder) o).directory.getName());
		} else
			return 1;
	}

	@CoinjemaDependency(method = "repository", type = "repository")
	public void setRepository(Lazerweb r) {
		repository = r;
	}

	@CoinjemaDependency(method = "extensions")
	public void setExtensions(Set<String> extSet) {
		acceptedExtensions = extSet;
	}

	@CoinjemaDependency(alias = "log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

	@CoinjemaDependency(method = "productFamily", order = CoinjemaDependency.Order.LAST)
	public void setFamily(String family) {
		this.family = dbutil.getProductFamily(family);

	}

	public ProductFamily getFamily() {
		return family;
	}

	@CoinjemaDependency(method = "process", hasDefault = true)
	public void setProcessingFolder(boolean t) {
		processingFolder = t;
	}

	@CoinjemaDependency(alias = "ignore", hasDefault = true)
	public void setIgnoredDirs(String commaDelimNames) {
		for (String dir : commaDelimNames.split(","))
			ignoredDirs.add(dir);
	}

	@CoinjemaDependency(method = "protectedCategories", hasDefault = true)
	public void setProtectedCategories(String[] protectedCategories) {
		this.protectedCategories = protectedCategories;
	}

	public File getDirectory() {
		return directory;
	}

	protected Logger getLog() {
		return log;
	}

	protected String getRoot() {
		return root;
	}

	@CoinjemaDependency(type = "dbutil")
	public void setDbutil(DatabaseUtilities dbutil) {
		this.dbutil = dbutil;
	}
}
