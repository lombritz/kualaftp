package org.kualaftp.config;

public class Config {

	private String serverHost;
	private int serverPort;
	private boolean passive;
	
	public Config() {
		serverHost = "127.0.0.1";
		serverPort = 21;
		passive = true;
	}
	
	public Config(String serverHost, int serverPort, boolean pasv) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.passive = pasv;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean isPassive() {
		return passive;
	}

	public void setPassive(boolean passive) {
		this.passive = passive;
	}
	
	
}
