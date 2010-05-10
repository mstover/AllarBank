package com.lazerbank;

public class LazerBankContext {
	
	private int iteration = 0;

	public synchronized int getIteration() {
		return iteration;
	}

	public synchronized void incrIteration() {
		this.iteration = (iteration+1) % 3;
	}
	
	

}
