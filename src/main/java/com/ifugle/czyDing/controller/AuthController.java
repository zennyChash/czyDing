package com.ifugle.czyDing.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.router.bean.ProxyRequest;
import com.ifugle.czyDing.service.AuthService;
import com.ifugle.czyDing.utils.DingHelper;
import com.ifugle.czyDing.utils.HttpHelper;
import com.ifugle.czyDing.utils.JResponse;
import com.ifugle.czyDing.utils.SecureUtils;
import com.ifugle.czyDing.utils.bean.User;
import com.ifugle.utils.Configuration;

@Controller
public class AuthController {
	private static Logger log = Logger.getLogger(AuthController.class);
	@Autowired
	private Configuration cg ;
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
		String accessToken = DingHelper.getAccessToken();
		User user = authService.getUserCzyConfig(accessToken,code,true);
		if(user!=null){
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				request.getSession().setAttribute("user", user);
			}
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject jucfg = new JSONObject();
			jucfg.put("userid", user.getUserid());
			jucfg.put("username", user.getDingname());
			jucfg.put("postname", user.getPostNames());
			jucfg.put("czfpbm", StringUtils.isEmpty(user.getCzfpbm())?"":user.getCzfpbm());
			jucfg.put("czfp", StringUtils.isEmpty(user.getCzfp())?"":user.getCzfp());
			jucfg.put("corpname", cg.getString("corpname", "未知"));
			jucfg.put("menus", user.getMenus());
			jucfg.put("pswd_on", user.getPswd_on());
			jucfg.put("pswd_mode", user.getPswd_mode());
			jr.setRetData(jucfg);
			log.info("用户"+ user.getDingname()+"登录权限返回："+JSONObject.toJSONString(jr));
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("指定的用户账户不存在或未配置业务权限！");
			jr.setRetData(null);
			System.out.println("出错了！用户不存在！");
		}
		return jr;
	}
	
	@RequestMapping("/validateLogin")
	@ResponseBody
	public JResponse validateLogin(@RequestParam("userid") String userid,@RequestParam("pswd") String pswd){
		JResponse jr = new JResponse();
		if(StringUtils.isEmpty(pswd)){
			jr.setRetCode("3");
			jr.setRetMsg("待验证的密码为空 ！");
			jr.setRetData(null);
			return jr;
		}
		User user = null;
		String username =userid;
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			user = (User)request.getSession().getAttribute("user");
		}
		if(user==null&&StringUtils.isEmpty(userid)){
			return new JResponse("9", "未知的用户账户或登录超时，请重新登录！",null);
		}else{
			userid = user==null?userid:user.getUserid();
			username = user==null?userid:user.getDingname();
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try{
				int flag = authService.validateLogin(userid,pswd);
				if(flag==0){
					jr.setRetCode("0");
					jr.setRetMsg("");
					log.info("用户"+username+"于"+df.format(new Date())+"登录系统！");
				}else if(flag==3){
					jr.setRetCode("3");
					jr.setRetMsg("用户未设置密码！");
				}else if(flag==5){
					jr.setRetCode("5");
					jr.setRetMsg("用户密码不正确！");
					jr.setRetData(null);
					log.info("用户"+username+"于"+df.format(new Date())+"登录时，密码错误！");
				}
			}catch(Exception e){
				log.info("用户"+username+"于"+df.format(new Date())+"登录，发生错误。"+e.toString());
				return new JResponse("9", "密码验证时发生错误！",null);
			}
		}
		return jr;
	}
	
	@RequestMapping("/getDingSsoInfo")
	@ResponseBody
	public JResponse getDingSsoInfo(@RequestParam("code") String code, @RequestParam("corpid") String corpid){
		JResponse jr = new JResponse();
		System.out.println("传入的code:"+code);
		System.out.println("传入的corpID:"+corpid);
		String accessToken = DingHelper.getAccessToken();
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = null;
		if (requestAttributes != null) {
			request = ((ServletRequestAttributes) requestAttributes).getRequest();
			request.getSession().setAttribute("accessToken", accessToken);
		}
		User user = authService.getUserCzyConfig(accessToken,code,true);
		if(user!=null){
			log.info("getDingSsoInfo访问时间："+new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS").format(new Date()));
			if (requestAttributes != null) {
				request = ((ServletRequestAttributes) requestAttributes).getRequest();
				request.getSession().setAttribute("user", user);
				String sessionId = request.getSession().getId();
				log.info("存放User到会话，会话ID："+sessionId);
				log.info("YES!!将user放入会话requestAttributes！");
			}else{
				log.error("requestAttributes为空，未能将user放入会话！");
			}
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject jucfg = new JSONObject();
			jucfg.put("userid", user.getUserid());
			jucfg.put("username", user.getDingname());
			jucfg.put("postname", user.getPostNames());
			jucfg.put("czfpbm", StringUtils.isEmpty(user.getCzfpbm())?"":user.getCzfpbm());
			jucfg.put("czfp", StringUtils.isEmpty(user.getCzfp())?"":user.getCzfp());
			jucfg.put("corpname", cg.getString("corpname", "未知"));
			jucfg.put("myMenus", user.getMenus());
			jucfg.put("pswd_on", user.getPswd_on());
			jucfg.put("pswd_mode", user.getPswd_mode());
			jucfg.put("dingInfo", user.getDinginfo());
			jr.setRetData(jucfg);
			log.info("用户"+ user.getDingname()+"登录权限返回："+JSONObject.toJSONString(jr));
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("指定的用户账户不存在或未配置业务权限！");
			jr.setRetData(null);
			System.out.println("出错了！用户不存在！");
		}
		return jr;
	}
	@RequestMapping("/getUserMenus")  
	@ResponseBody
	public JResponse getUserMenus(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		User user = null;
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		if (requestAttributes != null) {
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			String sessionId = request.getSession()==null?"空sessionid":request.getSession().getId();
			log.info("从会话取USER值，会话ID："+sessionId);
			user = (User)request.getSession().getAttribute("user");
		}
		if(user==null){
			return new JResponse("9", "未知的用户账户！",null);
		}else{
			try{
				List jmenus = authService.getUserMenus(user.getUserid());
				jr.setRetCode("0");
				jr.setRetMsg("");
				JSONObject jucfg = new JSONObject();
				jucfg.put("menus", jmenus);
				jr.setRetData(jucfg);
				log.info("用户"+user.getDingname()+"的应用权限模块："+jr.toString());
			}catch(Exception e){
				return new JResponse("9", "获取用户模块列表时发生系统错误！",null);
			}
		}
		return jr;
	}
	@RequestMapping("/canAccessTo")
	@ResponseBody
	public JResponse canAccessTo(@RequestParam("mid") String mid,@RequestParam("code") String code, @RequestParam("corpid") String corpid){
		boolean canAccess = false;
		JResponse jr = new JResponse();
		JSONObject jo= new JSONObject();
		String accessToken = DingHelper.getAccessToken();
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = null;
		if (requestAttributes != null) {
			request = ((ServletRequestAttributes) requestAttributes).getRequest();
			request.getSession().setAttribute("accessToken", accessToken);
		}
		User user = authService.getUserCzyConfig(accessToken,code,false);
		if(user==null){
			return new JResponse("9", "登录验证失败！",null);
		}else{
			if (requestAttributes != null) {
				request = ((ServletRequestAttributes) requestAttributes).getRequest();
				request.getSession().setAttribute("user", user);
			}
			try{
				canAccess = authService.canAccessModule(user.getUserid(),mid);
				if(canAccess){
					jr.setRetCode("0");
					jr.setRetMsg("");
					jo.put("denied", false);
					jr.setRetData(jo);
				}else{
					jr.setRetCode("0");
					jr.setRetMsg("");
					jo.put("denied", true);
					jr.setRetData(jo);
				}
			}catch(Exception e){
				jr.setRetCode("9");
				jr.setRetMsg("验证访问权限时发生错误！访问失败");
				jo.put("denied", true);
				jr.setRetData(jo);
			}
		}
		return jr;
	}
	
	
	
	
	@RequestMapping("/getMyMenusTest")
	@ResponseBody
	public JResponse getMyMenusTest(@RequestParam("userid") String userid){
		JResponse jr = new JResponse();
		User user = authService.getMyMenus(userid,null);
		if(user!=null){
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject jucfg = new JSONObject();
			jucfg.put("myMenus", user.getMenus());
			jr.setRetData(jucfg);
		}
		return jr;
	}
	@RequestMapping("/getUserMenusTest")  
	@ResponseBody
	public JResponse getUserMenusTest(@RequestParam("userid") String userid){
		JResponse jr = new JResponse();
		try{
			List jmenus = authService.getUserMenus(userid);
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject jucfg = new JSONObject();
			jucfg.put("menus", jmenus);
			jr.setRetData(jucfg);
		}catch(Exception e){
			return new JResponse("9", "获取用户模块列表时发生系统错误！",null);
		}
		return jr;
	}
	
	@RequestMapping("/getUserConfigTest")
	@ResponseBody
	public JResponse getUserConfigTest(@RequestParam("userid") String userid){
		JResponse jr = new JResponse();
		User user = authService.testAuth(userid);
		if(user!=null){
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				request.getSession().setAttribute("user", user);
			}
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject jucfg = new JSONObject();
			jucfg.put("userid", user.getUserid());
			jucfg.put("username", user.getDingname());
			jucfg.put("postname", user.getPostNames());
			jucfg.put("czfpbm", StringUtils.isEmpty(user.getCzfpbm())?"":user.getCzfpbm());
			jucfg.put("czfp", StringUtils.isEmpty(user.getCzfp())?"":user.getCzfp());
			jucfg.put("corpname", cg.getString("corpname", "未知"));
			jucfg.put("menus", user.getMenus());
			jucfg.put("pswd_on", user.getPswd_on());
			jucfg.put("pswd_mode", user.getPswd_mode());
			jr.setRetData(jucfg);
			log.info("用户"+userid+"登录权限返回："+JSONObject.toJSONString(jr));
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("指定的用户账户不存在或未配置业务权限！");
			jr.setRetData(null);
			System.out.println("出错了！用户不存在！");
		}
		return jr;
	}
	
	/**
	 * 2021-08-23 从财智云门户单点登录
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/portalLogin")
	@ResponseBody
	public JResponse portalLogin(HttpServletRequest request){
		JResponse jr = new JResponse();
		String jparams ="";
		String cipherText = request.getParameter("cip");
		if(StringUtils.isEmpty(cipherText)){
			jr.setRetCode("9");
			jr.setRetMsg("参数cip内容为空！");
			jr.setRetData(null);
			return jr;
		}
		org.json.JSONObject jo = null;
		try{
			jparams = SecureUtils.decryptPortal(cipherText);
		}catch(Exception e){
			jr.setRetCode("9");
			jr.setRetMsg("参数cip解密时发生错误！"+e.toString());
			jr.setRetData(null);
			return jr;
		}
		try{
			jo=new org.json.JSONObject(jparams);
		}catch(Exception e){
			jr.setRetCode("9");
			jr.setRetMsg("参数cip格式不正确！"+e.toString());
			jr.setRetData(null);
			return jr;
		}
		String mobile = "",appToken="";
		try{
			mobile = jo.getString("mobile");
		}catch(Exception e){
		}
		try{
			appToken = jo.getString("appToken");
		}catch(Exception e){
		}
		
		if(StringUtils.isEmpty(mobile)){
			jr.setRetCode("9");
			jr.setRetMsg("参数中缺少手机号码！");
			jr.setRetData(null);
			return jr;
		}
		if(StringUtils.isEmpty(appToken)){
			jr.setRetCode("9");
			jr.setRetMsg("参数中缺少appToken！");
			jr.setRetData(null);
			return jr;
		}
		String avatar = "";
		try{
			avatar = jo.getString("avatar");
		}catch(Exception e){
		}
		User user = authService.getUserCzyConfigPortal(jo);
		if(user!=null){
			log.info("portalLogin访问时间："+new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS").format(new Date()));
			request.getSession().setAttribute("user", user);
			request.getSession().setAttribute("portal_appToken", appToken);
			String sessionId = request.getSession().getId();
			log.info("存放User到会话，会话ID："+sessionId);
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject jucfg = new JSONObject();
			jucfg.put("portal_appToken", appToken);
			jucfg.put("mobile", mobile);
			jucfg.put("avatar", avatar);
			jucfg.put("userid", user.getUserid());
			jucfg.put("username", user.getDingname());
			jucfg.put("postname", user.getPostNames());
			jucfg.put("czfpbm", StringUtils.isEmpty(user.getCzfpbm())?"":user.getCzfpbm());
			jucfg.put("czfp", StringUtils.isEmpty(user.getCzfp())?"":user.getCzfp());
			jucfg.put("corpname", cg.getString("corpname", "未知"));
			jucfg.put("myMenus", user.getMenus());
			jucfg.put("pswd_on", user.getPswd_on());
			jucfg.put("pswd_mode", user.getPswd_mode());
			jucfg.put("dingInfo", user.getDinginfo());
			jr.setRetData(jucfg);
			log.info("用户"+ user.getDingname()+"登录权限返回："+JSONObject.toJSONString(jr));
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("指定的用户账户不存在或未配置业务权限！");
			jr.setRetData(null);
			System.out.println("出错了！用户不存在！");
		}
		return jr;
	}
}
