package com.axelor.apps.supplychain.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.batch.SupplychainBatchService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;

public class BillSubJob implements Job{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		try{
			Beans.get(SupplychainBatchService.class).run(SupplychainBatchRepository.CODE_BATCH_BILL_SUB);
		}catch(Exception e){
			TraceBackService.trace(new Exception(e));
		}
	}
}
