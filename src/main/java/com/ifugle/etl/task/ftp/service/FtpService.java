package com.ifugle.etl.task.ftp.service;

import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.OutputStream;  
import java.net.SocketException;  
import java.util.Map;  
  


import org.apache.commons.lang.StringUtils;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.apache.commons.net.ftp.FTP;  
import org.apache.commons.net.ftp.FTPClient;  
import org.apache.commons.net.ftp.FTPReply;  

import com.ifugle.etl.entity.task.FtpTransfer;
import com.ifugle.etl.schedule.SchedulerUtils;
  
/** 
 * 
 */  
public class FtpService {  
    public static final Logger log = LoggerFactory.getLogger(FtpService.class);  
    
    /** 
     * 从FTP服务器下载 
     */  
    public int download(FtpTransfer task,Map params, Map paramVals){
    	int flag=0;
		if(task==null){
			log.error("任务配置错误：未配置FTP下载的相关信息！");
			return flag;
		}
		log.info("FtpService开始执行任务：{}",task.getId());
		String ftpServer = task.getServerIp();
		if(StringUtils.isBlank(ftpServer)){
			log.error("未设置ftp服务器IP！");
			return flag;
		}
		if(task.getIsAnonymous()==0&&StringUtils.isBlank(task.getUserName())){
        	log.error("设置了非匿名访问的模式，但未设置访问用户名！");
			return flag;
        }
        if(StringUtils.isBlank(task.getFileToDownload())) { 
        	log.error("未设置要下载的文件名！");
			return flag;
        }  
        if(StringUtils.isBlank(task.getLocalStoredPath())) {  
        	log.error("未设置下载文件的本地保存目录！");
			return flag;
        } 
        try{
        	flag = transferFile(false, task,paramVals,null); 
        }catch(Exception e){
        	log.error(e.toString());
        	flag = 9;
        }
        return flag;
    }  
      
    private int transferFile(boolean isUpload, FtpTransfer task,Map paramVals,File fileToUpload)throws SocketException, IOException { 
    	int flag = 0;
        String host = task.getServerIp();  
        int port = task.getServerPort();
        boolean isAnonymous = task.getIsAnonymous()>0; 
        String username = task.getUserName(); 
        String password = task.getPassword(); 
        int isPASV = task.getIsPasv();
        //工作目录允许宏变量
        String workingDirectory = task.getWorkingDir();
        workingDirectory = SchedulerUtils.parseParamValue(workingDirectory, paramVals);
        FTPClient ftpClient = new FTPClient();  
        InputStream fileIn = null;  
        OutputStream fileOut = null;  
        try {  
            if (port <1||port>65535) { //未设置端口时，采用默认端口。 
                log.info("未指定端口，用默认端口进行连接。{}:{}",host, FTP.DEFAULT_PORT);  
                ftpClient.connect(host);  
            } else {  
                log.info("连接指定服务器和端口。{}:{}" + host + ":" + port);  
                ftpClient.connect(host, port);  
            }  
            int reply = ftpClient.getReplyCode();  
            if (!FTPReply.isPositiveCompletion(reply)){  
                log.error("FTP服务器拒绝连接！");  
                return 9;  
            }
            if ( isAnonymous && !StringUtils.isBlank(task.getAnonymousUser())){  
                username = task.getAnonymousUser();  
                password = task.getAnonymousPswd(); 
            }   
            log.info("登录FTP服务器成功，用户名：{}，密码：{} ",username, password);  
            if (!ftpClient.login(username, password)) {  
                log.error("登录FTP服务器失败！用户名：{}，密码：{} ",username, password);  
                ftpClient.logout();  
                return 9; 
            }  
            // 传输使用二进制方式  
            log.debug("二进制方式传输文件！");  
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);  
              
            if (isPASV >0 ) {  
                log.debug("使用被动方式传输文件。");  
                ftpClient.enterLocalPassiveMode();  
            } else {  
                log.debug("使用主动方式传输文件。");  
                ftpClient.enterLocalActiveMode();  
            }  
              
            if (StringUtils.isBlank(workingDirectory)) {  
                workingDirectory = "/";  
            }  
            log.debug("工作目录设置为:{} ",workingDirectory);  
            ftpClient.changeWorkingDirectory(workingDirectory);  
            if (isUpload) {
            	String serverFileStoredName = task.getServerStoredName();
                if (StringUtils.isBlank(serverFileStoredName)) {  
                	//上传到服务器上的文件名允许引用宏变量
                    serverFileStoredName = fileToUpload.getName();
                    serverFileStoredName = SchedulerUtils.parseParamValue(serverFileStoredName, paramVals);
                }  
                fileIn = new FileInputStream(fileToUpload);  
                log.debug("上传文件:{}。FTP服务器上保存为：{}", fileToUpload.getAbsolutePath(),serverFileStoredName);  
                if (!ftpClient.storeFile(serverFileStoredName, fileIn)) {  
                    log.error("上传失败。{}" , ftpClient.getReplyString());  
                } else {  
                    log.info("上传成功！");  
                }  
            } else { //download  
            	String localPath = task.getLocalStoredPath();
            	//下载文件保存的本地名允许引用宏变量
            	localPath = SchedulerUtils.parseParamValue(localPath, paramVals); 
                File fileStored = new File(localPath);  
                if (!fileStored.getParentFile().exists()) {  
                    fileStored.getParentFile().mkdirs();  
                }  
                fileOut = new FileOutputStream(fileStored);  
                String file2Download = task.getFileToDownload();
                //下载文件名允许引用宏变量
                file2Download = SchedulerUtils.parseParamValue(file2Download, paramVals);
                log.debug("下载文件 :{}，保存为本地文件：{} " + file2Download ,task.getLocalStoredPath());  
                if (!ftpClient.retrieveFile(file2Download, fileOut)) {  
                    log.error("下载失败,{} " + ftpClient.getReplyString());  
                } else {  
                    log.debug("下载成功.");  
                }  
            }  
            ftpClient.noop();  
            ftpClient.logout();
            flag=1;
        }finally {  
            if (ftpClient.isConnected()) {  
                try {  
                    ftpClient.disconnect();  
                } catch (IOException f) {  
                }  
            }  
            if (fileIn != null) {  
                try {  
                    fileIn.close();  
                } catch (IOException e) {  
                }  
            }  
            if (fileOut != null) {  
                try {  
                    fileOut.close();  
                } catch (IOException e) {  
                }  
            }  
        } 
        return flag;
    }  
    /** 
     * Upload a file to FTP server. 
     * @param serverCfg : FTP server configuration 
     * @param fileToUpload : file to upload 
     * @param fileStoredName : the name to give the remote stored file, 
     *  null, "" and other blank word will be replaced by the file name to upload 
     * @throws IOException  
     * @throws SocketException  
     */  
    public int upload(FtpTransfer task,Map params, Map paramVals){
    	if(task==null){
			log.error("任务配置错误：未配置FTP上传的相关信息！");
			return 9;
		}
    	int flag=0;
    	String file2Up = task.getFileToUpload();
    	//要上传的文件名允许引用宏变量
    	file2Up = SchedulerUtils.parseParamValue(file2Up, paramVals);
    	File fileToUpload = new File(file2Up);
        if (!fileToUpload.exists()) {   
        	log.error("指定的上传文件不存在，请检查文件路径或文件名！"); 
        	return 9;
        }  
        if (!fileToUpload.isFile()) {  
            log.error("指定的上传文件：{}不是一个文件！"+ fileToUpload.getAbsolutePath()); 
        	return 9;
        }  
        try{
        	flag = transferFile(true, task,paramVals,fileToUpload); 
        }catch(Exception e){
        	log.error(e.toString());
        	flag = 9;
        }
        return flag;
    }
}  