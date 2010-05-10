package com.lazerbank.nontest;

import org.apache.log4j.PropertyConfigurator;
import org.coinjema.context.CoinjemaContext;
import org.coinjema.context.ContextFactory;
import org.coinjema.context.source.FileContextSource;

import strategiclibrary.util.Files;
import junit.framework.TestCase;

public abstract class AbstractTest extends TestCase {

	public AbstractTest(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public void setUp() throws Exception {
		System.out.println("Copying Files for testing");
		Files.deleteTree("test");
		Files.copyFiles("../lazerweb/src/web/web-inf/contexts","test");
		Files.copyFiles("../lazerweb/src/web/web-inf/ormMapping","test/_ormMapping");
		Files.copyFiles("../lazerweb/src/web/web-inf/sqlTemplates","test/_sqlTemplates");
		Files.copyFiles("src/config","test",true);
		Files.copyFiles("src/test/config","test",true);
		PropertyConfigurator.configure("test/logging4j.properties");
		startCoinjema();
	}

	public void startCoinjema() throws Exception {
		ContextFactory.createRootContext(new FileContextSource("test"));
		ContextFactory.createContext("test",new FileContextSource("src/test/"+getTestDir()));
		ContextFactory.pushContext(new CoinjemaContext("test"));
	}

	public void tearDown() throws Exception {
		ContextFactory.destroyContext("");
	}
	
	abstract protected String getTestDir();

}
