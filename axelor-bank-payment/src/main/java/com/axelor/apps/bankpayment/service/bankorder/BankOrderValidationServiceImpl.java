package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentMoveCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.lang3.tuple.Pair;

public class BankOrderValidationServiceImpl implements BankOrderValidationService {

  protected AccountConfigService accountConfigService;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected InvoicePaymentMoveCreateService invoicePaymentMoveCreateService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected AppBaseService appBaseService;
  protected PaymentSessionRepository paymentSessionRepository;
  protected BankOrderRepository bankOrderRepository;
  protected BankOrderCheckService bankOrderCheckService;
  protected BankOrderService bankOrderService;
  protected AccountingSituationService accountingSituationService;
  protected BankOrderMoveService bankOrderMoveService;
  protected BankOrderLineOriginService bankOrderLineOriginService;
  protected PaymentSessionValidateService paymentSessionValidateService;

  @Inject
  public BankOrderValidationServiceImpl(
      AccountConfigService accountConfigService,
      InvoicePaymentToolService invoicePaymentToolService,
      InvoicePaymentMoveCreateService invoicePaymentMoveCreateService,
      InvoicePaymentRepository invoicePaymentRepository,
      AppBaseService appBaseService,
      PaymentSessionRepository paymentSessionRepository,
      BankOrderRepository bankOrderRepository,
      BankOrderCheckService bankOrderCheckService,
      BankOrderService bankOrderService,
      AccountingSituationService accountingSituationService,
      BankOrderMoveService bankOrderMoveService,
      BankOrderLineOriginService bankOrderLineOriginService,
      PaymentSessionValidateService paymentSessionValidateService) {
    this.accountConfigService = accountConfigService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.invoicePaymentMoveCreateService = invoicePaymentMoveCreateService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.appBaseService = appBaseService;
    this.paymentSessionRepository = paymentSessionRepository;
    this.bankOrderRepository = bankOrderRepository;
    this.bankOrderCheckService = bankOrderCheckService;
    this.bankOrderService = bankOrderService;
    this.accountingSituationService = accountingSituationService;
    this.bankOrderMoveService = bankOrderMoveService;
    this.bankOrderLineOriginService = bankOrderLineOriginService;
    this.paymentSessionValidateService = paymentSessionValidateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateFromBankOrder(InvoicePayment invoicePayment, boolean force)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException {

    // Payment date has been initialized at creation. But BankOrder may be validate on a later date
    // So updating paymentDate
    invoicePayment.setPaymentDate(invoicePayment.getBankOrder().getBankOrderDate());
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);

    Company company = invoicePayment.getInvoice().getCompany();

    if (accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()) {
      invoicePayment = invoicePaymentMoveCreateService.createMoveForInvoicePayment(invoicePayment);
      if (invoicePayment == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.VALIDATION_BANK_ORDER_MOVE_INV_PAYMENT_FAIL));
      }
    } else {
      accountingSituationService.updateCustomerCredit(invoicePayment.getInvoice().getPartner());
      invoicePayment = invoicePaymentRepository.save(invoicePayment);
    }

    invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realize(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException {

    LocalDate todayDate = appBaseService.getTodayDate(bankOrder.getSenderCompany());

    if (!bankOrder.getAreMovesGenerated()
        && bankOrder.getAccountingTriggerSelect()
            == PaymentModeRepository.ACCOUNTING_TRIGGER_REALIZATION) {
      if (ObjectUtils.isEmpty(bankOrder.getBankOrderDate())
          || bankOrder.getBankOrderDate().isBefore(todayDate)) {
        bankOrder.setBankOrderDate(todayDate);
      }
      bankOrder = this.generateMoves(bankOrder);
    }

    bankOrder.setSendingDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    bankOrder.setStatusSelect(BankOrderRepository.STATUS_CARRIED_OUT);

    bankOrderRepository.save(bankOrder);
  }

  protected BankOrder generateMoves(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException {

    if (bankOrder
        .getFunctionalOriginSelect()
        .equals(BankOrderRepository.FUNCTIONAL_ORIGIN_PAYMENT_SESSION)) {
      PaymentSession paymentSession =
          paymentSessionRepository.all().filter("self.bankOrder = ?", bankOrder).fetchOne();

      if (paymentSession != null) {
        List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund =
            new ArrayList<>();
        paymentSessionValidateService.reconciledInvoiceTermMoves(
            paymentSession, invoiceTermLinkWithRefund);

        paymentSessionValidateService.processPaymentSession(
            paymentSession, invoiceTermLinkWithRefund);
        bankOrder = bankOrderRepository.find(bankOrder.getId());
      }
    } else if (bankOrder
        .getFunctionalOriginSelect()
        .equals(BankOrderRepository.FUNCTIONAL_ORIGIN_INVOICE_PAYMENT)) {
      this.validatePayment(bankOrder);
    } else {
      bankOrderMoveService.generateMoves(bankOrder);
    }

    bankOrder.setAreMovesGenerated(true);

    return bankOrderRepository.find(bankOrder.getId());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(BankOrder bankOrder)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    bankOrderCheckService.checkBankDetails(bankOrder.getSenderBankDetails(), bankOrder);
    LocalDate todayDate = appBaseService.getTodayDate(bankOrder.getSenderCompany());

    if (bankOrder.getGeneratedMetaFile() == null) {
      bankOrderCheckService.checkLines(bankOrder);
    }

    bankOrderService.setNbOfLines(bankOrder);

    bankOrderService.setSequenceOnBankOrderLines(bankOrder);

    bankOrderService.generateFile(bankOrder);

    PaymentMode paymentMode = bankOrder.getPaymentMode();

    bankOrderService.processBankOrderStatus(bankOrder, paymentMode);

    if (bankOrder.getAccountingTriggerSelect()
        == PaymentModeRepository.ACCOUNTING_TRIGGER_CONFIRMATION) {
      if (ObjectUtils.isEmpty(bankOrder.getBankOrderDate())
          || bankOrder.getBankOrderDate().isBefore(todayDate)) {
        bankOrder.setBankOrderDate(todayDate);
      }
      this.generateMoves(bankOrder);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePayment(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException {

    List<InvoicePayment> invoicePaymentList =
        invoicePaymentRepository.findByBankOrder(bankOrder).fetch();

    for (InvoicePayment invoicePayment : invoicePaymentList) {

      if (invoicePayment != null
          && invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_VALIDATED
          && invoicePayment.getInvoice() != null) {

        if (bankOrderLineOriginService.existBankOrderLineOrigin(
            bankOrder, invoicePayment.getInvoice())) {

          this.validateFromBankOrder(invoicePayment, true);
        }
      }
    }
  }
}
