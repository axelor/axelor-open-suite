package com.axelor.apps.budget.module;

import com.axelor.app.AppModule;
import com.axelor.apps.account.module.AccountModule;
import com.axelor.apps.bankpayment.module.BankPaymentModule;
import com.axelor.apps.base.module.BaseModule;
import com.axelor.apps.businessproject.module.BusinessProjectModule;
import com.axelor.apps.contract.module.ContractModule;
import com.axelor.apps.crm.module.CrmModule;
import com.axelor.apps.hr.module.HumanResourceModule;
import com.axelor.apps.purchase.module.PurchaseModule;
import com.axelor.apps.sale.module.SaleModule;
import com.axelor.apps.supplychain.module.SupplychainModule;
import com.axelor.auth.AuthModule;
import com.axelor.db.JpaModule;
import com.google.inject.AbstractModule;

public class BudgetTestModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new JpaModule("testUnit", true, true));
    install(new BaseModule());
    install(new AccountModule());
    install(new BankPaymentModule());
    install(new BusinessProjectModule());
    install(new ContractModule());
    install(new HumanResourceModule());
    install(new SupplychainModule());
    install(new SaleModule());
    install(new PurchaseModule());
    install(new AuthModule());
    install(new AppModule());
    install(new CrmModule());
    install(new BudgetModule());
  }
}
