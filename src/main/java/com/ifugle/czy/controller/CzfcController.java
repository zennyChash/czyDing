package com.ifugle.czy.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ifugle.czy.service.CzfcService;

@Controller
public class CzfcController {
	private static Logger log = Logger.getLogger(CzfcController.class);
	@Autowired
	private CzfcService czfcService;
	
	@RequestMapping(value="/getApprovalLists2Check",method = RequestMethod.POST)
	@ResponseBody
	public Map getApprovalLists2Check(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String sort = params.get("sort");
			String dir = params.get("dir");
			String sstart = params.get("start");
			String slimit = params.get("limit");
			int start = 0,limit=0;
			try{
				start =Integer.parseInt(sstart);
			}catch(Exception e){}
			try{
				limit =Integer.parseInt(slimit);
			}catch(Exception e){}
			String qParams = params.get("params");
			String userid = params.get("userid");
			infos = czfcService.getApprovalLists2Check(0,userid,qParams,sort,dir,start,limit);
		}
		return infos;
	}
	@RequestMapping(value="/getApprovalListsChecked",method = RequestMethod.POST)
	@ResponseBody
	public Map getApprovalListsChecked(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String sort = params.get("sort");
			String dir = params.get("dir");
			String sstart = params.get("start");
			String slimit = params.get("limit");
			int start = 0,limit=0;
			try{
				start =Integer.parseInt(sstart);
			}catch(Exception e){}
			try{
				limit =Integer.parseInt(slimit);
			}catch(Exception e){}
			String qParams = params.get("params");
			String userid = params.get("userid");
			infos = czfcService.getApprovalLists2Check(1,userid,qParams,sort,dir,start,limit);
		}
		return infos;
	}
	
	//根据审批单id获取单个审批单信息
	@RequestMapping(value="/getApprovalListById",method = RequestMethod.POST)
	@ResponseBody
	public Map getApprovalListById(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String slid = params.get("lid");
			long lid = 0; 
			try{
				lid = Long.parseLong(slid);
			}catch(Exception e){}
			String userid = params.get("userid");
			infos = czfcService.getApprovalListById(userid,lid);
		}
		return infos;
	}
	@RequestMapping(value="/checkAppByList",method = RequestMethod.POST)
	@ResponseBody
	public Map checkAppByList(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String doType = params.get("doType");
			String slid = params.get("lid");
			long lid = 0; 
			try{
				lid = Long.parseLong(slid);
			}catch(Exception e){}
			String remark = params.get("remark");
			String userid = params.get("userid");
			infos = czfcService.checkAppByList(userid,doType,lid,remark);
		}
		return infos;
	}
	@RequestMapping(value="/getAppDetailsInList",method = RequestMethod.POST)
	@ResponseBody
	public Map getAppDetailsInList(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String sort = params.get("sort");
			String dir = params.get("dir");
			String sstart = params.get("start");
			String slimit = params.get("limit");
			int start = 0,limit=0;
			try{
				start =Integer.parseInt(sstart);
			}catch(Exception e){}
			try{
				limit =Integer.parseInt(slimit);
			}catch(Exception e){}
			String qParams = params.get("params");
			String userid = params.get("userid");
			infos = czfcService.getAppDetailsInList(userid,qParams,sort,dir,start,limit);
		}
		return infos;
	}
	@RequestMapping(value="/getCommentsOfAppList",method = RequestMethod.POST)
	@ResponseBody
	public Map getCommentsOfAppList(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		if(params!=null){
			String slid = params.get("lid");
			long lid = 0; 
			try{
				lid = Long.parseLong(slid);
			}catch(Exception e){}
			String userid = params.get("userid");
			infos = czfcService.getCommentsOfAppList(userid,lid);
		}
		return infos;
	}
	@RequestMapping(value="/sendCzfcCheckMsgAuto",method = RequestMethod.POST)
	@ResponseBody
	public Map sendCzfcCheckMsgAuto(@RequestParam Map<String, String> params){
		Map infos = new HashMap();
		infos = czfcService.sendCzfcCheckMsgAuto();
		return infos;
	}
}
