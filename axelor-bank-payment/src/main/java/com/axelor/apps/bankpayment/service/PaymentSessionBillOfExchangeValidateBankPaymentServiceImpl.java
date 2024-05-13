package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermReplaceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionBillOfExchangeValidateServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateService;
import com.axelor.apps.account.service.reconcile.ReconcileInvoiceTermComputationService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.lang3.tuple.Pair;

public class PaymentSessionBillOfExchangeValidateBankPaymentServiceImpl
    extends PaymentSessionBillOfExchangeValidateServiceImpl {

  protected PaymentSessionBankOrderService paymentSessionBankOrderService;
  protected BankOrderService bankOrderService;
  protected BankOrderValidationService bankOrderValidationService;
  protected BankOrderRepository bankOrderRepo;

  @Inject
  public PaymentSessionBillOfExchangeValidateBankPaymentServiceImpl(
      PaymentSessionValidateService paymentSessionValidateService,
      InvoiceTermRepository invoiceTermRepo,
      MoveValidateService moveValidateService,
      MoveCutOffService moveCutOffService,
      PaymentSessionRepository paymentSessionRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo,
      InvoicePaymentRepository invoicePaymentRepo,
      AccountConfigService accountConfigService,
      PartnerService partnerService,
      PaymentModeService paymentModeService,
      MoveInvoiceTermService moveInvoiceTermService,
      ReconcileService reconcileService,
      ReconcileInvoiceTermComputationService reconcileInvoiceTermComputationService,
      InvoiceTermService invoiceTermService,
      InvoiceTermReplaceService invoiceTermReplaceService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoicePaymentValidateService invoicePaymentValidateService,
      PaymentSessionBankOrderService paymentSessionBankOrderService,
      BankOrderService bankOrderService,
      BankOrderValidationService bankOrderValidationService,
      BankOrderRepository bankOrderRepo) {
    super(
        paymentSessionValidateService,
        invoiceTermRepo,
        moveValidateService,
        moveCutOffService,
        paymentSessionRepo,
        moveRepo,
        partnerRepo,
        invoicePaymentRepo,
        accountConfigService,
        partnerService,
        paymentModeService,
        moveInvoiceTermService,
        reconcileService,
        reconcileInvoiceTermComputationService,
        invoiceTermService,
        invoiceTermReplaceService,
        invoicePaymentCreateService,
        invoicePaymentValidateService);
    this.paymentSessionBankOrderService = paymentSessionBankOrderService;
    this.bankOrderService = bankOrderService;
    this.bankOrderValidationService = bankOrderValidationService;
    this.bankOrderRepo = bankOrderRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public int processPaymentSession(
      PaymentSession paymentSession,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {
    if (paymentSession.getPaymentMode() != null
        && paymentSession.getPaymentMode().getGenerateBankOrder()
        && paymentSession.getBankOrder() == null) {
      paymentSessionBankOrderService.generateBankOrderFromPaymentSession(paymentSession);
    }

    return super.processPaymentSession(paymentSession, invoiceTermLinkWithRefundList);
  }

  @Override
  protected void postProcessPaymentSession(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out)
      throws AxelorException {
    if (paymentSession.getBankOrder() != null) {
      BankOrder bankOrder = bankOrderRepo.find(paymentSession.getBankOrder().getId());
      bankOrderService.updateTotalAmounts(bankOrder);
      bankOrderRepo.save(bankOrder);

      if (paymentSession.getPaymentMode().getAutoConfirmBankOrder()
          && bankOrder.getStatusSelect() == BankOrderRepository.STATUS_DRAFT) {
        try {
          bankOrderValidationService.confirm(bankOrder);
        } catch (IOException | DatatypeConfigurationException | JAXBException e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
        }
      }
    }

    super.postProcessPaymentSession(paymentSession, moveDateMap, paymentAmountMap, out);
  }

  @Override
  protected void processInvoiceTermBillOfExchange(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund)
      throws AxelorException {

    if (paymentSession.getBankOrder() != null
        && paymentSession.getStatusSelect() != PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      paymentSessionBankOrderService.createOrUpdateBankOrderLineFromInvoiceTerm(
          paymentSession, invoiceTerm, paymentSession.getBankOrder(), invoiceTermLinkWithRefund);
    }

    paymentSessionBankOrderService.manageInvoicePayment(
        paymentSession, invoiceTerm, invoiceTerm.getAmountPaid());

    super.processInvoiceTermBillOfExchange(
        paymentSession, invoiceTerm, moveDateMap, paymentAmountMap, invoiceTermLinkWithRefund);
  }
}
