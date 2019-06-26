package com.ifugle.czy.service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import com.ifugle.czy.utils.DingHelper;
import com.ifugle.czy.utils.bean.User;
import com.ifugle.utils.Configuration;

import org.apache.log4j.*;
import org.mindrot.jbcrypt.BCrypt;
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
	@Autowired
	private Configuration cg ;
//	/**
//	 * 企业应用后台地址，用户管理后台免登使用
//	 */
//	public static final String OA_BACKGROUND_URL = "";
//	public static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
//	public static final String TICKET_URL = "https://oapi.dingtalk.com/get_jsapi_ticket";
//	public static final String USER_INFO_URL = "https://oapi.dingtalk.com/user/getuserinfo";
	
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
        String urlString = request.getHeader("Referer");
        String urlStringBak = request.getRequestURL().toString();
        System.out.print("---------------------------");
        System.out.print("referer:"+urlString);
        System.out.print("**************************");
        System.out.print("urlString:"+urlStringBak);
        System.out.print("---------------------------");
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
        //2018-08-20 如果一个后台处理来自多个微应用，根据ip对应配置的agentid
        //String ip = getIpAdrress(request);
        //log.info("请求来自IP："+ip);
        String agentid = cg.getString("AGENT_ID", "163161139");
        try {
            accessToken = DingHelper.getAccessToken();
            ticket = DingHelper.getJsapiTicket(accessToken);
            signature =DingHelper.sign(ticket, nonceStr, timeStamp, signedUrl);
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
    private String getIpAdrress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if(index != -1){
                return XFor.substring(0,index);
            }else{
                return XFor;
            }
        }
        XFor = Xip;
        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            return XFor;
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
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
    
	public User getUserCzyConfig(String accessToken,String code,boolean czyAuth) {
		User u = null;
		try{
			CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
			CorpUserBaseInfo cbu = corpUserService.getUserinfo(accessToken, code);
			System.out.println("进入第一层getUserCzyConfig，获取到了CorpUserBaseInfo");
			if(cbu!=null){
				if(czyAuth){
					String userid = cbu.getUserid();
					u = getCzyAuth(accessToken,userid);
				}else{
					u = new User();
					u.setUserid(cbu.getUserid());
				}
			}else{
				log.error("登录钉钉验证失败：未获取到钉钉用户信息！");
			}
		}catch(Exception e){
			log.error("登录钉钉验证失败："+e.toString());
			e.printStackTrace();
		}
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
			StringBuffer usql = new StringBuffer("select userid,dingname,dinginfo,u.qybj,nvl(u.pswd_on,0)pswd_on,nvl(u.pswd_mode,0)pswd_mode,");
			usql.append("czfpbm,b.mc czfp from users u,(select * from bm_cont where table_bm='BM_CZFP')b where u.czfpbm=b.bm(+) and u.userid=?");
			List users = jdbcTemplate.queryForList(usql.toString(),new Object[]{userid});
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
				Map mu = (Map)users.get(0);
				u.setUserid((String)mu.get("userid"));
				u.setDingname((String)mu.get("dingname"));
				u.setDinginfo((String)mu.get("dinginfo"));
				u.setCzfpbm((String)mu.get("czfpbm"));
				u.setCzfp((String)mu.get("czfp"));
				u.setQybj(((BigDecimal)mu.get("qybj")).intValue());
				u.setPswd_on(((BigDecimal)mu.get("pswd_on")).intValue());
				u.setPswd_mode(((BigDecimal)mu.get("pswd_mode")).intValue());
				getMyMenus(userid,u);
			}
		}catch(Exception e){
			System.out.println("getCzyAuth中发生错误！");
			log.error("获取钉钉用户信息失败："+e.toString());
			e.printStackTrace();
		}
		System.out.println("getCzyAuth中获得的自定义User："+u);
		return u;
	}
	
	public User getMyMenus(String userid,User u){
		if(u==null){
			u=new User();
		}
		//查找岗位
		List posts = jdbcTemplate.queryForList("select userid,to_char(u.postid)postid,postname from user_post u,post p "
				+ "where userid=? and u.postid=p.postid",
				new Object[]{userid});
		if(posts!=null&&posts.size()>0){
			List postids = new ArrayList(),postnames = new ArrayList();
			for(int i=0;i<posts.size();i++){
				Map p = (Map)posts.get(i);
				String pid = (String)p.get("postid");
				String pname = (String)p.get("postname");
				postids.add(pid);
				postnames.add(pname);
			}
			String spids = StringUtils.join(postids, ",");
			String spnames = StringUtils.join(postnames, ",");
			u.setPostIds(spids);
			u.setPostNames(spnames);
		}
		int cc =jdbcTemplate.queryForObject("select count(*)cc from user_post where userid=?",new Object[]{userid},Integer.class);
		List menus = null;
		//查找权限。如果用户还未分配、设置，使用默认权限。
		if(cc==0){
			StringBuffer sql = new StringBuffer("select * from( select distinct m.moduleid,m.name,to_char(m.isleaf)isleaf,m.pid,dorder,");
			sql.append("nvl(pos,'')pos,notnull,errmsg from ");
			String dfMenus = cg.getString("defaultMenus", "");
			System.out.println("用户尚未配置权限，使用默认权限："+dfMenus);
			sql.append(" (select moduleid from modules where moduleid in('").append(dfMenus.replace(",", "','")).append("')) p,modules m ");
			sql.append(" where m.moduleid=p.moduleid and isleaf=1 order by dorder) where rownum<=7");
			menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{});
		}else{
			//设置了权限的，先找用户配置，无用户配置就用权限表的配置
			int uc =jdbcTemplate.queryForObject("select count(*)uc from user_menus where userid=?",new Object[]{userid},Integer.class);
			StringBuffer sql = new StringBuffer("select * from( select distinct m.moduleid,m.name,to_char(m.isleaf)isleaf,m.pid,");
			sql.append("nvl(pos,'')pos,notnull,errmsg ");
			if(uc==0){
				sql.append(",m.dorder from (select pm.* from user_post u,post_module pm where u.postid=pm.postid and userid=?) p,modules m ");
				sql.append(" where m.moduleid=p.moduleid and isleaf=1 order by m.dorder");
			}else{
				sql.append(",p.dorder from (select mid moduleid,dorder from user_menus where userid=?) p,modules m ");
				sql.append(" where m.moduleid=p.moduleid and isleaf=1 order by p.dorder");
			}
			sql.append(") where rownum<=7");
			menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{userid});
		}
		if(menus!=null&&menus.size()>0){
			List jms = new ArrayList();
			for(int i=0;i<menus.size();i++){
				Map mm = (Map)menus.get(i);
				String mid = (String)mm.get("moduleid");
				Map jm = new HashMap();//上级
				jm.put("id", mid);
				jm.put("name", (String)mm.get("name"));
				jm.put("notNull", StringUtils.isEmpty((String)mm.get("notnull"))?"":(String)mm.get("notnull"));
				jm.put("errMsg", StringUtils.isEmpty((String)mm.get("errmsg"))?"":(String)mm.get("errmsg"));
				jms.add(jm);
			}
			u.setMenus(jms);
		}
		//记录登录事件
		jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,'login')",
				new Object[]{userid});
		return u;
	}
	
	public List getUserMenus(String userid){
		List jms = new ArrayList();
		int cc =jdbcTemplate.queryForObject("select count(*)cc from user_post where userid=?",new Object[]{userid},Integer.class);
		List menus = null;
		//查找权限。如果用户还未分配、设置，使用默认权限。
		StringBuffer sql = new StringBuffer("select distinct m.moduleid,m.name,to_char(m.isleaf)isleaf,m.pid,dorder,");
		sql.append("nvl(pos,'')pos,to_char(decode(m.qybj,0,9,decode(p.moduleid,null,0,1)))state,notnull,errmsg from ");
		if(cc==0){
			String dfMenus = cg.getString("defaultMenus", "");
			System.out.println("获取应用模块，用户尚未配置权限，使用默认权限："+dfMenus);
			sql.append(" (select distinct moduleid from modules connect by moduleid=prior pid start with moduleid in('");
			sql.append(dfMenus.replace(",", "','")).append("')) p,modules m ");
			sql.append(" where m.moduleid=p.moduleid order by dorder");
			menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{});
		}else{
			sql.append(" (select pm.* from user_post u,post_module pm where u.postid=pm.postid and userid=?) p,modules m ");
			sql.append(" where m.moduleid=p.moduleid order by dorder");
			menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{userid});
		}
		if(menus!=null&&menus.size()>0){
			for(int i=0;i<menus.size();i++){
				Map mm = (Map)menus.get(i);
				if("1".equals(mm.get("isleaf"))){
					continue;
				}
				String mid = (String)mm.get("moduleid");
				Map jm = new HashMap();//上级
				jm.put("id", mid);
				jm.put("name", (String)mm.get("name"));
				jm.put("pos", (String)mm.get("pos"));
				List lmenus = new ArrayList();//下级
				for(int j=0;j<menus.size();j++){
					Map lmm = (Map)menus.get(j);
					if(mid.equals((String)lmm.get("pid"))){
						Map ljm = new HashMap();
						ljm.put("id", (String)lmm.get("moduleid"));
						ljm.put("name", (String)lmm.get("name"));
						ljm.put("state", (String)lmm.get("state"));
						ljm.put("notNull", StringUtils.isEmpty((String)lmm.get("notnull"))?"":(String)lmm.get("notnull"));
						ljm.put("errMsg", StringUtils.isEmpty((String)lmm.get("errmsg"))?"":(String)lmm.get("errmsg"));
						lmenus.add(ljm);
					}
				}
				jm.put("list", lmenus);
				jms.add(jm);
			}
		}
		System.out.println("获取应用模块，获取到的模块有："+jms.size());
		return jms;
	}
	
	
	public User testAuth(String userid){
		User u = null;
		StringBuffer usql = new StringBuffer("select userid,dingname,dinginfo,u.qybj,nvl(u.pswd_on,0)pswd_on,nvl(u.pswd_mode,0)pswd_mode,");
		usql.append("czfpbm,b.mc czfp from users u,(select * from bm_cont where table_bm='BM_CZFP')b where u.czfpbm=b.bm(+) and u.userid=?");
		List users = jdbcTemplate.queryForList(usql.toString(),new Object[]{userid});
		u = new User();
		Map mu = (Map)users.get(0);
		u.setUserid((String)mu.get("userid"));
		u.setDingname((String)mu.get("dingname"));
		u.setDinginfo((String)mu.get("dinginfo"));
		u.setCzfpbm((String)mu.get("czfpbm"));
		u.setCzfp((String)mu.get("czfp"));
		u.setQybj(((BigDecimal)mu.get("qybj")).intValue());
		u.setPswd_on(((BigDecimal)mu.get("pswd_on")).intValue());
		System.out.println("pswd_on："+((BigDecimal)mu.get("pswd_on")).intValue());
		u.setPswd_mode(((BigDecimal)mu.get("pswd_mode")).intValue());
		System.out.println("pswd_mode："+((BigDecimal)mu.get("pswd_mode")).intValue());

		//查找岗位
		usql = new StringBuffer("select userid,to_char(u.postid)postid,postname from user_post u,post p where userid=? and u.postid=p.postid");
		List posts = jdbcTemplate.queryForList(usql.toString(),new Object[]{userid});
		if(posts!=null&&posts.size()>0){
			List postids = new ArrayList(),postnames = new ArrayList();
			for(int i=0;i<posts.size();i++){
				Map p = (Map)posts.get(i);
				String pid = (String)p.get("postid");
				String pname = (String)p.get("postname");
				postids.add(pid);
				postnames.add(pname);
			}
			String spids = StringUtils.join(postids, ",");
			String spnames = StringUtils.join(postnames, ",");
			u.setPostIds(spids);
			u.setPostNames(spnames);
		}
		int cc =jdbcTemplate.queryForObject("select count(*)cc from user_post where userid=?",new Object[]{userid},Integer.class);
		List menus = null;
		//查找权限。如果用户还未分配、设置，使用默认权限。
		StringBuffer sql = new StringBuffer("select distinct m.moduleid,m.name,to_char(m.isleaf)isleaf,m.pid,dorder,");
		sql.append("nvl(pos,'')pos,to_char(decode(m.qybj,0,9,decode(p.moduleid,null,0,1)))state,notnull,errmsg from ");
		if(cc==0){
			String dfMenus = cg.getString("defaultMenus", "");
			sql.append(" (select moduleid from modules where moduleid in('").append(dfMenus.replace(",", "','")).append("')) p,modules m ");
			sql.append(" where m.moduleid=p.moduleid(+) order by dorder");
			menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{});
		}else{
			sql.append(" (select pm.* from user_post u,post_module pm where u.postid=pm.postid and userid=?) p,modules m ");
			sql.append(" where m.moduleid=p.moduleid(+) order by dorder");
			menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{userid});
		}
		if(menus!=null&&menus.size()>0){
			List jms = new ArrayList();
			for(int i=0;i<menus.size();i++){
				Map mm = (Map)menus.get(i);
				if("1".equals(mm.get("isleaf"))){
					continue;
				}
				String mid = (String)mm.get("moduleid");
				Map jm = new HashMap();//上级
				jm.put("id", mid);
				jm.put("name", (String)mm.get("name"));
				jm.put("pos", (String)mm.get("pos"));
				List lmenus = new ArrayList();//下级
				for(int j=0;j<menus.size();j++){
					Map lmm = (Map)menus.get(j);
					if(mid.equals((String)lmm.get("pid"))){
						Map ljm = new HashMap();
						ljm.put("id", (String)lmm.get("moduleid"));
						ljm.put("name", (String)lmm.get("name"));
						ljm.put("state", (String)lmm.get("state"));
						ljm.put("notNull", StringUtils.isEmpty((String)lmm.get("notnull"))?"":(String)lmm.get("notnull"));
						ljm.put("errMsg", StringUtils.isEmpty((String)lmm.get("errmsg"))?"":(String)lmm.get("errmsg"));
						lmenus.add(ljm);
					}
				}
				jm.put("list", lmenus);
				jms.add(jm);
			}
			u.setMenus(jms);
		}
		//记录登录事件
		//jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,'login')",new Object[]{userid});
		return u;
	}
	public boolean canAccessModule(String userid,String mid) {
		boolean canAccess = false;
		StringBuffer sql = new StringBuffer("select count(*)cc from modules m,user_post u,post_module p ");
		sql.append("where u.postid=p.postid and p.moduleid=m.moduleid and u.userid=? and m.moduleid=?");
		int cc = jdbcTemplate.queryForObject(sql.toString(), new Object[]{userid,mid},Integer.class);
		if(cc>0){
			canAccess = true;
		}
		return canAccess;
	}
	public int validateLogin(String userid, String pswdToTest) {
		StringBuffer sql = new StringBuffer("select decode(pswd_mode,0,pswd_kb,pswd_gesture)pswd from users where userid=?");
		String hashed = jdbcTemplate.queryForObject(sql.toString(), new Object[]{userid},String.class);
		int flag = 0;
		if(StringUtils.isEmpty(hashed)){
			return 3;
		}
		try{
			boolean consist = BCrypt.checkpw(pswdToTest, hashed==null?"":hashed);
			flag = consist?0:5;
		}catch(Exception e){
			log.error(userid+"验证密码时发生错误："+e.toString());
		}
		return flag;
	}
	
	public static void main(String[] args) {
		// Hash a password for the first time
		String password = "11";
		String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
		System.out.println(hashed);
		String hashed2 = BCrypt.hashpw(password, BCrypt.gensalt(4));
		String candidate = "11";
		//String candidate = "wrongtestpassword";
		if (BCrypt.checkpw(candidate, hashed))
			System.out.println("It matches");
		else
			System.out.println("It does not match");
	}
}
