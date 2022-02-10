package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.PaymentSessionValidateServiceImpl;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

public class PaymentSessionValidateBankPaymentServiceImpl
    extends PaymentSessionValidateServiceImpl {
  protected BankOrderService bankOrderService;
  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderLineService bankOrderLineService;
  protected BankOrderLineOriginService bankOrderLineOriginService;
  protected BankOrderRepository bankOrderRepo;

  @Inject
  public PaymentSessionValidateBankPaymentServiceImpl(
      AppBaseService appBaseService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      ReconcileService reconcileService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo,
      BankOrderService bankOrderService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderLineService bankOrderLineService,
      BankOrderLineOriginService bankOrderLineOriginService,
      BankOrderRepository bankOrderRepo) {
    super(
        appBaseService,
        moveCreateService,
        moveValidateService,
        moveLineCreateService,
        reconcileService,
        paymentSessionRepo,
        invoiceTermRepo,
        moveRepo,
        partnerRepo);
    this.bankOrderService = bankOrderService;
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderLineService = bankOrderLineService;
    this.bankOrderLineOriginService = bankOrderLineOriginService;
    this.bankOrderRepo = bankOrderRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public int processPaymentSession(PaymentSession paymentSession) throws AxelorException {
    if (paymentSession.getPaymentMode() != null
        && paymentSession.getPaymentMode().getGenerateBankOrder()) {
      this.generateBankOrderFromPaymentSession(paymentSession);
    }

    return super.processPaymentSession(paymentSession);
  }

  @Override
  protected void postProcessPaymentSession(
      PaymentSession paymentSession,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    super.postProcessPaymentSession(paymentSession, moveMap, paymentAmountMap, out, isGlobal);

    BankOrder bankOrder = bankOrderRepo.find(paymentSession.getBankOrder().getId());
    bankOrderService.updateTotalAmounts(bankOrder);
    bankOrderRepo.save(bankOrder);

    if (paymentSession.getPaymentMode().getAutoConfirmBankOrder()) {
      try {
        bankOrderService.confirm(bankOrder);
      } catch (JAXBException | IOException | DatatypeConfigurationException e) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected BankOrder generateBankOrderFromPaymentSession(PaymentSession paymentSession)
      throws AxelorException {
    BankOrder bankOrder = this.createBankOrder(paymentSession);

    paymentSession.setBankOrder(bankOrder);
    bankOrderService.generateSequence(bankOrder);

    return bankOrder;
  }

  protected BankOrder createBankOrder(PaymentSession paymentSession) throws AxelorException {
    return bankOrderCreateService.createBankOrder(
        paymentSession.getPaymentMode(),
        paymentSession.getPartnerTypeSelect(),
        paymentSession.getPaymentDate(),
        paymentSession.getCompany(),
        paymentSession.getBankDetails(),
        paymentSession.getCurrency(),
        paymentSession.getSequence(),
        paymentSession.getName(),
        BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    paymentSession =
        super.processInvoiceTerm(
            paymentSession, invoiceTerm, moveMap, paymentAmountMap, out, isGlobal);

    if (paymentSession.getBankOrder() != null) {
      this.createOrUpdateBankOrderLineFromInvoiceTerm(
          paymentSession, invoiceTerm, paymentSession.getBankOrder());
    }

    return paymentSession;
  }

  protected void createOrUpdateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, BankOrder bankOrder)
      throws AxelorException {
    BankOrderLine bankOrderLine = null;

    if (paymentSession.getPaymentMode().getConsoBankOrderLinePerPartner()) {
      bankOrderLine =
          bankOrder.getBankOrderLineList().stream()
              .filter(
                  it ->
                      it.getPartner().equals(invoiceTerm.getMoveLine().getPartner())
                          && it.getReceiverBankDetails().equals(invoiceTerm.getBankDetails()))
              .findFirst()
              .orElse(null);
    }

    if (bankOrderLine == null) {
      this.generateBankOrderLineFromInvoiceTerm(paymentSession, invoiceTerm, bankOrder);
    } else {
      this.updateBankOrderLine(invoiceTerm, bankOrderLine);
    }

    bankOrderRepo.save(paymentSession.getBankOrder());
  }

  protected void generateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, BankOrder bankOrder)
      throws AxelorException {
    BankOrderLine bankOrderLine =
        bankOrderLineService.createBankOrderLine(
            bankOrder.getBankOrderFileFormat(),
            paymentSession.getCompany(),
            invoiceTerm.getMoveLine().getPartner(),
            invoiceTerm.getBankDetails(),
            invoiceTerm.getAmountPaid(),
            paymentSession.getCurrency(),
            paymentSession.getPaymentDate(),
            this.getReference(invoiceTerm),
            this.getLabel(paymentSession),
            invoiceTerm);

    bankOrder.addBankOrderLineListItem(bankOrderLine);
  }

  protected void updateBankOrderLine(InvoiceTerm invoiceTerm, BankOrderLine bankOrderLine) {
    this.updateReference(invoiceTerm, bankOrderLine);
    bankOrderLine.setBankOrderAmount(
        bankOrderLine.getBankOrderAmount().add(invoiceTerm.getAmountPaid()));
    bankOrderLine.addBankOrderLineOriginListItem(
        bankOrderLineOriginService.createBankOrderLineOrigin(invoiceTerm));
  }

  protected String getLabel(PaymentSession paymentSession) {
    return String.format(
        "%s - %s",
        paymentSession.getPaymentMode().getName(), paymentSession.getCompany().getName());
  }

  protected String getReference(InvoiceTerm invoiceTerm) {
    return String.format(
        "%s (%s)", invoiceTerm.getMoveLine().getOrigin(), invoiceTerm.getDueDate());
  }

  protected void updateReference(InvoiceTerm invoiceTerm, BankOrderLine bankOrderLine) {
    String newReference =
        String.format(
            "%s/%s", bankOrderLine.getReceiverReference(), this.getReference(invoiceTerm));

    if (newReference.length() < 256) {
      bankOrderLine.setReceiverReference(newReference);
    }
  }
}
