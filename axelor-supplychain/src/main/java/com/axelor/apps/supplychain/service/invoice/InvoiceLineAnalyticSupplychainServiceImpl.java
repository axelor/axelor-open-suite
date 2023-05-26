/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticServiceImpl;
import com.axelor.utils.service.ListToolService;
import com.google.inject.Inject;
import java.util.List;

public class InvoiceLineAnalyticSupplychainServiceImpl extends InvoiceLineAnalyticServiceImpl {

  @Inject
  public InvoiceLineAnalyticSupplychainServiceImpl(
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticToolService analyticToolService,
      AccountConfigService accountConfigService,
      ListToolService listToolService,
      AppAccountService appAccountService) {
    super(
        analyticAccountRepository,
        accountAnalyticRulesRepository,
        analyticMoveLineService,
        analyticToolService,
        accountConfigService,
        listToolService,
        appAccountService);
  }

  @Override
  public List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine) {
    List<AnalyticMoveLine> analyticMoveLineList =
        super.createAnalyticDistributionWithTemplate(invoiceLine);
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      analyticMoveLine.setSaleOrderLine(invoiceLine.getSaleOrderLine());
      analyticMoveLine.setPurchaseOrderLine(invoiceLine.getPurchaseOrderLine());
    }
    return analyticMoveLineList;
  }
}
