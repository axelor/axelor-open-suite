package com.axelor.apps.account.module;

import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.script.ScriptPolicyConfigurator;
import java.util.List;

public class AccountCustomPolicy implements ScriptPolicyConfigurator {
  @Override
  public void configure(
      List<String> allowPackages,
      List<Class<?>> allowClasses,
      List<String> denyPackages,
      List<Class<?>> denyClasses) {
    allowClasses.add(InvoiceTermService.class);
  }
}
