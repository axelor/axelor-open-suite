package com.axelor.apps.base.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;

public class MailJob implements Job{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		try{
			Beans.get(MailBatchService.class).run(MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
		}
		catch(AxelorException e){
			TraceBackService.trace(new Exception(e));
		}
	}
}
