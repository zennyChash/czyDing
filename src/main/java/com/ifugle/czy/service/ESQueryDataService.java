package com.ifugle.czy.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.VelocityException;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.JResponse;
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.czy.utils.bean.template.FilterField;
import com.ifugle.czy.utils.bean.template.JOutput;
import com.ifugle.czy.utils.bean.template.OrderField;
import com.ifugle.czy.utils.bean.template.ValuedDs;
import com.ifugle.utils.Configuration;

public class ESQueryDataService {
	private static Logger log = Logger.getLogger(ESQueryDataService.class);
	protected ESClientFactory esClient;
	@Autowired
	public void setEsService(ESClientFactory esClient){
		this.esClient = esClient;
	}
	
	public Map searchByKeyWord(String rptID,RptDataJson params){
		Map info = null;
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
		TransportClient client = esClient.getClient();
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
		   if(ov.getClass()==Integer.class){
			   context.put(key, (Integer)entry.getValue());
		   }else if(ov.getClass()==Double.class){
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
		List vds = jp.getValuedDs();
		if(vds!=null){
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
							context.put(refParam, fv);
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
					String ms =Configuration.getConfig().getString("maxQuerySize", "200");
					try{
						sReq.setFrom(0).setSize(Integer.parseInt(ms));
					}catch(Exception e){
						sReq.setFrom(0).setSize(20);
					}
				}
				
				SearchResponse response = sReq.get();
				SearchHits hits = response.getHits();
			    long total = hits.getTotalHits();
			    Map qinfos = new HashMap();
			    qinfos.put("total", total);
			    List rows = new ArrayList();
			    for(SearchHit hit : hits) {
			    	Map hitsrc = hit.getSourceAsMap();
			    	rows.add(hitsrc);
			    }
			    qinfos.put("rows", rows);
			    context.put(dsName, qinfos);
			}
		}
		try{
	        StringWriter sw = new StringWriter();
	        String tmp=jp.getjTemplate();
	        Velocity.evaluate(context, sw, jp.getId(), tmp);
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
					String ms =Configuration.getConfig().getString("maxQuerySize", "200");
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
	
}
