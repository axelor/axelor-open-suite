package com.axelor.apps.payroll.jobs;

import com.axelor.apps.payroll.db.Payroll;
import com.axelor.apps.payroll.db.repo.PayrollRepository;
import com.axelor.apps.payroll.service.PayrollServiceImplementation;
import com.axelor.inject.Beans;
import com.axelor.mail.MailException;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import org.eclipse.birt.core.exception.BirtException;
import org.quartz.*;

public class PayrollProcessingJob implements Job {

  @Inject private PayrollServiceImplementation payrollServiceImplementation;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    List<Payroll> payrolls = Beans.get(PayrollRepository.class).all().fetch();

    for (Payroll payroll : payrolls) {
      if (payroll.getEnabled() == Boolean.TRUE) {
        try {
          switch (payroll.getProcessingFrequency()) {
            case HOURLY:
              payrollServiceImplementation.processHourly(payroll);
              break;
            case DAILY:
              payrollServiceImplementation.processDaily(payroll);
              break;
            case WEEKLY:
              payrollServiceImplementation.processWeekly(payroll);
              break;
            case MONTHLY:
              payrollServiceImplementation.processMonthly(payroll);
              break;
            case QUARTERLY:
              payrollServiceImplementation.processQuarterly(payroll);
              break;
            case YEARLY:
              payrollServiceImplementation.processYearly(payroll);
              break;
          }
        } catch (BirtException | IOException | MailException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
