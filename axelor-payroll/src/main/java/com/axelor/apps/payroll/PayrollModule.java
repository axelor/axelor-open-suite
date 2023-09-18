package com.axelor.apps.payroll;

import com.axelor.app.AxelorModule;
import com.axelor.apps.payroll.service.PayrollService;
import com.axelor.apps.payroll.service.PayrollServiceImplementation;

public class PayrollModule extends AxelorModule {

  @Override
  public void configure() {
    bind(PayrollService.class).to(PayrollServiceImplementation.class);
  }
}
