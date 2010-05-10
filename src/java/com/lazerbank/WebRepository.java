package com.lazerbank;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import com.lazerinc.application.Product;
import com.lazerinc.ecommerce.ProductFamily;

@CoinjemaObject
public class WebRepository implements Lazerweb {
	
	Logger log;

	public WebRepository() {
	}

	public void createProduct(Product newProduct) {
		// TODO Auto-generated method stub
		
	}

	public void deleteAsset(DigitalAsset da) {
		// TODO Auto-generated method stub
		
	}

	public Product getExisting(ProductFamily family, String name, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<DigitalAsset> getMissingAssets(ProductFamily family, String pathName, Collection<Asset> existingAssets, boolean searchBlank) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateProduct(Product existing) {
		// TODO Auto-generated method stub
		
	}
	@CoinjemaDependency(alias="log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

}
