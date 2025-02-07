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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.move.MoveControlService;
import com.axelor.apps.account.service.move.MoveCustAccountService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.period.PeriodCheckService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.move.MoveValidateHRServiceImpl;
import com.google.inject.Inject;

public class MoveValidateBudgetServiceImpl extends MoveValidateHRServiceImpl {

  protected MoveBudgetService moveBudgetService;

  @Inject
  public MoveValidateBudgetServiceImpl(
      MoveLineControlService moveLineControlService,
      MoveLineToolService moveLineToolService,
      AccountConfigService accountConfigService,
      MoveSequenceService moveSequenceService,
      MoveCustAccountService moveCustAccountService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveRepository moveRepository,
      AccountRepository accountRepository,
      PartnerRepository partnerRepository,
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      FixedAssetGenerationService fixedAssetGenerationService,
      MoveLineTaxService moveLineTaxService,
      PeriodCheckService periodCheckService,
      MoveControlService moveControlService,
      MoveCutOffService moveCutOffService,
      MoveLineCheckService moveLineCheckService,
      CompanyConfigService companyConfigService,
      CurrencyScaleService currencyScaleService,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      ExpenseRepository expenseRepository,
      MoveBudgetService moveBudgetService,
      TaxAccountService taxAccountService,
      UserService userService) {
    super(
        moveLineControlService,
        moveLineToolService,
        accountConfigService,
        moveSequenceService,
        moveCustAccountService,
        moveToolService,
        moveInvoiceTermService,
        moveRepository,
        accountRepository,
        partnerRepository,
        appBaseService,
        appAccountService,
        fixedAssetGenerationService,
        moveLineTaxService,
        periodCheckService,
        moveControlService,
        moveCutOffService,
        moveLineCheckService,
        companyConfigService,
        currencyScaleService,
        moveLineFinancialDiscountService,
        expenseRepository,
        taxAccountService,
        userService);
    this.moveBudgetService = moveBudgetService;
  }

  @Override
  public void accounting(Move move) throws AxelorException {

    if (!moveBudgetService.isBudgetInLines(move)) {
      moveBudgetService.autoComputeBudgetDistribution(move);
    }

    super.accounting(move);
  }
}
