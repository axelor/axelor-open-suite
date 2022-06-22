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
package com.axelor.apps.supplychain.job;

import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MrpJob implements Job {

  @Inject MetaScheduleRepository metaScheduleRepo;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      JobDetail jobDetail = context.getJobDetail();
      MetaSchedule metaSchedule = metaScheduleRepo.findByName(jobDetail.getKey().getName());

      if (!metaSchedule.getParams().isEmpty()) {
        String code =
            metaSchedule
                .getParams()
                .stream()
                .filter(param -> param.getName().equals("code"))
                .findFirst()
                .get()
                .getValue();

        Mrp mrp = Beans.get(MrpRepository.class).findByCode(code);
        if (mrp != null) {
          Beans.get(MrpService.class).runCalculation(mrp);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(new Exception(e));
    }
  }
}
