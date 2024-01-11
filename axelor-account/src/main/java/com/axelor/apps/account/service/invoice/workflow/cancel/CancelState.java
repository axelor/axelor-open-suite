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
package com.axelor.apps.account.service.invoice.workflow.cancel;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CancelState extends WorkflowInvoice {

  private WorkflowCancelService workflowService;
  private BudgetService budgetService;

  @Inject
  CancelState(WorkflowCancelService workflowService, BudgetService budgetService) {
    this.workflowService = workflowService;
    this.budgetService = budgetService;
  }

  @Override
  public void init(Invoice invoice) {
    this.invoice = invoice;
  }

  @Override
  public void process() throws AxelorException {

    workflowService.beforeCancel(invoice);

    updateInvoiceFromCancellation();

    workflowService.afterCancel(invoice);
  }

  protected void updateInvoiceFromCancellation() throws AxelorException {
    setStatus();
    if (Beans.get(AccountConfigService.class)
        .getAccountConfig(invoice.getCompany())
        .getIsManagePassedForPayment()) {
      setPfpStatus();
    }

    budgetService.updateBudgetLinesFromInvoice(invoice);

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      invoiceLine.clearBudgetDistributionList();
    }
  }

  @Transactional
  protected void setStatus() {

    invoice.setStatusSelect(InvoiceRepository.STATUS_CANCELED);
  }

  protected void cancelMove() throws AxelorException {

    if (invoice.getOldMove() != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.INVOICE_CANCEL_1));
    }

    Move move = invoice.getMove();

    invoice.setMove(null);

    Beans.get(MoveCancelService.class).cancel(move);
  }

  protected void setPfpStatus() throws AxelorException {
    InvoiceToolService.setPfpStatus(invoice);
    invoice.setDecisionPfpTakenDateTime(null);

    invoice.getInvoiceTermList().stream()
        .filter(it -> it.getPfpValidateStatusSelect() != InvoiceTermRepository.PFP_STATUS_NO_PFP)
        .forEach(it -> it.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING));
  }
}
