package com.ifugle.czy.utils;

import org.apache.ibatis.ognl.OgnlException;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.oapi.lib.aes.DingTalkJsApiSingnature;
import com.dingtalk.open.client.ServiceFactory;
import com.dingtalk.open.client.api.model.corp.JsapiTicket;
import com.dingtalk.open.client.api.service.corp.CorpConnectionService;
import com.dingtalk.open.client.api.service.corp.JsapiService;
import com.ifugle.utils.Configuration;

public class DingHelper {
	// 调整到1小时50分钟
    public static final long cacheTime = 1000 * 60 * 55 * 2;
	private static DingHelper dingHelper;
	private DingHelper(){};
	public static DingHelper getDingHelper(){
		if(dingHelper==null){
			dingHelper = new DingHelper();
		}
		return dingHelper;
	}
	/*
     * 在此方法中，为了避免频繁获取access_token，
     * 在距离上一次获取access_token时间在两个小时之内的情况，
     * 将直接从持久化存储中读取access_token
     *
     * 因为access_token和jsapi_ticket的过期时间都是7200秒
     * 所以在获取access_token的同时也去获取了jsapi_ticket
     * 注：jsapi_ticket是在前端页面JSAPI做权限验证配置的时候需要使用的
     * 具体信息请查看开发者文档--权限验证配置
     */
    public static String getAccessToken(){
        long curTime = System.currentTimeMillis();
        String corp_id = Configuration.getConfig().getString("CORP_ID");
        String corp_secret = Configuration.getConfig().getString("CORP_SECRET");
        //如果有缓存的access_token,可以取出来用。
        JSONObject accessTokenValue = (JSONObject) FileUtils.getValue("accesstoken", corp_id);
        String accToken = "";
        JSONObject jsontemp = new JSONObject();
        if (accessTokenValue == null || curTime - accessTokenValue.getLong("begin_time") >= cacheTime) {
            try {
                ServiceFactory serviceFactory = ServiceFactory.getInstance();
                CorpConnectionService corpConnectionService = serviceFactory.getOpenService(CorpConnectionService.class);
                accToken = corpConnectionService.getCorpToken(corp_id, corp_secret);
                // save accessToken
                JSONObject jsonAccess = new JSONObject();
                jsontemp.clear();
                jsontemp.put("access_token", accToken);
                jsontemp.put("begin_time", curTime);
                jsonAccess.put(corp_id, jsontemp);
                //真实项目中最好保存到数据库中
                FileUtils.write2File(jsonAccess, "accesstoken");
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
        	return accessTokenValue.getString("access_token");
        }

        return accToken;
    }
    /**
     * 获取JSTicket, 用于js的签名计算
     * 正常的情况下，jsapi_ticket的有效期为7200秒，所以开发者需要在某个地方设计一个定时器，定期去更新jsapi_ticket
     */
    public static String getJsapiTicket(String accessToken) throws OgnlException {
    	String corp_id = Configuration.getConfig().getString("CORP_ID");
        String corp_secret = Configuration.getConfig().getString("CORP_SECRET");
        JSONObject jsTicketValue = (JSONObject) FileUtils.getValue("jsticket", corp_id);
        long curTime = System.currentTimeMillis();
        String jsTicket = "";

        if (jsTicketValue == null || curTime -jsTicketValue.getLong("begin_time") >= cacheTime) {
            ServiceFactory serviceFactory;
            try {
                serviceFactory = ServiceFactory.getInstance();
                JsapiService jsapiService = serviceFactory.getOpenService(JsapiService.class);

                JsapiTicket JsapiTicket = jsapiService.getJsapiTicket(accessToken, "jsapi");
                jsTicket = JsapiTicket.getTicket();

                JSONObject jsonTicket = new JSONObject();
                JSONObject jsontemp = new JSONObject();
                jsontemp.clear();
                jsontemp.put("ticket", jsTicket);
                jsontemp.put("begin_time", curTime);
                jsonTicket.put(Configuration.getConfig().getString("CORP_ID"), jsontemp);
                FileUtils.write2File(jsonTicket, "jsticket");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsTicket;
        } else {
            return jsTicketValue.getString("ticket");
        }
    }
    public static String sign(String ticket, String nonceStr, long timeStamp, String url) {
        String s = "";
    	try {
            s = DingTalkJsApiSingnature.getJsApiSingnature(url, nonceStr, timeStamp, ticket);
        } catch (Exception ex) {
        }
    	return s;
    }
    public static String getSsoToken() throws OApiException {
    	String corp_id = Configuration.getConfig().getString("CORP_ID");
        String corp_secret = Configuration.getConfig().getString("CORP_SECRET");
        String url = "https://oapi.dingtalk.com/sso/gettoken?corpid=" + corp_id + "&corpsecret=" + corp_secret;
        JSONObject response = HttpHelper.httpGet(url);
        String ssoToken;
        if (response.containsKey("access_token")) {
            ssoToken = response.getString("access_token");
        } else {
            throw new OApiException("Sso_token");
        }
        return ssoToken;

    }
}
