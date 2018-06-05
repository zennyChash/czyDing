package com.ifugle.czy.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.bean.RptDataJson;

@Transactional
public class ReportDataEsService {
	private static Logger log = Logger.getLogger(ReportDataEsService.class);
	protected ESClientFactory esClient;
	@Autowired
	public void setEsService(ESClientFactory esClient){
		this.esClient = esClient;
	}
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public Map getData(String rptID,RptDataJson params){
		JSONObject jrpt = null;
		TransportClient client = esClient.getClient();
		GetResponse response = client.prepareGet("rptData","dzjk","201805").execute()    
                .actionGet();    
        String json = response.getSourceAsString();    
        if (null != json) {    
        	jrpt = JSONObject.parseObject(json); 
        } else {    
            System.out.println("未查询到任何结果！");    
        }    
		return jrpt;
	}
	
	public String addIndexAndDocumentEn(String index,String type,RptDataJson params){ 
		StringBuffer info= new StringBuffer("{success:");
		JSONObject jparams = params==null?null:params.parseJRptParams();
		String sugType = jparams.getString("sugType");
		String indexName = index+("completion".equals(sugType)?"_cs":"");
		TransportClient client = esClient.getClient();
		IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);  
        IndicesExistsResponse inExistsResponse = client.admin().indices()  
                .exists(inExistsRequest).actionGet();  
        if(inExistsResponse.isExists()){//存在，先删除
	    	DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(indexName)  
	                .execute().actionGet();  
	    }
        CreateIndexRequestBuilder cib=client.admin().indices().prepareCreate(indexName);
        CreateIndexResponse res=cib.execute().actionGet(); 
        if(res.isAcknowledged()){
	    	createIKMapping(indexName,type,sugType);
	    	List lst = jdbcTemplate.queryForList("select swdjzh,mc from dj_cz");
			if(lst!=null){
		        BulkRequestBuilder bulkRequest = client.prepareBulk(); 
		        for(int i=0;i<lst.size();i++){
		        	Map row = (Map)lst.get(i);
		        	Map doc = new HashMap();
		        	String mc = (String)row.get("mc");
		        	doc.put("swdjzh", (String)row.get("swdjzh"));
		        	doc.put("mc", (String)row.get("mc"));
		        	if("completion".equals(sugType)){
		        	Map suggest = new HashMap();
			        	suggest.put("input",new String[]{StringUtils.isEmpty(mc)?"未知":mc});
			        	suggest.put("weight",10);
			        	doc.put("mc_suggest",suggest);
			        }
			        bulkRequest.add(client.prepareIndex(indexName, type).setSource(doc));
		        }
		        BulkResponse bulkResponse = bulkRequest.execute().actionGet();  
		        if (bulkResponse.hasFailures()) {  
		        	info.append("false}");
		        	log.error("索引创建失败!"+bulkResponse.buildFailureMessage());  
		        }else{
		        	info.append("true}");
		        	log.info("索引创建成功!"+indexName+":"+type);  
		        }
			}else{
				info.append("true}");
			}
        }
		return info.toString();
    } 
	
	public String deleteIndex(String indexName){
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
	public Map searchForWord(String index, String type,RptDataJson params){
		Map info = null;
		JSONObject jparams = params==null?null:params.parseJRptParams();
		String sugType = jparams.getString("sugType");
		String indexName = index+("completion".equals(sugType)?"_cs":"");
		if("completion".equals(sugType)){
			info=searchForCompletionSuggest(indexName,type,params);
		}else{
			info=searchByNormalQuery(indexName,type,params);
		}
		return info;
	}
	public Map searchByNormalQuery(String index, String type,RptDataJson params) { 
		String str="";
		int from =0,size=10;
		JSONObject jparams = params==null?null:params.parseJRptParams();
		str = jparams.getString("searchKey");
		from = jparams.getIntValue("start");
		size = jparams.getIntValue("limit");
		Map result = new HashMap();
		List<Map> qymcs = new ArrayList<Map>();
		SearchResponse searchResponse = esClient.getClient().prepareSearch(index)
			.setTypes(type)
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(QueryBuilders.matchQuery("mc",str))
			.setFrom(from).setSize(size)
			.get();
	    SearchHits hits = searchResponse.getHits();
	    long total = hits.getTotalHits();
	    result.put("total", total);
	    for(SearchHit hit : hits) {
	    	Map hitsrc = hit.getSourceAsMap();
	    	Map en = new HashMap();
	    	en.put("swdjzh", (String)hitsrc.get("swdjzh"));
	    	en.put("mc", (String)hitsrc.get("mc"));
            qymcs.add(en);
	    }
	    result.put("matches", qymcs);
        return result;
	}
	
	public Map searchForCompletionSuggest(String index, String type,RptDataJson params) { 
		String str="";
		int from =0,size=10;
		JSONObject jparams = params==null?null:params.parseJRptParams();
		str = jparams.getString("searchKey");
		from = jparams.getIntValue("from");
		size = jparams.getIntValue("size");
		Map result = new HashMap();
		List<String> qymcs = new ArrayList<String>();
		CompletionSuggestionBuilder sgBuilder   = new CompletionSuggestionBuilder("mc_suggest");
		sgBuilder.analyzer("ik_max_word");
		sgBuilder.text(str).size(size);

        SearchResponse searchResponse = esClient.getClient().prepareSearch(index)
                .setTypes(type)
                //.setQuery(QueryBuilders.matchAllQuery())
                .suggest(new SuggestBuilder().addSuggestion("mc_sg",sgBuilder))
                .execute().actionGet();
        CompletionSuggestion compSuggestion = searchResponse.getSuggest().getSuggestion("mc_sg");
        List<CompletionSuggestion.Entry> list = compSuggestion.getEntries();
        if(list != null) {
        	for (int i = 0; i < list.size(); i++) {
                List<CompletionSuggestion.Entry.Option> options = list.get(i).getOptions();
                for (int j = 0; j < options.size(); j++) {
                	result.put("total", options.size());
                    if (options.get(j) instanceof CompletionSuggestion.Entry.Option) {
                        CompletionSuggestion.Entry.Option op =  options.get(j);
                        System.out.println(op.getScore()+"--"+op.getText());
                        qymcs.add(op.getText().string());
                    }
                }
            }
        	result.put("matches", qymcs);
        }
        return result;
	}
	
	private  XContentBuilder createIKMapping(String indexName,String typeName,String sugType) {  
        XContentBuilder mapping = null;  
        try {  
        	if("completion".equals(sugType)){
        		mapping = XContentFactory.jsonBuilder().startObject()  
                    .startObject(typeName).startObject("properties")  
                    .startObject("swdjzh").field("type", "keyword").endObject()  
                    .startObject("mc").field("type", "text").endObject() 
                    .startObject("mc_suggest").field("type","completion")
                    .field("analyzer","ik_max_word").field("search_analyzer","ik_max_word").field("preserve_separators",false)
                    .field("preserve_position_increments",false).endObject() 
                    .endObject().endObject().endObject();
        	}else{
        		mapping = XContentFactory.jsonBuilder().startObject()  
                        .startObject(typeName).startObject("properties")  
                        .startObject("swdjzh").field("type", "keyword").endObject()  
                        .startObject("mc").field("type", "text")
                        .field("analyzer","ik_max_word").field("search_analyzer","ik_smart").endObject() 
                        .endObject().endObject().endObject();
        	}
            PutMappingRequest map = Requests.putMappingRequest(indexName).type(typeName).source(mapping);
            esClient.getClient().admin().indices().putMapping(map).actionGet();
            esClient.getClient().admin().indices().prepareRefresh().get();
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return mapping;  
    }  
}
