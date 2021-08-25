package com.ifugle.czyDing.service;

import static java.lang.System.out;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.utils.Configuration;

public class TianyanchaService {
	private static Logger log = Logger.getLogger(TianyanchaService.class);
	protected Configuration cg;
	@Autowired
	public void setCg(Configuration cg){
		this.cg = cg;
	}
	private static final String REQ_ENCODEING_UTF8 = "utf-8";
	private static PoolingHttpClientConnectionManager httpClientConnectionManager;
		
	public JSONObject queryTianyanCha(String dataID,JSONObject params) {
		String reqUri = cg.getString("tyc_"+dataID,"http://open.api.tianyancha.com/services/v4/open/searchV2");
		if(params!=null){
			reqUri = reqUri.concat("?");
			for (Map.Entry<String, Object> entry : params.entrySet()) {
	            String p = (String) entry.getKey();  
	            String v = "";
	            try{
	            	Object ov = entry.getValue(); 
	            	v = ov.toString();
	            	//v = java.net.URLEncoder.encode(v,"utf-8");
	            }catch (Exception e) {
	            	log.error(e.toString());
	            }
	            reqUri=reqUri.concat(p).concat("=").concat(v).concat("&");
			} 
			reqUri = reqUri.substring(0,reqUri.length()-1);
		}
		JSONObject jo = null;
		String token = cg.getString("tyc_token");
		try {
			HttpHead reqHeader = new HttpHead();
			reqHeader.setHeader("Authorization", token);
			String response = httpGet(reqUri, reqHeader.getAllHeaders());
			jo = JSONObject.parseObject(response);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return jo;
	}
	
	// get 请求
	public String httpGet(String url, Header[] headers) throws Exception {
		HttpUriRequest uriRequest = new HttpGet(url);
		if (null != headers)
			uriRequest.setHeaders(headers);
		CloseableHttpClient httpClient = null;
		try {
			httpClient = declareHttpClientSSL(url);
			CloseableHttpResponse httpresponse = httpClient.execute(uriRequest);
			HttpEntity httpEntity = httpresponse.getEntity();
			String result = EntityUtils.toString(httpEntity, REQ_ENCODEING_UTF8);
			return result;
		} catch (ClientProtocolException e) {
			out.println(String.format("http请求失败，uri{%s},exception{%s}", new Object[] { url, e }));
		} catch (IOException e) {
			out.println(String.format("IO Exception，uri{%s},exception{%s}", new Object[] { url, e }));
		} finally {
			if (null != httpClient)
				httpClient.close();
		}
		return null;
	}

	// post 请求
	public String httpPost(String url, String params) throws Exception {
		HttpPost post = new HttpPost(url);
		post.addHeader("Content-Type", "application/json;charset=" + REQ_ENCODEING_UTF8);
		// 设置传输编码格式
		StringEntity stringEntity = new StringEntity(params, REQ_ENCODEING_UTF8);
		stringEntity.setContentEncoding(REQ_ENCODEING_UTF8);
		post.setEntity(stringEntity);
		HttpResponse httpresponse = null;
		CloseableHttpClient httpClient = null;
		try {
			httpClient = declareHttpClientSSL(url);
			httpresponse = httpClient.execute(post);
			HttpEntity httpEntity = httpresponse.getEntity();
			String result = EntityUtils.toString(httpEntity, REQ_ENCODEING_UTF8);
			return result;
		} catch (ClientProtocolException e) {
			out.println(String.format("http请求失败，uri{%s},exception{%s}", new Object[] { url, e }));
		} catch (IOException e) {
			out.println(String.format("IO Exception，uri{%s},exception{%s}", new Object[] { url, e }));
		} finally {
			if (null != httpClient)
				httpClient.close();
		}
		return null;
	}

	private CloseableHttpClient declareHttpClientSSL(String url) {
		if (url.startsWith("https://")) {
			return sslClient();
		} else {
			httpClientConnectionManager = new PoolingHttpClientConnectionManager();
			httpClientConnectionManager.setMaxTotal(100);
			httpClientConnectionManager.setDefaultMaxPerRoute(20);
			return HttpClientBuilder.create().setConnectionManager(httpClientConnectionManager).build();
		}
	}

	/**
	 * 设置SSL请求处理
	 * 
	 * @param httpClient
	 */
	private CloseableHttpClient sslClient() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] xcs, String str) {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String str) {
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
			return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
	// get 请求
	public String httpGet(String url) throws Exception {
		return httpGet(url, null);
	}
}
