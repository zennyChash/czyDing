package com.ifugle.czyDing.service;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDingCreateRequest;
import com.dingtalk.api.response.OapiDingCreateResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.dingtalk.open.client.ServiceFactory;
import com.dingtalk.open.client.api.model.corp.CorpUser;
import com.dingtalk.open.client.api.model.corp.CorpUserDetail;
import com.dingtalk.open.client.api.model.corp.CorpUserList;
import com.dingtalk.open.client.api.model.corp.Department;
import com.dingtalk.open.client.api.model.corp.MessageBody;
import com.dingtalk.open.client.api.model.corp.MessageType;
import com.dingtalk.open.client.api.service.corp.CorpUserService;
import com.ifugle.czyDing.ding.message.LightAppMessageDelivery;
import com.ifugle.czyDing.ding.message.MessageHelper;
import com.ifugle.czyDing.utils.DingHelper;
import com.ifugle.czyDing.utils.HttpHelper;
import com.ifugle.czyDing.utils.JResponse;
import com.ifugle.czyDing.utils.MD5Util;
import com.ifugle.czyDing.utils.TemplatesLoader;
import com.ifugle.czyDing.utils.bean.*;
import com.ifugle.czyDing.utils.bean.template.DataSrc;
import com.ifugle.czyDing.utils.bean.template.FilterField;
import com.ifugle.czyDing.utils.bean.template.JOutput;
import com.ifugle.czyDing.utils.bean.template.ValuedDs;
import com.ifugle.utils.Configuration;

@Transactional
public class ConsoleServeice {
	private static Logger log = Logger.getLogger(ConsoleServeice.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	private RouterService rtService;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	protected Configuration cg;
	@Autowired
	public void setCg(Configuration cg){
		this.cg = cg;
	}
	@Autowired
	private ESQueryDataService esDataService;
	
	public int authLog(String userid,String pswd){
		int flag = 0;
		try{
			SimpleDateFormat formattwo = new SimpleDateFormat("yyyy/MM/dd' 'HH:mm:ss");
	        String now = formattwo.format(Calendar.getInstance().getTime());
			log.info("登录者："+userid+",时间："+now);
			int cu = jdbcTemplate.queryForObject("select count(*) from cs_user where userid=? ", new Object[]{userid},Integer.class);
			if(cu>0){
				int ca = jdbcTemplate.queryForObject("select count(*) from cs_user where userid=? and pswd=?",new Object[]{userid,pswd},Integer.class);
				if(ca>0){
					jdbcTemplate.update("insert into cs_log(id,userid,etime,eventtype)values(sq_cs_log.nextval,?,sysdate,'login')",
							new Object[]{userid});
					flag=1;
				}else{
					flag=3;
				}
			}else{
				flag=2;
			}
		}catch(Exception e){
			flag=9;
			log.error("控制台用户登录发生错误："+e.toString());
		}
		return flag;
	}
	@SuppressWarnings("unchecked")
	public Map getUsers(int start,int limit) {
		Map infos = new HashMap();
		try{
			int cu = jdbcTemplate.queryForObject("select count(*) from users", new Object[]{},Integer.class);
			infos.put("totalCount", cu);
			StringBuffer sql =new StringBuffer("select userid,dingname username,nvl(czfpbm,'')czfpbm,nvl(b.mc,'')czfp from users u,");
			sql.append("(select * from BM_CONT where table_bm='BM_CZFP')b where u.czfpbm=b.bm(+)");
			StringBuffer rSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
			rSql.append(sql);
			rSql.append(") A WHERE rownum<=");
			rSql.append((start+limit));
			rSql.append(") B WHERE r>");
			rSql.append(start);
			List users = jdbcTemplate.queryForList(rSql.toString(),new Object[]{});
			if(users!=null&&users.size()>0){
				sql = new StringBuffer("select userid,to_char(u.postid)postid,postname from user_post u,post p where userid=? and u.postid=p.postid");
				for(int i=0;i<users.size();i++){
					Map mu = (Map)users.get(i);
					String userid = (String)mu.get("userid");
					
					/*String accessToken = DingHelper.getAccessToken();
					try{
						CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
				        CorpUserDetail u = corpUserService.getCorpUser(accessToken, userid);
						if(u==null){
							log.error("未找到用户信息："+userid);
						}else{
							String mobile = u.getMobile();
							log.info("userid:"+userid+",mobile:"+mobile);
							jdbcTemplate.update("update users set mobile=? where userid=?",mobile,userid);
						}
					}catch(Exception e){
						log.error(e.toString());
					}*/
					
					
					List posts = jdbcTemplate.queryForList(sql.toString(),new Object[]{userid});
					if(posts!=null&&posts.size()>0){
						List postids = new ArrayList(),postnames = new ArrayList();
						for(int j=0;j<posts.size();j++){
							Map p = (Map)posts.get(j);
							String pid = (String)p.get("postid");
							String pname = (String)p.get("postname");
							postids.add(pid);
							postnames.add(pname);
						}
						String spids = StringUtils.join(postids, ",");
						String spnames = StringUtils.join(postnames, ",");
						mu.put("POSTID", spids);
						mu.put("POSTNAME", spnames);
					}
				}
			}
			infos.put("rows", users);
		}catch(Exception e){
			log.error("获取用户时发生错误："+e.toString());
		}
		return infos;
	}
	@SuppressWarnings("unchecked")
	public Map getPosts(){
		Map infos = new HashMap();
		try{
			StringBuffer sql =new StringBuffer("select postname,postid,remark from post");
			List posts = jdbcTemplate.queryForList(sql.toString(),new Object[]{});
			infos.put("rows", posts);
		}catch(Exception e){
			log.error("获取岗位列表时发生错误："+e.toString());
		}
		return infos;
	}
	public boolean removeUserPosts(String strUids) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("delete from user_post where userid in('");
			sql.append(strUids.replace(",", "','")).append("')");
			jdbcTemplate.update(sql.toString(),new Object[]{});
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	public boolean saveUserPosts(String strUids, String strPids) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("delete from user_post where userid in('");
			sql.append(strUids.replace(",", "','")).append("')");
			jdbcTemplate.update(sql.toString(),new Object[]{});
			if(!StringUtils.isEmpty(strPids)){
				sql =new StringBuffer("insert into user_post(userid,postid)values(?,?)");
				String[] uids = strUids.split(",");
				String[] pids = strPids.split(",");
				for(int i=0;i<uids.length;i++){
					String uid = uids[i];
					for(int j=0;j<pids.length;j++){
						String pid = pids[j];
						jdbcTemplate.update(sql.toString(),new Object[]{uid,pid});
					}
				}
			}
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	public Map getCzfps() {
		Map infos = new HashMap();
		try{
			StringBuffer sql =new StringBuffer("select bm,mc from bm_cont where table_bm='BM_CZFP'");
			List posts = jdbcTemplate.queryForList(sql.toString(),new Object[]{});
			infos.put("rows", posts);
		}catch(Exception e){
			log.error("获取财政分片信息时发生错误："+e.toString());
		}
		return infos;
	}
	public boolean saveCzfps(String strUids, String czfpbm) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("update users set czfpbm=? where userid in('");
			sql.append(strUids.replace(",", "','")).append("')");
			jdbcTemplate.update(sql.toString(),new Object[]{czfpbm});
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	public boolean removeCzfp(String strUids) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("update users set czfpbm='' where userid in('");
			sql.append(strUids.replace(",", "','")).append("')");
			jdbcTemplate.update(sql.toString(),new Object[]{});
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	public Map getModules(String strPid) {
		Map infos = new HashMap();
		try{
			StringBuffer sql = new StringBuffer("select m.moduleid id,m.name,m.pid,nvl(m.isleaf,0)isleaf,decode(p.moduleid,'',0,1)checked ");
			sql.append(" from modules m,(select * from post_module where postid=?) p where m.moduleid=p.moduleid(+)  order by dorder");
			List menus = jdbcTemplate.query(sql.toString(),new Object[]{strPid},new TreeNodeCheckableMapper());
			List jms = new ArrayList();
			if(menus!=null&&menus.size()>0){
				for(int i=0;i<menus.size();i++){
					TreeNode mm = (TreeNode)menus.get(i);
					if(mm.isLeaf()){
						continue;
					}
					String mid = mm.getId();
					List lmenus = new ArrayList();//下级
					for(int j=0;j<menus.size();j++){
						TreeNode lmm = (TreeNode)menus.get(j);
						if(mid.equals(lmm.getPid())){
							lmenus.add(lmm);
						}
					}
					mm.setChildren(lmenus);
					jms.add(mm);
				}
			}
			infos.put("children", jms);
		}catch(Exception e){
			log.error("获取模块信息时发生错误："+e.toString());
		}
		return infos;
		
	}
	
	public boolean setPostModules(String strPid, String strMids) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("delete from post_module where postid=?");
			jdbcTemplate.update(sql.toString(),new Object[]{strPid});
			if(!StringUtils.isEmpty(strMids)){
				sql =new StringBuffer("insert into post_module(postid,moduleid)values(?,?)");
				String[] mids = strMids.split(",");
				for(int i=0;i<mids.length;i++){
					jdbcTemplate.update(sql.toString(),new Object[]{strPid,mids[i]});
				}
			}
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	public boolean removePosts(String postid) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("delete from user_post where postid=?");
			jdbcTemplate.update(sql.toString(),new Object[]{postid});
			sql =new StringBuffer("delete from post_module where postid=?");
			jdbcTemplate.update(sql.toString(),new Object[]{postid});
			sql =new StringBuffer("delete from post where postid=?");
			jdbcTemplate.update(sql.toString(),new Object[]{postid});
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	public boolean savePost(String saveMode, int postid, String postname,String remark) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("insert into post(postname,remark,postid)values(?,?,sq_post.nextval)");
			if("modify".equals(saveMode)){
				sql =new StringBuffer("update post set postname=?,remark=? where postid=?");
				jdbcTemplate.update(sql.toString(),new Object[]{postname,remark,postid});
			}else{
				jdbcTemplate.update(sql.toString(),new Object[]{postname,remark});
			}
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	//重载取数模板
	public String[] refreshDtSrcTemplates() {
		String[] results = new String[]{"1",""};
		try{
	    	TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
	    	ltmp.refreshDataSrcs();
		}catch(Exception e){
			results[0]="9";
			results[1]=e.toString();
		}
		return results;
	}
	@SuppressWarnings("unchecked")
	public Map getDtsrcTemplates(int start, int limit) {
		Map infos = new HashMap();
		try{
			TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
			List allDts = ltmp.getDataSrcTemplates();
			if(allDts!=null&&allDts.size()>0){
				List esDts = new ArrayList();
				for(int i=0;i<allDts.size();i++){
					DataSrc dt = (DataSrc)allDts.get(i);
					if(dt.getUseType()==1){
						continue;
					}
					Map cells = new HashMap();
					cells.put("id", dt.getId());
					cells.put("name", dt.getName());
					cells.put("desc", dt.getDesc());
					cells.put("sourceType", dt.getSourceType());
					cells.put("useType", dt.getUseType());
					cells.put("infile", dt.getInfile());
					esDts.add(cells);
				}
				int count = esDts.size();
				infos.put("totalCount", count);
				int end = start+limit;
				List rows = esDts.subList(start, count>end?end:count);
				infos.put("rows", rows);
			}else{
				infos.put("totalCount", 0);
				infos.put("rows", null);
			}
		}catch(Exception e){
			log.error("获取取数脚本时发生错误："+e.toString());
		}
		return infos;
	}
	
	public Map getDataScriptsLog(String tid, int start, int limit) {
		Map logsInfo = new HashMap();
		int flag = cg.getTaskStatus(tid);
		List logs = cg.consumeLogs(tid);
		logsInfo.put("flag", flag);
		logsInfo.put("logs", logs);
		if(flag==1){
			logsInfo.put("total", cg.getSuccessScript(tid)+cg.getFailedScript(tid));
			cg.clearTaskLogs(tid);
		}
		return logsInfo;
	}
	//重载响应模板
	public String[] refreshResponseTemplates() {
		String[] results = new String[]{"1",""};
		try{
	    	TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
	    	ltmp.refreshJSONOutputs();
		}catch(Exception e){
			results[0]="9";
			results[1]=e.toString();
		}
		return results;
	}
	public Map getOutPutTemplates(int start, int limit) {
		Map infos = new HashMap();
		try{
			TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
			List allOutputs = ltmp.getJSONOutputTemplates();
			if(allOutputs!=null&&allOutputs.size()>0){
				int tt = allOutputs.size();
				infos.put("totalCount", tt);
				int end = start+limit;
				List dts = allOutputs.subList(start, tt>end?end:tt);
				List rows = new ArrayList();
				for(int i=0;i<dts.size();i++){
					JOutput jp = (JOutput)dts.get(i);
					Map cells = new HashMap();
					cells.put("id", jp.getId());
					cells.put("name", jp.getName());
					cells.put("desc", jp.getDesc());
					cells.put("infile", jp.getInfile());
					rows.add(cells);
				}
				infos.put("rows", rows);
			}else{
				infos.put("totalCount", 0);
				infos.put("rows", null);
			}
		}catch(Exception e){
			log.error("获取响应输出模板时发生错误："+e.toString());
		}
		return infos;
	}
	public List getParamsOfResponseTemplate(String id) {
		List flds = new ArrayList();
		Map fmap = new HashMap();
		try{
			TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
			JOutput jp = ltmp.getJOutput(id);
			List vdses = jp.getValuedDs();
			for(int i=0;i<vdses.size();i++){
				ValuedDs vds = (ValuedDs)vdses.get(i);
				List filters = vds.getFilterFlds();
				for(int j=0;j<filters.size();j++){
					FilterField flt = (FilterField)filters.get(j);
					String pname = StringUtils.isEmpty(flt.getRefParam())?flt.getName():flt.getRefParam();
					int type = flt.getDataType();
					String dfVal = flt.getValue();
					JSONObject jflt = new JSONObject();
					jflt.put("name", pname);
					jflt.put("fieldLabel", pname);
					jflt.put("value", dfVal);
					jflt.put("xtype", type==0?"textfield":"numberfield");
					if(!fmap.containsKey(pname)){
						fmap.put(pname, "1");
						flds.add(jflt);
					}
				}
			}
		}catch(Exception e){
			log.error("获取输出模板的参数时发生错误！"+e.toString());
		}
		return flds;
	}
	
	public JSONObject consoleLogin(String code,HttpServletRequest request) {
		JSONObject js = null;
		try {
			String ssoToken = DingHelper.getSsoToken();
			log.info("管理控制台，获取ssoToken："+ssoToken);
			request.getSession().setAttribute("console_accessToken", ssoToken);
			js = DingHelper.getAgentUserInfo(ssoToken, code);
			log.info("管理控制台免登后返回的信息："+js.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return js;
	}
	public Map getUsersFromDingTalk(int start, int limit) {
		Map infos = new HashMap();
		String accessToken = DingHelper.getAccessToken();
		log.info("准备获取钉钉用户："+accessToken);
		try{
	        List<Department> departments = new ArrayList<Department>();
	        // 1表示部门根目录，如果获取accessToken的corpSecret设置了部门范围，需要更改成对应部门的id
	        // 可以通过https://oapi.dingtalk.com/auth/scopes?access_token=ACCESS_TOKEN 查询部门id列表
	        departments = DingHelper.listDepartments(accessToken, "");
	        log.info("共"+departments.size()+"个部门。");
	        List users = new ArrayList();
	        int cc = 0;
	        //按部门循环获取用户
	        for (int i = 0; i < departments.size(); i++) {
	        	log.info("部门ID"+departments.get(i).getId());
	            CorpUserList corpUserList =  DingHelper.getDepartmentUser(accessToken, Long.valueOf(departments.get(i).getId())
	                        ,0L,100, null);
	            if (corpUserList.getUserlist().size() == 0) {
	                continue;
	            }
	            for (int j = 0; j < corpUserList.getUserlist().size(); j++) {
	            	CorpUser user = (CorpUser)corpUserList.getUserlist().get(j);
	            	JSONObject juser = (JSONObject)JSONObject.toJSON(user);
	            	Department d = (Department)departments.get(i);
	            	long did = d.getId();
	            	juser.put("departmentID", did);
	                users.add(juser);
	                log.info(JSON.toJSONString(juser));
	            }
	        }
	        Map czyUserMap=null;
			List czyUsers = jdbcTemplate.queryForList("select userid,dingname from users u",new Object[]{});
			if(czyUsers!=null&&czyUsers.size()>0){
				czyUserMap= new HashMap();
				for(int i=0;i<czyUsers.size();i++){
					Map row = (Map)czyUsers.get(i);
					czyUserMap.put(row.get(("userid")), 1);
				}
			}
			//剔除已经存在于czy的用户
			List resultUsers = new ArrayList();
			if(czyUserMap==null||czyUserMap.size()==0){
				resultUsers = users;
			}else{
				for(int i=0;i<users.size();i++){
					JSONObject juser = (JSONObject)users.get(i);
					if(czyUserMap.containsKey(juser.getString("userid"))){
						continue;
					}
					resultUsers.add(juser);
				}
			}
	        cc = resultUsers.size();
	        infos.put("totalCount", cc);
	        int end = ((start+limit)<cc)?(start+limit):cc;
	        List page = resultUsers.subList(start, end);
			infos.put("rows", page);
	    } catch (Exception e) {
	    	infos.put("totalCount", 0);
			infos.put("rows", null);
	        e.printStackTrace();
		}
		return infos;
	}
	
	public List getDepartments(String pdid,boolean recursive){
		JSONArray jdeps = new JSONArray();
		JSONObject jdepobj = new JSONObject();
		String accessToken = DingHelper.getAccessToken();
		log.info("准备获取钉钉部门："+accessToken);
		try{
			//如果传递的父id是空，取根部门id配置
			if(StringUtils.isEmpty(pdid)){
				String rdid = cg.getString("rootDid","1");
				pdid=rdid;
			}
	        //departments = DingHelper.listDepartments(accessToken, pdid);
	        String strDeps = DingHelper.getDepartmentByPid(accessToken, pdid,recursive);
	        jdepobj = JSONObject.parseObject(strDeps);
	        jdeps = jdepobj.getJSONArray("department");
	        log.info("共"+jdeps.size()+"个部门。");
		} catch (Exception e) {
	        e.printStackTrace();
		}
		return jdeps;
	}
	
	public Map getUsersFromDingTalkByDepartment(int start, int limit,long did) {
		Map infos = new HashMap();
		String accessToken = DingHelper.getAccessToken();
		log.info("准备获取钉钉用户："+accessToken);
		try{
	  
	        List users = new ArrayList();
        	log.info("部门ID"+did);
            CorpUserList corpUserList =  DingHelper.getDepartmentUser(accessToken, Long.valueOf(did)
                        ,0L,100, null);
            if (corpUserList.getUserlist().size() == 0) {
    	        infos.put("totalCount", 0);
    			infos.put("rows", null);
            }
            for (int j = 0; j < corpUserList.getUserlist().size(); j++) {
            	CorpUser user = (CorpUser)corpUserList.getUserlist().get(j);
            	JSONObject juser = (JSONObject)JSONObject.toJSON(user);
            	juser.put("departmentID", did);
                users.add(juser);
                log.info(JSON.toJSONString(juser));
            }
	        Map czyUserMap=null;
			List czyUsers = jdbcTemplate.queryForList("select userid,dingname from users u",new Object[]{});
			if(czyUsers!=null&&czyUsers.size()>0){
				czyUserMap= new HashMap();
				for(int i=0;i<czyUsers.size();i++){
					Map row = (Map)czyUsers.get(i);
					czyUserMap.put(row.get(("userid")), 1);
				}
			}
			//剔除已经存在于czy的用户
			List resultUsers = new ArrayList();
			if(czyUserMap==null||czyUserMap.size()==0){
				resultUsers = users;
			}else{
				for(int i=0;i<users.size();i++){
					JSONObject juser = (JSONObject)users.get(i);
					if(czyUserMap.containsKey(juser.getString("userid"))){
						continue;
					}
					resultUsers.add(juser);
				}
			}
	        int cc = resultUsers.size();
	        infos.put("totalCount", cc);
	        int end = ((start+limit)<cc)?(start+limit):cc;
	        List page = resultUsers.subList(start, end);
			infos.put("rows", page);
	    } catch (Exception e) {
	    	infos.put("totalCount", 0);
			infos.put("rows", null);
	        e.printStackTrace();
		}
		return infos;
	}
	
	public boolean addUserFromDingTalk(String strDingUsers) {
		
		try{
			JSONArray jusers = JSON.parseArray(strDingUsers);
			String accessToken = DingHelper.getAccessToken();
			if(jusers==null||jusers.size()==0){
				return true;
			}
			StringBuffer sql = new StringBuffer("insert into users(userid,mobile,dingname,dinginfo,qybj)values(?,?,?,?,1)");
			for(int i=0;i<jusers.size();i++){
				JSONObject ju = jusers.getJSONObject(i);
				String userid = ju.getString("userid");
				//获取手机号
				CorpUserService corpUserService = ServiceFactory.getInstance().getOpenService(CorpUserService.class);
		        CorpUserDetail u = corpUserService.getCorpUser(accessToken, userid);
		        String mobile = u.getMobile();
				
				String uname = ju.getString("name");
				String dingInfo = ju.toJSONString();
				jdbcTemplate.update("delete from users where userid=?",new Object[]{userid});
				jdbcTemplate.update(sql.toString(),new Object[]{userid,mobile,uname,dingInfo});
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return true;
	}
	public boolean removeUsers(String strUids) {
		try{
			String[] users = strUids.split(",");
			if(users==null||users.length==0){
				return true;
			}
			for(int i=0;i<users.length;i++){
				String userid = users[i];
				jdbcTemplate.update("delete from user_favorite where userid=?",new Object[]{userid});
				jdbcTemplate.update("delete from usermapping where userid=?",new Object[]{userid});
				jdbcTemplate.update("delete from user_menus where userid=?",new Object[]{userid});
				jdbcTemplate.update("delete from user_post where userid=?",new Object[]{userid});
				jdbcTemplate.update("delete from users where userid=?",new Object[]{userid});
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return true;
	}
	public Map getRemoteServiceUsers(String qparams, int start, int limit) {
		Map infos = new HashMap();
		try{
			StringBuffer sql =new StringBuffer("select * from(");
			sql.append("select u.servicename,u.userid,u.uname,m.dingid,m.dingname,decode(m.userid,null,0,1)mapped ");
			sql.append(" from remoteusers u,usermapping m where u.servicename = m.servicename(+) and u.userid=m.userid(+)");
			sql.append(") where (mapped=0 ");
			if(qparams!=null){
				JSONObject jp=  JSONObject.parseObject(qparams);
				if(jp!=null&&jp.entrySet()!=null){
					sql.append(" or dingid='").append(jp.getString("dingid")).append("')");
					if(jp.containsKey("servicename")&&!StringUtils.isEmpty(jp.getString("servicename"))){
						sql.append(" and servicename='").append(jp.getString("servicename")).append("'");
					}
					if(jp.containsKey("userid")&&!StringUtils.isEmpty(jp.getString("userid"))){
						sql.append(" and userid like '%").append(jp.getString("userid")).append("%'");
					}
					if(jp.containsKey("uname")&&!StringUtils.isEmpty(jp.getString("uname"))){
						sql.append(" and uname like '%").append(jp.getString("uname")).append("%'");
					}
				}else{
					sql.append(")");
				}
			}else{
				sql.append(")");
			}
			sql.append(" order by mapped desc,servicename,userid,dingid");
			StringBuffer csql = new StringBuffer("select count(*) from (");
			csql.append(sql).append(")");
			int cu = jdbcTemplate.queryForObject(csql.toString(), new Object[]{},Integer.class);
			infos.put("totalCount", cu);
			StringBuffer rSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
			rSql.append(sql);
			rSql.append(") A WHERE rownum<=");
			rSql.append((start+limit));
			rSql.append(") B WHERE r>");
			rSql.append(start);
			List users = jdbcTemplate.queryForList(rSql.toString(),new Object[]{});
			infos.put("rows", users);
		}catch(Exception e){
			log.error("获取远程服务系统用户信息是发生错误："+e.toString());
		}
		return infos;
	}
	public Map getRemoteServices() {
		Map infos = new HashMap();
		try{
			StringBuffer sql =new StringBuffer("select distinct servicename bm,servicename mc from remoteusers");
			List posts = jdbcTemplate.queryForList(sql.toString(),new Object[]{});
			infos.put("rows", posts);
		}catch(Exception e){
			log.error("获取远程服务列表时发生错误："+e.toString());
		}
		return infos;
	}
	public boolean toggleMapUser(int mapType,String dingid, String svc, String userid) {
		try{
			StringBuffer sql = new StringBuffer("delete from usermapping where servicename=? and userid=? and dingid=?");
			if(mapType==1){
				//先删除原先对应关系
				jdbcTemplate.update("delete from usermapping where servicename=? and dingid=?",new Object[]{svc,dingid});
				sql = new StringBuffer("insert into usermapping(id,dingid,dingname,servicename,userid) ");
				sql.append("select sq_usermapping_id.nextval,userid,dingname,?,? from users where userid=?");
			}
			jdbcTemplate.update(sql.toString(),new Object[]{svc,userid,dingid});
		}catch(Exception e){
			log.error(e.toString());
		}
		return true;
	}
	public Map getUsersAllMenus(String userid) {
		Map infos = new HashMap();
		try{
			StringBuffer sql =new StringBuffer("select m.moduleid mid,m.name mname,m.pid,pm.name pname,decode(um.mid,null,0,1)isdf from");
			sql.append("(select * from modules where isleaf=1) m,(select * from modules where isleaf=0) pm,");
			sql.append("(select * from user_menus where userid=?)um,");
			sql.append("(select distinct tm.moduleid from user_post up,post_module tm where up.postid=tm.postid and userid=?)u");
			sql.append(" where m.moduleid = u.moduleid and m.moduleid=um.mid(+) and m.pid=pm.moduleid order by pm.dorder,m.dorder");
			List menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{userid,userid});
			infos.put("rows", menus);
		}catch(Exception e){
			log.error("获取用户模块信息时发生错误："+e.toString());
		}
		return infos;
	}
	public Map getUsersDfMenus(String userid) {
		Map infos = new HashMap();
		try{
			StringBuffer sql =new StringBuffer("select m.moduleid mid,m.name mname,m.pid,pm.name pname from");
			sql.append("(select * from modules where isleaf=1) m,(select * from modules where isleaf=0) pm,");
			sql.append("(select * from user_menus where userid=?)um,");
			sql.append("(select distinct tm.moduleid from user_post up,post_module tm where up.postid=tm.postid and userid=?)u");
			sql.append(" where m.moduleid = u.moduleid and m.moduleid=um.mid and m.pid=pm.moduleid order by um.dorder,m.dorder");
			List menus = jdbcTemplate.queryForList(sql.toString(),new Object[]{userid,userid});
			infos.put("rows", menus);
		}catch(Exception e){
			log.error("获取用户默认菜单配置信息时发生错误："+e.toString());
		}
		return infos;
	}
	public boolean saveDfUserMenu(String userid, String strMids,String strOrders) {
		boolean done = false;
		try{
			StringBuffer sql =new StringBuffer("delete from user_menus where userid =?");
			jdbcTemplate.update(sql.toString(),new Object[]{userid});
			if(!StringUtils.isEmpty(strMids)){
				sql =new StringBuffer("insert into user_menus(userid,mid,stime,dorder)values(?,?,sysdate,?)");
				String[] pids = strMids.split(",");
				String[] orders = strOrders.split(",");
				for(int j=0;j<pids.length;j++){
					String pid = pids[j];
					jdbcTemplate.update(sql.toString(),new Object[]{userid,pid,new Integer(orders[j])});
				}
			}
			done = true;
		}catch(Exception e){
			done=false;
		}
		return done;
	}
	
	public Map sendLinkDingMsg(String msg,String strUsers) {
		Map infos = new HashMap();
		String[] users = strUsers.split(",");
		String accessToken = DingHelper.getAccessToken();
		String agentid= cg.getString("AGENT_ID", "163161139");
		for(int i=0;i<users.length;i++){
			String dingid = users[i];
			//发钉钉消息
			String touser = dingid,toparty="";
			//组织消息
            MessageBody.LinkBody linkBody = new MessageBody.LinkBody();
            String lingUrl = cg.getString("directLinkToXMSP","http://112.124.8.90:8088/czyweb/index.html?redirectTo=xmsp");
            linkBody.setMessageUrl(lingUrl);
            linkBody.setPicUrl("");
            linkBody.setTitle("待审批");
            linkBody.setText(msg);
            //装配deliver
			LightAppMessageDelivery delivery = new LightAppMessageDelivery(touser,toparty,agentid);
			delivery.withMessage(MessageType.LINK, linkBody);
			try{
				MessageHelper.send(accessToken, delivery);
				log.info("发送微应用消息"+"，接收者:"+dingid);
			}catch(Exception e){
				
			}
		}
		infos.put("info", "钉钉消息已发送给下一环节负责人！");
		return infos;
	}
	//发送钉钉工作消息
	public JSONObject sendTextDingMsgProxy(String msg,String strUsers) {
		JSONObject infos = new JSONObject();
		String[] users = strUsers.split(",");
		System.out.println("原始用户数组:"+strUsers);
		Set userSet = new HashSet();
		CollectionUtils.addAll(userSet, users);
		List ulist = new ArrayList(userSet);
		String[] usersNoDup = new String[ulist.size()];
		ulist.toArray(usersNoDup);
		System.out.println("不重复的用户:"+StringUtils.join(usersNoDup, ","));
		
		String accessToken = DingHelper.getAccessToken();
		String agentid= cg.getString("AGENT_ID", "163161139");
		Map um = getUserMapping("czfc");
		for(int i=0;i<usersNoDup.length;i++){
			SimpleValue su = (SimpleValue)um.get(usersNoDup[i]);
			if(su==null){
				continue;
			}
			String dingid = su.getBm();
			String touser = dingid,toparty="";
			MessageBody.TextBody textBody = new MessageBody.TextBody();
            textBody.setContent(msg);
            //装配deliver
			LightAppMessageDelivery delivery = new LightAppMessageDelivery(touser,toparty,agentid);
			delivery.withMessage(MessageType.TEXT, textBody);
			try{
				MessageHelper.send(accessToken, delivery);
				log.info("发送微应用文本消息"+"，接收者:"+dingid);
			}catch(Exception e){
				log.error(e.toString());
				infos.put("code", 9);
				infos.put("msg", "钉钉发送失败！具体错误原因请参考日志。");
				return infos;
			}
		}
		infos.put("code", 0);
		infos.put("msg", "钉钉消息已发送！");
		return infos;
	}
	//通过短信平台发送短信
	public JSONObject sendMobileMsgProxy(String msg, String mobiles) {
		String url = cg.getString("mtUrl", "http://api.eyun.openmas.net/yunmas_api/smsApi/batchSendMessage");
		String applicationId = cg.getString("mtApplicationId", "1JbAqrjhyGp2LtI5jcw8HnsOppOp4E1BkIM");
		String password = cg.getString("mtPassword", "VGXw5iAOHxhTpHI");
		String[] mobile_list = mobiles.split(",");
		System.out.println("原始手机号数组:"+mobiles);
		Set mobileSet = new HashSet();
		CollectionUtils.addAll(mobileSet, mobile_list);
		List mlist = new ArrayList(mobileSet);
		String[] mobileNoDup = new String[mlist.size()];
		mlist.toArray(mobileNoDup);
		System.out.println("不重复的手机号码:"+StringUtils.join(mobileNoDup, ","));
		
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String requestTime=sdf.format(d);
		String funcode = cg.getString("mtFuncode", "1002");
		String signTOKEN= cg.getString("mtSignToken", "kXa8Tvc2SPjLSwU");
		System.out.println("用于sign的数据：" + applicationId+password+requestTime+signTOKEN);
		String sign = MD5Util.MD5(applicationId+password+requestTime+signTOKEN);
		
		JSONObject p = new JSONObject();
		p.put("applicationId", applicationId);
		p.put("password", password);
		p.put("requestTime", requestTime);
		p.put("funCode", funcode);
		p.put("mobiles", mobileNoDup);
		p.put("content", msg);
		p.put("sendTime", "");
		p.put("extendCode", "");
		p.put("sign", sign);
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpResponse response = null;
		int isout = 2000,icout = 2000;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().
        		setSocketTimeout(isout).setConnectTimeout(icout).build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        try {
        	StringEntity requestEntity = new StringEntity(JSON.toJSONString(p), "utf-8");
        	System.out.println("短信平台url：" + url);
        	System.out.println("发送短信时的参数：" + p.toJSONString());
            httpPost.setEntity(requestEntity);
            response = httpClient.execute(httpPost, new BasicHttpContext());
            System.out.println("短信平台的响应：" + response.toString());
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("request url failed, http code=" + response.getStatusLine().getStatusCode()
                                   + ", url=" + url);
                JSONObject res = new JSONObject();
                res.put("code", "9");
                res.put("msg", "短信平台连接错误，错误码:"+response.getStatusLine().getStatusCode());
                return res;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String resultStr = EntityUtils.toString(entity, "utf-8");
                System.out.println("短信平台的响应Entity：" + resultStr);
                JSONObject result = JSON.parseObject(resultStr);
                JSONObject res = new JSONObject();
                if(result.containsKey("resultCode")&&result.getInteger("resultCode")==0){
                	res.put("code", 0);
                	res.put("msg", "短信已发送！");
                }else{
                	res.put("code", 9);
                	res.put("msg", result.getString("resultMsg"));
                }
                return res;
            }
        } catch (IOException e) {
            System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
            e.printStackTrace();
            JSONObject res = new JSONObject();
            res.put("code", "9");
            res.put("msg", "短信平台IO发生错误，具体情况请查看日志！");
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
	
	private Map getUserMapping(String serviceName){
		Map um = new HashMap();
		try{
			String sql = "select userid,dingid,dingname from usermapping where servicename=? ";
			List users = jdbcTemplate.queryForList(sql,new Object[]{serviceName});
			if(users!=null){
				for(int i=0;i<users.size();i++){
					Map u = (Map)users.get(i);
					String userid = (String)u.get("userid");
					String dingid = (String)u.get("dingid");
					String dingname = (String)u.get("dingname");
					SimpleValue fn = new SimpleValue(dingid, dingname);
					um.put(userid, fn);
				}
			}
		}catch(Exception e){}
		return um;
	}
	
	public Map sendDingForDsp(String reqService, String reqMethod,String svParams, String userid) {
		Map infos=new HashMap();
		//远程调用，按用户统计各自待审批数量。
		JResponse jr = rtService.routeRequest(reqService,reqMethod,svParams,userid);
		log.info("sendDing返回："+jr.toString());
		JSONObject jdsps = jr==null?null:(JSONObject)jr.getRetData();
		List dsps = jdsps==null?null:(JSONArray)jdsps.get("rows");
		log.info("dsps长度："+dsps.size());
		if(dsps==null||dsps.size()==0){
			return null;
		}
		String accessToken = DingHelper.getAccessToken();
		log.info("发钉前取accessToken："+accessToken);
		String sender= cg.getString("DINGSENDER", "manager431");
		for(int i=0;i<dsps.size();i++){
			Map dsp = (Map)dsps.get(i);
			String receiver = (String)dsp.get("czyuserid");
			String scc = (String)dsp.get("cc");
			int cc = Integer.parseInt(scc);
			log.info("receiver："+receiver+"，数量："+cc);
			if(StringUtils.isEmpty(receiver)||cc<=0){
				continue;
			}
			//存在对应钉钉用户的，发钉
			DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/ding/create");
			OapiDingCreateRequest request = new OapiDingCreateRequest();
			request.setCreatorUserid(sender);
			request.setReceiverUserids(receiver);
			request.setRemindType(2L);
			request.setRemindTime(System.currentTimeMillis()+10000);
			request.setTextContent("您还有"+cc+"条审批单待处理，请及时登录财智云进行审批。");
			log.info("向用户"+receiver+"发钉！");
			try{
				OapiDingCreateResponse response = client.execute(request, accessToken);
			}catch(Throwable e){
				log.error("发送钉失败。"+e.toString());
			}
		}
		return infos;
	}
	public Map sendDingForDspTest() {
		String accessToken = DingHelper.getAccessToken();
		String sender= cg.getString("DINGSENDER", "manager431");
		DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/ding/create");
		OapiDingCreateRequest request = new OapiDingCreateRequest();
		request.setCreatorUserid(sender);
		request.setReceiverUserids("manager431");
		request.setRemindType(1L);
		request.setRemindTime(System.currentTimeMillis()+10000);
		request.setTextContent("您还有100条审批单待处理，请及时登录财智云进行审批。");
		log.info("向用户manager431发钉！");
		try{
			OapiDingCreateResponse response = client.execute(request, accessToken);
		}catch(Exception e){
			log.error("发送钉失败。"+e.toString());
		}
		return null;
	}
}
