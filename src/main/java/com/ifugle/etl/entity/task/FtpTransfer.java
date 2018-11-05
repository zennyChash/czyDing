package com.ifugle.etl.entity.task;

import com.ifugle.etl.entity.base.Task;

public class FtpTransfer  extends Task{
    private String serverIp;  
    private int serverPort;
    //FTP 服务器，是否允许匿名访问
    private int isAnonymous;  
	//匿名访问，用户名 
    private String anonymousUser;  
    //匿名访问，密码 
    private String anonymousPswd;  
    //FTP服务器用户名 
    private String userName;  
    //密码
    private String password;  
    //FTP服务器的工作目录，登录后，当前目录为用户的home目录，工作目录基于这个目录设置。
    //工作目录不能自动创建，如果不存在 ，上传内容放在用户的home目录。 
    private String workingDir;  
    //FTP服务器配置，是否被动模式
    private int isPasv=1;
    private int isUpload;
	//要下载的文件和保存到本地的名字。
	private String fileToDownload;
	private String localStoredPath;
    //上传文件保存到服务器的名字
	private String serverStoredName;
	private String fileToUpload;
	
	public String getServerStoredName() {
		return serverStoredName;
	}
	public void setServerStoredName(String serverStoredName) {
		this.serverStoredName = serverStoredName;
	}
	public String getFileToUpload() {
		return fileToUpload;
	}
	public void setFileToUpload(String fileToUpload) {
		this.fileToUpload = fileToUpload;
	}
	public String getFileToDownload() {
		return fileToDownload;
	}
	public void setFileToDownload(String fileToDownload) {
		this.fileToDownload = fileToDownload;
	}
	public String getLocalStoredPath() {
		return localStoredPath;
	}
	public void setLocalStoredPath(String localStoredPath) {
		this.localStoredPath = localStoredPath;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public int getIsAnonymous() {
		return isAnonymous;
	}
	public void setIsAnonymous(int isAnonymous) {
		this.isAnonymous = isAnonymous;
	}
	public String getAnonymousUser() {
		return anonymousUser;
	}
	public void setAnonymousUser(String anonymousUser) {
		this.anonymousUser = anonymousUser;
	}
	public String getAnonymousPswd() {
		return anonymousPswd;
	}
	public void setAnonymousPswd(String anonymousPswd) {
		this.anonymousPswd = anonymousPswd;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getIsPasv() {
		return isPasv;
	}
	public void setIsPasv(int isPasv) {
		this.isPasv = isPasv;
	}
	public String getWorkingDir() {
		return workingDir;
	}
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}
	public int getIsUpload() {
		return isUpload;
	}
	public void setIsUpload(int isUpload) {
		this.isUpload = isUpload;
	}
}
