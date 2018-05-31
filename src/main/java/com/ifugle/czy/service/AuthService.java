package com.ifugle.czy.service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.ognl.OgnlException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.oapi.lib.aes.DingTalkJsApiSingnature;
import com.dingtalk.open.client.ServiceFactory;
import com.dingtalk.open.client.api.model.corp.CorpUserBaseInfo;
import com.dingtalk.open.client.api.model.corp.CorpUserDetail;
import com.dingtalk.open.client.api.model.corp.JsapiTicket;
import com.dingtalk.open.client.api.service.corp.CorpConnectionService;
import com.dingtalk.open.client.api.service.corp.CorpUserService;
import com.dingtalk.open.client.api.service.corp.JsapiService;
import com.ifugle.czy.utils.bean.User;
import com.ifugle.utils.Configuration;

import org.apache.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public class AuthService {
	private static Logger log = Logger.getLogger(AuthService.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
    
//	/**
//	 * 企业应用后台地址，用户管理后台免登使用
//	 */
//	public static final String OA_BACKGROUND_URL = "";
//	public static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
//	public static final String TICKET_URL = "https://oapi.dingtalk.com/get_jsapi_ticket";
//	public static final String USER_INFO_URL = "https://oapi.dingtalk.com/user/getuserinfo";
	
 // 调整到1小时50分钟
    public static final long cacheTime = 1000 * 60 * 55 * 2;
    /**
     * 计算当前请求的jsapi的签名数据<br/>
     * <p>
     * 如果签名数据是通过ajax异步请求的话，签名计算中的url必须是给用户展示页面的url
     *
     * @param request
     * @return
     */
    public Map getConfig(HttpServletRequest request) {
    	Map config = new HashMap();
        String urlString = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String queryStringEncode = null;
        Configuration cg = Configuration.getConfig();
        String url;
        if (queryString != null) {
            queryStringEncode = URLDecoder.decode(queryString);
            url = urlString + "?" + queryStringEncode;
            System.out.println("urlString加上queryStringEncode: "+url);
        } else {
            url = urlString;
            System.out.println("queryString为空的url: "+url);
        }
        String nonceStr = "abcdefg";
        long timeStamp = System.currentTimeMillis() / 1000;
        String signedUrl = url;
        String accessToken = null;
        String ticket = null;
        String signature = null;
        String agentid = cg.getString("AGENT_ID", "163161139");;
        try {
            accessToken = getAccessToken();
            ticket = getJsapiTicket(accessToken);
            signature = sign(ticket, nonceStr, timeStamp, signedUrl);
        } catch (OgnlException e) {
            e.printStackTrace();
            return config;
        }
        config.put("jsticket", ticket);
        config.put("signature", signature);
        config.put("nonceStr", nonceStr);
        config.put("timeStamp", timeStamp);
        config.put("corpId", cg.getString("CORP_ID"));
        config.put("agentid", agentid);
        String configValue = "{\"jsticket\":\"" + ticket + "\",\"signature\":\"" + signature + "\",\"nonceStr\":\"" + nonceStr + "\",\"timeStamp\":\""
                + timeStamp + "\",\"corpId\":\"" + cg.getString("CORP_ID") + "\",\"agentid\":\"" + agentid + "\"}";
        //System.out.println(configValue);
        return config;
    }
    /**
     * 获取JSTicket, 用于js的签名计算
     * 正常的情况下，jsapi_ticket的有效期为7200秒，所以开发者需要在某个地方设计一个定时器，定期去更新jsapi_ticket
     */
    public String getJsapiTicket(String accessToken) throws OgnlException {
        //JSONObject jsTicketValue = (JSONObject) FileUtils.getValue("jsticket", CORP_ID);
        long curTime = System.currentTimeMillis();
        String jsTicket = "";

        //if (jsTicketValue == null || curTime -jsTicketValue.getLong("begin_time") >= cacheTime) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsTicket;
        //} else {
            //return jsTicketValue.getString("ticket");
        //}
    }
    public String sign(String ticket, String nonceStr, long timeStamp, String url) {
        String s = "";
    	try {
            s = DingTalkJsApiSingnature.getJsApiSingnature(url, nonceStr, timeStamp, ticket);
        } catch (Exception ex) {
        }
    	return s;
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
    public String getAccessToken(){
        long curTime = System.currentTimeMillis();
        String accToken = "";
        JSONObject jsontemp = new JSONObject();
        String corp_id = Configuration.getConfig().getString("CORP_ID");
        String corp_secret = Configuration.getConfig().getString("CORP_SECRET");
        //如果有缓存的access_token,可以取出来用。
        //JSONObject accessTokenValue = (JSONObject) FileUtils.getValue("accesstoken", CORP_ID);
        //if (accessTokenValue == null || curTime - accessTokenValue.getLong("begin_time") >= cacheTime) {
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
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        //} else {
           // return accessTokenValue.getString("access_token");
        //}

        return accToken;
    }
    //获取具体的成员信息
    public CorpUserDetail getUser(String accessToken, String userid) throws Exception {
        CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
        return corpUserService.getCorpUser(accessToken, userid);
    }
    /**
     * 根据免登授权码查询免登用户基础信息，userId
     *
     * @param accessToken
     * @param code
     * @return
     * @throws Exception
     */
    public CorpUserBaseInfo getUserBaseInfo(String accessToken, String code) throws Exception {
        CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
        return corpUserService.getUserinfo(accessToken, code);
    }
    
	public User getUserCzyConfig(String accessToken,String code) {
		User u = null;
		try{
			CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
			CorpUserBaseInfo cbu = corpUserService.getUserinfo(accessToken, code);
			System.out.println("进入第一层getUserCzyConfig，获取到了CorpUserBaseInfo");
			if(cbu!=null){
				String userid = cbu.getUserid();
				u = getCzyAuth(accessToken,userid);
			}else{
				log.error("登录钉钉验证失败：未获取到钉钉用户信息！");
			}
		}catch(Exception e){
			log.error("登录钉钉验证失败："+e.toString());
			e.printStackTrace();
		}
		System.out.println("从getUserCzyConfig返回获取到的数据库的USER配置："+u.getConfig());
        return u;
	}
	public User getCzyAuth(String accessToken,String userid){
		User u = null;
		try{
			CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
			CorpUserDetail ud = corpUserService.getCorpUser(accessToken, userid);
			if(ud==null){
				return null;
			}
			System.out.println("进入了getCzyAuth，获取到了CorpUserDetail："+ud==null?"":ud.getName());
			List users = jdbcTemplate.queryForList("select userid,dingname,dinginfo,config,qybj from users where userid=?",
					new Object[]{userid});
			if(users==null||users.size()==0){//不存在的，先插入一条新的用户记录。
				//先增加该用户的记录，再记录登录情况。初始登录的用户，未经过业务系统的权限设置，看不到业务菜单。
				//config目前是空，今后可能会增加一些基础的默认配置
				jdbcTemplate.update("insert into users(userid,dingname,dinginfo,qybj)values(?,?,?,1)",
						new Object[]{userid,ud.getName(),JSON.toJSONString(ud)});
				u = new User();
				u.setUserid(userid);
				u.setDingname(ud.getName());
				u.setDinginfo(JSON.toJSONString(JSON.toJSON(ud)));
				u.setQybj(1);
			}else{
				u = new User();
				Map mu =(Map)users.get(0);
				u.setUserid((String)mu.get("userid"));
				u.setDingname((String)mu.get("dingname"));
				u.setDinginfo((String)mu.get("dinginfo"));
				u.setConfig((String)mu.get("config"));
				u.setQybj(((BigDecimal)mu.get("qybj")).intValue());
			}
			//记录登录事件
			jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,'login')",
					new Object[]{userid});
		}catch(Exception e){
			System.out.println("getCzyAuth中发生错误！");
			log.error("获取钉钉用户信息失败："+e.toString());
			e.printStackTrace();
		}
		System.out.println("getCzyAuth中获得的自定义User："+u);
		return u;
	}
}
