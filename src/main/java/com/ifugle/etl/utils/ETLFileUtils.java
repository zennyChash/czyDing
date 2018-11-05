package com.ifugle.etl.utils;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ETLFileUtils {
	private static Logger log = LoggerFactory.getLogger(ETLFileUtils.class);
	private static ETLFileUtils futils= null;
	
	private ETLFileUtils(){
	}
	public static ETLFileUtils getInstance(){
		if(futils ==null){
			futils = new ETLFileUtils();
		}
		return futils;
	}
	public synchronized String createSubDir(boolean deleteDuplicate,String rootDir,String subDir) {
		String tmpSbDir = StringUtils.isEmpty(subDir)?"":subDir;
		String wholeDir = (rootDir.endsWith("/")? rootDir : rootDir + "/")+tmpSbDir;
		java.io.File dir=new java.io.File(wholeDir);
	    if(!dir.exists()){
	    	dir.mkdirs();
	    }else{
	    	if(deleteDuplicate){
	    		dir.delete();
	    		dir.mkdirs();
	    	}else{
	    		wholeDir = createSubDirRandom(wholeDir);
	    	}
	    }
		return wholeDir.endsWith("/")? wholeDir : wholeDir + "/";
	}
	private String createSubDirRandom(String dir){
		String rs = RandomStringUtils.random(5, new char[]{'a','b','c','d','t','5','6','7','8','9'});
		dir = dir.endsWith("/")?dir.substring(0,dir.length()-1):dir;
		String tmpDir = dir+"_"+rs;
		java.io.File tmpdir=new java.io.File(tmpDir);
		if(!tmpdir.exists()){
			tmpdir.mkdirs();
		}else{
			tmpDir = createSubDirRandom(dir);
		}
		return tmpDir;
	}
}
