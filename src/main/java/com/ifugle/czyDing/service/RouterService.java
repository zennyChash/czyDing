package com.ifugle.czyDing.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.router.IBeforeRequest;
import com.ifugle.czyDing.router.IBeforeResponse;
import com.ifugle.czyDing.router.bean.*;
import com.ifugle.czyDing.utils.HttpHelper;
import com.ifugle.czyDing.utils.JResponse;
import com.ifugle.czyDing.utils.TemplatesLoader;

@Transactional
public class RouterService {
	private static Logger log = Logger.getLogger(RouterService.class);
	@Autowired
	private ApplicationContext applicationContext;
	public JResponse routeRequest(String reqService, String reqMethod,String svParams, String userid) {
		TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
		AppService svr = ltmp.getApiRouter(reqService);
		if(svr==null){
			JResponse jr=new JResponse("9","未找到所请求的服务："+reqService,"");
			return jr;
		}
		AppMethod md = svr.getMethod(reqMethod);
		if(md==null){
			JResponse jr=new JResponse("9","未找到所请求的方法，服务名:"+reqService+",方法名:"+reqMethod,"");
			return jr;
		}
		String rootUri = svr.getRootURI();
		if(StringUtils.isEmpty(rootUri)){
			JResponse jr=new JResponse("9","未设置远程服务"+reqService+"的rootURI","");
			return jr;
		}
		//一个前端请求，可以拆分成多个实际的远程请求。
		List reqs = md.getRequests();
		if(reqs==null||reqs.size()==0){
			JResponse jr=new JResponse("9","未设置远程方法"+md.getName()+"对应的请求。","");
			return jr;
		}
		JSONObject responses = new JSONObject();
		for(int i=0;i<reqs.size();i++){
			ProxyRequest req = (ProxyRequest)reqs.get(i);
			String m = req.getMethod();
			String reqDoBefore  = req.getDoBefore();
			String reqParams = svParams;
			//调用预处理 preParser指定的类
			if(!StringUtils.isEmpty(reqDoBefore)){
				IBeforeRequest reqPro = null;
				try{
					reqPro=(IBeforeRequest)applicationContext.getBean(reqDoBefore);
				}catch(Exception e){
					JResponse jr=new JResponse("9","请求"+md.getName()+"的预处理过程设置错误。","");
					return jr;
				}
				String[] preResult = reqPro.process(reqService, reqMethod, req,svParams, userid);
				//如果预处理结果中，第一个标志位表示是否取消该请求。9表示取消。
				if(preResult==null||"9".equals(preResult[0])){
					continue;
				}
				reqParams = preResult[1];
			}
			Map proMap = req.getProperties();
			//远程调用
			if("POST".equalsIgnoreCase(m)){
				JSONObject response = HttpHelper.httpPost(rootUri,req,reqParams);
				String rKey = req.getReturnProperty();
				responses.put(StringUtils.isEmpty(rKey)?"_RETURNED":rKey,response);
			}else if("GET".equalsIgnoreCase(m)){
				JSONObject response = HttpHelper.httpGet(rootUri,req,reqParams);
				String rKey = req.getReturnProperty();
				responses.put(StringUtils.isEmpty(rKey)?"_RETURNED":rKey,response);
			}
		}
		if(md.getResponse()!=null){
			String resDoBefore = md.getResponse().getDoBefore();
			//返回前调用response的预处理（ preParser指定的）类，如果一个方法中有多个请求返回，这个回调也负责合并
			if(!StringUtils.isEmpty(resDoBefore)){
				IBeforeResponse responsePro = null;
				try{
					responsePro=(IBeforeResponse)applicationContext.getBean(resDoBefore);
				}catch(Exception e){
					JResponse jr=new JResponse("9","请求"+md.getName()+"对响应的解析处理器设置错误。","");
					return jr;
				}
				JResponse jr = responsePro.process(reqService,reqMethod,md.getResponse(), responses);
				return jr;
			}
		}
		//不进行加工转换的，也提供默认的处理。默认远程系统返回的错误码属性errcode，错误信息errmsg
		JResponse jr = null;
		JSONObject data = new JSONObject();
		for(String key :responses.keySet()){
			JSONObject strRes = (JSONObject)responses.get(key);
			//如果多个记录集，只要有一个中有错误代码，就组织成财智云的错误信息格式立即返回。
			if(strRes.containsKey("errcode")){
				jr = new JResponse("9",strRes.getString("errmsg"),"");
				return jr;
			}
			//之前为了将多个返回统一处理，对于没设置returnProperty的设置了默认的_RETURNED属性，此处解除该外包属性。
			if("_RETURNED".equals(key)){
				data.putAll(strRes);
			}else{
				data.put(key, strRes);
			}
		}
		jr.setRetCode("0");
		jr.setRetData(data);
		return jr;
	}
}
