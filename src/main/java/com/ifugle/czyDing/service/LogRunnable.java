package com.ifugle.czyDing.service;

import com.ifugle.czyDing.utils.bean.LogInfo;
import com.ifugle.utils.Configuration;
import com.ifugle.utils.ContextUtil;

public class LogRunnable implements Runnable{
	private String userid;
	private LogInfo log;
	private Configuration cg ;
	private LogService logService;
	public LogRunnable(String userid, LogInfo log){
		cg = (Configuration)ContextUtil.getBean("config");
		logService = (LogService)ContextUtil.getBean("logService");
		this.userid = userid;
		this.log = log;
	}
	@Override
    public synchronized void run() {
		logService.writeLogFromWeb(userid, log);
	}
}
