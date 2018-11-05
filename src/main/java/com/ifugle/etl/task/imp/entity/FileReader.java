package com.ifugle.etl.task.imp.entity;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileReader {
	private static Logger log = LoggerFactory.getLogger(FileReader.class);
	//源文件目录
	private String dir;
	//文件总数
	private int dataFileCount;
	//保存编码
	private String encode = "GBK";
	private String colSeparator="|";
	@SuppressWarnings("unchecked")
	public List readFile(String fileName) throws Exception{
		List rows = null;
		try {
			InputStream is = new FileInputStream(dir+fileName) ;
			InputStreamReader isr = new InputStreamReader(is, encode); 
			BufferedReader reader = new BufferedReader(isr);
			String line = "";
			rows = new ArrayList();
			while( (line = reader.readLine()) != null ) {
				String[] values = StringUtils.split(line,colSeparator);
				rows.add(values);
			}
			is.close();
			reader.close();
		} catch (IOException ex) {
			throw new Exception(" read file error ", ex);
		}
		return rows;
	}

	public int getDataFileCount() {
		File f = new File(dir);
		File[] file = f.listFiles();
		//减一是因为其中有一个是元数据文件
		dataFileCount = file.length-1;
		for (int i = 0; i < file.length; i++) {
			//简单处理，dir已经是单次记录集的文件夹，认为其中都是文件，其下的文件夹忽略
			if (file[i].isDirectory()) {
				dataFileCount--;
			}
		}
		return dataFileCount;
	}
	
	public String getEncode(){
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
		if (!this.dir.endsWith("\\")) {
			this.dir = this.dir + "\\";
		}
	}
	public String getColSeparator() {
		return colSeparator;
	}

	public void setColSeparator(String colSeparator) {
		this.colSeparator = colSeparator;
	}

}
