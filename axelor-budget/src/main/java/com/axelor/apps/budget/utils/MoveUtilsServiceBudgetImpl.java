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
package com.axelor.apps.budget.utils;

import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.utils.MoveUtilsServiceBankPaymentImpl;
import com.axelor.utils.service.ArchivingService;
import com.google.inject.Inject;
import java.util.List;

public class MoveUtilsServiceBudgetImpl extends MoveUtilsServiceBankPaymentImpl {

  @Inject
  public MoveUtilsServiceBudgetImpl(
      ArchivingService archivingService,
      AccountingSituationService accountingSituationService,
      PfpService pfpService,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository) {
    super(
        archivingService,
        accountingSituationService,
        pfpService,
        bankStatementLineAFB120Repository);
  }

  @Override
  public List<String> getModelsToIgnoreList() {
    List<String> modelsToIgnoreList = super.getModelsToIgnoreList();
    modelsToIgnoreList.add("BudgetDistribution");
    return modelsToIgnoreList;
  }
}
