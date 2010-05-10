package com.lazerbank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import strategiclibrary.service.sql.ObjectMappingService;

import com.lazerinc.application.Product;
import com.lazerinc.application.SecurityModel;
import com.lazerinc.beans.User;
import com.lazerinc.ecommerce.CommerceUser;
import com.lazerinc.ecommerce.DatabaseUtilities;
import com.lazerinc.ecommerce.ExpiredImage;
import com.lazerinc.ecommerce.ProductFamily;
import com.lazerinc.server.ProductService;
import com.lazerinc.server.UserService;

@CoinjemaObject
public class Repository implements Lazerweb {
	
	ObjectMappingService objectMapper;
	UserService ugd;
	CommerceUser actingUser;
	SecurityModel permissions;
	Logger log;
	DatabaseUtilities dbutil;
	
	public Repository() {
		super();
	}

	private void deleteAllImages(String family,String pathName,Set<DigitalAsset> existingAssets)
	{
		Map values = new HashMap();
		values.put("pathName",pathName);
		values.put("existing",existingAssets);
		values.put("family",family);
		objectMapper.doUpdate("removeAssets.sql", values);
	}
	
	/* (non-Javadoc)
	 * @see com.lazerbank.Lazerweb#getMissingAssets(com.lazerinc.ecommerce.ProductFamily, java.lang.String, java.util.Collection, boolean)
	 */
	public Collection<DigitalAsset> getMissingAssets(ProductFamily family,String pathName,Collection<Asset> existingAssets,boolean searchBlank)
	{
		Map values = new HashMap();
		values.put("pathName",pathName);
		values.put("existing",existingAssets);
		Set<String> paths = new HashSet<String>();
		for(Asset a : existingAssets)
		{
			if(a instanceof DigitalAsset)
				paths.add(((DigitalAsset)a).getPath());
		}
		values.put("paths",paths);
		values.put("family",family.getTableName());
		values.put("searchBlankPaths",searchBlank);
		Collection<DigitalAsset> missing = (Collection<DigitalAsset>)objectMapper.getObjects("getMissingAssets.sql", values);
		return missing;
	}
	
	private void deleteAllPaths(String family,String pathName,Set<CatalogedFolder> existingSubFolders)
	{
		Map values = new HashMap();
		values.put("pathName",pathName);
		values.put("existing",existingSubFolders);
		values.put("family",family);
		objectMapper.doUpdate("removeAssetsInPaths.sql", values);
	}
	
	/* (non-Javadoc)
	 * @see com.lazerbank.Lazerweb#deleteAsset(com.lazerbank.DigitalAsset)
	 */
	public void deleteAsset(DigitalAsset da)
	{
		Product p = getExisting(da.getFamily(),da.getName(),da.getPath());
		p.getProductFamily().deleteProduct(p, permissions);
	}

	User getActingUser() {
		return actingUser;
	}
	
	/* (non-Javadoc)
	 * @see com.lazerbank.Lazerweb#getExisting(com.lazerinc.ecommerce.ProductFamily, java.lang.String, java.lang.String)
	 */
	public Product getExisting(ProductFamily family,String name,String path)
	{
		Product p = family.getProduct(name,path,permissions);
		if(p == null || p instanceof ExpiredImage || p.getId() < 0) return null;
		return p;
	}
	
	/* (non-Javadoc)
	 * @see com.lazerbank.Lazerweb#updateProduct(com.lazerinc.ecommerce.Product)
	 */
	public void updateProduct(Product existing)
	{
		log.debug("Physically updating product in database " + existing.getPrimary());
		existing.getProductFamily().updateProduct(existing);
	}
	
	/* (non-Javadoc)
	 * @see com.lazerbank.Lazerweb#createProduct(com.lazerinc.ecommerce.Product)
	 */
	public void createProduct(Product newProduct)
	{
		log.debug("Creating product physically in database product family = "+newProduct.getProductFamilyName());
		newProduct.setProductFamily(dbutil.getProductFamily(newProduct.getProductFamilyName()));
		newProduct.getProductFamily().addProduct(newProduct);
	}

	
	@CoinjemaDependency(method="actingUser",order=CoinjemaDependency.Order.LAST)
	public void setActingUser(String actingUserName) {
		log.debug("Setting ugd");
		this.actingUser = ugd.getUser(actingUserName);
		permissions = ugd.getSecurity(actingUser);
		log.debug("user = " + actingUser.getUsername());
	}

	protected Logger getLog() {
		return log;
	}

	
	@CoinjemaDependency(alias="log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

	UserService getUgd() {
		return ugd;
	}

	
	@CoinjemaDependency(type="userService")
	public void setUgd(UserService ugd) {
		this.ugd = ugd;
	}

	ObjectMappingService getObjectMapper() {
		return objectMapper;
	}

	@CoinjemaDependency(method="mappingService",type="objectMappingService")
	public void setObjectMapper(ObjectMappingService objectMapper) {
		this.objectMapper = objectMapper;
	}

	@CoinjemaDependency(type="dbutil")
	public void setDbutil(DatabaseUtilities dbutil) {
		this.dbutil = dbutil;
	}

}
