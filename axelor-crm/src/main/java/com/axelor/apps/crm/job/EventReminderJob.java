package com.axelor.apps.crm.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.axelor.apps.crm.db.repo.CrmBatchRepository;
import com.axelor.apps.crm.service.batch.CrmBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;

public class EventReminderJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		try{
			Beans.get(CrmBatchService.class).run(CrmBatchRepository.CODE_BATCH_EVENT_REMINDER);
		}
		catch(AxelorException e){
			TraceBackService.trace(new Exception(e));
		}
	}

}
