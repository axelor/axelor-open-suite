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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineCreateBudgetServiceImpl extends MoveLineCreateServiceImpl {

  @Inject
  public MoveLineCreateBudgetServiceImpl(
      CompanyConfigService companyConfigService,
      CurrencyService currencyService,
      FiscalPositionAccountService fiscalPositionAccountService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      TaxAccountService taxAccountService,
      MoveLineToolService moveLineToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineConsolidateService moveLineConsolidateService,
      InvoiceTermService invoiceTermService,
      MoveLineTaxService moveLineTaxService,
      AccountingSituationRepository accountingSituationRepository,
      AccountingSituationService accountingSituationService,
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AppBaseService appBaseService,
      AnalyticLineService analyticLineService,
      CurrencyScaleService currencyScaleService) {
    super(
        companyConfigService,
        currencyService,
        fiscalPositionAccountService,
        analyticMoveLineGenerateRealService,
        taxAccountService,
        moveLineToolService,
        moveLineComputeAnalyticService,
        moveLineConsolidateService,
        invoiceTermService,
        moveLineTaxService,
        accountingSituationRepository,
        accountingSituationService,
        fiscalPositionService,
        taxService,
        appBaseService,
        analyticLineService,
        currencyScaleService);
  }

  @Override
  public MoveLine fillMoveLineWithInvoiceLine(
      MoveLine moveLine, InvoiceLine invoiceLine, Company company) throws AxelorException {
    moveLine = super.fillMoveLineWithInvoiceLine(moveLine, invoiceLine, company);

    moveLine.setBudget(invoiceLine.getBudget());
    moveLine.setBudgetDistributionSumAmount(invoiceLine.getBudgetDistributionSumAmount());

    if (!CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
      for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
        moveLine.addBudgetDistributionListItem(budgetDistribution);
      }
    }

    return moveLine;
  }
}
