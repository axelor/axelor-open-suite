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
package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

@RequestScoped
public class InvoicePaymentValidateServiceBankPayImpl extends InvoicePaymentValidateServiceImpl {

  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderService bankOrderService;
  protected InvoiceTermService invoiceTermService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public InvoicePaymentValidateServiceBankPayImpl(
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
      DateService dateService,
      AccountingSituationService accountingSituationService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo) {
    super(
        paymentModeService,
        moveCreateService,
        moveValidateService,
        moveToolService,
        moveLineCreateService,
        accountConfigService,
        invoicePaymentRepository,
        reconcileService,
        appAccountService,
        accountManagementAccountService,
        invoicePaymentToolService,
        dateService,
        accountingSituationService);
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderService = bankOrderService;
    this.invoiceTermService = invoiceTermService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  @Override
  protected void setInvoicePaymentStatus(InvoicePayment invoicePayment) throws AxelorException {
    Invoice invoice = invoicePayment.getInvoice();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    PaymentSession paymentSession = invoicePayment.getPaymentSession();
    if (paymentMode == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.INVOICE_PAYMENT_MODE_MISSING),
          invoice.getInvoiceId());
    }

    if (paymentModeService.isPendingPayment(paymentMode)
        && paymentMode.getGenerateBankOrder()
        && ((paymentSession != null
                && paymentSession.getAccountingTriggerSelect()
                    != PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE)
            || (paymentSession == null
                && paymentMode.getAccountingTriggerSelect()
                    != PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE))
        && invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_DRAFT) {
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
    } else {
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    }
  }

  @Override
  protected void createInvoicePaymentMove(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    PaymentSession paymentSession = invoicePayment.getPaymentSession();

    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    if (accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()
        && (!paymentMode.getGenerateBankOrder()
            || (paymentSession != null
                && paymentSession.getAccountingTriggerSelect()
                    == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE)
            || (paymentSession == null
                && paymentMode.getAccountingTriggerSelect()
                    == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE))) {
      invoicePayment = this.createMoveForInvoicePayment(invoicePayment);
    } else {
      accountingSituationService.updateCustomerCredit(invoicePayment.getInvoice().getPartner());
      invoicePayment = invoicePaymentRepository.save(invoicePayment);
    }
    if (paymentMode.getGenerateBankOrder()
        && invoicePayment.getBankOrder() == null
        && paymentSession == null) {
      this.createBankOrder(invoicePayment);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validateFromBankOrder(InvoicePayment invoicePayment, boolean force)
      throws AxelorException {

    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);

    Company company = invoicePayment.getInvoice().getCompany();

    if (!processLcrPaymentWithBankOrder(invoicePayment)) {
      if (accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()) {
        invoicePayment = this.createMoveForInvoicePayment(invoicePayment);
        if (invoicePayment == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BankPaymentExceptionMessage.VALIDATION_BANK_ORDER_MOVE_INV_PAYMENT_FAIL));
        }
      } else {
        accountingSituationService.updateCustomerCredit(invoicePayment.getInvoice().getPartner());
        invoicePayment = invoicePaymentRepository.save(invoicePayment);
      }
    }

    invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
  }

  /**
   * Method to create a bank order for an invoice Payment
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   * @throws DatatypeConfigurationException
   * @throws IOException
   * @throws JAXBException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void createBankOrder(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

    BankOrder bankOrder = bankOrderCreateService.createBankOrder(invoicePayment);

    if (invoicePayment.getPaymentMode().getAutoConfirmBankOrder()) {
      bankOrderService.confirm(bankOrder);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean processLcrPaymentWithBankOrder(InvoicePayment invoicePayment)
      throws AxelorException {
    boolean isAlreadyPaid = false;
    if (invoicePayment == null
        || invoicePayment.getBankOrder() == null
        || invoicePayment.getPaymentMode() == null
        || invoicePayment.getPaymentMode().getTypeSelect()
            != PaymentModeRepository.TYPE_EXCHANGES) {
      return isAlreadyPaid;
    }
    PaymentSession paymentSession =
        paymentSessionRepo.findByBankOrder(invoicePayment.getBankOrder());
    if (paymentSession == null) {
      return isAlreadyPaid;
    }
    for (BankOrderLine bankOrderLine : invoicePayment.getBankOrder().getBankOrderLineList()) {
      if (bankOrderLine.getSenderMove() != null
          && !ObjectUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
        Move move = bankOrderLine.getSenderMove();
        invoicePayment.setMove(move);
        Optional<MoveLine> cashMoveLine =
            move.getMoveLineList().stream().filter(ml -> ml.getCredit().signum() != 0).findFirst();
        List<InvoiceTerm> invoiceTermList =
            invoicePayment.getInvoiceTermPaymentList().stream()
                .map(InvoiceTermPayment::getInvoiceTerm)
                .filter(it -> it.getPlacementMoveLine() != null)
                .collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(invoiceTermList)) {
          for (InvoiceTerm invoiceTerm : invoiceTermList) {
            if (invoiceTerm != null && cashMoveLine.isPresent()) {
              reconcileService.reconcile(
                  invoiceTerm.getPlacementMoveLine(),
                  cashMoveLine.get(),
                  invoicePayment,
                  false,
                  false);
              isAlreadyPaid = true;
            }
          }
        }
      }
    }
    return isAlreadyPaid;
  }
}
