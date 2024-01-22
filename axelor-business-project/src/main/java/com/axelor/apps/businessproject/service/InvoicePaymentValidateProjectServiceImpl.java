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
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

@RequestScoped
public class InvoicePaymentValidateProjectServiceImpl
    extends InvoicePaymentValidateServiceBankPayImpl {

  private InvoicingProjectRepository invoicingProjectRepo;

  @Inject
  public InvoicePaymentValidateProjectServiceImpl(
      PaymentModeService paymentModeService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveToolService moveToolService,
      MoveLineCreateService moveLineCreateService,
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepository,
      ReconcileService reconcileService,
      InvoicePaymentToolService invoicePaymentToolService,
      InvoiceTermService invoiceTermService,
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderService bankOrderService,
      InvoicingProjectRepository invoicingProjectRepo,
      DateService dateService,
      MoveLineInvoiceTermService moveLineInvoiceTermService) {
    super(
        paymentModeService,
        moveCreateService,
        moveValidateService,
        moveToolService,
        moveLineCreateService,
        accountConfigService,
        invoicePaymentRepository,
        reconcileService,
        invoicePaymentToolService,
        invoiceTermService,
        appAccountService,
        accountManagementAccountService,
        bankOrderCreateService,
        bankOrderService,
        dateService,
        moveLineInvoiceTermService);
    this.invoicingProjectRepo = invoicingProjectRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(InvoicePayment invoicePayment, boolean force)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    super.validate(invoicePayment, force);
    Invoice invoice = invoicePayment.getInvoice();

    InvoicingProject invoicingProject =
        invoicingProjectRepo
            .all()
            .filter(
                "self.invoice.id = ?1 AND self.project.invoicingSequenceSelect = ?2",
                invoice.getId(),
                ProjectRepository.INVOICING_SEQ_INVOICE_PRE_TASK)
            .fetchOne();

    if (invoicingProject != null) {
      for (ProjectTask projectTask : invoicingProject.getProjectTaskSet()) {
        projectTask.setIsPaid(invoice.getHasPendingPayments());
      }
    }
  }
}
