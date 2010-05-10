package com.lazerbank;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

@CoinjemaObject
public class ConvertCommand {
	static Pattern tokenPattern = Pattern.compile("@([^@]+)@");
	static Pattern preCommandPattern = Pattern.compile("\\(([^)]+)\\)");
	String label;
	String command;
	String preCommand;
	String fileExtension;
	String reference;
	Logger log;
	
	public ConvertCommand(Entry<Object,Object> propEntry) throws MagicException
	{
		label = (String)propEntry.getKey();
		parseCommand((String)propEntry.getValue());
	}
	
	/** 
	 * Will modify the command part of the string array after parsing any references out of it.
	 * @param extAndCommand
	 * @return
	 */
	private void parseCommand(String extAndCommand) throws MagicException
	{
		String[] tokens = extAndCommand.split(":",2);
		if (tokens.length != 2)
			throw new MagicException(
					"Bad command format.  Format must be <file-extension>:<ImageMagick conversion options>.",null,null,null);
		fileExtension = tokens[0];
		command = tokens[1];
		Matcher match = tokenPattern.matcher(command);
		if(match.find())
		{
			command = command.substring(match.group().length()).trim();
			reference =  match.group(1);
		}
		match = preCommandPattern.matcher(command);
		if(match.find())
		{
			preCommand = match.group(1);
			command = command.substring(match.group().length()).trim();
		}
		//extAndCommand[1].m
	}
	
	public String toString()
	{
		return "Conversion( " + label+ ", " + fileExtension + ", " + command + ", " + reference + ")";
	}
	
	@CoinjemaDependency(alias="log4j",hasDefault=true)
	public void setLogger(Logger l)
	{
		log = l;
	}

}
