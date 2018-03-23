/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;

public class MailJob implements Job{
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		try{
			Beans.get(MailBatchService.class).run(MailBatchRepository.CODE_BATCH_EMAIL_ALL_TIME_SHEET);
		}
		catch(Exception e){
			TraceBackService.trace(new Exception(e));
		}
	}
}
