package com.ifugle.etl.entity.base;

public class ETLFile {
	private String colSeparator;
	private String encode = "GBK";
	private String format;
	private String dir;
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getColSeparator() {
		return colSeparator;
	}
	public void setColSeparator(String colSeparator) {
		this.colSeparator = colSeparator;
	}
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
}
