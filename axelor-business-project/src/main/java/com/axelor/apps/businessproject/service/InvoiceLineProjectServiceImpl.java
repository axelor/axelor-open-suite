/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class InvoiceLineProjectServiceImpl extends InvoiceLineSupplychainService
    implements InvoiceLineProjectService {

  @Inject private InvoiceLineRepository invoiceLineRepo;

  @Inject
  public InvoiceLineProjectServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      PurchaseProductService purchaseProductService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        purchaseProductService);
  }

  @Transactional
  @Override
  public void setProject(List<Long> invoiceLineIds, Project project) {

    if (invoiceLineIds != null) {

      List<InvoiceLine> invoiceLineList =
          invoiceLineRepo.all().filter("self.id in ?1", invoiceLineIds).fetch();

      for (InvoiceLine line : invoiceLineList) {
        line.setProject(project);
        invoiceLineRepo.save(line);
      }
    }
  }

  @Override
  public List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine) {
    List<AnalyticMoveLine> analyticMoveLineList =
        super.createAnalyticDistributionWithTemplate(invoiceLine);

    if (invoiceLine.getProject() != null && analyticMoveLineList != null) {
      analyticMoveLineList.forEach(
          analyticLine -> analyticLine.setProject(invoiceLine.getProject()));
    }
    return analyticMoveLineList;
  }
}
