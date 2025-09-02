/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
