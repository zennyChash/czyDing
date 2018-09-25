package com.ifugle.czy.controller;

import java.util.HashMap;
import java.util.List;
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

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.service.ConsoleServeice;
import com.ifugle.czy.service.ESDataSourceService;
import com.ifugle.czy.service.ESQueryDataService;
import com.ifugle.czy.service.ExecuteScriptRunnable;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.utils.Configuration;

@Controller
public class ConsoleScriptController {
	private static Logger log = Logger.getLogger(ConsoleScriptController.class);
	@Autowired
	private ConsoleServeice csService;
	@Autowired
	private Configuration cg;
	@Autowired
	private ESQueryDataService esDataService;
	@Autowired
	private ESDataSourceService esDtSrcService;
	
	@RequestMapping(value="/refreshDtSrcTemplates",method = RequestMethod.POST)
	@ResponseBody
	public JResponse refreshDtSrcTemplates(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		String[] infos = csService.refreshDtSrcTemplates();
		if(infos!=null&&"1".equals(infos[0])){
			jr.setRetCode("0");
			jr.setRetMsg("");
		}else if(infos!=null&&!StringUtils.isEmpty(infos[1])){
			jr.setRetCode("9");
			jr.setRetMsg(infos[1]);
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("重载模板时发生错误！");
			jr.setRetData("");
		}
		return jr;
	}
	@RequestMapping(value="/getDtSrcTemplates",method = RequestMethod.GET)
	@ResponseBody
	public Map getDtsrcTemplates(@RequestParam Map<String, String> params){
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
			
			infos = csService.getDtsrcTemplates(start,limit);
		}
		return infos;
	}
	
	@RequestMapping(value="/executeDataScripts",method = RequestMethod.POST)
	@ResponseBody
	public JResponse executeDataScripts(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String strIds = params.get("dtIds");
			if(StringUtils.isEmpty(strIds)){
				jr.setRetCode("9");
				jr.setRetMsg("没有要执行的脚本！");
				jr.setRetData("");
			}else{
				String sdeleteOld= params.get("deleteOldData");
				String sReIndex= params.get("reIndex");
				boolean reIndex = false,deleteOldData=false;
				if("1".equals(sReIndex)){
					reIndex = true;
				}
				if("1".equals(sdeleteOld)){
					deleteOldData = true;
				}
				String[] dtIds = strIds.split(",");
				String tid = String.valueOf(System.currentTimeMillis());
				cg.buildTaskLog(tid);
		    	cg.setTaskStatus(tid, 0);
		    	cg.buildTaskScriptsCounts(tid);
				//将任务交给另一个线程去做
				ExecuteScriptRunnable exeScript = new ExecuteScriptRunnable(tid,dtIds,reIndex,deleteOldData);
				new Thread(exeScript).start();
				//请求立刻返回
				jr.setRetCode("0");
				jr.setRetMsg("");
				JSONObject jdata = new JSONObject();
				jdata.put("tid", tid);
				jr.setRetData(jdata);
			}
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("缺少参数，无法执行！");
			jr.setRetData("");
		}
		return jr;
	}
	
	@RequestMapping(value="/getDataScriptsLog",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getDataScriptsLog(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String tid = params.get("tid");
			String sStart = params.get("start");
			String sLimit = params.get("limit");
			int start=0,limit=3;
			try{
				start = Integer.parseInt(sStart);
			}catch(Exception e){}
			try{
				limit = Integer.parseInt(sLimit);
			}catch(Exception e){}
			if(StringUtils.isEmpty(tid)){
				jr.setRetCode("9");
				jr.setRetMsg("未指定要查看的任务ID！");
				jr.setRetData("");
			}else{
				Map logInfos = csService.getDataScriptsLog(tid,start,limit);
				jr.setRetCode("0");
				jr.setRetMsg("");
				jr.setRetData(logInfos);
			}
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("缺少参数，无法执行！");
			jr.setRetData("");
		}
		return jr;
	}
	
	@RequestMapping(value="/refreshResponseTemplates",method = RequestMethod.POST)
	@ResponseBody
	public JResponse refreshResponseTemplates(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		String[] infos = csService.refreshResponseTemplates();
		if(infos!=null&&"1".equals(infos[0])){
			jr.setRetCode("0");
			jr.setRetMsg("");
		}else if(infos!=null&&!StringUtils.isEmpty(infos[1])){
			jr.setRetCode("9");
			jr.setRetMsg(infos[1]);
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("重载模板时发生错误！");
			jr.setRetData("");
		}
		return jr;
	}
	
	@RequestMapping(value="/getOutPutTemplates",method = RequestMethod.GET)
	@ResponseBody
	public Map getOutPutTemplates(@RequestParam Map<String, String> params){
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
			
			infos = csService.getOutPutTemplates(start,limit);
		}
		return infos;
	}
	
	@RequestMapping(value="/getParamsOfResponseTemplate",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getParamsOfResponseTemplate(@RequestParam Map<String, String> params){
		JResponse jr = new JResponse();
		if(params!=null){
			String id = params.get("id");
			if(StringUtils.isEmpty(id)){
				jr.setRetCode("9");
				jr.setRetMsg("未指定要查询的模板ID！");
				jr.setRetData("");
			}else{
				List flds= csService.getParamsOfResponseTemplate(id);
				jr.setRetCode("0");
				jr.setRetMsg("");
				jr.setRetData(flds);
			}
		}else{
			jr.setRetCode("9");
			jr.setRetMsg("缺少参数，无法执行！");
			jr.setRetData("");
		}
		return jr;
	}
	@RequestMapping(value="/parsedResponseTemplate",method = RequestMethod.POST)
	@ResponseBody
	public JResponse parsedResponseTemplate(@RequestParam Map<String, String> params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.get("id");
			String strParams =params.get("rptParams");
			JSONObject qParams = new JSONObject();
			if(!StringUtils.isEmpty(strParams)){
				qParams = JSONObject.parseObject(strParams);
			}
			Map data = null;
			try{
				data = esDataService.getData(rptID,qParams);
			}catch(Exception e){
				jr = new JResponse("9","查询数据时发生异常，未能查找到数据。",null);
			}
			if(data!=null&&data.containsKey("done")){
				boolean done = (Boolean)data.get("done");
				if(done){
					JSONObject infos = new JSONObject();
					infos.put("params", qParams);
					JSONObject jdata = (JSONObject)data.get("jpData");
					infos.put("parsedResponse", jdata);
					jr = new JResponse("0","",infos);
				}else{
					String info = (String)data.get("info");
					jr = new JResponse("9",info,null);
				}
			}else{
				jr = new JResponse("9","获取页面数据失败！",null);
			}
			log.info(rptID+"的输出:"+jr.toString());
		}else{
			jr = new JResponse("9","获取报表数据失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
}
