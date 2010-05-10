package com.lazerbank;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;

import com.lazerinc.application.Product;
import com.lazerinc.ecommerce.ProductFamily;

public interface Lazerweb {

	public abstract Collection<DigitalAsset> getMissingAssets(
			ProductFamily family, String pathName,
			Collection<Asset> existingAssets, boolean searchBlank);

	public abstract void deleteAsset(DigitalAsset da);

	public abstract Product getExisting(ProductFamily family, String name,
			String path);

	public abstract void updateProduct(Product existing);

	public abstract void createProduct(Product newProduct);

}