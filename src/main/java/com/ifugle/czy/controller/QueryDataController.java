package com.ifugle.czy.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import com.ifugle.czy.service.ESQueryDataService;
import com.ifugle.czy.service.ReportDataEsService;
import com.ifugle.czy.service.ReportDataService;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.bean.DeleteUserObj;
import com.ifugle.czy.utils.bean.QueryUserObj;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.czy.utils.bean.SaveUserObj;
import com.ifugle.czy.utils.bean.User;

@Controller
public class QueryDataController {
	@Autowired
	private ReportDataService rptDataService;
	@Autowired
	private ReportDataEsService rptEsService;
	
	@RequestMapping(value="/queryData",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getRptData(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			Map data = rptDataService.getData(rptID,params);
			jr = new JResponse("0","",data);
		}else{
			jr = new JResponse("9","获取报表数据失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	
	@RequestMapping(value="/paramOptions",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getParamOptions(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			Map data = rptDataService.getParamOptions(rptID,params);
			jr = new JResponse("0","",data);
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/buildIndex",method = RequestMethod.POST)
	@ResponseBody
	public JResponse buildIndex(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			if(StringUtils.isEmpty(rptID)){
				return new JResponse("9","未设置索引的类型！",null);
			}else{
				String data = rptEsService.addIndexAndDocumentEn("rptdata", rptID.toLowerCase(),params);
				jr = new JResponse("0","",data);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/deleteIndex",method = RequestMethod.POST)
	@ResponseBody
	public JResponse deleteIndex(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			if(StringUtils.isEmpty(rptID)){
				return new JResponse("9","未设置索引的类型！",null);
			}else{
				String data = rptEsService.deleteIndex("rptdata");
				jr = new JResponse("0","",data);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/searchForWord",method = RequestMethod.POST)
	@ResponseBody
	public JResponse searchForWord(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			if(StringUtils.isEmpty(rptID)){
				return new JResponse("9","未设置索引的类型！",null);
			}else{
				Map data = rptEsService.searchForWord("rptdata", rptID.toLowerCase(),params);
				jr = new JResponse("0","",data);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/saveUserInfo",method = RequestMethod.POST)
	@ResponseBody
	public JResponse saveUserInfo(@RequestBody SaveUserObj so){
		JResponse jr = null;
		if(so!=null){
			JSONObject jsave = so.parseSaveContent();
			if(jsave==null){
				return new JResponse("9", "未找到要保存的信息！",null);
			}
			User user = null;
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				user = (User)request.getSession().getAttribute("user");
			}
			if(user==null&&StringUtils.isEmpty(so.getUserid())){
				return new JResponse("9", "未知的用户账户！",null);
			}else if(user!=null){
				so.setUserid(user.getUserid());
			}
			Map result = rptDataService.saveUserInfo(so);
			if((boolean)result.get("saved")){
				jr = new JResponse("0","",result);
			}else{
				jr = new JResponse("9",(String)result.get("msg"),null);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	
	@RequestMapping(value="/deleteUserInfo",method = RequestMethod.POST)
	@ResponseBody
	public JResponse deleteUserInfo(@RequestBody DeleteUserObj dobj){
		JResponse jr = null;
		if(dobj!=null){
			JSONObject jdel = dobj.parseDeleteContent();
			if(jdel==null){
				return new JResponse("9", "未找到要删除的信息！",null);
			}
			User user = null;
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				user = (User)request.getSession().getAttribute("user");
			}
			if(user==null&&StringUtils.isEmpty(dobj.getUserid())){
				return new JResponse("9", "未知的用户账户！",null);
			}else if(user!=null){
				dobj.setUserid(user.getUserid());
			}
			Map result = rptDataService.deleteUserInfo(dobj);
			if((boolean)result.get("deleted")){
				jr = new JResponse("0","",result);
			}else{
				jr = new JResponse("9",(String)result.get("msg"),null);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	
	@RequestMapping(value="/getUserInfo",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getUserInfo(@RequestBody QueryUserObj qo){
		JResponse jr = null;
		if(qo!=null){
			JSONObject jq = qo.parseQueryContent();
			if(jq==null){
				return new JResponse("9", "未设置要查询的信息类型！",null);
			}
			User user = null;
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				user = (User)request.getSession().getAttribute("user");
			}
			if(user==null&&StringUtils.isEmpty(qo.getUserid())){
				return new JResponse("9", "未知的用户账户！",null);
			}else if(user!=null){
				qo.setUserid(user.getUserid());
			}
			Map data = rptDataService.getUserInfo(qo);
			jr = new JResponse("0","",data);
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
}
