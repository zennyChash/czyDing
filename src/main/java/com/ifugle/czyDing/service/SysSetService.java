package com.ifugle.czyDing.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.utils.JResponse;
import com.ifugle.czyDing.utils.bean.DeleteUserObj;
import com.ifugle.czyDing.utils.bean.QueryUserObj;
import com.ifugle.czyDing.utils.bean.RptDataJson;
import com.ifugle.czyDing.utils.bean.SaveUserObj;
import com.ifugle.czyDing.utils.bean.User;

@Transactional
public class SysSetService {
	private static Logger log = Logger.getLogger(SysSetService.class);
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	public Map getData(String rptID,RptDataJson params){
		JSONObject jrpt = null;
		String jparam = params.getRptParams();
		try{
			Map rpt = jdbcTemplate.queryForMap("select rptinfo from rptdata where rptid=? and params=?",new Object[]{rptID,jparam});
			if(rpt!=null){
				String rptinfo=(String)rpt.get("rptinfo"); 
				jrpt = JSONObject.parseObject(rptinfo);
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return jrpt;
	}
	
	public Map getParamOptions(String rptID, RptDataJson params) {
		Map mapOps = new HashMap();
		Map allOptions = new HashMap();
		JSONArray jparams = params==null?null:params.parseJOptionParams();
		try{
			if(jparams!=null){
				for(int i=0;i<jparams.size();i++){
					String spara = jparams.getString(i);
					Map opinfo = jdbcTemplate.queryForMap("select opsinfo from paramoptions where rptid=? and paramid=?",new Object[]{rptID,spara});
					if(opinfo!=null){
						String ostr=(String)opinfo.get("opsinfo"); 
						JSONObject op = JSONObject.parseObject(ostr);
						allOptions.put(spara,op);
					}
				}
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		mapOps.put("paramOptions",allOptions);
		return mapOps;
	}
	/*private Map getFndxqData(RptDataJson params) {
		Map datas = new HashMap();
		Map data = new HashMap();
		JSONObject jparams = params.parseJRptParams();
		List czsrs = new ArrayList();
		Map czsr = new HashMap();
		czsr.put("date","2008");
		czsr.put("value","123");
		czsr.put("zz","-9");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2009");
		czsr.put("value","12");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2010");
		czsr.put("value","44");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2011");
		czsr.put("value","435");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2012");
		czsr.put("value","2");
		czsr.put("zz","6");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2013");
		czsr.put("value","323");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2014");
		czsr.put("value","432");
		czsr.put("zz","21");
		czsrs.add(czsr);
		czsr = new HashMap();
		czsr.put("date","2015");
		czsr.put("value","23");
		czsr.put("zz","21");
		czsrs.add(czsr);
		data.put("czzsr", czsrs);
		
		List dfs = new ArrayList();
		Map df = new HashMap();
		df.put("date","2011");
		df.put("value","37");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2012");
		df.put("value","43");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2013");
		df.put("value","45");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2014");
		df.put("value","12");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2015");
		df.put("value","44");
		df.put("zz","-9");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2016");
		df.put("value","22");
		df.put("zz","-7");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2017");
		df.put("value","121");
		df.put("zz","21");
		dfs.add(df);
		df = new HashMap();
		df.put("date","2018");
		df.put("value","23");
		df.put("zz","21");
		dfs.add(df);
		data.put("dfczzsr", dfs);
		datas.put("data", data);
		return datas;
	}
	private Map getDzjkData(RptDataJson params){
		Map datas = new HashMap();
		JSONObject jparams = params.parseJRptParams();
		String cyear = jparams.getString("thisYear");
		String lyear = jparams.getString("lastYear");
		cyear = cyear!=null&&cyear.length()>3?cyear.substring(0,4):"";
		lyear = lyear!=null&&lyear.length()>3?lyear.substring(0,4):"";
		datas.put("total", "290912.78");
		datas.put("rate", "75");
		Map line = new HashMap();
		line.put("month", new int[]{1,2,3,4,5,6,7,8,9,10,11,12});
		List dtObjs = new ArrayList();
		Map dObj= new HashMap();
		dObj.put("name", cyear+"年");
		dObj.put("data", new Double[]{1900.89,2018.9,3309.80,4099.93,3809.1,5090.89,4578.9,6789.9,4456.89,2988.9,5674.55,3456.78});
		dtObjs.add(dObj);
		dObj= new HashMap();
		dObj.put("name", lyear+"年");
		dObj.put("data", new Double[]{1878.9,3009.87,3456.65,5543.5,2347.8,4567.34,3356.78,5643.22,3122.11,2987.5,4322.12,1233.45});
		dtObjs.add(dObj);
		line.put("object",dtObjs);
		datas.put("line", line);
		return datas;
	}
	private Map getFszxqData(RptDataJson params){
		Map datas = new HashMap();
		JSONObject jparams = params.parseJRptParams();
		List data = new ArrayList();
		Map szData = new HashMap();
		szData.put("value","335");
		szData.put("name","增值税");
		szData.put("bn","1212");
		szData.put("zz","22");
		szData.put("id","zzs");
		szData.put("bl","90");
		data.add(szData);
		szData = new HashMap();
		szData.put("value","310");
		szData.put("name","企业所得税");
		szData.put("bn","21");
		szData.put("zz","33");
		szData.put("id","qysds");
		szData.put("bl","76");
		data.add(szData);
		szData = new HashMap();
		szData.put("value","234");
		szData.put("name","其他税");
		szData.put("bn","23");
		szData.put("zz","-32");
		szData.put("id","qts");
		szData.put("bl","23");
		data.add(szData);
		szData = new HashMap();
		szData.put("value","135");
		szData.put("name","个人所得税");
		szData.put("bn","23");
		szData.put("zz","-3");
		szData.put("id","grsds");
		szData.put("bl","21");
		data.add(szData);
		datas.put("data", data);
		return datas;
	}*/
	public Map saveUserInfo(SaveUserObj so) {
		Map info = new HashMap();
		String userid = so.getUserid();
		JSONObject saveInfo = so.parseSaveContent();
		if(saveInfo==null||StringUtils.isEmpty(saveInfo.getString("saveType"))){
			info.put("saved", false);
			info.put("msg", "未设置保存类型！");
			return info;
		}
		JSONObject sobj = saveInfo.getJSONObject("saveObj");
		if(sobj==null){
			info.put("saved", false);
			info.put("msg", "未找到要保存的内容！");
			return info;
		}
		if("myFavorite".equals(saveInfo.getString("saveType"))){
			info = saveMyFavorite(userid,sobj);
		}else if("myMenus".equals(saveInfo.getString("saveType"))){
			info = saveMyMenus(userid,sobj);
		}else if("myPswd".equals(saveInfo.getString("saveType"))){
			info = saveMyPassword(userid,sobj);
		}
		return info;
	}
	private Map saveMyFavorite(String userid,JSONObject sobj){
		Map info = new HashMap();
		String swdjzh=sobj.getString("swdjzh");
		String mc = sobj.getString("mc");
		int cc = jdbcTemplate.queryForObject("select count(*)cc from user_favorite where userid=? and swdjzh=?",
				new Object[]{userid,swdjzh},Integer.class);
		if(cc==0){
			jdbcTemplate.update("insert into user_favorite(userid,swdjzh,mc)values(?,?,?)",
					new Object[]{userid,swdjzh,mc});
		}else{
			jdbcTemplate.update("update user_favorite set mc=? where userid=? and swdjzh=?",
					new Object[]{mc,userid,swdjzh});
		}
		//记录收藏事件
		jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,?)",
				new Object[]{userid,"add_myFavorite"});
		
		info.put("saved", true);
		info.put("msg", "");
		return info;
	}
	private Map saveMyMenus(String userid,JSONObject sobj){
		Map info = new HashMap();
		JSONArray menus=sobj.getJSONArray("menus");
		jdbcTemplate.update("delete from user_menus where userid=?",new Object[]{userid});
		if(menus!=null){
			for(int i=0;i<menus.size();i++){
				JSONObject menu = menus.getJSONObject(i);
				String mid = menu.getString("id");
				jdbcTemplate.update("insert into user_menus(userid,mid,stime,dorder)values(?,?,sysdate,?)",new Object[]{userid,mid,i+1});
			}
		}
		//记录调序事件
		jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,?)",
				new Object[]{userid,"sort_myMenus"});
		info.put("saved", true);
		info.put("msg", "");
		return info;
	}
	
	private Map saveMyPassword(String userid,JSONObject sobj){
		Map info = new HashMap();
		String oldPswd = sobj.getString("oldPswd");
		StringBuffer sql = null;
		if(!StringUtils.isEmpty(oldPswd)){
			sql = new StringBuffer("select decode(pswd_mode,0,pswd_kb,pswd_gesture)pswd from users where userid=?");
			String hashed = jdbcTemplate.queryForObject(sql.toString(), new Object[]{userid},String.class);
			if (!BCrypt.checkpw(oldPswd, hashed)){
				info.put("saved", false);
				info.put("msg", "旧密码输入不正确，不能保存设置！");
				return info;
			}
		}
		String pswd = sobj.getString("pswd");
		String spswd_on=sobj.getString("pswd_on");
		String spswd_mode = sobj.getString("pswd_mode");
		int pswd_on=0,pswd_mode=0;
		try{
			pswd_on = Integer.parseInt(spswd_on);
		}catch(Exception e){
		}
		try{
			pswd_mode = Integer.parseInt(spswd_mode);
		}catch(Exception e){
		}
		if(pswd_on==0){//关闭，清空密码设置信息。开启时需要重新设置
			jdbcTemplate.update("update users set pswd_on=0,pswd_mode=0,pswd_kb='',pswd_gesture='' where userid=?",new Object[]{userid});
		}else{
			String hashed = BCrypt.hashpw(pswd, BCrypt.gensalt());
			if(pswd_mode==0){
				jdbcTemplate.update("update users set pswd_on=1,pswd_mode=0,pswd_kb=?,pswd_gesture='' where userid=?",new Object[]{hashed,userid});
			}else{
				jdbcTemplate.update("update users set pswd_on=1,pswd_mode=1,pswd_kb='',pswd_gesture=? where userid=?",new Object[]{hashed,userid});
			}
		}
		//记录修改事件
		jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,?)",
				new Object[]{userid,"set_myPswd"});
		
		info.put("saved", true);
		info.put("msg", "");
		return info;
	}
	
	
	public Map deleteUserInfo(DeleteUserObj dobj) {
		Map info = new HashMap();
		String userid = dobj.getUserid();
		JSONObject delInfo = dobj.parseDeleteContent();
		if(delInfo==null||StringUtils.isEmpty(delInfo.getString("deleteType"))){
			info.put("deleted", false);
			info.put("msg", "未设置删除类型！");
			return info;
		}
		JSONArray dels = delInfo.getJSONArray("deleteObj");
		if(dels==null){
			info.put("deleted", false);
			info.put("msg", "未找到要删除的内容！");
			return info;
		}
		if("myFavorite".equals(delInfo.getString("deleteType"))){
			info = deleteMyFavorite(userid,dels);
		}
		return info;
	}
	private Map deleteMyFavorite(String userid, JSONArray dels) {
		Map info = new HashMap();
		//先获取用户的配置信息。
		for(int i=0;i<dels.size();i++){
			String swdjzh=dels.getJSONObject(i).getString("swdjzh");
			jdbcTemplate.update("delete from user_favorite where userid=? and swdjzh=?",
					new Object[]{userid,swdjzh});
		}
		//记录收藏事件
		jdbcTemplate.update("insert into user_log(id,userid,etime,eventtype)values(sq_user_log.nextval,?,sysdate,?)",
				new Object[]{userid,"delete_myFavorite"});
		info.put("deleted", true);
		info.put("msg", "");
		return info;
	}
	public JResponse getUserInfo(User user,QueryUserObj qo) {
		JResponse jr =null;
		Map info =  new HashMap();
		String userid = qo.getUserid();
		JSONObject jcdt = qo.parseQueryContent();
		String qtype = jcdt.getString("qType");
		if("myFavorite".equals(qtype)){
			info = getMyFavorite(userid,jcdt.getJSONObject("params"));
			if(info!=null){
				jr = new JResponse("0","",info); 
			}else{
				jr = new JResponse("9","获取用户收藏信息时发生错误！",null); 
			}
		}else if("czfpbm".equals(qtype)){
			if(user!=null){
				try{
					String czfpbm = user.getCzfpbm();
					if(StringUtils.isEmpty(czfpbm)){
						jr = new JResponse("9","用户未设置财政分片信息！",null);
					}else{
						info = new HashMap();
						info.put("czfpbm", czfpbm);
						jr = new JResponse("0","",info); 
					}
				}catch(Exception e){
					log.error(e.toString());
					jr = new JResponse("9","获取用户所属财政分片信息时发生错误！",null);
				}
			}else{
				log.error("未找到当前用户的信息。");
				jr = new JResponse("9","未找到当前用户的信息。",null); 
			}
		}else{
			jr = new JResponse("9","未知的用户信息类型："+qtype,null);
		}
		return jr;
	}
	
	private Map getMyFavorite(String userid,JSONObject params){
		Map info =  null;
		try{
			int from = params.getIntValue("from");
			int size = params.getIntValue("size");
			int cc = jdbcTemplate.queryForObject("select count(*) from user_favorite where userid=?", new Object[]{userid},Integer.class);
			info = new HashMap();
			info.put("total", cc);
			StringBuffer sql = new StringBuffer("select swdjzh,mc from user_favorite where userid=?");
			StringBuffer rSql = new StringBuffer("select * from (select a.*, rownum r from (");
			rSql.append(sql);
			rSql.append(") a where rownum<=?) b where r>?");
			List fvs = jdbcTemplate.queryForList(rSql.toString(),new Object[]{userid,from+size,from});
			info.put("rows", fvs);
		}catch(Exception e){
			log.error(e.toString());
			return null;
		}
		return info;
	}
}
