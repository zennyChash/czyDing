package com.ifugle.czy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.router.IBeforeRequest;
import com.ifugle.czy.router.IBeforeResponse;
import com.ifugle.czy.router.bean.*;

@Transactional
public class RouterService {
	private static Logger log = Logger.getLogger(RouterService.class);

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
		String uri = svr.getRootURI();
		if(StringUtils.isEmpty(uri)){
			JResponse jr=new JResponse("9","未设置远程服务"+reqService+"的rootURI","");
			return jr;
		}
		//一个前端请求，可以拆分成多个实际的远程请求。
		List reqs = md.getRequests();
		if(reqs==null||reqs.size()==0){
			JResponse jr=new JResponse("9","未设置远程方法"+md.getName()+"对应的请求。","");
			return jr;
		}
		List responsList = new ArrayList();
		for(int i=0;i<reqs.size();i++){
			ProxyRequest req = (ProxyRequest)reqs.get(i);
			String m = req.getMethod();
			String reqDoBefore  = req.getDoBefore();
			//调用预处理 preParser指定的类
			if(!StringUtils.isEmpty(reqDoBefore)){
				IBeforeRequest reqPro = null;
				try{
					reqPro=(IBeforeRequest)Class.forName(reqDoBefore).newInstance();
				}catch(Exception e){
					JResponse jr=new JResponse("9","请求"+md.getName()+"的预处理过程设置错误。","");
					return jr;
				}
				String[] preResult = reqPro.process(reqService, reqMethod, req,svParams, userid);
				//如果预处理结果中，第一个标志位表示是否取消该请求。9表示取消。
				if(preResult==null||"9".equals(preResult[0])){
					continue;
				}
			}
			Map proMap = req.getProperties();
			String subUri = req.getSubURI();
			String wholeUri = uri.endsWith("/")?(uri+subUri):(uri+"/"+subUri);
			//远程调用
			if("POST".equalsIgnoreCase(m)){
				
				
				String response = null;
				responsList.add(response);
			}else if("GET".equalsIgnoreCase(m)){
				
				
				String response = null;
				responsList.add(response);
			}
		}
		if(md.getResponse()!=null){
			String resDoBefore = md.getResponse().getDoBefore();
			//返回前调用response的预处理（ preParser指定的）类，如果一个方法中有多个请求返回，这个回调也负责合并
			if(!StringUtils.isEmpty(resDoBefore)){
				IBeforeResponse responsePro = null;
				try{
					responsePro=(IBeforeResponse)Class.forName(resDoBefore).newInstance();
				}catch(Exception e){
					JResponse jr=new JResponse("9","请求"+md.getName()+"对响应的解析处理器设置错误。","");
					return jr;
				}
				Object result = responsePro.process(reqService, reqMethod, responsList);
				JResponse jr = new JResponse("0","",result);
				return jr;
			}
		}
		//不进行加工转换的，则只取responseList第一个记录，将该对象返回。
		String strRes = (String)responsList.get(0);
		Object resObj = JSON.parse(strRes);
		if(resObj!=null&&resObj.getClass()==JSONArray.class){
			JSONObject o = new JSONObject();
			o.put("list", resObj);
			JResponse jr = new JResponse("0","",o);
			return jr;
		}else{
			JResponse jr = new JResponse("0","",strRes);
			return jr;
		}
	}
}
