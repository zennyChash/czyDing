package com.ifugle.etl.schedule.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.ifugle.etl.entity.task.Execute;
import com.ifugle.etl.entity.task.Extract;
import com.ifugle.etl.entity.task.FtpTransfer;
import com.ifugle.etl.entity.task.Import;
import com.ifugle.etl.entity.task.RequestTask;
import com.ifugle.etl.entity.task.SendMail;
import com.ifugle.etl.entity.task.Unzip;
import com.ifugle.etl.entity.task.Zip;
import com.ifugle.etl.entity.ScheduledJob;
import com.ifugle.etl.entity.ScheduledTask;
import com.ifugle.etl.entity.SchedulerInfo;
import com.ifugle.etl.entity.base.Task;
import com.ifugle.etl.entity.component.Parameter;
import com.ifugle.etl.schedule.SchedulerUtils;
import com.ifugle.etl.task.execute.service.DBExecuteService;
import com.ifugle.etl.task.execute.service.IExecute;
import com.ifugle.etl.task.extract.service.DBExtractService;
import com.ifugle.etl.task.extract.service.IExtract;
import com.ifugle.etl.task.ftp.service.FtpService;
import com.ifugle.etl.task.imp.service.DBImportService;
import com.ifugle.etl.task.imp.service.IImport;
import com.ifugle.etl.task.mail.service.MailService;
import com.ifugle.etl.task.request.service.RequestService;
import com.ifugle.etl.task.zip.service.ZipService;
import com.ifugle.etl.utils.TemplateLoader;

public class CollectionJob implements Job {
	private static Logger log = LoggerFactory.getLogger(CollectionJob.class);
	public CollectionJob() {
	}
	
	public void execute(JobExecutionContext context)throws JobExecutionException {
		JobKey jobName = context.getJobDetail().getKey();
		SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info("执行任务集: {}。 开始时间：{} ",jobName, sdf.format(new Date()));
		String jobId = null;
		Map jobsMap = null;
		ScheduledJob sjob = null;
		ScheduledTask firstScheduledTask = null;
		String sdId = context.getJobDetail().getJobDataMap().getString("schedulerId");
		SchedulerInfo sdinfo = (SchedulerInfo) TemplateLoader.getLoader().getETLObjectTemplate(2, sdId);
		if (sdinfo != null) {
			jobId = context.getJobDetail().getJobDataMap().getString("jobId");
			jobsMap = sdinfo.getJobsMap();
			sjob = jobsMap == null ? null : (ScheduledJob) jobsMap.get(jobId);
			if(sjob==null||sjob.getDisabled()==1){
				log.info("未定义任务集" + jobId );
				return;
			}
			if(sjob.getDisabled()==1){
    			log.info("调度工作单元"+sjob.getJobId()+"为禁用状态，不被调度执行。");
    			return;
    		}
			firstScheduledTask = sjob.getHeadTask();
		}
		if (firstScheduledTask == null) {
			log.error("未定义任务集" + jobName + "的子任务！");
			return;
		}
		String tid = firstScheduledTask.getTaskId();
		Task task = (Task)TemplateLoader.getLoader().getETLObjectTemplate(0,tid);
		if(task==null){
			log.error("未找到指定的任务{}",tid);
			return;
		}
		try {
			//解析参数，参数名值对值放入paramsVals中。
			Map mparams = sjob.getParamMap();
			List params = sjob.getParameters();
			Map paramVals = new HashMap();
			if(params!=null){
				for(int i=0;i<params.size();i++){
					Parameter pa = (Parameter)params.get(i);
					String v = SchedulerUtils.parseParam(pa);
					paramVals.put(pa.getName(), v);
				}
			}
			excuteTasks(task,mparams,paramVals,firstScheduledTask,0);
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	public void excuteTasks(Task task,Map params,Map paramVals,ScheduledTask schTask,int forceExe) {
		//非强制执行时，如果是禁用的，不执行。强制执行（手工触发）时，无论是否禁用，都执行。
		if(forceExe==0&&schTask.getDisabled()==1){
			log.info("调度任务"+task.getId()+"为禁用状态，不被调度执行。");
			return;
		}
		ScheduledTask schTaskOnS = schTask.getTaskOnSuccess();
		ScheduledTask schTaskOnF = schTask.getTaskOnFail();
		if (task instanceof Extract) {
			Extract et = (Extract) task;
			IExtract expSvc = new DBExtractService();
			int flag = expSvc.extractToFile(et,params,paramVals);
			//如果执行失败，重试指定次数，只要其中有一次成功就认为成功。
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = expSvc.extractToFile(et,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		} else if (task instanceof Import) {
			Import imp = (Import) task;
			IImport ipSvc = new DBImportService();
			int flag =ipSvc.importData(imp,params,paramVals);
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = ipSvc.importData(imp,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		} else if (task instanceof Execute) {
			Execute exe = (Execute) task;
			IExecute exeSvc = new DBExecuteService();
			int flag =exeSvc.doExecute(exe,params,paramVals);
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = exeSvc.doExecute(exe,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		}else if(task instanceof FtpTransfer){
			FtpTransfer ft = (FtpTransfer) task;
			FtpService exeSvc = new FtpService();
			int flag = 0;
			if(ft!=null&&ft.getIsUpload()==0){
				flag = exeSvc.download(ft,params,paramVals);
			}else{
				flag = exeSvc.upload(ft,params,paramVals);
			}
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = 0;
						if(ft!=null&&ft.getIsUpload()==0){
							reFlag = exeSvc.download(ft,params,paramVals);
						}else{
							reFlag = exeSvc.upload(ft,params,paramVals);
						}
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		}else if(task instanceof Zip){
			Zip zip = (Zip) task;
			ZipService zSvc = new ZipService();
			int flag = zSvc.zip(zip, params, paramVals);
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = reFlag = zSvc.zip(zip,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		}else if(task instanceof Unzip){
			Unzip unzip = (Unzip) task;
			ZipService zSvc = new ZipService();
			int flag = zSvc.unzip(unzip, params, paramVals);
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = reFlag = zSvc.unzip(unzip,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		}else if(task instanceof SendMail){
			SendMail mail = (SendMail) task;
			MailService mSvc = new MailService();
			int flag = mSvc.sendMail(mail, params, paramVals);
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = reFlag = mSvc.sendMail(mail,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		}else if(task instanceof RequestTask){
			RequestTask rt = (RequestTask) task;
			RequestService rSvc = new RequestService();
			int flag = rSvc.doRequest(rt, params, paramVals);
			if(flag!=1){
				if(schTask.getOnFailRetry()>0){
					int fc = 0;
					boolean isOk = false;
					while(fc<schTask.getOnFailRetry()){
						int reFlag = reFlag = rSvc.doRequest(rt,params,paramVals);
						fc++;
						if(reFlag == 1){
							isOk = true;
							break;
						}
					}
					if(isOk){
						log.info("重试{}次后，任务{}执行成功。",fc,task.getId());
						onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
					}else{
						log.info("重试{}次后，任务{}执行仍旧失败。",fc,task.getId());
						onFailDo(task, params, paramVals, schTaskOnF,forceExe);
					}
				}else{
					log.info("任务{}执行失败。",task.getId());
					onFailDo(task, params, paramVals, schTaskOnF,forceExe);
				}
			}else{
				log.info("任务{}执行成功。",task.getId());
				onSuccessDo(task, params, paramVals,schTaskOnS,forceExe);
			}
		}
	}
	
	private void onSuccessDo(Task preTask,Map params,Map paramVals,ScheduledTask schTaskOnS,int forceExe){
		if(schTaskOnS!=null){
			String tid = schTaskOnS.getTaskId();
			Task stask = (Task)TemplateLoader.getLoader().getETLObjectTemplate(0,tid);
			log.info("开始执行后续任务：{}",tid);
			excuteTasks(stask, params, paramVals, schTaskOnS,forceExe);
		}
	}
	
	private void onFailDo(Task preTask,Map params,Map paramVals,ScheduledTask schTaskOnF,int forceExe){
		if(schTaskOnF!=null){
			String tid = schTaskOnF.getTaskId();
			Task ftask = (Task)TemplateLoader.getLoader().getETLObjectTemplate(0,tid);
			log.info("开始执行失败后续任务：{}",tid);
			excuteTasks(ftask, params, paramVals, schTaskOnF,forceExe);
		}
	}
}
