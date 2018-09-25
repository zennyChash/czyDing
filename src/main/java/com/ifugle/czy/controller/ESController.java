package com.ifugle.czy.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.utils.bean.DataSourceJson;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.JOutput;
@Controller
public class ESController {
	private static Logger log = Logger.getLogger(ESController.class);
	@Autowired
	private ESQueryDataService esDataService;
	@Autowired
	private ESDataSourceService esDtSrcServicev;
	
	@RequestMapping(value="/queryData",method = RequestMethod.POST)
	@ResponseBody
	public JResponse queryESData(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			JSONObject qParams = params.parseJRptParams();
			Map data = null;
			try{
				data = esDataService.getData(rptID,qParams);
			}catch(Exception e){
				jr = new JResponse("9","查询数据时发生异常，未能查找到数据。",null);
			}
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
			log.info(rptID+"的输出:"+jr.toString());
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
			boolean reIndex = params.getBoolean("reIndex");
			boolean deleteOldData = params.getBoolean("deleteOldData");
			if(StringUtils.isEmpty(dsID)){
				return new JResponse("9","未找到数据源的ID",null);
			}else{
				//索引名一律转化为小写
				String data = esDtSrcServicev.indexData(dsID.toLowerCase(),reIndex,deleteOldData,params);
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
	@RequestMapping(value="/searchForWord",method = RequestMethod.POST)
	@ResponseBody
	public JResponse searchForWord(@RequestBody RptDataJson params){
		JResponse jr = null;
		if(params!=null){
			String rptID = params.getRptID();
			if(StringUtils.isEmpty(rptID)){
				return new JResponse("9","未设置索引的类型！",null);
			}else{
				Map data = esDataService.searchByKeyWord(rptID,params);
				jr = new JResponse("0","",data);
			}
		}else{
			jr = new JResponse("9","加载参数选项失败，没有获得正确的请求参数！",null);
		}
		return jr;
	}
	
	
	
	
	
	
	
	@RequestMapping(value="/indexAllData2ES",method = RequestMethod.POST)
	@ResponseBody
	public JResponse indexAllData2ES(@RequestBody DataSourceJson jds){
		JResponse jr = null;
		List dts = TemplatesLoader.getTemplatesLoader().getDataSrcTemplates();
		if(dts!=null){
			for(int i=0;i<dts.size();i++){
				DataSrc ds = (DataSrc)dts.get(i);
				String dsID = ds.getId();
				JSONObject params = jds.parseJDsParams();
				boolean reIndex = params.getBoolean("reIndex");
				boolean deleteOldData = params.getBoolean("deleteOldData");
				if(StringUtils.isEmpty(dsID)){
					log.info("未找到数据源的ID:"+dsID);
				}else{
					//索引名一律转化为小写
					String data = esDtSrcServicev.indexData(dsID.toLowerCase(),reIndex,deleteOldData,params);
					log.info(data);
				}
				log.info("*****************");
			}
		}
		jr = new JResponse("0","","{done:true,info:'共生成"+(dts==null?"0":dts.size())+"个数据源索引'}");
		return jr;
	}
	
	@RequestMapping(value="/testQueryData",method = RequestMethod.POST)
	@ResponseBody
	public JResponse testQueryESData(@RequestBody RptDataJson params){
		JResponse jr = null;
		List jps = TemplatesLoader.getTemplatesLoader().getJSONOutputTemplates();
		if(jps!=null){
			for(int i=0;i<jps.size();i++){
				JOutput joutput = (JOutput)jps.get(i);
				String rptID = joutput.getId();
				JSONObject qParams = params.parseJRptParams();
				Map data = esDataService.getData(rptID,qParams);
				if(data!=null&&data.containsKey("done")){
					boolean done = (Boolean)data.get("done");
					if(done){
						JSONObject jdata = (JSONObject)data.get("jpData");
						log.info(rptID+"的输出:"+jdata.toString());
					}else{
						String info = (String)data.get("info");
						log.info(rptID+"取数发生错误:"+info);
					}
				}else{
					jr = new JResponse("9","获取页面数据失败！",null);
					log.info(rptID+"获取页面数据失败！");
				}
				log.info("*****************");
			}
		}
		jr = new JResponse("0","","{done:true,info:'共输出"+(jps==null?"0":jps.size())+"个数据展示结果'}");
		return jr;
	}
}
