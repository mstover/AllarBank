package com.lazerbank;

import java.io.File;

import org.coinjema.context.CoinjemaContext;

import com.lazerbank.nontest.AbstractTest;

public class TestCatalog extends AbstractTest {

	public TestCatalog(String arg0) {
		super(arg0);
	}
	
	protected String getTestDir()
	{
		return "test_2";
	}
	
	public void testDupOrig() throws Exception
	{
		CatalogedFolder cf = new CatalogedFolder(new File(System.getProperty("user.dir")+"/src/test/test_2/assets"),new CoinjemaContext("test/assets"));
		cf.catalog();
	}

}
