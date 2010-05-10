package com.lazerbank;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import strategiclibrary.util.Converter;
import strategiclibrary.util.Files;

import com.lazerinc.application.Product;
import com.lazerinc.beans.Path;
import com.lazerinc.ecommerce.CommerceProduct;
import com.lazerinc.ecommerce.DatabaseUtilities;
import com.lazerinc.ecommerce.ProductFamily;

@CoinjemaObject(type = "asset")
public class DigitalAsset implements Asset, Comparable<Asset> {
	public static final String ORIGINALS = "_originals";

	public static final String INVALID = "_invalid_originals";

	protected File file;

	protected Lazerweb repository;

	protected Logger log;

	protected ProductFamily family;

	protected String pathRoot;
	
	DatabaseUtilities dbutil;

	private CatalogedFolder parent;

	protected Properties categories;// = new Properties();

	private Properties conversions;// = new Properties();

	boolean skipConversions = false;

	// Should conversions overwrite already existing conversions?
	protected boolean recreateConversions = false;

	Class<? extends Product> productClass = CommerceProduct.class;

	ImageMagick magic;

	Lazerweb getRepository() {
		return repository;
	}

	@CoinjemaDependency(type = "repository", method = "repository")
	public void setRepository(Lazerweb repository) {
		this.repository = repository;
	}

	public boolean exists() {
		return file.exists()
				&& repository.getExisting(family, getName(), getPath()) != null;
	}

	public DigitalAsset(String root, File f) {
		file = f.getAbsoluteFile();
		pathRoot = root;
	}

	public DigitalAsset(String root, CatalogedFolder directory, File f) {
		file = f.getAbsoluteFile();
		pathRoot = root;
		parent = directory;
	}

	public DigitalAsset(String root, String name) {
		file = new File(name).getAbsoluteFile();
		pathRoot = root;
	}

	public String getName() {
		return Files.hackOffExtension(file.getName());
	}

	public String getPrimary() {
		return file.getName();
	}

	public int compareTo(Asset o) {
		if (o instanceof DigitalAsset) {
			return getName().compareTo(((DigitalAsset) o).getName());
		} else
			return -1;
	}

	@CoinjemaDependency(alias = "log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

	public void delete() {
		log.info("Deleting " + this);
		repository.deleteAsset(this);
		family.setDirty(true);
	}

	public void add() {
		log.info("Adding " + this);
		try {
			convertImage();
			repository.createProduct(processAsset());
			family.setDirty(true);
			moveAsset();
			getMetadataFile().delete();
		} catch (BadImageFileException e) {
			log.error("Bad Image:  " + this, e);
			try {
				moveBadAsset();
			} catch (IOException ex) {
				log.error("IO Exception dealing with asset " + this, ex);
			}
		} catch (ConversionTimeoutException e) {
			log.error("Conversion Timeout:  " + this, e);
		} catch (BadConversionException e) {
			log.error("Bad Image:  " + this, e);
			try {
				moveBadAsset();
			} catch (IOException ex) {
				log.error("IO Exception dealing with asset " + this, ex);
			}
		} catch (MagicException e) {
			log.error("Problem Magicking image: " + this, e);
		} catch (IOException e) {
			log.error("IO Exception dealing with asset " + this, e);
		}
	}
	
	protected void setValues(Product dest,Product src,String key)
	{
		Collection<Object> values = src.getValues(key);
		for(Object s : values)
			dest.setValue(key,s);
	}

	public void update() {
		try {
			convertImage();
			Product existing = repository.getExisting(family, getName(),
					getPath());
			Product asset = processAsset();
			boolean hasUpdate = false;
			if (existing != null) {
				existing.setPath(asset.getPath());
				for (String key : (Set<String>) asset.getValueNames()) {
					if (existingDoesNotContain(existing, asset, key)) {
						existing.removeValue(key);
						setValues(existing,asset,key);
						hasUpdate = true;
					}
				}
				if (hasUpdate)
				{
					try {
						determineImageProperties(existing);
					} catch (Exception e) {
						
					}
					log.info("Update in db " + this);
					family.setDirty(true);
					existing.setDateModified(new GregorianCalendar());
					repository.updateProduct(existing);
				}
			}
			moveAsset();
			getMetadataFile().delete();
		} catch (BadImageFileException e) {
			log.error("Bad Image:  " + this, e);
			try {
				moveBadAsset();
			} catch (IOException ex) {
				log.error("IO Exception dealing with asset " + this, ex);
			}
		} catch (ConversionTimeoutException e) {
			log.error("Conversion Timeout:  " + this, e);
		} catch (BadConversionException e) {
			log.error("Bad Image:  " + this, e);
			try {
				moveBadAsset();
			} catch (IOException ex) {
				log.error("IO Exception dealing with asset " + this, ex);
			}
		} catch (MagicException e) {
			log.error("Bad Image:  " + this, e);
		} catch (IOException e) {
			log.error("IO Exception dealing with asset " + this, e);
		} catch(Throwable e)
		{
			log.error("Unknown error",e);
		} finally {
			log.info("Done updating " + this);
		}
	}

	protected File getMetadataFile() {
		return new File(new File(pathRoot,getPath()),getPrimary()+".properties");
	}

	protected boolean existingDoesNotContain(Product existing,
			Product cataloged, String key) {
		return true;
	}

	public void moveTo(DigitalAsset dest) {
		log.info("Moving " + this + " to " + dest);
		try {
			Product existing = repository.getExisting(family, getName(),
					getPath());
			dest.convertImage();
			Product asset = dest.processAsset();
			existing.setPath(asset.getPath());
			existing.setPrimary(asset.getPrimary());
			if (existing != null) {
				for (String key : (Set<String>) asset.getValueNames()) {
					existing.removeValue(key);
					setValues(existing,asset,key);
				}
				existing.setDateModified(new GregorianCalendar());
				repository.updateProduct(existing);
				family.setDirty(true);
			}
			dest.moveAsset();
		} catch (BadImageFileException e) {
			log.error("Bad Image:  " + this, e);
			try {
				moveBadAsset();
			} catch (IOException ex) {
				log.error("IO Exception dealing with asset " + this, ex);
			}
		} catch (ConversionTimeoutException e) {
			log.error("Conversion Timeout:  " + this, e);
		} catch (BadConversionException e) {
			log.error("Bad Image:  " + this, e);
			try {
				moveBadAsset();
			} catch (IOException ex) {
				log.error("IO Exception dealing with asset " + this, ex);
			}
		} catch (MagicException e) {
			log.error("Problem Magicking image: " + this, e);
		} catch (IOException e) {
			log.error("IO Exception dealing with asset " + this, e);
		}
	}

	public Catalog catalog() throws IOException, MagicException {
		throw new UnsupportedOperationException();
	}

	protected void moveAsset() throws IOException {
		moveAsset(ORIGINALS);
	}

	void moveBadAsset() throws IOException {
		moveAsset(INVALID);
	}

	protected void moveAsset(String dirname) {
		if(file.exists())
		{
			File originals = new File(file.getParentFile(), dirname);
			if (!originals.exists()) {
				originals.mkdirs();
			}
			log.info("Moving " + file.getAbsolutePath() + " to " + originals.getAbsolutePath());
			file.renameTo(new File(originals, getPrimary()));
		}
		else log.warn(file.getAbsolutePath() + " doesn't exist to move");
	}

	protected Product processAsset() throws MagicException, IOException {
		Product p = createProductObject();
		p.setPrimary(getPrimary());
		p.setDateCataloged(new GregorianCalendar());
		p.setDateModified(new GregorianCalendar());
		p.setProductFamily(family);
		setCategoryValues(p);
		p.setPath(new Path(getPath()));
		try {
			determineImageProperties(p);
		} catch (IOException e) {
			determineImageProperties(p);
		}
		return p;
	}

	protected File getExtensionDirectory(Object ext) {
		return new File(file.getParentFile(), "_" + ext);
	}

	protected void addAllCommands(LinkedList<ConvertCommand> commands,
			Properties conv) throws MagicException {
		for (Entry<Object, Object> entry : conv.entrySet()) {
			commands.add(new ConvertCommand(entry));
		}
	}

	protected void convertImage() throws IOException, MagicException {
		Map<String, File> previousConverts = new HashMap<String, File>();
		LinkedList<ConvertCommand> commands = new LinkedList<ConvertCommand>();
		addAllCommands(commands, conversions);
		if (!skipConversions) {
			ConvertCommand command = null;
			while (commands.size() > 0
					&& (command = commands.removeFirst()) != null) {
				log.debug("Run command " + command);
				File extDir = getExtensionDirectory(command.label);
				extDir.mkdir();
				File converted = new File(extDir,
						getNameWithNewExt(command.fileExtension));
				if (recreateConversions || !converted.exists()) {
					log.debug("recreate conversions = " + recreateConversions
							+ " converted(" + converted.getAbsolutePath()
							+ ") exists: " + converted.exists());
					if (command.reference != null) {
						if (previousConverts.get(command.reference) == null) {
							commands.addLast(command);
							log.debug("Couldn't find pre-existing conversion("
									+ command.reference + ") in map: "
									+ previousConverts + " threadname = "
									+ Thread.currentThread().getName());
							continue;
						} else
							magic.convertImage(previousConverts
									.get(command.reference),command.preCommand,command.command,
									converted);
					} else
						magic.convertImage(file, command.preCommand,command.command, converted);
					log.debug("storing previous conversion "
							+ converted.getAbsolutePath());
					previousConverts.put(command.label, converted);
				} else if (converted.exists()) {
					previousConverts.put(command.label, converted);
				}
			}
		}
	}

	protected String getNameWithNewExt(String ext) {
		return getName() + "." + ext;
	}

	protected void determineImageProperties(Product p) throws IOException,
			MagicException {
		Map<String, Object> imageProps = magic.getMagickProperties(file);
		p.setDateCreated((Calendar)imageProps.get("Creation Date"));
		imageProps.remove("Creation Date");
		for (Object key : imageProps.keySet()) {
			p.removeValue((String) key);
			p.setValue((String) key, imageProps.get(key));
		}
	}

	public void setCategoryValues(Product p) throws MagicException {
		for (Object rawKey : categories.keySet()) {
			String key = ((String) rawKey).replaceAll("_", " ");
			p.removeValue(key);
			String value = categories.getProperty((String) rawKey).trim();
			if (value.matches("(\\.+\\|?)+")) {
				String[] pathElements = getPath().split("/");
				String[] vals = value.split("\\|");
				for(String v : vals)
				{
					int len = v.trim().length();
					if (pathElements.length >= len) {
						value = value.replaceFirst(Pattern.quote(v), pathElements[len - 1]);
					} else
						value = null;
				}
			}
			if (value != null && !"null".equals(value))
			{
				setValues(p,(String)key,value.trim());
			}
			else p.setValue((String)key,"N/A");
		}
		Properties metaDataFile = getMetaDataProperties();
		for(Object key : metaDataFile.keySet())
		{
			p.setValue((String)key,metaDataFile.getProperty((String)key).trim());
		}
		for (Object ext : conversions.keySet()) {
			File extDir = getExtensionDirectory(ext);
			String[] extAndCommand = ((String) conversions.get(ext)).split(":",
					2);
			if (extAndCommand.length != 2)
				throw new MagicException(
						"Bad command format.  Format must be <file-extension>:<ImageMagick conversion options>.",file,null,null);
			File converted = new File(extDir,
					getNameWithNewExt((String) extAndCommand[0]));
			if (converted.exists())
				p.setValue("_"+ ext, extDir.getName() + "/"
						+ converted.getName());
		}
		p.setValue(ORIGINALS, ORIGINALS + "/" + getPrimary());
	}
	
	protected void setValues(Product p,String key,String value)
	{
		String[] values = value.split("\\|");
		for(String v : values)
		{
			p.setValue((String) key, v.trim());
		}
	}
	
	/**
	 * @deprecated Use {@link #getMetaDataProperties()} instead
	 */
	protected Properties getMetaDataFile()
	{
		return getMetaDataProperties();
	}

	protected Properties getMetaDataProperties()
	{
		Properties extraMeta = new Properties();
		File meta = getMetadataFile();
		log.debug("Looking for meta file: " + meta.getAbsolutePath());
		if(meta.exists())
		{
			try {
				extraMeta.load(new FileInputStream(meta));
			} catch(IOException e)
			{
				log.warn("No property file",e);
			}
		}
		return extraMeta;
	}

	protected Product createProductObject() {
		try {
			return productClass.newInstance();
		} catch (Exception e) {
			log.error("Failure to create product object from class: "
					+ productClass);
			return new CommerceProduct();
		}
	}

	public String getPath() {
		if (parent != null)
			return parent.getPath();
		String path = file.getAbsolutePath().substring(pathRoot.length(),
				file.getAbsolutePath().length() - getPrimary().length());
		if (!path.endsWith("/"))
			path = path + "/";
		if (path.length() > 1 && path.startsWith("/"))
			path = path.substring(1);
		return path;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result
				+ ((getName() == null) ? 0 : getName().hashCode());
		result = PRIME * result
				+ ((getPath() == null) ? 0 : getPath().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DigitalAsset))
			return false;
		final DigitalAsset other = (DigitalAsset) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (getPath() == null) {
			if (other.getPath() != null)
				return false;
		} else if (!getPath().equals(other.getPath()))
			return false;
		return true;
	}

	public ProductFamily getFamily() {
		return family;
	}

	@CoinjemaDependency(method = "productFamily",order=CoinjemaDependency.Order.LAST)
	public void setFamily(String family) {
		this.family = dbutil.getProductFamily(family);
		
	}

	public String toString() {
		return "Digital Asset(" + getPath() + ";" + getPrimary() + ")";
	}

	@CoinjemaDependency(method = "conversion")
	public void setConversions(Properties conv) {
		conversions = conv;
	}

	@CoinjemaDependency(method = "category")
	public void setCategories(Properties cats) {
		categories = cats;
	}

	@CoinjemaDependency(method = "productClass", hasDefault = true)
	public void setProductClass(Class<? extends Product> pClass) {
		this.productClass = pClass;
	}

	public void setPathRoot(String pathRoot) {
		this.pathRoot = pathRoot;
	}

	protected void deleteConvertedVersions() {

		File[] conversionDirs = file.getParentFile().getParentFile().listFiles(
				new FileFilter() {
					public boolean accept(File f) {
						return (f.isDirectory() && f.exists()
								&& f.getName().startsWith("_")
								&& !f.getName().equals(ORIGINALS) && !f
								.getName().equals(INVALID));
					}
				});
		for (File dir : conversionDirs) {
			for (File convertedImage : dir.listFiles()) {
				if (convertedImage.exists()
						&& new DigitalAsset("", convertedImage).getName()
								.equals(getName())) {
					convertedImage.delete();
				}
			}
		}
	}

	@CoinjemaDependency(method = "skipConversions", hasDefault = true)
	public void setSkipConversions(boolean skipConversions) {
		this.skipConversions = skipConversions;
	}

	public void setImageMagick(ImageMagick im) {
		magic = im;
	}

	@CoinjemaDependency(type="dbutil")
	public void setDbutil(DatabaseUtilities dbutil) {
		this.dbutil = dbutil;
	}
}