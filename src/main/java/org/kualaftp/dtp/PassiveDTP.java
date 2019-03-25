package org.kualaftp.dtp;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.kualaftp.PI;

/**
 * Base class that represents a Passive Data Transfer Process.
 * 
 * @author jaime
 */
public abstract class PassiveDTP implements Runnable {
	PI pi = null;
	Socket dataSocket = null;
	Lock lock = null;
	boolean done = false;
	
	public PassiveDTP(PI pi, String host, int port) throws IOException {
		this.pi = pi;
		this.lock = new ReentrantLock();
		dataSocket = new Socket(host, port);
	}
	
	@Override
	public void run() {
		try {
			lock.lock();
			handleTransfer();
			lock.unlock();
			done = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isDone() {
		return done;
	}
	
	public Lock getLock() {
		return lock;
	}
	
	public abstract void handleTransfer() throws IOException ;
	
}
