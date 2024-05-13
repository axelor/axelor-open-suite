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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;

@RequestScoped
public class InvoicePaymentValidateServiceImpl implements InvoicePaymentValidateService {

  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected InvoicePaymentMoveCreateService invoicePaymentMoveCreateService;
  protected int jpaLimit = 10;

  @Inject
  public InvoicePaymentValidateServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentToolService invoicePaymentToolService,
      InvoicePaymentMoveCreateService invoicePaymentMoveCreateService) {

    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.invoicePaymentMoveCreateService = invoicePaymentMoveCreateService;
  }

  /**
   * Method to validate an invoice Payment
   *
   * <p>Create the eventual move (depending general configuration) and reconcile it with the invoice
   * move Compute the amount paid on invoice Change the status to validated
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   * @throws DatatypeConfigurationException
   * @throws IOException
   * @throws JAXBException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(InvoicePayment invoicePayment, boolean force)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

    Invoice invoice = invoicePayment.getInvoice();
    validatePartnerAccount(invoice);
    invoice.setNextDueDate(Beans.get(InvoiceToolService.class).getNextDueDate(invoice));

    if (!force && invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_DRAFT) {
      return;
    }

    setInvoicePaymentStatus(invoicePayment);
    invoicePaymentMoveCreateService.createInvoicePaymentMove(invoicePayment);
    invoicePaymentToolService.updateAmountPaid(invoice);

    if (invoice != null
        && invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
        && !InvoiceToolService.isRefund(invoice)) {
      invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_ADVANCEPAYMENT);
    }
    invoicePaymentRepository.save(invoicePayment);
  }

  @Override
  public void validateMultipleInvoicePayment(
      List<InvoicePayment> invoicePaymentList, boolean force) {
    if (ObjectUtils.isEmpty(invoicePaymentList)) {
      return;
    }

    int i = 0;
    for (InvoicePayment invoicePayment : invoicePaymentList) {
      try {
        if (invoicePayment != null) {
          invoicePayment = invoicePaymentRepository.find(invoicePayment.getId());
          validate(invoicePayment, force);
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
      } finally {
        if (++i % jpaLimit == 0) {
          JPA.clear();
        }
      }
    }
  }

  protected void validatePartnerAccount(Invoice invoice) throws AxelorException {
    Account partnerAccount = invoice.getPartnerAccount();
    if (!partnerAccount.getReconcileOk() || !partnerAccount.getUseForPartnerBalance()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_RECONCILABLE_USE_FOR_PARTNER_BALANCE));
    }
  }

  protected void setInvoicePaymentStatus(InvoicePayment invoicePayment) throws AxelorException {
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    validate(invoicePayment, false);
  }
}
