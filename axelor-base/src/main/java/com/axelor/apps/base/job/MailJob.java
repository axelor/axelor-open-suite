package com.axelor.apps.base.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.batch.BatchReminderMail;
import com.axelor.inject.Beans;

public class MailJob implements Job{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		Beans.get(BatchReminderMail.class).run(Beans.get(MailBatchRepository.class).findByCode(MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET));
	}
}
