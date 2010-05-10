package com.lazerbank;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import com.lazerinc.ecommerce.CommerceUser;
import com.lazerinc.ecommerce.ProductFamily;
import com.lazerinc.server.UserService;

@CoinjemaObject(type="lazerweb")
public class FamilyRefresher {
	ProductFamily family;
	UserService ugd;
	CommerceUser actingUser;
	String url;
	Logger log;
	
	public FamilyRefresher(ProductFamily family)
	{
		this.family = family;
	}
	
	public void refresh()
	{
		try {
		URL tempURL = new URL(url + URLEncoder.encode(family.getTableName(),"utf-8"));
		log.debug("refreshing url = " + tempURL.toString());
		URLConnection connect = tempURL.openConnection();
		connect.connect();
		log.info("Refreshed family " + family.getDescriptiveName());
		connect.getInputStream().close();
		}catch(Exception e)
		{
			log.error("Failure to update lazerweb",e);
		}
	}
	
	@CoinjemaDependency(type="userService")
	public void setUserService(UserService u)
	{
		ugd = u;
	}
	
	@CoinjemaDependency(method="url")
	public void setLazerwebUrl(String url)
	{
		this.url = url;
	}
	
	@CoinjemaDependency(method="actingUser",order=CoinjemaDependency.Order.LAST)
	public void setActingUser(String actingUserName) {
		this.actingUser = ugd.getUser(actingUserName);
	}
	
	@CoinjemaDependency(alias="log4j")
	public void setLogger(Logger l)
	{
		log = l;
	}

}
