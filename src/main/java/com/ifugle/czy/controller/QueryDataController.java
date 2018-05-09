package com.ifugle.czy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ifugle.czy.service.ReportDataService;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.bean.RptDataJson;

@Controller
public class QueryDataController {
	@Autowired
	private ReportDataService rptDataService;

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
}
