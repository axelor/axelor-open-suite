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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.EmployeeAdvanceService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.bankorder.BankOrderCreateServiceHr;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseServiceImpl;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ExpenseServiceProjectImpl extends ExpenseServiceImpl {

  @Inject
  public ExpenseServiceProjectImpl(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      ExpenseRepository expenseRepository,
      ExpenseLineRepository expenseLineRepository,
      MoveLineCreateService moveLineCreateService,
      AccountManagementAccountService accountManagementService,
      AppAccountService appAccountService,
      AccountConfigHRService accountConfigService,
      AccountingSituationService accountingSituationService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      PaymentModeService paymentModeService,
      PeriodRepository periodRepository,
      PeriodService periodService,
      MoveLineConsolidateService moveLineConsolidateService,
      KilometricService kilometricService,
      BankDetailsService bankDetailsService,
      EmployeeAdvanceService employeeAdvanceService,
      MoveRepository moveRepository,
      BankOrderCreateServiceHr bankOrderCreateServiceHr,
      BankOrderRepository bankOrderRepository,
      ReconcileService reconcileService,
      BankOrderService bankOrderService,
      MoveCancelService moveCancelService,
      AppBaseService appBaseService,
      SequenceService sequenceService) {
    super(
        moveCreateService,
        moveValidateService,
        expenseRepository,
        expenseLineRepository,
        moveLineCreateService,
        accountManagementService,
        appAccountService,
        accountConfigService,
        accountingSituationService,
        analyticMoveLineService,
        analyticMoveLineGenerateRealService,
        hrConfigService,
        templateMessageService,
        paymentModeService,
        periodRepository,
        periodService,
        moveLineConsolidateService,
        kilometricService,
        bankDetailsService,
        employeeAdvanceService,
        moveRepository,
        bankOrderCreateServiceHr,
        bankOrderRepository,
        reconcileService,
        bankOrderService,
        moveCancelService,
        appBaseService,
        sequenceService);
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
