package com.ifugle.czy.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.router.bean.ProxyRequest;

/**
 * HTTP请求封装，建议直接使用sdk的API
 */
public class HttpHelper {
	
	public static JSONObject httpGet(String rootUri,ProxyRequest req,String svParams){
		JSONObject res = null;
		StringBuffer wholeUri = new StringBuffer(rootUri);
		String subUri = req.getSubURI();
		wholeUri.append(rootUri.endsWith("/")?subUri:("/"+subUri));
		if(!StringUtils.isEmpty(svParams)){
			JSONObject param = JSON.parseObject(svParams);
			Iterator iter = param.entrySet().iterator();
			wholeUri.append("?");
			while (iter.hasNext()) { 
			    Map.Entry entry = (Map.Entry) iter.next(); 
			    String key = (String)entry.getKey(); 
			    String val = (String)entry.getValue(); 
			    wholeUri.append("&").append(key).append("=").append(val);
			} 
		}
		try{
			res = httpGet(wholeUri.toString(),req);
        } catch (Exception e) {
        } finally {
        }
        return res;
    }
	public static JSONObject httpPost(String rootUri,ProxyRequest req,String svParams){
		JSONObject res = null;
		StringBuffer wholeUri = new StringBuffer(rootUri);
		String subUri = req.getSubURI();
		wholeUri.append(rootUri.endsWith("/")?subUri:("/"+subUri));
		JSONObject data = JSON.parseObject(svParams);
		try{
			res = httpPost(wholeUri.toString(),req,data);
        } catch (Exception e) {
        } finally {
        }
		return res;
	}
	public static JSONObject httpGet(String url,ProxyRequest req) throws Exception{
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String stout = req.getSocketTimeout();
        String ctout = req.getConnTimeout();
        int isout = 2000,icout = 2000;
        try{
        	isout = Integer.parseInt(stout);
        }catch(Exception e){}
        try{
        	icout = Integer.parseInt(ctout);
        }catch(Exception e){}
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(isout).setConnectTimeout(icout).build();
        httpGet.setConfig(requestConfig);
        //设置头信息
        Map props = req.getProperties();
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
                                   + ", url=" + url);
                JSONObject res = new JSONObject();
                res.put("errcode", "9");
                res.put("errmsg", "远程服务连接错误，错误码:"+response.getStatusLine().getStatusCode());
                return res;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                JSONObject result = JSON.parseObject(resultStr);
                return result;
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
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
	public static JSONObject httpPost(String url,ProxyRequest req, JSONObject data) throws OApiException {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String stout = req.getSocketTimeout();
        String ctout = req.getConnTimeout();
        int isout = 2000,icout = 2000;
        try{
        	isout = Integer.parseInt(stout);
        }catch(Exception e){}
        try{
        	icout = Integer.parseInt(ctout);
        }catch(Exception e){}
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(isout).setConnectTimeout(icout).build();
        httpPost.setConfig(requestConfig);
        
        //设置头信息
        Map props = req.getProperties();
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
        	if(data!=null){
        		Set set = data.keySet();
	        	Iterator iterator = set.iterator();
	        	while (iterator.hasNext()) {
		        	Object key = iterator.next();
		        	Object value = data.get(key);
		        	formparams.add(new BasicNameValuePair(key.toString(), value.toString()));
	        	}
        	}
        	
        	httpPost.setEntity(new UrlEncodedFormEntity(formparams,"utf-8"));
            response = httpClient.execute(httpPost, new BasicHttpContext());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                   + ", url=" + url);
                JSONObject res = new JSONObject();
                res.put("errcode", "9");
                res.put("errmsg", "远程服务连接错误，错误码:"+response.getStatusLine().getStatusCode());
                return res;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                JSONObject result = JSON.parseObject(resultStr);
                return result;
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
            JSONObject res = new JSONObject();
            res.put("errcode", "9");
            res.put("errmsg", "远程服务的IO发生错误，具体情况请查看日志！");
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
	
	
	public static JSONObject uploadMedia(String url, File file) throws OApiException {
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpPost.setConfig(requestConfig);

        HttpEntity requestEntity = MultipartEntityBuilder.create().addPart("media",
        		new FileBody(file, ContentType.APPLICATION_OCTET_STREAM, file.getName())).build();
        httpPost.setEntity(requestEntity);

        try {
            response = httpClient.execute(httpPost, new BasicHttpContext());

            if (response.getStatusLine().getStatusCode() != 200) {

                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                   + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");

                JSONObject result = JSON.parseObject(resultStr);
                if (result.getInteger("errcode") == 0) {
                    // 成功
                	result.remove("errcode");
                	result.remove("errmsg");
                    return result;
                } else {
                    System.out.println("request url=" + url + ",return value=");
                    System.out.println(resultStr);
                    int errCode = result.getInteger("errcode");
                    String errMsg = result.getString("errmsg");
                    throw new OApiException(errCode, errMsg);
                }
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
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
	
	
	public static JSONObject downloadMedia(String url, String fileDir) throws OApiException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpGet.setConfig(requestConfig);

        try {
            HttpContext localContext = new BasicHttpContext();

            response = httpClient.execute(httpGet, localContext);

            RedirectLocations locations = (RedirectLocations) localContext.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
            if (locations != null) {
                URI downloadUrl = locations.getAll().get(0);
                String filename = downloadUrl.toURL().getFile();
                System.out.println("downloadUrl=" + downloadUrl);
                File downloadFile = new File(fileDir + File.separator + filename);
                FileUtils.writeByteArrayToFile(downloadFile, EntityUtils.toByteArray(response.getEntity()));
                JSONObject obj = new JSONObject();
                obj.put("downloadFilePath", downloadFile.getAbsolutePath());
                obj.put("httpcode", response.getStatusLine().getStatusCode());
                return obj;
            } else {
                if (response.getStatusLine().getStatusCode() != 200) {

                    System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                       + ", url=" + url);
                    return null;
                }
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String resultStr = EntityUtils.toString(entity, "utf-8");

                    JSONObject result = JSON.parseObject(resultStr);
                    if (result.getInteger("errcode") == 0) {
                        // 成功
                    	result.remove("errcode");
                    	result.remove("errmsg");
                        return result;
                    } else {
                        System.out.println("request url=" + url + ",return value=");
                        System.out.println(resultStr);
                        int errCode = result.getInteger("errcode");
                        String errMsg = result.getString("errmsg");
                        throw new OApiException(errCode, errMsg);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
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
	
	public static JSONObject httpGet(String url) throws OApiException{
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(2000).setConnectTimeout(2000).build();
        httpGet.setConfig(requestConfig);

        try {
            response = httpClient.execute(httpGet, new BasicHttpContext());

            if (response.getStatusLine().getStatusCode() != 200) {

                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                   + ", url=" + url);
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");

                JSONObject result = JSON.parseObject(resultStr);
                if (result.getInteger("errcode") == 0) {
                    return result;
                } else {
                    System.out.println("request url=" + url + ",return value=");
                    System.out.println(resultStr);
                    int errCode = result.getInteger("errcode");
                    String errMsg = result.getString("errmsg");
                    throw new OApiException(errCode, errMsg);
                }
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
