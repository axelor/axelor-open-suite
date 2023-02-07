package com.axelor.apps.gdpr.job;

import com.axelor.apps.gdpr.db.GDPRProcessingRegister;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRepository;
import com.axelor.apps.gdpr.service.GDPRProcessingRegisterService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingRegisterJob implements Job {

  private final Logger log = LoggerFactory.getLogger(ProcessingRegisterJob.class);

  @Inject GDPRProcessingRegisterService processingRegisterService;
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
      } catch (ClassNotFoundException | AxelorException e) {
        TraceBackService.trace(e);
      }
    }
  }
}
