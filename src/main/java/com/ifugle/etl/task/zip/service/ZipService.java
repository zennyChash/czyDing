package com.ifugle.etl.task.zip.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang.StringUtils;

import com.ifugle.etl.entity.task.Unzip;
import com.ifugle.etl.entity.task.Zip;
import com.ifugle.etl.schedule.SchedulerUtils;

public class ZipService {
	public static final Logger log = LoggerFactory.getLogger(ZipService.class);  
    /** 
     * 解压
     */  
	
    public int unzip(Unzip task,Map params, Map paramVals){
    	int flag=0;
		if(task==null){
			log.error("任务配置错误：未配置解压缩任务相关信息！");
			return flag;
		}
		log.info("ZipService开始执行任务：{}",task.getId());
		String fileToUnzip = task.getFileToUnzip();
		if(StringUtils.isBlank(fileToUnzip)){
			log.error("未设置要解压缩的文件！");
			return flag;
		}
		String storedPath = task.getStoredPath();
		if(StringUtils.isBlank(storedPath)){
        	log.error("未设置解压缩文件的保存路径！");
			return flag;
        }
		fileToUnzip = SchedulerUtils.parseParamValue(fileToUnzip, paramVals);
		storedPath = SchedulerUtils.parseParamValue(storedPath, paramVals);
		try{
        	boolean done = unzip(fileToUnzip, storedPath);
        	flag = done?1:0;
        }catch(Exception e){
        	log.error(e.toString());
        	flag = 9;
        }
		return flag;
    }
    
    public int zip(Zip task,Map params, Map paramVals){
    	int flag=0;
		if(task==null){
			log.error("任务配置错误：未配置压缩任务的相关信息！");
			return flag;
		}
		log.info("ZipService开始执行任务：{}",task.getId());
		String dirsToZip = task.getDirectoryToZip();
		if(StringUtils.isBlank(dirsToZip)){
			log.error("未设置要进行压缩的文件或文件目录！");
			return flag;
		}
		String zippedName = task.getZippedFileName();
		if(StringUtils.isBlank(zippedName)){
        	log.error("未设置压缩后的ZIP文件名！");
			return flag;
        }
		dirsToZip = SchedulerUtils.parseParamValue(dirsToZip, paramVals);
		zippedName = SchedulerUtils.parseParamValue(zippedName, paramVals);
		try{
        	boolean done = zip(dirsToZip, zippedName);
        	flag = done?1:0;
        }catch(Exception e){
        	log.error(e.toString());
        	flag = 9;
        }
		return flag;
    }
    
    /** 
     * zip压缩文件 
     * @param dir 待压缩的文件/文件夹 
     * @param zippath 压缩后的文件名
     */  
    public boolean zip(String dir ,String zippath){
        List<String> paths = getFiles(dir);   
        boolean done = compressFilesZip(paths.toArray(new String[paths.size()]),zippath,dir); 
        return done;
    }  
    /** 
     * 递归取到当前目录所有文件 
     * @param dir 
     * @return 
     */  
    public List<String> getFiles(String dir){  
        List<String> lstFiles = null;       
        if(lstFiles == null){  
            lstFiles = new ArrayList<String>();  
        }  
        File file = new File(dir);
        if(!file.isDirectory()){
        	lstFiles.add(file.getAbsolutePath());
        }else{
	        File [] files = file.listFiles();  
	        for(File f : files){  
	            if(f.isDirectory()){  
	                lstFiles.add(f.getAbsolutePath());  
	                lstFiles.addAll(getFiles(f.getAbsolutePath()));  
	            }else{   
	                String str =f.getAbsolutePath();  
	                lstFiles.add(str);  
	            }  
	        } 
        }
        return lstFiles;  
    }  
      
    /** 
     * 文件名处理 
     * @param dir 
     * @param path 
     * @return 
     */  
    public String getFilePathName(String dir,String path){  
        String p = path.replace(dir+File.separator, "");  
        p = p.replace("\\", "/");  
        return p;  
    }  
    /** 
     * 把文件压缩成zip格式 
     * @param files       需要压缩的文件 
     * @param zipFilePath 压缩后的zip文件路径   ,如"D:/test/aa.zip"; 
     */  
    public boolean compressFilesZip(String[] files,String zipFilePath,String dir) {  
        if(files == null || files.length <= 0) { 
        	log.info("指定要压缩的文件目录不存在或无文件。");
            return true;  
        }  
        ZipArchiveOutputStream zaos = null;  
        try {  
            File zipFile = new File(zipFilePath);  
            zaos = new ZipArchiveOutputStream(zipFile);  
            zaos.setUseZip64(Zip64Mode.AsNeeded);  
            //将每个文件用ZipArchiveEntry封装  
            //再用ZipArchiveOutputStream写到压缩文件中  
            for(String strfile : files) {  
                File file = new File(strfile);  
                if(file != null) {  
                    String name = getFilePathName(dir,strfile);  
                    ZipArchiveEntry zipArchiveEntry  = new ZipArchiveEntry(file,name);  
                    zaos.putArchiveEntry(zipArchiveEntry);  
                    if(file.isDirectory()){  
                        continue;  
                    }  
                    InputStream is = null;  
                    try {  
                        is = new BufferedInputStream(new FileInputStream(file));  
                        byte[] buffer = new byte[1024 ];   
                        int len = -1;  
                        while((len = is.read(buffer)) != -1) {  
                            //把缓冲区的字节写入到ZipArchiveEntry  
                            zaos.write(buffer, 0, len);  
                        }  
                        zaos.closeArchiveEntry();   
                    }catch(Exception e) {  
                    	log.error("压缩过程发生IO错误：{}",e.toString());
                        return false;  
                    }finally {  
                        if(is != null)  
                            is.close();  
                    }  
                       
                }  
            }  
            zaos.finish(); 
        }catch(Exception e){  
        	log.error("压缩过程发生错误：{}",e.toString());
            return false; 
        }finally {  
                try {  
                    if(zaos != null) {  
                        zaos.close();  
                    }  
                } catch (IOException e) {  
                	log.error("压缩后，未能正确关闭IO资源：{}",e.toString());
                    return false;    
                }  
        }  
        return true;  
    }  
      
     
    /** 
    * 把zip文件解压到指定的文件夹 
    * @param zipFilePath zip文件路径, 如 "D:/test/aa.zip" 
    * @param saveFileDir 解压后的文件存放路径, 如"D:/test/" 
    */  
    public boolean unzip(String zipFilePath, String saveFileDir) {  
        if(!saveFileDir.endsWith("\\") && !saveFileDir.endsWith("/") ){  
            saveFileDir += "/";  
        }  
        File dir = new File(saveFileDir);  
        if(!dir.exists()){  
            dir.mkdirs();  
        }  
        File file = new File(zipFilePath);  
        if(file.exists()){
            InputStream is = null;   
            ZipArchiveInputStream zais = null;  
            try {  
                is = new FileInputStream(file);  
                zais = new ZipArchiveInputStream(is);  
                ArchiveEntry archiveEntry = null;  
                while ((archiveEntry = zais.getNextEntry()) != null) {   
                    // 获取文件名  
                    String entryFileName = archiveEntry.getName();
                    if(!StringUtils.isEmpty(entryFileName)){
                    	int idx = entryFileName.indexOf(":");
                    	entryFileName = entryFileName.substring(idx+1);
                    }
                    // 构造解压出来的文件存放路径  
                    String entryFilePath = saveFileDir + entryFileName; 
                    OutputStream os = null;  
                    try {  
                        // 把解压出来的文件写到指定路径  
                        File entryFile = new File(entryFilePath);  
                        if(entryFileName.endsWith("/")){  
                            entryFile.mkdirs();  
                        }else{  
                        	int idxDir = entryFilePath.lastIndexOf("/");
                            String strDir = entryFilePath.substring(0,idxDir);
                            File entryDir = new File(strDir);
                            if(!entryDir.exists()){
                            	entryDir.mkdirs();
                            }
                            os = new BufferedOutputStream(new FileOutputStream(  
                                    entryFile));                              
                            byte[] buffer = new byte[1024 ];   
                            int len = -1;   
                            while((len = zais.read(buffer)) != -1) {  
                                os.write(buffer, 0, len);   
                            }  
                        }  
                    } catch (IOException e) {
                    	log.error("解压缩过程发生IO错误：{}",e.toString());
                        return false; 
                    } finally {  
                        if (os != null) {  
                            os.flush();  
                            os.close();  
                        }  
                    }  
  
                }   
            } catch (Exception e) {  
            	log.error("解压缩过程发生错误：{}",e.toString());
                return false;   
            } finally {  
                try {  
                    if (zais != null) {  
                        zais.close();  
                    }  
                    if (is != null) {  
                        is.close();  
                    }  
                } catch (IOException e) {  
                	log.error("解压缩后，未能正确关闭IO资源：{}",e.toString());
                    return false;     
                }  
            }  
        } 
        return true;
    }
    public static void main(String[] args) { 
    	ZipService zs = new ZipService();
//        String dir = "D:/WEB-INF";  
//        String zippath = "D:/webZip.zip";  
//        ZipService.zip(dir, zippath);  
   
        String unzipdir = "D:/haha";  
        String unzipfile = "D:/upzip.zip";  
        zs.unzip(unzipfile, unzipdir);  
          
        System.out.println("success!");  
    }   
}
