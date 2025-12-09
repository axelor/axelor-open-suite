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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticLineModelServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.google.inject.Inject;

public class AnalyticLineModelSupplychainServiceImpl extends AnalyticLineModelServiceImpl {

  protected PurchaseConfigService purchaseConfigService;
  protected SaleConfigService saleConfigService;

  @Inject
  public AnalyticLineModelSupplychainServiceImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      AnalyticToolService analyticToolService,
      CurrencyScaleService currencyScaleService,
      AnalyticAxisService analyticAxisService,
      PurchaseConfigService purchaseConfigService,
      SaleConfigService saleConfigService) {
    super(
        appBaseService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        analyticToolService,
        currencyScaleService,
        analyticAxisService);
    this.purchaseConfigService = purchaseConfigService;
    this.saleConfigService = saleConfigService;
  }

  @Override
  public boolean analyticDistributionTemplateRequired(boolean isPurchase, Company company)
      throws AxelorException {
    return super.analyticDistributionTemplateRequired(isPurchase, company)
        && ((isPurchase
                && purchaseConfigService
                    .getPurchaseConfig(company)
                    .getIsAnalyticDistributionRequired())
            || (!isPurchase
                && saleConfigService.getSaleConfig(company).getIsAnalyticDistributionRequired()));
  }
}
