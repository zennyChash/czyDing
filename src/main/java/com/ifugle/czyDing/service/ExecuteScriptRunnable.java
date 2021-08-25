package com.ifugle.czyDing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifugle.czyDing.utils.TemplatesLoader;
import com.ifugle.czyDing.utils.bean.template.DataSrc;
import com.ifugle.utils.Configuration;
import com.ifugle.utils.ContextUtil;

public class ExecuteScriptRunnable implements Runnable{
	private String tid ;
	private String[] dtIds;
	private boolean reIndex;
	private boolean deleteOldData;
	private Configuration cg ;
	private ESDataSourceService esDtSrcService;
	public ExecuteScriptRunnable(String tid,String[] ids,boolean reindex,boolean deleteOld){
		this.tid = tid;
		this.dtIds = ids;
		this.reIndex = reindex;
		this.deleteOldData = deleteOld;
		cg = (Configuration)ContextUtil.getBean("config");
		esDtSrcService = (ESDataSourceService)ContextUtil.getBean("esDtSrcService");
	}
    @Override
    public synchronized void run() {
    	//多线程处理具体任务循环。此处构造完多线程任务就返回。
		for(int i=0;i<dtIds.length;i++){
			String indexName = dtIds[i];
			//2019-04-23虽然前台列表数据已增加控制，只显示ETL迁移的源，这里还是再加一道控制
			TemplatesLoader ltmp=TemplatesLoader.getTemplatesLoader();
			DataSrc dt = ltmp.getDataSrc(indexName);
			if(dt.getUseType()==1){
				continue;
			}
			String info = esDtSrcService.indexData(indexName, reIndex, deleteOldData, null);
			JSONObject ji = JSON.parseObject(info);
			if(ji.getBoolean("success")){
				int cc = ji.getIntValue("indexDocs");
				StringBuffer str = new StringBuffer("数据脚本执行成功，id:");
				str.append(indexName).append("，共生成").append(cc).append("条记录！");
				cg.addLogItem(tid, str.toString());
				cg.addSuccessScript(tid);
			}else{
				String str = ji.getString("info");
				cg.addLogItem(tid, str);
				cg.addFailedScript(tid);
			}
		}
		cg.setTaskStatus(tid, 1);
    }
}
