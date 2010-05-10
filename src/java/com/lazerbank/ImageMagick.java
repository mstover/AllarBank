package com.lazerbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;

import strategiclibrary.util.Converter;
import strategiclibrary.util.Files;

@CoinjemaObject
public class ImageMagick {

	Pattern colorTypePat = Pattern.compile("Colorspace:(.*)");

	Pattern geometryPat = Pattern.compile("Geometry:(.*)");

	Pattern unitsPat = Pattern.compile("Units:(.*)");

	Pattern resolutionPat = Pattern.compile("Resolution:(.*)");

	Pattern fileTypePat = Pattern.compile("Format:(.*)");

	Pattern fileSizePat = Pattern.compile("Filesize:(.*)");

	Pattern timestampPat = Pattern
			.compile("Timestamp:\\s*(\\d\\d\\d\\d:\\d\\d:\\d\\d)");

	Pattern createDatePat = Pattern.compile(Pattern.quote("<xap:CreateDate>")
			+ "(\\d\\d\\d\\d-\\d\\d-\\d\\d)[^<]*"
			+ Pattern.quote("</xap:CreateDate>"));

	String executablePath = "";

	long[] conversionTimeout = { 30 * 1000, 120 * 1000, 20 * 60 * 1000 };

	Logger log;

	LazerBankContext context;

	@CoinjemaDependency(type = "lazerBankContext")
	public void setContext(LazerBankContext context) {
		this.context = context;
	}

	public Map<String, Object> getMagickProperties(File image)
			throws IOException, MagicException {
		if (!image.exists())
			throw new NoSuchImageException("Couldn't find image", image, null,
					null);
		ProcessBuilder pb = new ProcessBuilder(executablePath + "identify",
				"-verbose",/*
							 * "File_Size:%b\\nHeight:%h\\nWidth:%w\\nResolution-x:%x\\nResolution-y:%y\\nColor_Type:%r\\nFile_Type:%m\\n",
							 */
				image.getName());
		pb.directory(image.getAbsoluteFile().getParentFile());
		Process process = pb.start();
		try {
			StringBuffer data = new StringBuffer();
			StringBuffer xap = new StringBuffer();
			readInto(data, xap, new BufferedReader(new InputStreamReader(
					process.getInputStream())), process);
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				throw new IOException("Process was interrupted");
			}
			if (process.exitValue() != 0 || data == null || data.length() < 7) {
				log.error("identify exitvalue = " + process.exitValue()
						+ " props = " + data);
				throw new BadImageFileException(
						"Failed to correctly identify file: ", image, null,
						process.getErrorStream());
			}
			try {
				Map<String, Object> imageProperties = processRawProperties(
						image, data, xap);
				return imageProperties;
			} catch (Exception e) {
				log.error("Image Properties = " + data, e);
				throw new BadImageFileException(
						"Failed to correctly identify file: ", image, null,
						process.getErrorStream());
			}
		} finally {
			closeProcess(process);
		}
	}

	private String readLineFromProcess(BufferedReader stream, Process p)
			throws IOException {
		String line = stream.readLine();
		while (line == null) {
			try {
				int exit = p.exitValue();
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(2000);
					line = stream.readLine();
				} catch (InterruptedException err) {
					throw new IOException("Process was interrupted");
				}
			}
		}
		return line;
	}

	private void readInto(StringBuffer buf, StringBuffer xap,
			BufferedReader stream, Process p) throws IOException {
		String line = readLineFromProcess(stream, p);
		while (line != null && line.indexOf("<") == -1) {
			buf.append(line).append("\n");
			line = stream.readLine();
		}
		while (line != null && line.indexOf("<xap:") == -1)
			line = stream.readLine();
		while (line != null) {
			xap.append(line).append("\n");
			line = stream.readLine();
		}
	}

	private void closeProcess(Process process) {
		try {
			process.getErrorStream().close();
		} catch (Exception e) {
		}
		try {
			process.getInputStream().close();
		} catch (Exception e) {
		}
		try {
			process.getOutputStream().close();
		} catch (Exception e) {
		}
	}

	/**
	 * This is intensive enough to disallow multiple threads from running.
	 * 
	 * @param src
	 * @param command
	 * @param dest
	 * @throws MagicException
	 * @throws IOException
	 */
	public void convertImage(File src, String preCommand, String command,
			File dest) throws MagicException, IOException {
		if (!src.exists())
			throw new NoSuchImageException("Couldn't find image", src, null,
					null);
		List<String> commandArgs = new LinkedList<String>();
		commandArgs.add(executablePath + "convert");
		if (preCommand != null && preCommand.length() > 0)
			for (String com : preCommand.split("\\s;;\\s"))
				commandArgs.add(com);
		commandArgs.add(src.getName());
		if (command != null && command.length() > 0)
			for (String com : command.split("\\s;;\\s"))
				commandArgs.add(com);
		else if (Files.getExtension(src).equals(Files.getExtension(dest))) {
			Files.copyFile(src, dest);
			return;
		}
		commandArgs.add(dest.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(commandArgs);
		pb.directory(src.getAbsoluteFile().getParentFile());
		log.info("Executing conversion: " + pb.command());
		Process process = pb.start();
		try {
			try {
				waitForProcess(process);
			} catch (MagicException e) {
				e.setImage(src);
				throw e;
			}
			checkForAndRenameMultiples(dest);
			if (!dest.exists()) {
				throw new BadConversionException("Error while converting file",
						src, process.getInputStream(), process.getErrorStream());
			}
		} finally {
			closeProcess(process);
		}
	}

	private boolean checkForAndRenameMultiples(File dest) {
		String name = Files.hackOffExtension(dest.getName());
		name = name + "-0." + Files.getExtension(dest);
		File multFile = new File(dest.getParentFile(), name);
		log.debug("Looking for mult file " + multFile.getAbsolutePath());
		if (multFile.exists()) {
			multFile.renameTo(dest);
			removeExcessMultFiles(dest);
			return true;
		} else
			return false;
	}

	private void removeExcessMultFiles(File dest) {
		int count = 1;
		String name = Files.hackOffExtension(dest.getName());
		String newName = name + "-" + (count++) + "."
				+ Files.getExtension(dest);
		File multFile = new File(dest.getParentFile(), newName);
		while (multFile.exists()) {
			multFile.delete();
			newName = name + "-" + (count++) + "." + Files.getExtension(dest);
			multFile = new File(dest.getParentFile(), newName);
		}
	}

	private int waitForProcess(Process process) throws IOException,
			BadConversionException {
		long time = System.currentTimeMillis();
		boolean running = true;
		while (running) {
			try {
				return process.exitValue();
			} catch (IllegalThreadStateException e) {
				if (System.currentTimeMillis() - time > conversionTimeout[context
						.getIteration()]) {
					process.destroy();
					try {
						process.waitFor();
					} catch (InterruptedException er) {
						Thread.currentThread().interrupt();
					}
					if (context.getIteration() == 3)
						throw new BadConversionException(
								"Conversion taking too long ("
										+ conversionTimeout[context
												.getIteration()]
										+ "), please convert manually", null,
								process.getInputStream(), process
										.getErrorStream());
					else
						throw new ConversionTimeoutException(
								"Conversion took too long ("
										+ conversionTimeout[context
												.getIteration()] + ")", null,
								process.getInputStream(), process
										.getErrorStream());

				}
				try {
					Thread.sleep((long) (conversionTimeout[context
							.getIteration()] / 60));
				} catch (InterruptedException er) {
					process.destroy();
					Thread.currentThread().interrupt();
					throw new IOException("Process was interrupted");
				}
			}
		}
		return -1;
	}

	private Map<String, Object> processRawProperties(File image,
			StringBuffer data, StringBuffer xap) {
		String rawData = data.toString();
		String colorspace = getMatch(colorTypePat.matcher(rawData.subSequence(
				0, rawData.length())));
		Map<String, Object> imageProperties = new HashMap<String, Object>();
		imageProperties.put("Color Type", colorspace);
		String geometry = getMatch(geometryPat.matcher(rawData.subSequence(0,
				rawData.length())));
		int index1 = geometry.indexOf("x");
		int index2 = geometry.indexOf("+");
		if (index2 == -1)
			index2 = geometry.length();
		imageProperties.put("Width", Converter.getDouble(geometry.substring(0,
				index1)));
		imageProperties.put("Height", Converter.getDouble(geometry.substring(
				index1 + 1, index2)));
		String units = getMatch(unitsPat.matcher(rawData.subSequence(0, rawData
				.length())));
		String resolution = getMatch(resolutionPat.matcher(rawData.subSequence(
				0, rawData.length())));
		resolution = resolution.substring(0, resolution.indexOf("x"));
		if (units.equals("PixelsPerCentimeter")) {
			imageProperties.put("Resolution",
					Converter.getDouble(resolution) * 2.54D);
		} else
			imageProperties.put("Resolution", Converter.getDouble(resolution));
		String format = getMatch(fileTypePat.matcher(rawData.subSequence(0,
				rawData.length())));
		format = format.substring(0, format.indexOf(" "));
		if (format.equals("PS"))
			format = "EPS";
		imageProperties.put("File Type", format);
		String size = getMatch(fileSizePat.matcher(rawData.subSequence(0,
				rawData.length())));
		if (size.endsWith("kb")) {
			imageProperties.put("File Size", Converter.getDouble(size
					.substring(0, size.indexOf("kb")), 0) * 1024);
		} else if (size.endsWith("mb"))
			imageProperties.put("File Size", Converter.getDouble(size
					.substring(0, size.indexOf("mb")), 0) * 1024 * 1024);
		else
			imageProperties.put("File Size", Converter.getDouble(size
					.substring(0, size.indexOf("kb")), 0));
		String xapData = xap.toString();
		String createDate = findCreationDate(rawData, xapData);
		imageProperties.put("Creation Date", Converter.getCalendar(createDate,null));
		return imageProperties;
	}

	private String findCreationDate(String main, String xap) {
		String date = getMatch(createDatePat.matcher(xap.subSequence(0, xap
				.length())));
		if (date != null && date.length() > 0)
			return date;
		else {
			String timestamp = getMatch(timestampPat.matcher(main.subSequence(
					0, main.length())));
			if (timestamp != null && timestamp.length() > 0) {
				int index = timestamp.indexOf(" ");
				if (index > -1)
					timestamp = timestamp.substring(0, index);
				timestamp = timestamp.replace(':', '-');
				return timestamp;
			}
		}
		return null;
	}

	private String getMatch(Matcher match) {
		if (match.find())
			return match.group(1).trim();
		else
			return "";
	}

	@CoinjemaDependency(method = "magicPath", hasDefault = true)
	public void setExecutable(String path) {
		executablePath = path;
	}

	@CoinjemaDependency(alias = "log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

	@CoinjemaDependency(method = "timeout", hasDefault = true)
	public void setConversionTimeout(long[] conversionTimeout) {
		this.conversionTimeout = conversionTimeout;
	}

}
