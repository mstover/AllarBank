package com.lazerbank;

import static org.coinjema.context.CoinjemaDependency.Order.LAST;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import strategiclibrary.service.notification.NotificationService;
import strategiclibrary.util.Files;
import strategiclibrary.util.TextFile;

@CoinjemaObject(type = "errors")
public class MagicException extends Exception {
	private static final long serialVersionUID = 1;

	NotificationService email;

	String[] recips;

	File image;

	String message;

	String out;

	String err;

	boolean msgSent = false;

	public MagicException() {
		super();
	}

	public MagicException(String message, File image, InputStream stdout,
			InputStream stderr) {
		super(message);
		this.message = message;
		out = readInput(stdout);
		err = readInput(stderr);
		this.image = image;
	}

	protected String readInput(InputStream in) {
		if (in == null) {
			return "";
		}
		try {
			StringBuffer buf = new StringBuffer();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null)
				buf.append(line).append("\n");
			return buf.toString();
		} catch (IOException e) {
			return "";
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public String getMessage() {
		return message + "\n" + "Standard Out: " + out + "\n\n"
				+ "Standard Error: " + err;
	}

	protected void sendMessage() {
		if (!msgSent && isFatal()) {
			StringWriter sw = new StringWriter();
			printStackTrace(new PrintWriter(sw));
			sw.write("Image in question: " + image.getAbsolutePath());
			email.sendMessage(recips, "LazerBank Bad Image", "text/plain", sw
					.toString());
			logErrorToFile();
			msgSent = true;
		}
	}
	
	protected void logErrorToFile()
	{
		File parent = image.getParentFile();
		if(parent.getName().startsWith("_"))
			parent = parent.getParentFile();
		File invalidDir = new File(parent,DigitalAsset.INVALID);
		invalidDir.mkdir();
		TextFile logFile = new TextFile(invalidDir,Files.hackOffExtension(image) + ".errlog");
		logFile.setText(toString());
	}
	
	protected boolean isFatal()
	{
		return true;
	}

	@CoinjemaDependency(type = "emailService", method = "emailService", order = LAST)
	public void setEmail(NotificationService email) {
		this.email = email;
		if (image != null)
			sendMessage();
	}

	@CoinjemaDependency(method = "recipients")
	public void setRecips(String[] recips) {
		this.recips = recips;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
		sendMessage();
	}

}
