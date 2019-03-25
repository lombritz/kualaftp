package org.kualaftp.dtp;

import java.util.Date;

public class ListFileWrapper {
	String permissions;
	long size;
	Date date;
	String name;
	boolean directory;
	
	public ListFileWrapper() {
		// TODO Auto-generated constructor stub
	}
	
	public String getPermissions() {
		return permissions;
	}
	
	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
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
	
	@Override
	public String toString() {
		return name+" "+permissions+" "+size;
	}
}