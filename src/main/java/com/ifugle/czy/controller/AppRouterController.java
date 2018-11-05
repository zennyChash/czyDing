package com.ifugle.czy.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.router.bean.JRemoteParams;
import com.ifugle.czy.service.ConsoleServeice;
import com.ifugle.czy.service.RouterService;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.czy.utils.bean.User;

@Controller
public class AppRouterController {
	private static Logger log = Logger.getLogger(AppRouterController.class);
	@Autowired
	private RouterService rtService;
	
	@RequestMapping(value="/remoteService",method = RequestMethod.POST)
	@ResponseBody
	public JResponse requestRemoteService(@RequestBody JRemoteParams params){
		JResponse jr = new JResponse();
		if(params!=null){
			String reqService = params.getReqService();
			String reqMethod = params.getReqMethod();
			String svParams = params.getSvParams();
			if(StringUtils.isEmpty(reqService)){
				jr.setRetCode("9");
				jr.setRetMsg("未指明要请求的服务名！");
				jr.setRetData("");
			}else if(StringUtils.isEmpty(reqMethod)){
				jr.setRetCode("9");
				jr.setRetMsg("未指明要请求的方法名！");
				jr.setRetData("");
			}else{
				JSONObject jp = params.parseJRemoteParams();
				String userid = jp==null?"":jp.getString("userid");
				if(StringUtils.isEmpty(userid)){
					User user = null;
					RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
					if (requestAttributes != null) {
						HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
						user = (User)request.getSession().getAttribute("user");
					}
					if(user!=null){
						userid = user.getUserid();
					}
				}
				jr = rtService.routeRequest(reqService,reqMethod,svParams,userid);
			}
		}
		return jr;
	}
	
}
