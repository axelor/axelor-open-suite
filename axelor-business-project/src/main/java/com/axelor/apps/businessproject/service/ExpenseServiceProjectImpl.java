/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseServiceImpl;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ExpenseServiceProjectImpl extends ExpenseServiceImpl {

  @Inject
  public ExpenseServiceProjectImpl(
      MoveService moveService,
      ExpenseRepository expenseRepository,
      MoveLineService moveLineService,
      AccountManagementAccountService accountManagementService,
      AppAccountService appAccountService,
      AccountConfigHRService accountConfigService,
      AccountingSituationService accountingSituationService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      PaymentModeService paymentModeService,
      PeriodRepository periodRepository) {
    super(
        moveService,
        expenseRepository,
        moveLineService,
        accountManagementService,
        appAccountService,
        accountConfigService,
        accountingSituationService,
        analyticMoveLineService,
        analyticMoveLineGenerateRealService,
        hrConfigService,
        templateMessageService,
        paymentModeService,
        periodRepository);
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException {

    if (!appAccountService.isApp("business-project")) {
      return super.createInvoiceLines(invoice, expenseLineList, priority);
    }

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    int count = 0;
    for (ExpenseLine expenseLine : expenseLineList) {

      invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine, priority * 100 + count));
      count++;
      invoiceLineList.get(invoiceLineList.size() - 1).setProject(expenseLine.getProject());
    }

    return invoiceLineList;
  }
}
