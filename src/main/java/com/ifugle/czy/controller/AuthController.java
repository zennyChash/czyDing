package com.ifugle.czy.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.service.AuthService;
import com.ifugle.czy.utils.bean.User;
import com.ifugle.czy.utils.JResponse;

@Controller
public class AuthController {
	@Autowired
	private AuthService authService;
	@RequestMapping("/getDingConfig")
	@ResponseBody
	public JResponse getDingConfig(HttpServletRequest request){
		Map config = authService.getConfig(request);
		JResponse jr = new JResponse();
		if(config==null||config.isEmpty()){
			jr.setRetCode("9");
			jr.setRetMsg("获取钉钉配置信息失败！");
			jr.setRetData(config);
		}else{
			jr.setRetCode("0");
			jr.setRetMsg("");
			jr.setRetData(config);
		}
		return jr;
	}
	@RequestMapping("/getUserConfig")
	@ResponseBody
	public JResponse getUserConfig(@RequestParam("code") String code, @RequestParam("corpid") String corpid){
		JResponse jr = new JResponse();
		System.out.println("传入的code:"+code);
		System.out.println("传入的corpID:"+corpid);
		String accessToken = authService.getAccessToken();
		User user = authService.getUserCzyConfig(accessToken,code);
		System.out.println("最外层的调用中的user:"+user==null?"空":user.getConfig());
		if(user!=null){
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				request.getSession().setAttribute("user", user);
			}
			jr.setRetCode("0");
			jr.setRetMsg("");
			String uconfig = user.getConfig();
			JSONObject jucg = JSON.parseObject(uconfig);
			jr.setRetData(jucg);
			System.out.println("数据库中的用户配置串:"+uconfig+"。。。。整个用户配置信息:"+jucg.toJSONString());
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("获取用户的配置信息失败！");
			jr.setRetData(null);
			System.out.println("出错了！用户配置信息没有获取到！");
		}
		return jr;
	}
}
