package com.ifugle.czy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.ifugle.czy.utils.bean.RptDataJson;

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
		String str="",fld = "";
		int from =0,size=10;
		str = jparams.getString("searchKey");
		from = jparams.getIntValue("start");
		size = jparams.getIntValue("limit");
		fld = jparams.getString("field")==null?"mc":jparams.getString("field");
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
	
	public Map getData(String rptID,RptDataJson params){
		JSONObject jrpt = null;
		TransportClient client = esClient.getClient();
		GetResponse response = client.prepareGet("INDEXNAME","_doc","201805").execute()    
                .actionGet();    
        String json = response.getSourceAsString();    
        if (null != json) {    
        	jrpt = JSONObject.parseObject(json); 
        } else {    
            System.out.println("未查询到任何结果！");    
        }    
		return jrpt;
	}
}
