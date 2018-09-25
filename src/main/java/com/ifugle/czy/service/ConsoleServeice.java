package com.ifugle.czy.service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.utils.bean.*;
import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.FilterField;
import com.ifugle.czy.utils.bean.template.JOutput;
import com.ifugle.czy.utils.bean.template.ValuedDs;
import com.ifugle.utils.Configuration;

@Transactional
public class ConsoleServeice {
	private static final RowCallbackHandler TreeNodeMapper = null;
	private static Logger log = Logger.getLogger(ConsoleServeice.class);
	protected JdbcTemplate jdbcTemplate;
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
				int tt = allDts.size();
				infos.put("totalCount", tt);
				int end = start+limit;
				List dts = allDts.subList(start, tt>end?end:tt);
				List rows = new ArrayList();
				for(int i=0;i<dts.size();i++){
					DataSrc dt = (DataSrc)dts.get(i);
					Map cells = new HashMap();
					cells.put("id", dt.getId());
					cells.put("name", dt.getName());
					cells.put("desc", dt.getDesc());
					cells.put("sourceType", dt.getSourceType());
					cells.put("infile", dt.getInfile());
					rows.add(cells);
				}
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
}
