package com.ifugle.czy.service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.DataSourceTemplateParser;
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.utils.bean.template.Column;
import com.ifugle.czy.utils.bean.template.DataSrc;
import com.ifugle.czy.utils.bean.template.ProParaIn;
import com.ifugle.czy.utils.bean.template.ProParaOut;
import com.ifugle.czy.utils.bean.template.ProcedureBean;

@Transactional
public class ESDataSourceService {
	private static Logger log = Logger.getLogger(ESDataSourceService.class);
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
	//删除索引
	public String delelteIndex(String indexName){
		StringBuffer info= new StringBuffer("{success:");
		//如果传入的indexName不存在会出现异常，先判断要删除的是否存在
		IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);  
		TransportClient client = esClient.getClient();
        IndicesExistsResponse inExistsResponse = client.admin().indices()  
                .exists(inExistsRequest).actionGet();  
        if(inExistsResponse.isExists()){  
        	DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(indexName)  
                .execute().actionGet();  
        	info.append(dResponse.isAcknowledged()?"true":"false,info:'删除索引时发生系统错误！'").append("}");  
        }else{
        	info.append("false,info:'").append("指定的索引不存在。索引名:"+indexName).append("'}");  
        }
		return info.toString();
	}
	//重新索引数据。如果指定reIndex，就重建整个索引。reIndex为false表示只将数据重索引。
	public String indexData(String indexName,boolean reIndex,boolean deleteOldData, Map paramVals){
		log.info("脚本执行开始:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
		StringBuffer info= new StringBuffer("{success:");
		TransportClient client = esClient.getClient();
		boolean needRemap = false;
		if(reIndex){
			buildNewIndex(indexName);
			needRemap = true;
		}else{
			//检查是否存在
			IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);  
	        IndicesExistsResponse inExistsResponse = client.admin().indices().exists(inExistsRequest).actionGet();  
	        //不存在的话，也要先构建index和mapping
	        if(!inExistsResponse.isExists()){  
	        	CreateIndexRequestBuilder cib=client.admin().indices().prepareCreate(indexName);
	            CreateIndexResponse res=cib.execute().actionGet(); 
	            needRemap = true;
//	            if(res.isAcknowledged()){
//	            	buildMappings(indexName);
//	            }
	        }else if(deleteOldData){//如果已经存在，检查是否需要删除旧数据。
				//先删除以前的数据，按查询删除数据
				DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
				.source(indexName)
				.filter(QueryBuilders.matchAllQuery())
				.get(); 
			}
		}
		//取数，执行，将数据作为document添加到索引。
		try{
			int cc = indexDbData(indexName,paramVals,needRemap);
			info.append("true,indexDocs:").append(cc).append("}");
			log.info("脚本执行结束:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
			log.info("共处理"+cc+"条记录！");
		}catch(Exception e){
			info.append("false,info:'").append("从数据库读取数据生成ES索引时发生错误！数据源："+indexName+"。错误信息："+e.getMessage()+"'}");
			log.error(e.toString());
		}
		return info.toString();
	}
	//建索引。如果已存在，删除旧的重建
	private String buildNewIndex(String indexName){
		StringBuffer info= new StringBuffer("{success:");
		TransportClient client = esClient.getClient();
		
		delelteIndex(indexName);
		
        CreateIndexRequestBuilder cib=client.admin().indices().prepareCreate(indexName);
        CreateIndexResponse res=cib.execute().actionGet(); 
//        if(res.isAcknowledged()){
//        	buildMappings(indexName);
//        }
        info.append("true}");
        return info.toString();
	}
	private int indexDbData(String indexName,Map paramVals,boolean needRemap)throws Exception{
		int cc = 0;
		DataSrc ds = TemplatesLoader.getTemplatesLoader().getDataSrc(indexName);
	    if(ds==null){
	    	throw new Exception("未找到ID为"+indexName+"的数据源定义信息！");
	    }
	    if(ds.getSourceType()==1){
	    	cc = excuteSql(ds,paramVals,needRemap);
		}else if(ds.getSourceType()==2){
			cc = excuteProcedure(ds,paramVals,needRemap);
		}
		return cc;
	}
	
	private XContentBuilder buildMappings(String indexName,List meta){
		XContentBuilder mapping = null;
		DataSrc ds = TemplatesLoader.getTemplatesLoader().getDataSrc(indexName);
	    if(ds==null){
	    	return null;
	    }
	    try{
		    mapping = XContentFactory.jsonBuilder().startObject()  
	                .startObject("_doc").startObject("properties");
		    Map cols = ds.getColMap();
		    for(int i = 0;i<meta.size();i++){
		    	Map oneCol = (Map)meta.get(i);
		    	String cn = (String)oneCol.get("cName");
		    	String ctype = (String)oneCol.get("cType");
		    	//如果在xml中特别定义了col，按col定义，否则，默认按字段属性构造mapping
		    	if(cols.containsKey(cn)){
		    		Column col = (Column)cols.get(cn);
			    	String fldtype = StringUtils.isEmpty(col.getFldType())?"text":col.getFldType();
			    	mapping.startObject(col.getName()).field("type",fldtype);
			    	if(col.getIsFilter()==1||col.getCanOrder()==1){
			    		mapping.startObject("fields").startObject("raw")
			    		.field("type","keyword").endObject().endObject();
			    	}
			    	if(!StringUtils.isEmpty(col.getAnalyzer())){
			    		mapping.field("analyzer",col.getAnalyzer());
			    	}
			    	if(!StringUtils.isEmpty(col.getSearch_analyzer())){
			    		mapping.field("search_analyzer",col.getSearch_analyzer());
			    	}
		    	}else{
		    		mapping.startObject(cn).field("type",ctype);
		    	}
		    	mapping.endObject();
		    }
		    mapping.endObject().endObject().endObject();
		    PutMappingRequest map = Requests.putMappingRequest(indexName).type("_doc").source(mapping);
	        esClient.getClient().admin().indices().putMapping(map).actionGet();
	        esClient.getClient().admin().indices().prepareRefresh().get();
	    }catch(Exception e){
	    	log.error(e.toString());
	    }
		return mapping;
	}
	
	@SuppressWarnings("unchecked")
	private int excuteProcedure(DataSrc ds, Map paramVals,boolean needRemap)throws Exception{
		int cc = 0;
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
		cc=(Integer)jdbcTemplateDt.execute(proStmt.toString(),new CallableStatementCallback(){
			public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
				if(parasIn!=null&&parasIn.size()>0){
					for(int i=0;i<parasIn.size();i++){
						//过程参数引用方式分直接引用固定值和引用参数两种
						ProParaIn pi=(ProParaIn)parasIn.get(i);
						if(pi!=null&&pi.getReferMode()==0){
							if(pi.getDataType()==1){
								int ival=0;
								try{ival=Integer.parseInt(pi.getValue());}
								catch(Exception e){}
								cs.setInt(i+1, ival);
								log.info("参数(整型)"+pi.getReferTo()+":"+ival);
							}else if(pi.getDataType()==2){
								double dval=0;
								try{dval=Double.parseDouble(pi.getValue());}
								catch(Exception e){}
								cs.setDouble(i+1, dval);
								log.info("参数(小数)"+pi.getReferTo()+":"+dval);
							}else{
								cs.setString(i+1, pi.getValue());
								log.info("参数(字符串)"+pi.getReferTo()+":"+pi.getValue());
							}
						}else{
							if(paramVals==null){
								log.error("缺少参数值。参数："+pi.getReferTo());
							}
							//找出输入参数的定义
							String val=(String)paramVals.get(pi.getReferTo());
							if(val==null){
								log.error("缺少参数"+pi.getReferTo()+"的值！");
							}
							if(pi.getDataType()==1){
								int iVal=0;
								try{
									iVal=Integer.parseInt(val);
								}catch(Exception e){}
								cs.setInt(i+1, iVal);
								log.info("参数(整型)"+pi.getReferTo()+":"+iVal);
							}else if(pi.getDataType()==2){
								double dVal=0;
								try{
									dVal=Double.parseDouble(val);
								}catch(Exception e){}
								cs.setDouble(i+1, dVal);
								log.info("参数(小数)"+pi.getReferTo()+":"+dVal);
							}else{
								cs.setString(i+1,val);
								log.info("参数(字符串)"+pi.getReferTo()+":"+val);
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
                ResultSet rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex()); 
                if(rs==null){
                	return 0;
                }
                int count = 0;
                ResultSetMetaData rsmd=rs.getMetaData();
        		//获取元信息
        		int colNum=rsmd.getColumnCount();
        		if( needRemap){
        			List colsMeata = new ArrayList();
        			for (int i = 1; i <= colNum; i++) {  
        				Map<String,String> col = new HashMap<String,String>();
        				col.put("cName", rsmd.getColumnLabel(i).toLowerCase());
        				col.put("cType", getTypeName(rsmd.getColumnType(i)));
        				colsMeata.add(col);
        			}
        			buildMappings(ds.getId(),colsMeata);
        		}
        		
        		TransportClient client = esClient.getClient();
    	        BulkRequestBuilder bulkRequest = client.prepareBulk();
                while (rs.next()) {
                	Map row = new HashMap();
                	for(int i=1;i<=colNum;i++){
        				String sVal=rs.getString(i);
        				String colName = rsmd.getColumnLabel(i).toLowerCase();
        				row.put(colName, sVal);
        			}
                	bulkRequest.add(client.prepareIndex(ds.getId(),"_doc").setSource(row));
                	count++;
                }
                BulkResponse bulkResponse = bulkRequest.execute().actionGet();  
    	        if (bulkResponse.hasFailures()) {  
    	        	log.error("索引创建失败!"+bulkResponse.buildFailureMessage());  
    	        	throw new SQLException("索引创建失败!"+bulkResponse.buildFailureMessage());
    	        }
                return new Integer(count);
			}
		});
		return cc;
	}
	private int excuteSql(DataSrc ds, Map paramVals,boolean needRemap)throws Exception{
		int cc = 0;
		String sql = ds.getSql();
		if(StringUtils.isEmpty(sql)){
			throw new Exception("数据源定了sql取数方式，但未找到sql语句！");
		}
		sql = parseParamValue(sql,paramVals);
		if( needRemap){
			SqlRowSet rowSet = jdbcTemplateDt.queryForRowSet(sql);
			SqlRowSetMetaData metaData = rowSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			List colsMeata = new ArrayList();
			for (int i = 1; i <= columnCount; i++) {  
				Map<String,String> col = new HashMap<String,String>();
				col.put("cName", metaData.getColumnLabel(i).toLowerCase());
				col.put("cType", getTypeName(metaData.getColumnType(i)));
				colsMeata.add(col);
			}
			buildMappings(ds.getId(),colsMeata);
		}
		List lst = jdbcTemplateDt.queryForList(sql);
		if(lst!=null&&lst.size()>0){
			Map esCols = ds.getColMap();
			TransportClient client = esClient.getClient();
	        BulkRequestBuilder bulkRequest = client.prepareBulk(); 
	        for(int i=0;i<lst.size();i++){
	        	Map row = (Map)lst.get(i);
	        	Map nr = new HashMap();
	        	for (Iterator it = row.keySet().iterator(); it.hasNext();) {  
	        		String key = (String)it.next();  
	        		if(esCols.containsKey(key)){
	        			Column escol = (Column)esCols.get(key);
	        			if("integer".equals(escol.getFldType())){
	        				nr.put(key.toLowerCase(), (Integer)row.get(key));
	        			}else if("double".equals(escol.getFldType())){
	        				nr.put(key.toLowerCase(),((BigDecimal)row.get(key)).doubleValue());
	        			}else{
	        				nr.put(key.toLowerCase(), row.get(key)); 
	        			}
	        		}else{
	        			nr.put(key.toLowerCase(), row.get(key)); 
	        		}
		        } 
		        bulkRequest.add(client.prepareIndex(ds.getId(),"_doc").setSource(nr));
	        }
	        cc = lst.size();
	        BulkResponse bulkResponse = bulkRequest.execute().actionGet();  
	        if (bulkResponse.hasFailures()) {  
	        	log.error("索引创建失败!"+bulkResponse.buildFailureMessage());  
	        	throw new Exception("索引创建失败!"+bulkResponse.buildFailureMessage());
	        }
		}
		return cc;
	}
	private String parseParamValue(String exp,Map paramVals){
    	if(StringUtils.isEmpty(exp)){
    		return "";
    	}
    	String s = exp;
    	String[] paras=StringUtils.substringsBetween(exp, "{", "}");
    	if(paras!=null&&paras.length>0){
    		for(int i=0;i<paras.length;i++){
    			String p = paras[i];
    			if(paramVals!=null&&paramVals.containsKey(p)){
    				String val = (String)paramVals.get(p);
    				s=exp.replaceAll("\\{\\w*\\}", val);
    			}
    		}
    	}
    	return s;
    }
	private String getTypeName(int type) throws SQLException {
        switch (type){
	        case Types.BIGINT:
	            return "integer";
	        case Types.BIT:
	            return "integer";
	        case Types.DOUBLE:
	        	return "double";
	        default :
	        	return "text";
        }
	}
	
	public String[] deleteDtSrcs(String[] dtIds) {
		String[] results = new String[]{"1",""};
		StringBuffer bfInfos = new StringBuffer("");
		try{
			for(int i=0;i<dtIds.length;i++){
				String strDel = delelteIndex(dtIds[i].toLowerCase());
				try{
					JSONObject odel =JSON.parseObject(strDel);
					if(!odel.getBoolean("success")){
						results[0]="5";
						bfInfos.append("数据源"+dtIds[i]+"删除未成功;\n");
					}
				}catch(Exception e){}
			}
		}catch(Exception e){
			results[0]="9";
			results[1]=e.toString();
		}
		TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
		List allDts = ltmp.getDataSrcTemplates();
		results[1]=bfInfos.toString();
		return results; 
	}
	
	public String[] deleteAllDtSrcs() {
		String[] results = new String[]{"1",""};
		TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
		List allDts = ltmp.getDataSrcTemplates();
		StringBuffer bfInfos = new StringBuffer("");
		try{
			for(int i=0;i<allDts.size();i++){
				DataSrc ds= (DataSrc)allDts.get(i);
				String dtId = ds.getId().toLowerCase();
				String strDel = delelteIndex(dtId.toLowerCase());
				try{
					JSONObject odel =JSON.parseObject(strDel);
					if(!odel.getBoolean("success")){
						results[0]="5";
						bfInfos.append("数据源"+dtId+"删除未成功;\n");
					}
				}catch(Exception e){}
			}
		}catch(Exception e){
			results[0]="9";
			results[1]=e.toString();
		}
		results[1]=bfInfos.toString();
		return results; 
	}
	
	
	//获取构造出的mapping语句
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public String showMapping(String indexName,Map paramVals)throws Exception{
		String strMapping = "";
		DataSrc ds = TemplatesLoader.getTemplatesLoader().getDataSrc(indexName);
	    if(ds==null){
	    	throw new Exception("未找到ID为"+indexName+"的数据源定义信息！");
	    }
	    List colsMata = new ArrayList();
	    if(ds.getSourceType()==1){
	    	String sql = ds.getSql();
			if(StringUtils.isEmpty(sql)){
				throw new Exception("数据源定了sql取数方式，但未找到sql语句！");
			}
			sql = parseParamValue(sql,paramVals);
			SqlRowSet rowSet = jdbcTemplateDt.queryForRowSet(sql);
			SqlRowSetMetaData metaData = rowSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {  
				Map<String,String> col = new HashMap<String,String>();
				col.put("cName", metaData.getColumnLabel(i).toLowerCase());
				col.put("cType", getTypeName(metaData.getColumnType(i)));
				colsMata.add(col);
			}
		}else if(ds.getSourceType()==2){
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
			colsMata= (List)jdbcTemplateDt.execute(proStmt.toString(),new CallableStatementCallback(){
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					List cm = new ArrayList();
					if(parasIn!=null&&parasIn.size()>0){
						for(int i=0;i<parasIn.size();i++){
							//过程参数引用方式分直接引用固定值和引用参数两种
							ProParaIn pi=(ProParaIn)parasIn.get(i);
							if(pi!=null&&pi.getReferMode()==0){
								if(pi.getDataType()==1){
									int ival=0;
									try{ival=Integer.parseInt(pi.getValue());}
									catch(Exception e){}
									cs.setInt(i+1, ival);
									log.info("参数(整型)"+pi.getReferTo()+":"+ival);
								}else if(pi.getDataType()==2){
									double dval=0;
									try{dval=Double.parseDouble(pi.getValue());}
									catch(Exception e){}
									cs.setDouble(i+1, dval);
									log.info("参数(小数)"+pi.getReferTo()+":"+dval);
								}else{
									cs.setString(i+1, pi.getValue());
									log.info("参数(字符串)"+pi.getReferTo()+":"+pi.getValue());
								}
							}else{
								if(paramVals==null){
									log.error("缺少参数值。参数："+pi.getReferTo());
								}
								//找出输入参数的定义
								String val=(String)paramVals.get(pi.getReferTo());
								if(val==null){
									log.error("缺少参数"+pi.getReferTo()+"的值！");
								}
								if(pi.getDataType()==1){
									int iVal=0;
									try{
										iVal=Integer.parseInt(val);
									}catch(Exception e){}
									cs.setInt(i+1, iVal);
									log.info("参数(整型)"+pi.getReferTo()+":"+iVal);
								}else if(pi.getDataType()==2){
									double dVal=0;
									try{
										dVal=Double.parseDouble(val);
									}catch(Exception e){}
									cs.setDouble(i+1, dVal);
									log.info("参数(小数)"+pi.getReferTo()+":"+dVal);
								}else{
									cs.setString(i+1,val);
									log.info("参数(字符串)"+pi.getReferTo()+":"+val);
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
	                ResultSet rs = (ResultSet)cs.getObject(oStart-1+pro.getDataSetIndex()); 
	                if(rs==null){
	                	return null;
	                }
	                ResultSetMetaData rsmd=rs.getMetaData();
	        		//获取元信息
	        		int colNum=rsmd.getColumnCount();
        			for (int i = 1; i <= colNum; i++) {  
        				Map<String,String> col = new HashMap<String,String>();
        				col.put("cName", rsmd.getColumnLabel(i).toLowerCase());
        				col.put("cType", getTypeName(rsmd.getColumnType(i)));
        				cm.add(col);
        			}
        			return cm;
				}
			});
		}
	    //根据列的metaData构造mapping语句
	    XContentBuilder mapping = null;
	    try{
		    mapping = XContentFactory.jsonBuilder().startObject()  
	                .startObject("_doc").startObject("properties");
		    Map cols = ds.getColMap();
		    for(int i = 0;i<colsMata.size();i++){
		    	Map oneCol = (Map)colsMata.get(i);
		    	String cn = (String)oneCol.get("cName");
		    	String ctype = (String)oneCol.get("cType");
		    	//如果在xml中特别定义了col，按col定义，否则，默认按字段属性构造mapping
		    	if(cols.containsKey(cn)){
		    		Column col = (Column)cols.get(cn);
			    	String fldtype = StringUtils.isEmpty(col.getFldType())?"text":col.getFldType();
			    	mapping.startObject(col.getName()).field("type",fldtype);
			    	if(col.getIsFilter()==1||col.getCanOrder()==1){
			    		mapping.startObject("fields").startObject("raw")
			    		.field("type","keyword").endObject().endObject();
			    	}
			    	if(!StringUtils.isEmpty(col.getAnalyzer())){
			    		mapping.field("analyzer",col.getAnalyzer());
			    	}
			    	if(!StringUtils.isEmpty(col.getSearch_analyzer())){
			    		mapping.field("search_analyzer",col.getSearch_analyzer());
			    	}
		    	}else{
		    		mapping.startObject(cn).field("type",ctype);
		    	}
		    	mapping.endObject();
		    }
		    mapping.endObject().endObject().endObject();
		    PutMappingRequest map = Requests.putMappingRequest(indexName).type("_doc").source(mapping);
		    strMapping = map.source();
	    }catch(Exception e){
	    	log.error(e.toString());
	    }
		return strMapping;
	}
}
