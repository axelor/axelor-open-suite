/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.gdpr.job;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.gdpr.db.GDPRProcessingRegister;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRepository;
import com.axelor.apps.gdpr.service.GdprProcessingRegisterService;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingRegisterJob implements Job {

  private final Logger log = LoggerFactory.getLogger(ProcessingRegisterJob.class);

  @Inject GdprProcessingRegisterService processingRegisterService;
  @Inject GDPRProcessingRegisterRepository processingRegisterRepository;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.trace("Begin processing register job");
    List<GDPRProcessingRegister> activeProcessingRegister =
        processingRegisterRepository
            .findByStatus(GDPRProcessingRegisterRepository.PROCESSING_REGISTER_STATUS_ACTIVE)
            .fetch();

    for (GDPRProcessingRegister processingRegister : activeProcessingRegister) {
      processingRegister = processingRegisterRepository.find(processingRegister.getId());
      try {
        processingRegisterService.launchProcessingRegister(processingRegister);
      } catch (ClassNotFoundException | AxelorException | IOException e) {
        TraceBackService.trace(e);
      }
    }
  }
}
