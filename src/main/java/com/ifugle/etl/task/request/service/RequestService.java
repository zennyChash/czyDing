package com.ifugle.etl.task.request.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.etl.entity.task.RequestTask;

public class RequestService {
	public static final Logger log = LoggerFactory.getLogger(RequestService.class);
	public int doRequest(RequestTask req,Map params, Map paramVals){
	   	if(req==null){
			log.error("任务配置错误：未配置请求相关信息！");
			return 9;
	   	}
	   	int flag=0;
	   	if (StringUtils.isEmpty(req.getUri())) {  
	   		log.error("请求的uri未设置！"); 
	       	return 9;
	   	}
	   	try{
	   		String m = req.getMethod();
			String reqDoBefore  = req.getBeforeReq();
			//调用预处理 preParser指定的类
			if(!StringUtils.isEmpty(reqDoBefore)){
				IRequestBefore reqPro = null;
				try{
					reqPro=(IRequestBefore)Class.forName(reqDoBefore).newInstance();
					String[] preResult = reqPro.process(req, params, paramVals);
					//如果预处理结果中，第一个标志位表示是否取消该请求。9表示取消。
					if(preResult==null||"9".equals(preResult[0])){
						return 5;
					}
				}catch(Exception e){
					log.error("任务"+req.getId()+"请求预处理过程设置错误。");
					return 9;
				}
			}
			//远程调用
			JSONObject response = null;
			if("POST".equalsIgnoreCase(m)){
				if("syn".equals(req.getMethod())){
					response = httpPost(req,params, paramVals);
				}else{
					httpAsynPost(req,params, paramVals);
					return 1;
				}
			}else if("GET".equalsIgnoreCase(m)){
				if("syn".equals(req.getMethod())){
					response = httpGet(req,params, paramVals);
				}else{
					httpAsynGet(req,params, paramVals);
					return 1;
				}
			}
			if(!StringUtils.isEmpty(req.getBeforeResponse())){
				String resDoBefore = req.getBeforeResponse();
				IResponseBefore responsePro = null;
				try{
					responsePro=(IResponseBefore)Class.forName(resDoBefore).newInstance();
				}catch(Exception e){
					log.error("任务"+req.getId()+"对响应的解析处理器设置错误。");
					return 9;
				}
				response = responsePro.process(req,response);
			}
			if(response==null||response.containsKey("errcode")){
				log.error("任务"+req.getId()+"请求失败。错误码："+response.getString("errcode")+"，错误信息："+response.getString("errmsg"));
				return 9;
			}
	   		flag=1;
        }catch(Exception e){
        	log.error(e.toString());
        	flag = 9;
        }
        return flag;
	}
	private JSONObject httpGet(RequestTask req, Map params, Map paramVals) {
		HttpGet httpGet = new HttpGet(req.getUri());
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        int isout = req.getSocketTimeout();
        int icout = req.getConnTimeout();
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(isout).setConnectTimeout(icout).build();
        httpGet.setConfig(requestConfig);
        //设置头信息
        Map props = req.getProps();
        if(props!=null){
	        Iterator iter = props.entrySet().iterator();
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    String key = (String)entry.getKey(); 
			    String val = (String)entry.getValue(); 
			    httpGet.setHeader(key, val);
			} 
        }
        try {
            response = httpClient.execute(httpGet, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                   + ", url=" + req.getUri());
                JSONObject res = new JSONObject();
                try{
                	res.put("errcode", "9");
                    res.put("errmsg", "远程服务连接错误，错误码:"+response.getStatusLine().getStatusCode());
                }catch (Exception e) {}
                return res;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                JSONObject result = null;
                try{
                	result = JSON.parseObject(resultStr);
                }catch (Exception e) {}
                return result;
            }
        } catch (IOException e) {
            System.out.println("request url=" + req.getUri() + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
	}
	private JSONObject httpPost(RequestTask req, Map params, Map paramVals) {
		String url = req.getUri();
		HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        int isout = req.getSocketTimeout();
        int icout = req.getConnTimeout();
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(isout).setConnectTimeout(icout).build();
        httpPost.setConfig(requestConfig);
        
        //设置头信息
        Map props = req.getProps();
        if(props!=null){
	        Iterator iter = props.entrySet().iterator();
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    String key = (String)entry.getKey(); 
			    String val = (String)entry.getValue(); 
			    httpPost.setHeader(key, val);
			} 
        }
        if(!props.containsKey("Content-Type")){//post方式提交，Content-Type不设置时，默认为application/json
        	httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        }
        try {
        	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        	if(paramVals!=null){
        		Set set = paramVals.keySet();
	        	Iterator iterator = set.iterator();
	        	while (iterator.hasNext()) {
		        	Object key = iterator.next();
		        	Object value = paramVals.get(key);
		        	formparams.add(new BasicNameValuePair(key.toString(), value.toString()));
	        	}
        	}
        	httpPost.setEntity(new UrlEncodedFormEntity(formparams,"utf-8"));
            response = httpClient.execute(httpPost, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                   + ", url=" + url);
                JSONObject res = new JSONObject();
                try{
                	res.put("errcode", "9");
                	res.put("errmsg", "远程服务连接错误，错误码:"+response.getStatusLine().getStatusCode());
                }catch (Exception e){}
                return res;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                JSONObject result = null ;
                try{
                	result = JSON.parseObject(resultStr);
                }catch (Exception e){}
                return result;
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
            JSONObject res = new JSONObject();
            try{
            	res.put("errcode", "9");
            	res.put("errmsg", "远程服务的IO发生错误，具体情况请查看日志！");
            }catch (Exception ex) {}
            return res;
        } finally {
            if (response != null) try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
	}
	
	
	
	/** 
     * http async get 
     * 
     * @param url 
     * @param data 
     * @return 
     */  
    private void httpAsynGet(RequestTask req, Map params, Map paramVals) {  
        CookieStore cookieStore = new BasicCookieStore();  
        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();  
        httpClient.start();  
        HttpGet httpGet = new HttpGet(req.getUri());  
        //httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");  
        try {  
            httpClient.execute(httpGet, new FutureCallback<HttpResponse>() {  
                @Override  
                public void completed(HttpResponse result) {  
                    String body="";  
                    //这里使用EntityUtils.toString()方式时会大概率报错，原因：未接受完毕，链接已关  
                    try {  
                        HttpEntity entity = result.getEntity();  
                        if (entity != null) {  
                            final InputStream instream = entity.getContent();  
                            try {  
                                final StringBuilder sb = new StringBuilder();  
                                final char[] tmp = new char[1024];  
                                final Reader reader = new InputStreamReader(instream,"UTF-8");  
                                int l;  
                                while ((l = reader.read(tmp)) != -1) {  
                                    sb.append(tmp, 0, l);  
                                }  
                                body = sb.toString();  
                                System.out.println(body);  
                            } finally {  
                                instream.close();  
                                EntityUtils.consume(entity);  
                            }  
                        }  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }finally {  
                        close(httpClient);  
                    }  
                }  
                @Override  
                public void failed(Exception ex) {  
                    System.out.println(ex.toString());  
                    close(httpClient);  
                }  
  
                @Override  
                public void cancelled() {  
  
                }  
            });  
        } catch (Exception e) {  
        }  
        System.out.println("end-----------------------");  
    }  
  
    /** 
     * http async post 
     * 
     * @param url 
     * @param values 
     * @return 
     */  
    private void httpAsynPost(RequestTask req, Map params, Map paramVals) {  
        String url = req.getUri();
        CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();  
        HttpPost httpPost = new HttpPost(url);
        //设置头信息
        Map props = req.getProps();
        if(props!=null){
	        Iterator iter = props.entrySet().iterator();
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    String key = (String)entry.getKey(); 
			    String val = (String)entry.getValue(); 
			    httpPost.setHeader(key, val);
			} 
        }
        if(!props.containsKey("Content-Type")){//post方式提交，Content-Type不设置时，默认为application/json
        	httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        }
        try {
        	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        	if(paramVals!=null){
        		Set set = paramVals.keySet();
	        	Iterator iterator = set.iterator();
	        	while (iterator.hasNext()) {
		        	Object key = iterator.next();
		        	Object value = paramVals.get(key);
		        	formparams.add(new BasicNameValuePair(key.toString(), value.toString()));
	        	}
        	}
        	httpPost.setEntity(new UrlEncodedFormEntity(formparams,"utf-8"));
        	//start
        	httpClient.start();
            httpClient.execute(httpPost, new FutureCallback<HttpResponse>() {  
                @Override  
                public void completed(HttpResponse result) {  
                    System.out.println(result.toString());  
                }  
                @Override  
                public void failed(Exception ex) {  
                    System.out.println(ex.toString());  
                }  
                @Override  
                public void cancelled() {  
                }  
            });  
        } catch (Exception e) {  
        }  
    }  
  
    private static void close(CloseableHttpAsyncClient client) {  
        try {  
            client.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * 直接把Response内的Entity内容转换成String 
     * 
     * @param httpResponse 
     * @return 
     */  
    public static String toString(CloseableHttpResponse httpResponse) {  
        // 获取响应消息实体  
        String result = null;  
        try {  
            HttpEntity entity = httpResponse.getEntity();  
            if (entity != null) {  
                result = EntityUtils.toString(entity, "UTF-8");  
            }  
        } catch (Exception e) {  
        } finally {  
            try {  
                httpResponse.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return result;  
    }  
}
