package com.lazerbank;

import java.util.Properties;

import org.coinjema.context.CoinjemaContext;
import org.coinjema.context.ContextFactory;
import org.coinjema.context.source.FileContextSource;

import junit.framework.TestCase;

public class TestCommandCompiler extends TestCase {

	public TestCommandCompiler(String arg0) {
		super(arg0);
	}
	
	public void setUp() throws Exception
	{

		ContextFactory.createRootContext(new FileContextSource("test"));
		ContextFactory.createContext("test",new FileContextSource("src/test/"+getTestDir()));
		ContextFactory.pushContext(new CoinjemaContext("test"));
	}
	
	protected String getTestDir()
	{
		return "test_2";
	}
	
	public void testPreCommand() throws Exception
	{
		Properties props = new Properties();
		props.setProperty("jpg","jpg:(-density ;; 300) -resample ;; 150");
		ConvertCommand cc = new ConvertCommand(props.entrySet().iterator().next());
		assertEquals("-density ;; 300",cc.preCommand);
		assertEquals("-resample ;; 150",cc.command);
		
		props = new Properties();
		props.setProperty("jpg","jpg:@png@ -resample ;; 150");
		cc = new ConvertCommand(props.entrySet().iterator().next());
		assertNull(cc.preCommand);
		assertEquals("-resample ;; 150",cc.command);

		props = new Properties();
		props.setProperty("jpg","jpg:@png@(-density ;; 300) -resample ;; 150");
		cc = new ConvertCommand(props.entrySet().iterator().next());
		assertEquals("-density ;; 300",cc.preCommand);
		assertEquals("-resample ;; 150",cc.command);
	}

}
