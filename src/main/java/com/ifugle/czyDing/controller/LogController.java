package com.ifugle.czyDing.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.service.ExecuteScriptRunnable;
import com.ifugle.czyDing.service.LogRunnable;
import com.ifugle.czyDing.service.LogService;
import com.ifugle.czyDing.utils.JResponse;
import com.ifugle.czyDing.utils.bean.LogInfo;
import com.ifugle.czyDing.utils.bean.User;
import com.ifugle.utils.Configuration;
@Controller
public class LogController {
	private static Logger log = Logger.getLogger(LogController.class);
	@Autowired
	private LogService logService;
	@Autowired
	private Configuration cg;
	
	@RequestMapping(value="/log",method = RequestMethod.POST)
	@ResponseBody
	public JResponse log(@RequestBody LogInfo log){
		JResponse jr = null;
		if(log!=null){
			String userid = "";
			if(StringUtils.isEmpty(log.getLogType())){
				return new JResponse("9","日志信息不完整，缺少日志类型！",null);
			}else if(StringUtils.isEmpty(log.getLogId())){
				return new JResponse("9","日志信息不完整，缺少日志ID！",null);
			}
			User user = null;
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				user = (User)request.getSession().getAttribute("user");
			}
			if(user==null&&StringUtils.isEmpty(log.getUserid())){
				return new JResponse("9", "未知的用户账户！",null);
			}else if(user!=null){
				userid = user.getUserid();
			}else{
				userid = log.getUserid();
			}
			//将任务交给另一个线程去做
			LogRunnable writeLog = new LogRunnable(userid,log);
			new Thread(writeLog).start();
			//请求立刻返回
			jr = new JResponse("0","", new JSONObject());
		}else{
			jr = new JResponse("9","日志请求的参数格式错误！",null);
		}
		return jr;
	}
}
