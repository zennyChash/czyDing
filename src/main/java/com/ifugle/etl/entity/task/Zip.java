package com.ifugle.etl.entity.task;

import com.ifugle.etl.entity.base.Task;

public class Zip extends Task{
	private String directoryToZip;
	
	public String getZippedFileName() {
		return zippedFileName;
	}
	public void setZippedFileName(String zippedFileName) {
		this.zippedFileName = zippedFileName;
	}
	private String zippedFileName;
	public String getDirectoryToZip() {
		return directoryToZip;
	}
	public void setDirectoryToZip(String directoryToZip) {
		this.directoryToZip = directoryToZip;
	}
}
