package org.kualaftp.util;

public class FtpFile {

	private String name;
	private boolean directory;
	
	public FtpFile() {
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDirectory() {
		return directory;
	}
	
	public void setDirectory(boolean directory) {
		this.directory = directory;
	}
	
}
