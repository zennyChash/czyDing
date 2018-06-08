package com.ifugle.czy.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.service.ESDataSourceService;
import com.ifugle.czy.service.ESQueryDataService;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.bean.DataSourceJson;
import com.ifugle.czy.utils.bean.RptDataJson;
@Controller
public class ESController {
	@Autowired
	private ESQueryDataService esDataService;
	@Autowired
	private ESDataSourceService esDtSrcServicev;
	
	@RequestMapping(value="/queryESData",method = RequestMethod.POST)
	@ResponseBody
	public JResponse queryESData(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			JSONObject qParams = params.parseJRptParams();
			Map data = esDataService.getData(rptID,qParams);
			if(data!=null&&data.containsKey("done")){
				boolean done = (Boolean)data.get("done");
				if(done){
					JSONObject jdata = (JSONObject)data.get("jpData");
					jr = new JResponse("0","",jdata);
				}else{
					String info = (String)data.get("info");
					jr = new JResponse("9",info,null);
				}
			}else{
				jr = new JResponse("9","获取页面数据失败！",null);
			}
		}else{
			jr = new JResponse("9","获取报表数据失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/indexData2ES",method = RequestMethod.POST)
	@ResponseBody
	public JResponse indexData2ES(@RequestBody DataSourceJson jds){
		JResponse jr = null;
		if(jds!=null){
			String dsID = jds.getDsID();
			JSONObject params = jds.parseJDsParams();
			boolean reMapping = params.getBoolean("reMapping");
			boolean deleteOldData = params.getBoolean("deleteOldData");
			if(StringUtils.isEmpty(dsID)){
				return new JResponse("9","未找到数据源的ID",null);
			}else{
				//索引名一律转化为小写
				String data = esDtSrcServicev.indexData(dsID.toLowerCase(),reMapping,deleteOldData,params);
				jr = new JResponse("0","",data);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	@RequestMapping(value="/deleteESIndex",method = RequestMethod.POST)
	@ResponseBody
	public JResponse deleteESIndex(@RequestBody DataSourceJson jds){
		JResponse jr = null;
		if(jds!=null){
			String dsID = jds.getDsID();
			Map params = jds.parseJDsParams();
			if(StringUtils.isEmpty(dsID)){
				return new JResponse("9","未找到数据源的ID",null);
			}else{
				//索引名一律转化为小写
				String data = esDtSrcServicev.delelteIndex(dsID.toLowerCase());
				jr = new JResponse("0","",data);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
}
