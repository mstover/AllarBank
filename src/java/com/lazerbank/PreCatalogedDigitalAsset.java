package com.lazerbank;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.GregorianCalendar;

import com.lazerinc.application.Product;
import com.lazerinc.beans.Path;

public class PreCatalogedDigitalAsset extends DigitalAsset {

	public PreCatalogedDigitalAsset(String root, CatalogedFolder directory,
			File f) {
		super(root, directory, f);
		recreateConversions = false;
	}

	@Override
	public void delete() {
		super.delete();
		deleteConvertedVersions();
		if (file.exists())
			file.delete();
	}

	@Override
	protected boolean existingDoesNotContain(Product existing,
			Product cataloged, String key) {
		Collection<Object> values = cataloged.getValues(key);
		boolean ret = false;
		if (values != null) {
			for (Object value : values) {
				if (value.equals("N/A") && values.size() == 1) {
					if (existing.getValue(key) != null
							&& !existing.getValue(key).toString().trim()
									.equals("N/A")) {
						ret = true;
					}
				} else if (existing.getValue(key) == null
						|| !existing.getValues(key).contains(value))
				{
					log.debug("Existing value = " + existing.getValues(key) + " cataloged = " + cataloged.getValues(key));
					ret = true;
				}
			}
		}
		return ret;
	}

	@Override
	protected Product processAsset() throws MagicException, IOException {
		Product p = createProductObject();
		p.setPrimary(getPrimary());
		p.setDateCataloged(new GregorianCalendar());
		p.setDateModified(new GregorianCalendar());
		p.setProductFamily(family);
		setCategoryValues(p);
		p.setPath(new Path(getPath()));
		if (!exists()) {
			try {
				determineImageProperties(p);
			} catch (IOException e) {
				determineImageProperties(p);
			}
		}
		return p;
	}

	@Override
	protected File getExtensionDirectory(Object ext) {
		return new File(file.getParentFile().getParentFile(), "_" + ext);
	}

	@Override
	protected void moveAsset() throws IOException {
	}

	@Override
	void moveBadAsset() throws IOException {
	}

}
