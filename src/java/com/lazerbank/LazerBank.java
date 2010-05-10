package com.lazerbank;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.coinjema.context.CoinjemaContext;
import org.coinjema.context.CoinjemaContextTrack;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaDynamic;
import org.coinjema.context.CoinjemaObject;
import org.coinjema.context.ContextFactory;
import org.coinjema.context.source.FileContextSource;

import strategiclibrary.service.notification.NotificationService;
import strategiclibrary.util.TimeConstants;

import com.lazerbank.upload.PendingUploads;
import com.lazerbank.upload.UploadFolder;
import com.lazerbank.upload.UploadPackage;

@CoinjemaObject
@CoinjemaContextTrack
public class LazerBank {
	Logger log;

	File[] baseDirs;

	NotificationService email;

	String[] recips;

	boolean running = true;

	int runLength = -1;

	int count = 0;
	long miniDelay = 60000L;

	LazerBankContext context;

	public void run() {
		log.debug("Start running");
		int delayCount = (int)(getDelay() / miniDelay);
		int countToCatalog = delayCount;
		try {
			while (running && (runLength < 0 || count < runLength)) {
				try {
					if(checkUploads() || countToCatalog >= delayCount)
					{
						catalogLibraries();
						countToCatalog = 0;
					}
					else countToCatalog++;
				} catch (Throwable e) {
					log.error("Error while cataloging", e);
					break;
				}
				try {
					Thread.sleep(miniDelay);
				} catch (Throwable e) {
					log.fatal("Main thread was interrupted", e);
					break;
				}
				context.incrIteration();
				count++;
				log.debug("finished iteration");
			}
		} finally {
			email.sendMessage(recips, "LazerBank shut down", "text/plain",
					"LazerBank has shutdown");
		}
		count = 0;
		log.debug("Stop running");

	}

	private boolean checkUploads() {
		PendingUploads uploadPackages = new PendingUploads();
		boolean action = false;
		for (UploadPackage pack : uploadPackages) {
			log.info("Examining upload package: " + pack.getMeta());
			int dirCount = 1;
			UploadFolder match = createMatchFromPath(pack);
			if (match == null) {
				for (File dir : baseDirs) {
					log.info("Looking for upload place in "
							+ dir.getAbsolutePath());
					UploadFolder top = new UploadFolder(dir,
							new CoinjemaContext("assets" + dirCount));
					match = top.findMatch(pack.getProductFamily(), pack
							.getMeta(), 0.1F);
					log.info("Looking for match");
					if (match != null) {
						log.info("Found match");
						break;

					}
					dirCount++;
				}
			}
			if (match != null) {
				try {
					boolean b = match.copyFiles(pack);
					action = action || b;
					pack.delete();
				} catch (Exception e) {
					log.error("Failed to transfer upload package named "
							+ pack.getName(), e);
					try { pack.delete();
					} catch(Exception err){
						log.error("Failed to delete file " + pack.getName(),err);}
				}
			} else
			{
				try { pack.delete();
				} catch(Exception err){
					log.error("Failed to delete file " + pack.getName(),err);}
				log.info("Didn't find match");
			}

		}
		return action;
	}

	private UploadFolder createMatchFromPath(UploadPackage pack) {
		int dirCount = 1;
		if (!pack.getMeta().containsKey("path"))
			return null;
		else {
			String path = pack.getMeta().getProperty("path");
			String family = pack.getProductFamily();
			File basedir = null;
			for (File dir : baseDirs) {
				UploadFolder top = new UploadFolder(dir, new CoinjemaContext(
						"assets" + dirCount)).findFamilyMatch(family);
				if (top != null)
				{
					basedir = top.getDirectory();
					break;
				}
				dirCount++;
			}
			if (basedir == null)
				return null;
			else {
				if(path.startsWith("/")) path = path.substring(1);
				File f = new File(basedir, path);
				f.mkdirs();
				return new UploadFolder(f,
						new CoinjemaContext("assets" + dirCount));
			}
		}
	}

	private void catalogLibraries() {
		int dirCount = 1;
		for (File dir : baseDirs) {
			Catalog finished = new CatalogedFolder(dir, new CoinjemaContext(
					"assets" + dirCount)).catalog();
			finished.waitFor();
			dirCount++;
		}
	}

	@CoinjemaDependency(method="runs",hasDefault=true)
	public void setRunLength(int runLength) {
		this.runLength = runLength;
	}

	public LazerBank(File[] baseDir) {
		this.baseDirs = baseDir;
	}

	public LazerBank(File[] baseDir, CoinjemaContext cc) {
		this.baseDirs = baseDir;
	}

	@CoinjemaDynamic(method = "delay")
	public long getDelay() {
		return TimeConstants.MINUTE * 10L;
	}

	/**
	 * Arg #1: optional. Give directory to catalog. Arg #2: optional. First
	 * argument provides directory with base configuration. Second argument is
	 * directory to catalog.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LazerBank masterCat = null;
		try {
			if (args == null || args.length == 0) {
				ContextFactory.createRootContext();
				System.out.println("Creating context in current direcotyr = "
						+ System.getProperty("user.dir"));
				PropertyConfigurator.configure("logging4j.properties");
			} else {
				ContextFactory
						.createRootContext(new FileContextSource(args[0]));
				PropertyConfigurator.configure(args[0]
						+ "/logging4j.properties");
			}
			if (args == null || args.length == 0) {
				masterCat = new LazerBank(new File[] { new File("") });
			} else if (args.length == 1) {
				masterCat = new LazerBank(new File[] { new File(args[0]) });
			} else {
				File[] dirs = new File[args.length - 1];
				for (int x = 1; x < args.length; x++) {
					System.out
							.println("Creating asset directory in " + args[x]);
					ContextFactory.createContext("assets" + x,
							new FileContextSource(args[x]));
					dirs[x - 1] = new File(args[x]);
				}
				masterCat = new LazerBank(dirs);
			}
			masterCat.run();
		} catch (Exception e) {
			System.out.println("Failure to startup");
			throw new RuntimeException(e);
		}
	}

	@CoinjemaDependency(alias = "log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

	@CoinjemaDependency(type = "emailService", method = "emailService")
	public void setEmail(NotificationService email) {
		this.email = email;
	}

	@CoinjemaDependency(method = "recipients")
	public void setRecips(String[] recips) {
		this.recips = recips;
	}

	@CoinjemaDependency(type = "lazerBankContext")
	public void setContext(LazerBankContext context) {
		this.context = context;
	}

}
