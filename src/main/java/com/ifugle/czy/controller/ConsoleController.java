package com.ifugle.czy.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import com.ifugle.czy.utils.SubmitResult;

@Controller
public class ConsoleController {
	private static Logger log = Logger.getLogger(ConsoleController.class);
	@Autowired
	private ConsoleServeice csService;
	
	@RequestMapping(value="/authConsolLog",method = RequestMethod.POST)
	@ResponseBody
	public SubmitResult AuthLogin(@RequestParam Map<String, String> params){
		SubmitResult result = new SubmitResult();
		Map errors = new HashMap();
		Map infos = new HashMap();
		if(params!=null){
			String userid = params.get("username");
			String pswd = params.get("password");
			int flag = csService.authLog(userid,pswd);
			if(flag==1){
				result.setSuccess(true);
				infos.put("msg", "");
			}else if(flag==2){
				result.setSuccess(false);
				infos.put("msg", "用户不存在！");
			}else if(flag==3){
				result.setSuccess(false);
				infos.put("msg", "密码不正确！");
			}else{
				result.setSuccess(false);
				infos.put("msg", "登录过程中发生系统错误！");
			}
			result.setInfos(infos);
		}else{
			result.setSuccess(false);
			infos.put("msg", "未找到请求参数，登录失败！");
			result.setInfos(infos);
		}
		return result;
	}
	
	@RequestMapping(value="/getUsers",method = RequestMethod.GET)
	@ResponseBody
	public Map getUsers(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String sStart = params.get("start");
			String sLimit = params.get("limit");
			int start =0,limit=0;
			try{
				start = Integer.parseInt(sStart);
			}catch(Exception e){}
			try{
				limit = Integer.parseInt(sLimit);
			}catch(Exception e){}
			infos = csService.getUsers(start,limit);
		}
		return infos;
	}
	@RequestMapping(value="/getPosts",method = RequestMethod.GET)
	@ResponseBody
	public Map getPosts(@RequestParam Map<String, String> params){
		Map infos = csService.getPosts();
		return infos;
	}
	@RequestMapping(value="/saveUserPosts",method = RequestMethod.POST)
	@ResponseBody
	public JResponse saveUserPosts(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strUids = params.get("userids");
			String strPids = params.get("postids");
			if(StringUtils.isEmpty(strUids)){
				jr.setRetCode("9");
				jr.setRetMsg("缺少要操作的用户ID！");
				jr.setRetData("");
			}else{
				boolean done = csService.saveUserPosts(strUids,strPids);
				if(done){
					jr.setRetCode("0");
					jr.setRetMsg("");
				}else{
					jr.setRetCode("9");
					jr.setRetMsg("保存用户的角色信息失败！");
					jr.setRetData("");
				}
			}
		}
		return jr;
	}
	@RequestMapping(value="/removeUserPosts",method = RequestMethod.POST)
	@ResponseBody
	public JResponse removeUserPosts(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strUids = params.get("userids");
			if(StringUtils.isEmpty(strUids)){
				jr.setRetCode("9");
				jr.setRetMsg("缺少要操作的用户ID！");
				jr.setRetData("");
			}else{
				boolean done = csService.removeUserPosts(strUids);
				if(done){
					jr.setRetCode("0");
					jr.setRetMsg("");
				}else{
					jr.setRetCode("9");
					jr.setRetMsg("删除用户的角色信息失败！");
					jr.setRetData("");
				}
			}
		}
		return jr;
	}
	@RequestMapping(value="/getCzfps",method = RequestMethod.GET)
	@ResponseBody
	public Map getCzfps(@RequestParam Map<String, String> params){
		Map infos = csService.getCzfps();
		return infos;
	}
	@RequestMapping(value="/saveCzfps",method = RequestMethod.POST)
	@ResponseBody
	public JResponse saveCzfps(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strUids = params.get("userids");
			String czfpbm = params.get("czfpbm");
			if(StringUtils.isEmpty(strUids)){
				jr.setRetCode("9");
				jr.setRetMsg("缺少要操作的用户ID！");
				jr.setRetData("");
			}else{
				boolean done = csService.saveCzfps(strUids,czfpbm);
				if(done){
					jr.setRetCode("0");
					jr.setRetMsg("");
				}else{
					jr.setRetCode("9");
					jr.setRetMsg("配置用户的分片信息时发生错误！");
					jr.setRetData("");
				}
			}
		}
		return jr;
	}
	@RequestMapping(value="/removeCzfp",method = RequestMethod.POST)
	@ResponseBody
	public JResponse removeCzfp(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strUids = params.get("userids");
			if(StringUtils.isEmpty(strUids)){
				jr.setRetCode("9");
				jr.setRetMsg("缺少要操作的用户ID！");
				jr.setRetData("");
			}else{
				boolean done = csService.removeCzfp(strUids);
				if(done){
					jr.setRetCode("0");
					jr.setRetMsg("");
				}else{
					jr.setRetCode("9");
					jr.setRetMsg("解除用户的分片信息时发生错误！");
					jr.setRetData("'");
				}
			}
		}
		return jr;
	}
	
	@RequestMapping(value="/getModules",method = RequestMethod.GET)
	@ResponseBody
	public Map getModules(@RequestParam Map<String, String> params){
		String strPid = "";
		if(params!=null){
			strPid = params.get("postid");
		}
		Map infos = csService.getModules(strPid);
		return infos;
	}
	
	@RequestMapping(value="/setPostModules",method = RequestMethod.POST)
	@ResponseBody
	public JResponse setPostModules(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strPid = params.get("postid");
			String strMids = params.get("moduleids");
			if(StringUtils.isEmpty(strPid)){
				jr.setRetCode("9");
				jr.setRetMsg("缺少要操作的角色ID！");
				jr.setRetData("");
			}else{
				boolean done = csService.setPostModules(strPid,strMids);
				if(done){
					jr.setRetCode("0");
					jr.setRetMsg("");
				}else{
					jr.setRetCode("9");
					jr.setRetMsg("设置角色的权限信息时发生错误！");
					jr.setRetData("");
				}
			}
		}
		return jr;
	}
	@RequestMapping(value="/removePosts",method = RequestMethod.POST)
	@ResponseBody
	public JResponse removePosts(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strPid = params.get("postid");
			if(StringUtils.isEmpty(strPid)){
				jr.setRetCode("9");
				jr.setRetMsg("缺少要操作的角色ID！");
				jr.setRetData("");
			}else{
				boolean done = csService.removePosts(strPid);
				if(done){
					jr.setRetCode("0");
					jr.setRetMsg("");
				}else{
					jr.setRetCode("9");
					jr.setRetMsg("删除角色信息时发生错误！");
					jr.setRetData("");
				}
			}
		}
		return jr;
	}
	@RequestMapping(value="/savePost",method = RequestMethod.POST)
	@ResponseBody
	public JResponse savePost(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String saveMode = params.get("saveMode");
			String strPostid = params.get("postid");
			int postid = 0;
			try{
				postid = Integer.parseInt(strPostid);
			}catch(Exception e){}
			String postname = params.get("postname");
			String remark = params.get("remark");
			boolean done = csService.savePost(saveMode,postid,postname,remark);
			if(done){
				jr.setRetCode("0");
				jr.setRetMsg("");
			}else{
				jr.setRetCode("9");
				jr.setRetMsg("保存用户的角色信息失败！");
				jr.setRetData("");
			}
		}
		return jr;
	}
}
