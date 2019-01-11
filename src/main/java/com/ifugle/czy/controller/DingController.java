package com.ifugle.czy.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ifugle.czy.service.ConsoleServeice;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.bean.DingMsgJson;
@Controller
//处理钉钉相关的请求
public class DingController {
	private static Logger log = Logger.getLogger(DingController.class);
	@Autowired
	private ConsoleServeice csService;
	
	@RequestMapping(value="/sendDingMsg",method = RequestMethod.POST)
	@ResponseBody
	public JResponse sendDingMsg(@RequestBody DingMsgJson params){
		JResponse jr = null;
		if(params!=null){
			String msg = params.getMsg();
			String users = params.getUsers();
			log.info("sendDingMsg接收者参数:"+users);
			if(StringUtils.isEmpty(msg)){
				return new JResponse("9","要发送的消息内容为空！",null);
			}
			if(StringUtils.isEmpty(users)){
				return new JResponse("9","没有指明消息接收者！",null);
			}
			Map data = csService.sendLinkDingMsg(msg,users);
			jr = new JResponse("0","",data);
		}else{
			jr = new JResponse("9","获取报表数据失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/sendDingForDsp",method = RequestMethod.GET)
	@ResponseBody
	public Map sendDingForDsp(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		String reqService = params.get("reqService");
		String reqMethod = params.get("reqMethod");
		infos = csService.sendDingForDsp(reqService,reqMethod,null,null);
		return infos;
	}
	@RequestMapping(value="/sendDingForDspTest",method = RequestMethod.GET)
	@ResponseBody
	public Map sendDingForDspTest(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		String reqService = params.get("reqService");
		String reqMethod = params.get("reqMethod");
		log.info("reqService:"+reqService);
		log.info("reqMethod:"+reqMethod);
		infos = csService.sendDingForDspTest();
		return infos;
	}
}
