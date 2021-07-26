package com.ifugle.czy.service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.VelocityException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.SecureUtils;
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.utils.bean.FtsParam;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.czy.utils.bean.template.Column;
import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.FilterField;
import com.ifugle.czy.utils.bean.template.JOutput;
import com.ifugle.czy.utils.bean.template.OrderField;
import com.ifugle.czy.utils.bean.template.ProParaIn;
import com.ifugle.czy.utils.bean.template.ProParaOut;
import com.ifugle.czy.utils.bean.template.ProcedureBean;
import com.ifugle.czy.utils.bean.template.ValuedDs;
import com.ifugle.utils.Configuration;

public class ESQueryDataService {
	private static Logger log = Logger.getLogger(ESQueryDataService.class);
	@Autowired
	private Configuration cg;
	protected ESClientFactory esClient;
	@Autowired
	public void setEsService(ESClientFactory esClient){
		this.esClient = esClient;
	}
	protected JdbcTemplate jdbcTemplateDt;
	@Autowired
	public void setJdbcTemplateDt(JdbcTemplate jdbcTemplateDt){
		this.jdbcTemplateDt = jdbcTemplateDt;
	}
	
	public Map searchByKeyWord(String rptID,RptDataJson params){
		JSONObject jparams = params==null?null:params.parseJRptParams();
		String str="",fld = "",fldsToGet="";
		int from =0,size=10;
		str = jparams.getString("searchKey");
		try{
			from = jparams.getIntValue("from");
		}catch(Exception e){
			try{
				from = Integer.parseInt(jparams.getString("from"));
			}catch(Exception ex){
			}
		}
		try{
			size = jparams.getIntValue("size");
		}catch(Exception e){
			try{
				size = Integer.parseInt(jparams.getString("size"));
			}catch(Exception ex){
			}
		}
		fld = jparams.getString("field")==null?"mc":jparams.getString("field");
		fldsToGet = jparams.getString("fldsToGet")==null?"swdjzh,mc":jparams.getString("fldsToGet");
		JSONObject filterBy = jparams.getJSONObject("filterBy");
		Map result = new HashMap();
		List<Map> qymcs = new ArrayList<Map>();
		MatchQueryBuilder qb = QueryBuilders.matchQuery(fld,str);
		SearchRequestBuilder sReq = esClient.getClient().prepareSearch(rptID).setTypes("_doc");
		sReq.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		.setQuery(qb);
		//如果有filterBy，说明关键字检索的结果还要筛选过。比如街道用户，搜索结果应限制在该用户街道
		BoolQueryBuilder bflt = null;
		if(filterBy!=null){
			bflt = QueryBuilders.boolQuery();
			for (Map.Entry entry : filterBy.entrySet()) {  
			   String key = (String)entry.getKey();  
			   String value = (String)entry.getValue();
			   bflt.filter(QueryBuilders.termsQuery(key+".raw", value));
			}
			sReq.setPostFilter(bflt);
		}
		sReq.setFrom(from).setSize(size);
		SearchResponse searchResponse =sReq.get();
	    SearchHits hits = searchResponse.getHits();
	    long total = hits.getTotalHits();
	    result.put("total", total);
	    String[] flds = fldsToGet.split(",");
	    for(SearchHit hit : hits) {
	    	Map hitsrc = hit.getSourceAsMap();
	    	Map en = new HashMap();
	    	for(int i=0;i<flds.length;i++){
	    		en.put(flds[i], (String)hitsrc.get(flds[i]));
	    	}
            qymcs.add(en);
	    }
	    result.put("matches", qymcs);
        return result;
	}
	
	public Map getData(String jpID,JSONObject params){
		Map result = new HashMap();
		JSONObject jrpt = null;
		log.info("开始查询"+jpID);
		JOutput jp = TemplatesLoader.getTemplatesLoader().getJOutput(jpID);
		if(jp==null){
			result.put("done", false);
			result.put("info", "未找到页面数据的定义信息。");
			log.equals("未找到JOutput信息，ID："+jpID);
			return result;
		}
		Properties p=new Properties(); 
		p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"); 
		Velocity.init(p);  
        VelocityContext context = new VelocityContext(); 
        //先把外部请求参数也放到velocity中
        for (Map.Entry entry : params.entrySet()) {  
		   String key = (String)entry.getKey();
		   Object ov = entry.getValue();
		   if(ov!=null&&ov.getClass()==Integer.class){
			   context.put(key, (Integer)entry.getValue());
		   }else if(ov!=null&&ov.getClass()==Double.class){
			   context.put(key, (Double)entry.getValue());
		   }else{
			   try{
				   context.put(key, (String)entry.getValue());
			   }catch(Exception e){
				   log.error("解析参数值时发生错误，rptID："+jpID+",参数："+key);
				   context.put(key, "");
			   }
		   }
		}  
        log.info(jpID+"初始化模板并设置参数成功！");
		List vds = jp.getValuedDs();
		if(vds!=null){
			//vds中的ds，逐个查找，加载。
			//筛选字段的值，在参数params中查找，如果没有，则用设计文件中的值（起默认值的作用）
			log.info(jpID+"循环解析数据源,共"+vds.size()+"个数据源");
			for(int i=0;i<vds.size();i++){
				ValuedDs vd = (ValuedDs)vds.get(i);
				String dsRef = vd.getRefDtSrc();
				TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
				DataSrc ds = ltmp.getDataSrc(dsRef);
				if(ds.getUseType()==1){
					try{
						parseDtSrcOfRDB(jpID,vd,ds,params,context);
					}catch(Exception e){
						result.put("done", false);
						result.put("info", e.toString());
						log.equals(e.toString());
						return result;
					}
				}else{
					try{
						parseDtSrcOfES(jpID,vd,params,context);
					}catch(Exception e){
						result.put("done", false);
						result.put("info", e.toString());
						log.equals(e.toString());
						return result;
					}
				}
			}
		}
		try{
			log.info(jpID+"开始合并模板...");
	        StringWriter sw = new StringWriter();
	        String tmp=jp.getjTemplate();
	        Velocity.evaluate(context, sw, jp.getId(), tmp);
	        log.info(jpID+"模板合并完成...");
	        String otmp = sw.toString();
	        JSONObject jtmp = JSONObject.parseObject(otmp);
	        log.info(jtmp.toJSONString());
	        result.put("done", true);
			result.put("jpData", jtmp);
		}catch(Exception ve){
			result.put("done", false);
			result.put("info", "数据源查询成功，但模板解析时发生错误。");
			log.error(ve.toString());
		}
		return result;
	}
	
	//从ES查询数据
	private void parseDtSrcOfES(String jpID,ValuedDs vd,JSONObject params,VelocityContext context)throws Exception{
		TransportClient client = esClient.getClient();
		if(client==null){
			throw new Exception();
		}
		String dsName = vd.getName();
		log.info("解析数据源:"+dsName);
		String dsRef = vd.getRefDtSrc();
		DataSrc realDs =TemplatesLoader.getTemplatesLoader().getDataSrc(dsRef);
		List flts = vd.getFilterFlds();
		List orders = vd.getOrderByFlds();
		log.info(jpID+"筛选字段:"+(flts==null?-1:flts.size())+"个");
		log.info(jpID+"排序字段:"+(orders==null?-1:orders.size())+"个");
		SearchRequestBuilder sReq = client.prepareSearch(dsRef).setTypes("_doc");
		//如果指定了获取的字段，只取指定字段
		if(!StringUtils.isEmpty(vd.getFields())){
			sReq.setFetchSource(vd.getFields().split(","),null);
		}
		BoolQueryBuilder qb = QueryBuilders.boolQuery();
		if(flts!=null){
			log.info(jpID+"处理筛选字段...");
			for(int j=0;j<flts.size();j++){
				FilterField flt = (FilterField)flts.get(j);
				String ftype = flt.getFtype();
				String fname = flt.getName();
				String refParam = StringUtils.isEmpty(flt.getRefParam())?fname:flt.getRefParam();
				String fv = "";
				//先获取引用参数的值。如无，则使用默认的静态值。
				if(params.containsKey(refParam)){
					fv = params.getString(refParam);
				}else{
					fv = flt.getValue();
					context.put(refParam, fv);
				}
				if(StringUtils.isEmpty(fv)){
					log.error("构造JOutput信息失败，ID："+jpID+"，缺少参数"+refParam+"的值。");
					throw new Exception("缺少参数"+refParam+"的值。");
				}
				int dtype = flt.getDataType();
				if("terms".equalsIgnoreCase(ftype)){
					qb.filter(QueryBuilders.termsQuery(fname+".raw", fv));
				}else if("range".equalsIgnoreCase(ftype)){
					if(StringUtils.isEmpty(fv)||fv.indexOf(",")<0){
						continue;
					}
					boolean includeLower = false,includeUpper=false;
					if(fv.startsWith("[")){
						includeLower = true;
						fv = fv.substring(1);
					}else if(fv.startsWith("(")){
						fv = fv.substring(1);
					}
					if(fv.endsWith("]")){
						includeUpper = true;
						fv = fv.substring(0, fv.length()-1);
					}else if(fv.endsWith(")")){
						fv = fv.substring(0, fv.length()-1);
					}
					String[] sranges = fv.split(",");
					String sfrom = "",sto = "";
					if(sranges.length==1){
						if(fv.startsWith(",")){
							sto = sranges[0];
						}else{
							sfrom = sranges[0];
						}
					}else{
						sfrom = sranges[0];
						sto = sranges[1];
					}
					if(dtype==1){
						RangeQueryBuilder rq = QueryBuilders.rangeQuery(fname);
						if(!StringUtils.isEmpty(sfrom)){
							if(includeLower){
								rq.gte(Integer.parseInt(sfrom));
							}else{
								rq.gt(Integer.parseInt(sfrom));
							}
						}
						if(!StringUtils.isEmpty(sto)){
							if(includeUpper){
								rq.lte(Integer.parseInt(sto));
							}else{
								rq.lt(Integer.parseInt(sto));
							}
						}
						qb.filter(rq);
					}else if(dtype==2){
						RangeQueryBuilder rq = QueryBuilders.rangeQuery(fname);
						if(!StringUtils.isEmpty(sfrom)){
							if(includeLower){
								rq.gte(Double.parseDouble(sfrom));
							}else{
								rq.gt(Double.parseDouble(sfrom));
							}
						}
						if(!StringUtils.isEmpty(sto)){
							if(includeUpper){
								rq.lte(Double.parseDouble(sto));
							}else{
								rq.lt(Double.parseDouble(sto));
							}
						}
						qb.filter(rq);
					}else{
						RangeQueryBuilder rq = QueryBuilders.rangeQuery(fname+".raw");
						if(!StringUtils.isEmpty(sfrom)){
							rq = rq.from(sfrom,includeLower);
						}
						if(!StringUtils.isEmpty(sto)){
							rq = rq.to(sto, includeUpper);
						}
						qb.filter(rq);
					}
				}else{
					qb.filter(QueryBuilders.termsQuery(fname+".raw", fv));
				}
			}
		}
		if(orders!=null){
			log.info(jpID+"处理排序字段...");
			for(int k=0;k<orders.size();k++){
				OrderField oflt = (OrderField)orders.get(k);
				SortBuilder sortBuilder = SortBuilders.fieldSort(oflt.getDataType()==0?oflt.getName()+".raw":oflt.getName());
				if("desc".equalsIgnoreCase(oflt.getDir())){
					sortBuilder.order(SortOrder.DESC);
				}else{
					sortBuilder.order(SortOrder.ASC);
				}
				sReq.addSort(sortBuilder);
			}
		}
		sReq.setQuery(qb);
		//处理查询的分页。外部。和作为查询筛选条件的参数不一样。
		if(vd.isPaging()){
			log.info(jpID+"处理分页字段...");
			String fromFld = vd.getStartParam();
			String sizeFld = vd.getSizeParam();
			int from = 0,size=0;
			try{
				from = params.getInteger(fromFld);
			}catch(Exception e){}
			try{
				size = params.getInteger(sizeFld);
			}catch(Exception e){}
			if(size==0){
				size=10;
			}
			sReq.setFrom(from).setSize(size);
		}else{
			log.info(jpID+"添加默认分页字段...");
			String ms =cg.getString("maxQuerySize", "200");
			try{
				sReq.setFrom(0).setSize(Integer.parseInt(ms));
			}catch(Exception e){
				sReq.setFrom(0).setSize(20);
			}
		}
		log.info(jpID+"ES查询语句组织完成，开始查询...");
		Map decipherCols = realDs.getDecipherColsMap();
		SearchResponse response = sReq.get();
		SearchHits hits = response.getHits();
	    long total = hits.getTotalHits();
	    Map qinfos = new HashMap();
	    qinfos.put("total", total);
	    List rows = new ArrayList();
	    for(SearchHit hit : hits) {
	    	Map hitsrc = hit.getSourceAsMap();
	    	Map nrow = new HashMap();
	    	for (Iterator it = hitsrc.keySet().iterator(); it.hasNext();) {  
        		String key = (String)it.next();
        		String v = hitsrc.get(key)==null?"":hitsrc.get(key).toString();
    			//如果有需要解密的字段，解密该字段
		    	if(decipherCols!=null&&decipherCols.containsKey(key.toLowerCase())){
		    		Column col = (Column)decipherCols.get(key.toLowerCase());
		    		String algorithm = col.getAlgorithm();
		    		v = doDecipher(v,algorithm);
		    	}
        		nrow.put(key.toLowerCase(), v); 
	        }
        	rows.add(nrow);
	    }
	    qinfos.put("rows", rows);
	    context.put(dsName, qinfos);
	    log.info("完成"+dsName+"的查询！");
	}
	
	//从关系型数据库查询数据
	private void parseDtSrcOfRDB(String jpID,ValuedDs vd,DataSrc ds,JSONObject params,VelocityContext context)throws Exception{
		String dsName = vd.getName();
		log.info("解析数据源:"+dsName);
		if(ds.getSourceType()==1){
	    	excuteSql(ds,vd,params,context);
		}else if(ds.getSourceType()==2){
			excuteProcedure(ds,vd,params,context);
		}
	}
	@SuppressWarnings("unchecked")
	private void excuteProcedure(DataSrc ds,ValuedDs vd,JSONObject paramVals,VelocityContext context)throws Exception{
		final Map infos = new HashMap();
		ProcedureBean pro=ds.getProcedure();
		if(pro==null){
			log.error("未设置取数存储过程！");
			throw new Exception("数据源定了存储过程取数方式，但未找到存储过程定义！");
		}
		
		List parasIn=pro.getInParas();
		StringBuffer proStmt=new StringBuffer("{call ");
		proStmt.append(pro.getName());
		//根据输入参数定义的个数设置?
		if(parasIn!=null&&parasIn.size()>0){
			proStmt.append("(");
			for(int i=0;i<parasIn.size();i++){
				proStmt.append("?");
				if(i<parasIn.size()-1){
					proStmt.append(",");
				}
			}
		}
		//根据输出参数定义继续设置?
		List parasOut=pro.getOutParas();
		if(parasOut!=null&&parasOut.size()>0){
			if(parasIn==null||parasIn.size()==0){
				proStmt.append("(");
			}else{
				proStmt.append(",");
			}
			for(int i=0;i<parasOut.size();i++){
				proStmt.append("?");
				if(i<parasOut.size()-1){
					proStmt.append(",");
				}else{
					proStmt.append(")");
				}
			}
		}else{
			if(parasIn!=null&&parasIn.size()>0){
				proStmt.append(")");
			}
		}
		proStmt.append("}");
		final List rows = new ArrayList();
		infos.put("total", 0);
		infos.put("rows", rows);
		Map decipherCols = ds.getDecipherColsMap();
		Map parasDef = vd.getFilterFldsMap();
		jdbcTemplateDt.execute(proStmt.toString(),new CallableStatementCallback(){
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
				if(parasIn!=null&&parasIn.size()>0){
					for(int i=0;i<parasIn.size();i++){
						//过程参数引用方式分直接引用固定值和引用参数两种
						ProParaIn pi=(ProParaIn)parasIn.get(i);
						if(pi!=null&&pi.getReferMode()==0){
							FilterField pf = (FilterField)parasDef.get(pi.getReferTo());
							if(pf.getDataType()==1){
								int ival=0;
								try{ival=Integer.parseInt(pi.getValue());}
								catch(Exception e){}
								cs.setInt(i+1, ival);
								log.info("参数(整型)"+pi.getReferTo()+":"+ival);
							}else if(pf.getDataType()==2){
								double dval=0;
								try{dval=Double.parseDouble(pi.getValue());}
								catch(Exception e){}
								cs.setDouble(i+1, dval);
								log.info("参数(小数)"+pi.getReferTo()+":"+dval);
							}else{
								cs.setString(i+1, pf.getValue());
								log.info("参数(字符串)"+pi.getReferTo()+":"+pi.getValue());
							}
						}else{
							if(paramVals==null){
								log.error("缺少参数值。参数："+pi.getReferTo());
							}
							//找出输入参数的定义
							FilterField pf = (FilterField)parasDef.get(pi.getReferTo());
							String val="";
							String pname = pf==null? pi.getReferTo():pf.getRefParam();
							Object oval=paramVals.get(pname);
							if(oval==null){
								val=null;
							}else{
								val = String.valueOf(oval);
							}
							if(pf!=null&&pf.getDataType()==1){
								int iVal=0;
								try{
									iVal=Integer.parseInt(val);
								}catch(Exception e){}
								cs.setInt(i+1, iVal);
								log.info("参数(整型)"+pf.getRefParam()+":"+iVal);
							}else if(pf!=null&&pf.getDataType()==2){
								double dVal=0;
								try{
									dVal=Double.parseDouble(val);
								}catch(Exception e){}
								cs.setDouble(i+1, dVal);
								log.info("参数(小数)"+pf.getRefParam()+":"+dVal);
							}else{
								cs.setString(i+1,val);
								log.info("参数(字符串)"+pname+":"+val);
							}
						}
					}
				}
				//注册输出参数
				int oStart=parasIn==null?1:parasIn.size()+1;
				if(parasOut!=null){
					for(int i=0;i<parasOut.size();i++){
						ProParaOut po=(ProParaOut)parasOut.get(i);
						if(po.getDataType()==1||po.getDataType()==2){
							cs.registerOutParameter(oStart+i, Types.NUMERIC);
						}else if(po.getDataType()==0){
							cs.registerOutParameter(oStart+i, Types.VARCHAR);
						}else if(po.getDataType()==3){
							cs.registerOutParameter(oStart+i, oracle.jdbc.OracleTypes.CURSOR);
						}
					}
				}
                cs.execute(); 
                int ti=ds.getProcedure().getTotalIndex();
            	int total = cs.getInt(oStart-1+ti);
                infos.put("total", total);
                ResultSet rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex()); 
                if(rs==null){
                	return 0;
                }
                String[] flds = null;
                if(!StringUtils.isEmpty(vd.getFields())){
    				flds=vd.getFields().split(",");
    				while(rs.next()){
    		        	Map row = new HashMap();
    		        	for (int j=0;j<flds.length;j++){  
    		        		try{
    		        			String v = rs.getString(flds[j].toLowerCase());
    		        			//如果有需要解密的字段，解密该字段
    					    	if(decipherCols!=null&&decipherCols.containsKey(flds[j].toLowerCase())){
    					    		Column col = (Column)decipherCols.get(flds[j].toLowerCase());
    					    		String algorithm = col.getAlgorithm();
    					    		v = doDecipher(v,algorithm);
    					    	}
    		        			row.put(flds[j].toLowerCase(),v);
    		        		}catch(Exception e){
    		        		}
    			        }
    		        	rows.add(row);
    				}
    			}else{
	                ResultSetMetaData rsmd=rs.getMetaData();
	        		//获取元信息
	        		int colNum=rsmd.getColumnCount();
	                while (rs.next()) {
	                	Map row = new HashMap();
	                	for(int i=1;i<=colNum;i++){
	        				String colName = rsmd.getColumnLabel(i).toLowerCase();
			        		String v = rs.getString(i);
		        			//如果有需要解密的字段，解密该字段
					    	if(decipherCols!=null&&decipherCols.containsKey(colName.toLowerCase())){
					    		Column col = (Column)decipherCols.get(colName.toLowerCase());
					    		String algorithm = col.getAlgorithm();
					    		v = doDecipher(v,algorithm);
					    	}
					    	row.put(colName, v);
	        			}
	                	rows.add(row);
	                }
    			}
                infos.put("rows", rows);
                return infos;
			}
		});
		context.put(vd.getName(), infos);
		return ;
	}
	
	private void excuteSql(DataSrc ds,ValuedDs vd,JSONObject paramVals,VelocityContext context)throws Exception{
		int cc = 0;
		String sql=ds.getSql();
		if(sql==null)return ;
		Map qinfos = new HashMap();
		Map parasDef = vd.getFilterFldsMap();
		//先处理参数值替换
		String[] paras=StringUtils.substringsBetween(sql, "{", "}");
		String[] rplParas = StringUtils.substringsBetween(sql, "[", "]");
		String[] rpl2pers = StringUtils.substringsBetween(sql,"%[","]%");
		if(rpl2pers!=null&&rpl2pers.length>0){
			for(int i=0;i<rpl2pers.length;i++){
				FilterField pf = (FilterField)parasDef.get(rpl2pers[i]);
				sql = replaceParamValue(sql,pf==null?0:pf.getDataType(),rpl2pers[i],paramVals,2);
			}
		}
		String[] rplFpers=StringUtils.substringsBetween(sql,"%[","]");
		if(rplFpers!=null&&rplFpers.length>0){
			for(int i=0;i<rplFpers.length;i++){
				FilterField pf = (FilterField)parasDef.get(rpl2pers[i]);
				sql = replaceParamValue(sql,pf==null?0:pf.getDataType(),rplFpers[i],paramVals,0);
			}
		}
		String[] rplTpers=StringUtils.substringsBetween(sql,"[","]%");
		if(rplTpers!=null&&rplTpers.length>0){
			for(int i=0;i<rplTpers.length;i++){
				FilterField pf = (FilterField)parasDef.get(rpl2pers[i]);
				sql = replaceParamValue(sql,pf==null?0:pf.getDataType(),rplTpers[i],paramVals,1);
			}
		}
		if(rplParas!=null&&rplParas.length>0){
			for(int i=0;i<rplParas.length;i++){
				FilterField pf = (FilterField)parasDef.get(rpl2pers[i]);
				sql = replaceParamValue(sql,pf==null?0:pf.getDataType(),rplParas[i],paramVals,9);
			}
		}
		//如果有参数引用――{abc..}，替换成?，并提取其中的参数
		//2009-04-28为适应like中的%%
		String[] has2pers=StringUtils.substringsBetween(sql,"%{","}%");
		sql=sql.replaceAll("%\\{\\w*\\}%","?");
		String[] hasFpers=StringUtils.substringsBetween(sql,"%{","}");
		sql=sql.replaceAll("%\\{\\w*\\}","?");
		//String[] hasTpers=StringUtils.substringsBetween(sql,"{","}%");
		List lstTpers = new ArrayList();
		String tmpSql = sql;
		while(true){
			int end= tmpSql.indexOf("}%");
			if(end<0){
				break;
			}
			String preSql = tmpSql.substring(0,end);
			int start = preSql.lastIndexOf("{");
			String p = preSql.substring(start+1,end);
			tmpSql = tmpSql.substring(end+1);
			lstTpers.add(p);
		}
		String[] hasTpers =lstTpers.size()==0?null:new String[lstTpers.size()]; 
		for(int i=0;i<lstTpers.size();i++){
			hasTpers[i]=(String)lstTpers.get(i);
		}
		sql=sql.replaceAll("\\{\\w*\\}%","?");
		Map paraSearchModes=new HashMap();
		if(has2pers!=null){
			for(int i=0;i<has2pers.length;i++){
				paraSearchModes.put(has2pers[i], "2");
			}
		}
		if(hasFpers!=null){
			for(int i=0;i<hasFpers.length;i++){
				paraSearchModes.put(hasFpers[i], "0");
			}
		}
		if(hasTpers!=null){
			for(int i=0;i<hasTpers.length;i++){
				paraSearchModes.put(hasTpers[i], "1");
			}
		}
		sql=sql.replaceAll("\\{\\w*\\}","?");
		StringBuffer qSql = new StringBuffer("SELECT COUNT(*) AS RCOUNT FROM(");
        qSql.append(sql).append(")");
      //如果没有参数引用
  		if(paras==null||paras.length==0){
  			cc = jdbcTemplateDt.queryForObject(qSql.toString(),Integer.class);
  		}else{
  			Object[] params = parseSqlParameter(parasDef,paramVals,paras,paraSearchModes);
  			cc = jdbcTemplateDt.queryForObject(qSql.toString(),params,Integer.class);
  		}
	    qinfos.put("total", cc);
		//分页时，加工分页sql
		if(vd.isPaging()){
			String fromFld = vd.getStartParam();
			String sizeFld = vd.getSizeParam();
			int from = 0,size=0;
			try{
				from = paramVals.getInteger(fromFld);
			}catch(Exception e){}
			try{
				size = paramVals.getInteger(sizeFld);
			}catch(Exception e){}
			if(size==0){
				size=10;
			}
			qSql = new StringBuffer("SELECT * FROM (SELECT A.*, rownum r FROM (");
	        qSql.append(sql);
	        qSql.append(") A WHERE rownum<=");
	        qSql.append((from+size));
	        qSql.append(") B WHERE r>");
	        qSql.append(from);
	        sql = qSql.toString();
		}else{
			qSql = new StringBuffer(sql);
		}
		List lst = null;
		//如果没有参数引用
		if(paras==null||paras.length==0){
			lst = jdbcTemplateDt.queryForList(qSql.toString());
		}else{
			Object[] params = parseSqlParameter(parasDef,paramVals,paras,paraSearchModes);
			lst = jdbcTemplateDt.queryForList(qSql.toString(),params);
		}
		Map decipherCols = ds.getDecipherColsMap();
		if(lst!=null&&lst.size()>0){
			List rows = new ArrayList();
			String[] flds = null;
			if(!StringUtils.isEmpty(vd.getFields())){
				flds=vd.getFields().split(",");
				for(int i=0;i<lst.size();i++){
					Map row = (Map)lst.get(i);
		        	Map nrow = new HashMap();
		        	for (int j=0;j<flds.length;j++){  
		        		if(row.containsKey(flds[j].toLowerCase())){
		        			String v = row.get(flds[j].toLowerCase())==null?"":row.get(flds[j].toLowerCase()).toString();
		        			//如果有需要解密的字段，解密该字段
					    	if(decipherCols!=null&&decipherCols.containsKey(flds[j].toLowerCase())){
					    		Column col = (Column)decipherCols.get(flds[j].toLowerCase());
					    		String algorithm = col.getAlgorithm();
					    		v = doDecipher(v,algorithm);
					    	}
		        			nrow.put(flds[j].toLowerCase(),v); 
		        		}
			        }
		        	rows.add(nrow);
				}
			}else{
		        for(int i=0;i<lst.size();i++){
		        	Map row = (Map)lst.get(i);
		        	Map nrow = new HashMap();
		        	for (Iterator it = row.keySet().iterator(); it.hasNext();) {  
		        		String key = (String)it.next();
		        		String v = row.get(key)==null?"":row.get(key).toString();
	        			//如果有需要解密的字段，解密该字段
				    	if(decipherCols!=null&&decipherCols.containsKey(key.toLowerCase())){
				    		Column col = (Column)decipherCols.get(key.toLowerCase());
				    		String algorithm = col.getAlgorithm();
				    		v = doDecipher(v,algorithm);
				    	}
		        		nrow.put(key.toLowerCase(), v); 
			        }
		        	rows.add(nrow);
		        }
			}
			qinfos.put("rows", rows);
		    context.put(vd.getName(), qinfos);
		}
		return;
	}
	private String replaceParamValue(String sql,int dataType,String pName,JSONObject paraValues,int rmode){
		String rvalue = (String)paraValues.get(pName);
		//按参数类型转变值
		if(dataType==0){
			if(rmode==9){//如果不是like，('%%')的形式，需转化成每个逗号之间都插入单引号
				rvalue = rvalue.replaceAll(",", "','");
			}
		}
		//值的替换，有like操作的，一律当字符串处理，加''，非like操作，要根据数据类型确定是否添加''
		if(rmode==2){//%在两头
			sql=sql.replace("%["+pName+"]%","'%"+rvalue+"%'");
		}else if(rmode==0){//%在前
			sql=sql.replace("%["+pName+"]","'%"+rvalue+"'");
		}else if(rmode==1){//%在后
			sql=sql.replace("["+pName+"]%","'"+rvalue+"%'");
		}else{//无%
			sql=sql.replace("["+pName+"]","'"+rvalue+"'");
		}
		return sql;
	}
	private Object[] parseSqlParameter(Map parasDef,JSONObject paraValues,String[] paras,Map fuzzySearchPara)throws Exception{
		//如果没有引用参数，可以直接返回
		if(paras==null||paras.length==0)return null;
		Object[] pvs = new Object[paras.length];
		for(int i=0;i<paras.length;i++){
			FilterField pf = (FilterField)parasDef.get(paras[i]);
			String val="";
			String pname = pf==null? paras[i]:pf.getRefParam();
			Object oval=paraValues.get(pname);
			if(oval==null){
				val=null;
			}else{
				val = String.valueOf(oval);
			}
			if(pf!=null&&pf.getDataType()==1){
				int iVal=0;
				try{
					iVal=Integer.parseInt(val);
				}catch(Exception e){}
				pvs[i]=iVal;
			}else if(pf!=null&&pf.getDataType()==2){
				double dVal=0;
				try{
					dVal=Double.parseDouble(val);
				}catch(Exception e){}
				pvs[i]=dVal;
			}else{
				if(fuzzySearchPara.containsKey(paras[i])){
					if("2".equals(fuzzySearchPara.get(paras[i]))){
						val="%"+val+"%";
					}else if("0".equals(fuzzySearchPara.get(paras[i]))){
						val="%"+val;
					}else if("1".equals(fuzzySearchPara.get(paras[i]))){
						val=val+"%";
					}
				}
				pvs[i]=val;
			}
			log.info("参数"+pname+":"+val);
		}
		return pvs;
	 }
//	private String replaceBlank(String str) {
//		String dest = "";
//		if (str!=null) {
//			Pattern p = Pattern.compile("\\t|\r|\n");
//			Matcher m = p.matcher(str);
//			dest = m.replaceAll("");
//		}
//		return dest;
//	}

	public Map showQuery(String jpID, JSONObject params) {
		Map result = new HashMap();
		JSONObject jrpt = null;
		TransportClient client = esClient.getClient();
		JOutput jp = TemplatesLoader.getTemplatesLoader().getJOutput(jpID);
		if(jp==null){
			result.put("done", false);
			result.put("info", "未找到页面数据的定义信息。");
			log.equals("未找到JOutput信息，ID："+jpID);
			return result;
		}
		List vds = jp.getValuedDs();
		if(vds!=null){
			Map mapQuery = new HashMap();
			//vds中的ds，逐个查找，加载。
			//筛选字段的值，在参数params中查找，如果没有，则用设计文件中的值（起默认值的作用）
			for(int i=0;i<vds.size();i++){
				ValuedDs vd = (ValuedDs)vds.get(i);
				String dsName = vd.getName();
				String dsRef = vd.getRefDtSrc();
				List flts = vd.getFilterFlds();
				List orders = vd.getOrderByFlds();
				SearchRequestBuilder sReq = client.prepareSearch(dsRef).setTypes("_doc");
				//如果指定了获取的字段，只取指定字段
				if(!StringUtils.isEmpty(vd.getFields())){
					sReq.setFetchSource(vd.getFields().split(","),null);
				}
				BoolQueryBuilder qb = QueryBuilders.boolQuery();
				if(flts!=null){
					for(int j=0;j<flts.size();j++){
						FilterField flt = (FilterField)flts.get(j);
						String ftype = flt.getFtype();
						String fname = flt.getName();
						String refParam = StringUtils.isEmpty(flt.getRefParam())?fname:flt.getRefParam();
						String fv = "";
						//先获取引用参数的值。如无，则使用默认的静态值。
						if(params.containsKey(refParam)){
							fv = params.getString(refParam);
						}else{
							fv = flt.getValue();
						}
						if(StringUtils.isEmpty(fv)){
							result.put("done", false);
							result.put("info", "缺少参数"+refParam+"的值。");
							log.equals("构造JOutput信息失败，ID："+jpID+"，缺少参数"+refParam+"的值。");
							return result;
						}
						int dtype = flt.getDataType();
						if("terms".equalsIgnoreCase(ftype)){
							qb.filter(QueryBuilders.termsQuery(fname+".raw", fv));
						}else if("range".equalsIgnoreCase(ftype)){
							if(StringUtils.isEmpty(fv)||fv.indexOf(",")<0){
								continue;
							}
							boolean includeLower = false,includeUpper=false;
							if(fv.startsWith("[")){
								includeLower = true;
								fv = fv.substring(1);
							}else if(fv.startsWith("(")){
								fv = fv.substring(1);
							}
							if(fv.endsWith("]")){
								includeUpper = true;
								fv = fv.substring(0, fv.length()-1);
							}else if(fv.endsWith(")")){
								fv = fv.substring(0, fv.length()-1);
							}
							String[] sranges = fv.split(",");
							String sfrom = "",sto = "";
							if(sranges.length==1){
								if(fv.startsWith(",")){
									sto = sranges[0];
								}else{
									sfrom = sranges[0];
								}
							}else{
								sfrom = sranges[0];
								sto = sranges[1];
							}
							if(dtype==1){
								RangeQueryBuilder rq = QueryBuilders.rangeQuery(fname);
								if(!StringUtils.isEmpty(sfrom)){
									if(includeLower){
										rq.gte(Integer.parseInt(sfrom));
									}else{
										rq.gt(Integer.parseInt(sfrom));
									}
								}
								if(!StringUtils.isEmpty(sto)){
									if(includeUpper){
										rq.lte(Integer.parseInt(sto));
									}else{
										rq.lt(Integer.parseInt(sto));
									}
								}
								qb.filter(rq);
							}else if(dtype==2){
								RangeQueryBuilder rq = QueryBuilders.rangeQuery(fname);
								if(!StringUtils.isEmpty(sfrom)){
									if(includeLower){
										rq.gte(Double.parseDouble(sfrom));
									}else{
										rq.gt(Double.parseDouble(sfrom));
									}
								}
								if(!StringUtils.isEmpty(sto)){
									if(includeUpper){
										rq.lte(Double.parseDouble(sto));
									}else{
										rq.lt(Double.parseDouble(sto));
									}
								}
								qb.filter(rq);
							}else{
								RangeQueryBuilder rq = QueryBuilders.rangeQuery(fname+".raw");
								if(!StringUtils.isEmpty(sfrom)){
									rq = rq.from(sfrom,includeLower);
								}
								if(!StringUtils.isEmpty(sto)){
									rq = rq.to(sto, includeUpper);
								}
								qb.filter(rq);
							}
						}else{
							qb.filter(QueryBuilders.termsQuery(fname+".raw", fv));
						}
					}
				}
				if(orders!=null){
					for(int k=0;k<orders.size();k++){
						OrderField oflt = (OrderField)orders.get(k);
						SortBuilder sortBuilder = SortBuilders.fieldSort(oflt.getDataType()==0?oflt.getName()+".raw":oflt.getName());
						if("desc".equalsIgnoreCase(oflt.getDir())){
							sortBuilder.order(SortOrder.DESC);
						}else{
							sortBuilder.order(SortOrder.ASC);
						}
						sReq.addSort(sortBuilder);
					}
				}
				sReq.setQuery(qb);
				//处理查询的分页。外部。和作为查询筛选条件的参数不一样。
				if(vd.isPaging()){
					String fromFld = vd.getStartParam();
					String sizeFld = vd.getSizeParam();
					int from = 0,size=0;
					try{
						from = params.getInteger(fromFld);
					}catch(Exception e){}
					try{
						size = params.getInteger(sizeFld);
					}catch(Exception e){}
					if(size==0){
						size=10;
					}
					sReq.setFrom(from).setSize(size);
				}else{
					String ms =cg.getString("maxQuerySize", "200");
					try{
						sReq.setFrom(0).setSize(Integer.parseInt(ms));
					}catch(Exception e){
						sReq.setFrom(0).setSize(20);
					}
				}
				String strQ=sReq.toString();
				JSONObject jq = JSON.parseObject(strQ);
			    mapQuery.put(dsName, jq);
			}
			result.put("done", true);
			result.put("queries", mapQuery);
		}
		return result;
	}
	
	private String doDecipher(String rawValue,String algorithm){
		String newVal = SecureUtils.decipher(rawValue,algorithm);
		return newVal;
	}
	//2020-05 获取全文检索可关联的资源
	public List getFtsResources(String idx) throws Exception{
		List objs = new ArrayList();
		StringBuffer sql =new StringBuffer("select * from fts_resource where idx =? order by showorder");
		List rows = jdbcTemplateDt.queryForList(sql.toString(),new Object[]{idx});
	    for(int i=0;i<rows.size();i++) {
	    	Map row = (Map)rows.get(i);
	    	Map o = new HashMap();
	    	Iterator it = row.entrySet().iterator();
            while(it.hasNext()) {
                Entry<String, Object> entry = (Entry)it.next();
                o.put(((String)entry.getKey()).toLowerCase(), row.get(entry.getKey()));
            }
	    	objs.add(o);
	    }
		return objs;
	}

	public Map oraFtsByKeyWord(String idx, FtsParam params) throws Exception{
		Map result = new HashMap();
		long total =0; 
	    List objs = null;
		JSONObject jparams = params==null?null:params.parseFtsParams();
		String searchKey = jparams.containsKey("searchKey")?jparams.getString("searchKey"):null;
		if(StringUtils.isEmpty(searchKey)){
			result.put("total", total);
		    result.put("matches", objs);
	        return result;
		}
		int searchSize = jparams.containsKey("searchSize")?jparams.getIntValue("searchSize"):5;
		//sql查找
		Map midx = cg.getOraFtsIdxMap();
		if(midx==null){
			throw new Exception("未配置可用的全文检索索引！");
		}
		if(!midx.containsKey(idx)){
			throw new Exception("未找到指定索引"+idx+"的配置信息！");
		}
		Map oi=(Map)midx.get(idx);
		String sql = (String)oi.get("qsql");
		if(sql==null||"".equals(sql)){
			throw new Exception("未找到指定索引"+idx+"的配置搜索语句配置信息！"); 
		}
		List rows = jdbcTemplateDt.queryForList(sql.toString(),new Object[]{searchKey,searchSize});
	    if(rows!=null){
	    	total = rows.size();
	    	objs = new ArrayList();
		    for(int i=0;i<rows.size();i++) {
		    	Map row = (Map)rows.get(i);
		    	Map o = new HashMap();
		    	Iterator it = row.entrySet().iterator();
	            while(it.hasNext()) {
	                Entry<String, Object> entry = (Entry)it.next();
	                o.put(((String)entry.getKey()).toLowerCase(), row.get(entry.getKey()));
	            }
		    	objs.add(o);
		    }
	    }
	    result.put("total", total);
	    result.put("matches", objs);
        return result;
	}
}
