package com.ifugle.czy.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.service.AuthService;
import com.ifugle.czy.utils.bean.User;
import com.ifugle.czy.utils.JResponse;

@Controller
public class AuthController {
	private static Logger log = Logger.getLogger(AuthController.class);
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
		String uconfig = "";
		if(user!=null){
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				request.getSession().setAttribute("user", user);
			}
			jr.setRetCode("0");
			jr.setRetMsg("");
			uconfig = user.getConfig();
			if(!StringUtils.isEmpty(uconfig)){
				JSONObject jucg = null;
				try{
					jucg = JSON.parseObject(uconfig);
					jr.setRetData(jucg);
				}catch(Exception e){
					jr.setRetCode("9");
					jr.setRetMsg("用户业务权限信息的解析出错，请检查配置信息的格式！");
					jr.setRetData(null);
				}
				log.info("数据库中的用户配置串:"+uconfig);
			}else{
				jr.setRetCode("9");
				jr.setRetMsg("当前用户还未进行业务权限的配置！");
				jr.setRetData(null);
			}
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("指定的用户账户不存在或未配置业务权限！");
			jr.setRetData(null);
			System.out.println("出错了！用户不存在！");
		}
		log.info("返回的jr.retCode:"+jr.getRetCode()+",jr.regData:"+JSONObject.toJSONString(jr.getRetData())+",jr.regMsg:"+jr.getRetMsg());
		return jr;
	}
}
