package com.ifugle.etl.entity.task;

import com.ifugle.etl.entity.base.Task;

public class Unzip extends Task{
	private String fileToUnzip;
	private String storedPath;
	
	public String getFileToUnzip() {
		return fileToUnzip;
	}
	public void setFileToUnzip(String fileToUnzip) {
		this.fileToUnzip = fileToUnzip;
	}
	public String getStoredPath() {
		return storedPath;
	}
	public void setStoredPath(String storedPath) {
		this.storedPath = storedPath;
	}
}
