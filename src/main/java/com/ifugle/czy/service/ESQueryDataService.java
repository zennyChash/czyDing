package com.ifugle.czy.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.TemplatesLoader;
import com.ifugle.czy.utils.bean.RptDataJson;
import com.ifugle.czy.utils.bean.template.FilterField;
import com.ifugle.czy.utils.bean.template.JPage;
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
	
	public Map searchByKeyWord(String index,RptDataJson params){
		Map info = null;
		JSONObject jparams = params==null?null:params.parseJRptParams();
		String str="",fld = "",fldsToGet="";
		int from =0,size=10;
		str = jparams.getString("searchKey");
		from = jparams.getIntValue("start");
		size = jparams.getIntValue("limit");
		fld = jparams.getString("field")==null?"mc":jparams.getString("field");
		fldsToGet = jparams.getString("fldsToGet")==null?"swdjzh,mc":jparams.getString("fldsToGet");
		Map result = new HashMap();
		List<Map> qymcs = new ArrayList<Map>();
		SearchResponse searchResponse = esClient.getClient().prepareSearch(index)
			.setTypes("_doc")
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(QueryBuilders.matchQuery(fld,str))
			.setFrom(from).setSize(size)
			.get();
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
		JPage jp = TemplatesLoader.getTemplatesLoader().getJPage(jpID);
		if(jp==null){
			result.put("done", false);
			result.put("info", "未找到页面数据的定义信息。");
			log.equals("未找到JPage信息，ID："+jpID);
			return result;
		}
		Properties p=new Properties(); 
		p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"); 
		Velocity.init(p);  
        VelocityContext context = new VelocityContext(); 
        //先把外部请求参数也放到velocity中
        for (Map.Entry entry : params.entrySet()) {  
		   String key = (String)entry.getKey();  
		   context.put(key, entry.getValue());
		}  
		String tmp=jp.getjTemplate();
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
				BoolQueryBuilder qb = QueryBuilders.boolQuery();
				if(flts!=null){
					for(int j=0;j<flts.size();j++){
						FilterField flt = (FilterField)flts.get(j);
						String ftype = flt.getFtype();
						String fname = flt.getName();
						String refParam = flt.getRefParam();
						String fv = flt.getValue();
						if(params.containsKey(refParam)){
							fv = params.getString(refParam);
						}
						int dtype = flt.getDataType();
						if("terms".equalsIgnoreCase(ftype)){
							qb.filter(QueryBuilders.termsQuery(fname, fv));
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
							String sfrom = sranges[0],sto = sranges[1];
							if(dtype==1){
								qb.filter(QueryBuilders.rangeQuery(fname).from(Integer.parseInt(sfrom),includeLower)
										.to(Integer.parseInt(sfrom), includeUpper));
							}else if(dtype==2){
								qb.filter(QueryBuilders.rangeQuery(fname).from(Double.parseDouble(sfrom),includeLower)
										.to(Double.parseDouble(sfrom), includeUpper));
							}else{
								qb.filter(QueryBuilders.rangeQuery(fname).from(sfrom, includeLower).to(sto, includeUpper));
							}
						}else{
							qb.filter(QueryBuilders.termQuery(fname, fv));
						}
					}
				}
				if(orders!=null){
					for(int k=0;k<orders.size();k++){
						OrderField oflt = (OrderField)orders.get(k);
						SortBuilder sortBuilder = SortBuilders.fieldSort(oflt.getName()+".raw");
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
					
					sReq.setFrom(from).setSize(size);
				}else{
					String ms =Configuration.getConfig().getString("maxQuerySize", "20");
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
        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, jp.getId(), tmp);
        System.out.println(" string : " + sw);
        String otmp = sw.toString();
        JSONObject jtmp = JSONObject.parseObject(otmp);
        result.put("done", true);
		result.put("jpData", jtmp);
		return result;
	}
}
