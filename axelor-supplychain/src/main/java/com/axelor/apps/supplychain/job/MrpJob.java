package com.axelor.apps.supplychain.job;

import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** Class implementing Job to be called by the quartz scheduler. */
public class MrpJob implements Job {

  /**
   * Called from the scheduler, will call {@link MrpService#runCalculation(Mrp)} with the MRP
   * configured with the scheduler.
   *
   * @param context context given by quartz scheduler, containing the configuration.
   * @throws JobExecutionException if any exception happens during the process.
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      JobDetail jobDetail = context.getJobDetail();
      MetaSchedule metaSchedule =
          Beans.get(MetaScheduleRepository.class).findByName(jobDetail.getKey().getName());
      String mrpSeq = metaSchedule.getMrpSeq();
      if (mrpSeq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.MRP_SCHEDULER_SEQ_MISSING));
      }
      Mrp mrp = Beans.get(MrpRepository.class).findByMrpSeq(mrpSeq);
      if (mrp == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.MRP_NOT_FOUND_WITH_SEQ),
            mrpSeq);
      }
      if (mrp.getStatusSelect() == MrpRepository.STATUS_CALCULATION_STARTED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.MRP_CANNOT_PROCESS_ONGOING),
            mrp.getFullName());
      }
      runCalculationWithExceptionManagement(mrp);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new JobExecutionException(e);
    }
  }

  /*
   * Might have to move this method to a more appropriate service.
   */
  protected void runCalculationWithExceptionManagement(Mrp mrp) throws AxelorException {
    MrpService mrpService = Beans.get(MrpService.class);
    try {
      mrpService.runCalculation(mrp);
    } catch (Exception e) {
      mrpService.reset(mrp);
      mrpService.saveErrorInMrp(mrp, e);
      throw e;
    }
  }
}
