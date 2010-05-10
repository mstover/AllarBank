package com.lazerbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;
import org.coinjema.context.CoinjemaDependency;
import org.coinjema.context.CoinjemaObject;
import org.coinjema.util.Functor;

import strategiclibrary.util.Files;

import com.lazerinc.ecommerce.ProductFamily;

/**
 * The Catalog stores all assets to be added and deleted from the folder
 * structure in order to determine when an asset has been moved ( and not
 * removed and added).
 * 
 * @author mikes
 * 
 */
@CoinjemaObject
public class Catalog {

	MultiMap removed = MultiValueMap.decorate(new HashMap(), new Factory() {
		public Object create() {
			return new HashSet();
		}
	});

	Set<DigitalAsset> added = new HashSet<DigitalAsset>();

	Set<DigitalAsset> update = new HashSet<DigitalAsset>();

	List<Asset> existing = new ArrayList<Asset>();

	Map<DigitalAsset, DigitalAsset> moved;

	boolean live;

	ProductFamily family;

	ThreadPoolExecutor threadPool;

	public Catalog(ProductFamily family) {
		super();
		live = true;
		this.family = family;
	}

	private Logger log;

	public void forAddition(DigitalAsset da) {
		if (added.contains(da)) {
			if (!update.contains(da))
			{
				addIfMoreOriginal(da, added, null);
				log.debug("Already added, checking for most original " + added);
			}
			else
			{
				chooseMostOriginal(da, update, added);
				log.debug("Already added and updated, checking for most original");
			}
		} else if (update.contains(da)) {
			addIfMoreOriginal(da, update, update);
			log.debug("Already updated, checking for most original");
		} else
			added.add(da);
	}

	public void forRemoval(DigitalAsset da) {
		removed.put(da.getName(), da);
	}

	public void addExisting(Asset asset) {
		existing.add(asset);
	}

	public List<Asset> getExisting() {
		return existing;
	}

	public void forUpdate(DigitalAsset da) {
		if (update.contains(da)) {
			if (!added.contains(da))
				addIfMoreOriginal(da, update, null);
			else
				chooseMostOriginal(da, update, added);
		} else if (added.contains(da)) {
			addIfMoreOriginal(da, added, update);
		} else
			update.add(da);
	}

	private void chooseMostOriginal(DigitalAsset newAsset,
			Set<DigitalAsset> primary, Set<DigitalAsset> secondary) {
		DigitalAsset primaryAsset = findAsset(newAsset, primary);
		DigitalAsset secondaryAsset = findAsset(newAsset, secondary);
		if (compareOriginality(newAsset, primaryAsset) < 0) {
			if (compareOriginality(newAsset, secondaryAsset) < 0) {
				primary.remove(newAsset);
				primary.add(newAsset);
				secondary.remove(secondaryAsset);
			} else {
				primary.remove(primaryAsset);
				secondary.remove(secondaryAsset);
				primary.add(secondaryAsset);
			}
		} else if (compareOriginality(primaryAsset, secondaryAsset) < 0) {
			secondary.remove(secondaryAsset);
		} else
		{
			primary.remove(primaryAsset);
			secondary.remove(secondaryAsset);
			primary.add(secondaryAsset);
		}
	}

	private int compareOriginality(DigitalAsset one, DigitalAsset two) {
		String oneExt = Files.getExtension(one.getPrimary());
		String twoExt = Files.getExtension(two.getPrimary());
		if(oneExt.equals(twoExt))
		{
			if(one instanceof PreCatalogedDigitalAsset && !(two instanceof PreCatalogedDigitalAsset)) return 1;
			else if(!(one instanceof PreCatalogedDigitalAsset) && two instanceof PreCatalogedDigitalAsset) return -1;
			else return -1;
		}
		else if (oneExt.equals("tif")) {
			return -1;
		} else if (oneExt.equals("eps") && !twoExt.equals("tif")) {
			return -1;
		} else if (oneExt.equals("pdf") && !twoExt.equals("tif") && !twoExt.equals("eps")) {
			return -1;
		} else if (twoExt.equals("jpg")) {
			return -1;
		}
		return 1;
	}

	private DigitalAsset findAsset(DigitalAsset newAsset,
			Set<DigitalAsset> primary) {
		for (DigitalAsset pa : primary) {
			if (pa.equals(newAsset)) {
				return pa;
			}
		}
		return newAsset;
	}

	private void addIfMoreOriginal(DigitalAsset da, Set<DigitalAsset> from,
			Set<DigitalAsset> to) {
		log.debug("Comparing " + da);
		for (DigitalAsset prev : new LinkedList<DigitalAsset>(from)) {
			if (prev.equals(da)) {
				if (compareOriginality(da, prev) < 0) {
					log.debug("is more original");
					if (to == null)
					{
						from.remove(da);
						from.add(da);
					}
					else {
						from.remove(da);
						to.add(da);
					}

				}
				else if(to != null)
				{
					from.remove(prev);
					to.add(prev);
				}
			}
		}
	}

	public Set<DigitalAsset> getAdded() {
		return added;
	}

	public Collection<DigitalAsset> getRemoved() {
		return (Collection<DigitalAsset>) removed.values();
	}

	public Set<DigitalAsset> getUpdated() {
		return update;
	}

	public Map<DigitalAsset, DigitalAsset> getMovedAssets() {
		Set<DigitalAsset> copy = new HashSet<DigitalAsset>(added);
		moved = new HashMap<DigitalAsset, DigitalAsset>();
		for (DigitalAsset a : copy) {
			if (removed.containsKey(a.getName())
					&& ((Collection) removed.get(a.getName())).size() == 1) {
				log.debug("Moved asset = " + a);
				moved.put(
						(DigitalAsset) ((Collection) removed.get(a.getName()))
								.iterator().next(), a);
				removed.remove(a.getName());
				added.remove(a);
			}
		}
		return moved;
	}

	void updateAssets() {
		for (DigitalAsset updated : getUpdated()) {
			if (live)
				threadPool.submit((Runnable) new Functor(updated, "update"));
		}
	}

	public boolean isFinished() throws ExecutionException {
		log.debug("tasks remaining = " + threadPool.getTaskCount());
		log.debug("Tasks Done: " + threadPool.getCompletedTaskCount());
		log.debug("Current threads: " + threadPool.getPoolSize());
		log.debug("Active threads " + threadPool.getActiveCount());
		return threadPool.getTaskCount() - threadPool.getCompletedTaskCount() <= 0;
	}

	public void clear() {
		moved = null;
		update.clear();
		removed.clear();
		added.clear();
		existing.clear();
	}

	void processCollectedAssets() {
		Map<DigitalAsset, DigitalAsset> moved = getMovedAssets();
		for (DigitalAsset remove : getRemoved()) {
			if (live)
				threadPool.submit((Runnable) new Functor(remove, "delete"));
		}
		addNew();
		moveAssets(moved);
		updateAssets();
	}

	void moveAssets(Map<DigitalAsset, DigitalAsset> moved) {
		for (DigitalAsset move : moved.keySet()) {
			if (live)
				threadPool.submit((Runnable) new Functor(move, "moveTo", moved
						.get(move)));
		}
	}

	void addNew() {
		for (DigitalAsset added : getAdded()) {
			if (live)
				threadPool.submit((Runnable) new Functor(added, "add"));
		}
	}

	@CoinjemaDependency(alias = "log4j")
	public void setLog(Logger log) {
		this.log = log;
	}

	@CoinjemaDependency(alias = "live", hasDefault = true)
	public void setLive(boolean live) {
		this.live = live;
	}

	@CoinjemaDependency(method = "threadPool", type = "threadPool")
	public void setExecutorService(ThreadPoolExecutor es) {
		threadPool = es;
	}

	public void waitFor() {
		try {

			while (!isFinished())
				Thread.sleep(6000);
		} catch (InterruptedException e) {
			log.warn("Thread interrupted");
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			log.error("Error while cataloging", e);
		}
		if (family != null && family.isDirty()) {
			new FamilyRefresher(family).refresh();
			family.setDirty(false);
		}
	}

}
