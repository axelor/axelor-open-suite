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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateServiceImpl;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PaymentSessionValidateBankPaymentServiceImpl
    extends PaymentSessionValidateServiceImpl {
  protected BankOrderService bankOrderService;
  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderLineService bankOrderLineService;
  protected BankOrderLineOriginService bankOrderLineOriginService;
  protected BankOrderRepository bankOrderRepo;
  protected CurrencyService currencyService;
  protected AppAccountService appAccountService;
  protected DateService dateService;

  @Inject
  public PaymentSessionValidateBankPaymentServiceImpl(
      AppBaseService appBaseService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveCutOffService moveCutOffService,
      MoveLineCreateService moveLineCreateService,
      ReconcileService reconcileService,
      InvoiceTermService invoiceTermService,
      MoveLineTaxService moveLineTaxService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoicePaymentValidateService invoicePaymentValidateService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo,
      AccountConfigService accountConfigService,
      PartnerService partnerService,
      PaymentModeService paymentModeService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      BankOrderService bankOrderService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderLineService bankOrderLineService,
      BankOrderLineOriginService bankOrderLineOriginService,
      BankOrderRepository bankOrderRepo,
      CurrencyService currencyService,
      AppAccountService appAccountService,
      InvoicePaymentRepository invoicePaymentRepo,
      DateService dateService) {
    super(
        appBaseService,
        moveCreateService,
        moveValidateService,
        moveCutOffService,
        moveLineCreateService,
        reconcileService,
        invoiceTermService,
        moveLineTaxService,
        invoicePaymentCreateService,
        invoicePaymentValidateService,
        paymentSessionRepo,
        invoiceTermRepo,
        moveRepo,
        partnerRepo,
        invoicePaymentRepo,
        accountConfigService,
        partnerService,
        paymentModeService,
        moveLineInvoiceTermService);
    this.bankOrderService = bankOrderService;
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderLineService = bankOrderLineService;
    this.bankOrderLineOriginService = bankOrderLineOriginService;
    this.bankOrderRepo = bankOrderRepo;
    this.currencyService = currencyService;
    this.appAccountService = appAccountService;
    this.dateService = dateService;
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
      this.generateBankOrderFromPaymentSession(paymentSession);
    }

    return super.processPaymentSession(paymentSession, invoiceTermLinkWithRefundList);
  }

  @Override
  protected void postProcessPaymentSession(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (paymentSession.getBankOrder() != null) {
      BankOrder bankOrder = bankOrderRepo.find(paymentSession.getBankOrder().getId());
      bankOrderService.updateTotalAmounts(bankOrder);
      bankOrderRepo.save(bankOrder);

      if (paymentSession.getPaymentMode().getAutoConfirmBankOrder()
          && bankOrder.getStatusSelect() == BankOrderRepository.STATUS_DRAFT) {
        try {
          bankOrderService.confirm(bankOrder);
        } catch (JAXBException | IOException | DatatypeConfigurationException e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
        }
      }
    }

    super.postProcessPaymentSession(paymentSession, moveDateMap, paymentAmountMap, out, isGlobal);
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
    BankOrder bankOrder =
        bankOrderCreateService.createBankOrder(
            paymentSession.getPaymentMode(),
            paymentSession.getPartnerTypeSelect(),
            paymentSession.getPaymentDate(),
            paymentSession.getCompany(),
            paymentSession.getBankDetails(),
            paymentSession.getCurrency(),
            paymentSession.getSequence(),
            this.getLabel(paymentSession),
            BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            BankOrderRepository.FUNCTIONAL_ORIGIN_PAYMENT_SESSION,
            paymentSession.getAccountingTriggerSelect());

    if (!paymentSession.getCurrency().equals(paymentSession.getCompany().getCurrency())) {
      bankOrder.setIsMultiCurrency(true);
    }

    return bankOrder;
  }

  protected boolean isMultiDate(PaymentSession paymentSession) {
    return this.isFileFormatMultiDate(paymentSession)
        && paymentSession.getMoveAccountingDateSelect()
            == PaymentSessionRepository.MOVE_ACCOUNTING_DATE_ORIGIN_DOCUMENT;
  }

  protected boolean isFileFormatMultiDate(PaymentSession paymentSession) {
    return Optional.of(paymentSession)
        .map(PaymentSession::getPaymentMode)
        .map(PaymentMode::getBankOrderFileFormat)
        .map(BankOrderFileFormat::getIsMultiDate)
        .orElse(false);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList,
      boolean out,
      boolean isGlobal)
      throws AxelorException {

    if (paymentSession.getBankOrder() != null
        && paymentSession.getStatusSelect() != PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      this.createOrUpdateBankOrderLineFromInvoiceTerm(
          paymentSession,
          invoiceTerm,
          paymentSession.getBankOrder(),
          invoiceTermLinkWithRefundList);
    }

    paymentSession =
        super.processInvoiceTerm(
            paymentSession,
            invoiceTerm,
            moveDateMap,
            paymentAmountMap,
            invoiceTermLinkWithRefundList,
            out,
            isGlobal);

    return paymentSession;
  }

  @Override
  protected boolean generatePaymentsFirst(PaymentSession paymentSession) {
    return super.generatePaymentsFirst(paymentSession)
        || (paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_ONGOING
            && paymentSession.getPaymentMode().getGenerateBankOrder()
            && paymentSession.getAccountingTriggerSelect()
                != PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE);
  }

  @Override
  @Transactional
  protected InvoicePayment generatePendingPaymentFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    InvoicePayment invoicePayment =
        super.generatePendingPaymentFromInvoiceTerm(paymentSession, invoiceTerm);
    if (invoicePayment == null) {
      return null;
    }
    invoicePayment.setBankOrder(paymentSession.getBankOrder());

    return invoicePaymentRepo.save(invoicePayment);
  }

  protected void createOrUpdateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrder bankOrder,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {
    BankOrderLine bankOrderLine = null;

    if (paymentSession.getPaymentMode().getConsoBankOrderLinePerPartner()) {
      bankOrderLine =
          bankOrder.getBankOrderLineList().stream()
              .filter(
                  it ->
                      (it.getBankOrderDate() == null
                              || it.getBankOrderDate().equals(invoiceTerm.getDueDate()))
                          && it.getPartner().equals(invoiceTerm.getMoveLine().getPartner())
                          && ((it.getReceiverBankDetails() == null
                                  && invoiceTerm.getBankDetails() == null)
                              || (it.getReceiverBankDetails() != null
                                  && it.getReceiverBankDetails()
                                      .equals(invoiceTerm.getBankDetails()))))
              .findFirst()
              .orElse(null);
    }

    BigDecimal reconciledAmount = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(invoiceTermLinkWithRefundList)) {
      List<Pair<InvoiceTerm, BigDecimal>> invoiceTermByAmountList =
          invoiceTermLinkWithRefundList.stream()
              .filter(pair -> pair.getLeft().equals(invoiceTerm))
              .map(pair -> pair.getRight())
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(invoiceTermByAmountList)) {
        for (Pair<InvoiceTerm, BigDecimal> pair : invoiceTermByAmountList) {
          reconciledAmount = reconciledAmount.add(pair.getRight());
        }
      }
    }

    if (bankOrderLine == null) {
      this.generateBankOrderLineFromInvoiceTerm(
          paymentSession, invoiceTerm, bankOrder, reconciledAmount);
    } else {
      this.updateBankOrderLine(paymentSession, invoiceTerm, bankOrderLine, reconciledAmount);
    }

    bankOrderRepo.save(paymentSession.getBankOrder());
  }

  protected void generateBankOrderLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrder bankOrder,
      BigDecimal reconciledAmount)
      throws AxelorException {
    LocalDate bankOrderDate = null;
    if (invoiceTerm.getAmountPaid().subtract(reconciledAmount).signum() == 0) {
      return;
    }
    if (this.isFileFormatMultiDate(paymentSession)) {
      bankOrderDate =
          paymentSession.getMoveAccountingDateSelect()
                  == PaymentSessionRepository.MOVE_ACCOUNTING_DATE_PAYMENT
              ? paymentSession.getPaymentDate()
              : invoiceTerm.getDueDate();
    }

    BankOrderLine bankOrderLine =
        bankOrderLineService.createBankOrderLine(
            bankOrder.getBankOrderFileFormat(),
            null,
            invoiceTerm.getMoveLine().getPartner(),
            invoiceTerm.getBankDetails(),
            invoiceTerm.getAmountPaid().subtract(reconciledAmount),
            paymentSession.getCurrency(),
            bankOrderDate,
            this.getReference(invoiceTerm),
            this.getLabel(paymentSession),
            invoiceTerm);

    bankOrder.addBankOrderLineListItem(bankOrderLine);
    bankOrderLine.setCompanyCurrencyAmount(
        this.getAmountPaidInCompanyCurrency(
            paymentSession, invoiceTerm, bankOrderLine, reconciledAmount));
  }

  protected void updateBankOrderLine(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrderLine bankOrderLine,
      BigDecimal reconciledAmount)
      throws AxelorException {

    if (invoiceTerm.getAmountPaid().subtract(reconciledAmount).signum() == 0) {
      return;
    }
    this.updateReference(invoiceTerm, bankOrderLine);
    bankOrderLine.setBankOrderAmount(
        bankOrderLine
            .getBankOrderAmount()
            .add(invoiceTerm.getAmountPaid().subtract(reconciledAmount)));
    if (bankOrderLine.getBankOrderAmount().signum() == 0) {
      resetBankOrderLine(bankOrderLine);
      return;
    }
    bankOrderLine.setCompanyCurrencyAmount(
        bankOrderLine
            .getCompanyCurrencyAmount()
            .add(
                this.getAmountPaidInCompanyCurrency(
                    paymentSession, invoiceTerm, bankOrderLine, reconciledAmount)));
    bankOrderLine.addBankOrderLineOriginListItem(
        bankOrderLineOriginService.createBankOrderLineOrigin(invoiceTerm));
  }

  protected String getLabel(PaymentSession paymentSession) {
    return String.format(
        "%s - %s",
        paymentSession.getPaymentMode().getName(), paymentSession.getCompany().getName());
  }

  protected String getReference(InvoiceTerm invoiceTerm) throws AxelorException {
    if (StringUtils.isEmpty(invoiceTerm.getMoveLine().getOrigin())) {
      return null;
    }
    return String.format(
        "%s (%s)",
        invoiceTerm.getMoveLine().getOrigin(),
        invoiceTerm.getDueDate().format(dateService.getDateFormat()));
  }

  protected void updateReference(InvoiceTerm invoiceTerm, BankOrderLine bankOrderLine)
      throws AxelorException {
    String newReference =
        String.format(
            "%s/%s", bankOrderLine.getReceiverReference(), this.getReference(invoiceTerm));

    if (newReference.length() < 256) {
      bankOrderLine.setReceiverReference(newReference);
    }
  }

  protected BigDecimal getAmountPaidInCompanyCurrency(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      BankOrderLine bankOrderLine,
      BigDecimal reconciledAmount)
      throws AxelorException {
    return bankOrderLine.getBankOrder().getIsMultiCurrency()
        ? currencyService
            .getAmountCurrencyConvertedAtDate(
                paymentSession.getCurrency(),
                paymentSession.getCompany().getCurrency(),
                invoiceTerm.getAmountPaid().subtract(reconciledAmount),
                bankOrderLine.getBankOrderDate())
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
        : invoiceTerm.getAmountPaid().subtract(reconciledAmount);
  }

  public StringBuilder generateFlashMessage(PaymentSession paymentSession, int moveCount) {
    StringBuilder flashMessage = super.generateFlashMessage(paymentSession, moveCount);

    if (paymentSession.getBankOrder() != null) {
      flashMessage.append(
          String.format(
              I18n.get(BankPaymentExceptionMessage.PAYMENT_SESSION_GENERATED_BANK_ORDER),
              paymentSession.getBankOrder().getBankOrderSeq()));
    }

    return flashMessage;
  }

  @Override
  public List<Partner> getPartnersWithNegativeAmount(PaymentSession paymentSession)
      throws AxelorException {
    TypedQuery<Partner> partnerQuery =
        JPA.em()
            .createQuery(
                "SELECT DISTINCT Partner FROM Partner Partner "
                    + " FULL JOIN MoveLine MoveLine on Partner.id = MoveLine.partner "
                    + " FULL JOIN InvoiceTerm InvoiceTerm on  MoveLine.id = InvoiceTerm.moveLine "
                    + " WHERE InvoiceTerm.paymentSession = :paymentSession "
                    + " AND InvoiceTerm.isSelectedOnPaymentSession = true "
                    + " GROUP BY Partner.id , InvoiceTerm.bankDetails "
                    + " HAVING SUM(InvoiceTerm.paymentAmount) < 0 ",
                Partner.class);

    partnerQuery.setParameter("paymentSession", paymentSession);

    return partnerQuery.getResultList();
  }

  @Transactional
  protected void resetBankOrderLine(BankOrderLine bankOrderLine) {
    if (bankOrderLine != null) {
      BankOrder bankOrder = bankOrderLine.getBankOrder();
      bankOrder.removeBankOrderLineListItem(bankOrderLine);
      bankOrderLine.setBankOrder(null);
      if (!ObjectUtils.isEmpty(bankOrderLine.getBankOrderLineOriginList())) {
        for (BankOrderLineOrigin origin : bankOrderLine.getBankOrderLineOriginList()) {
          origin.setBankOrderLine(null);
          bankOrderLine.removeBankOrderLineOriginListItem(origin);
        }
      }
    }
  }

  @Override
  public void createAndReconcileMoveLineFromPair(
      PaymentSession paymentSession,
      Move move,
      InvoiceTerm invoiceTerm,
      Pair<InvoiceTerm, BigDecimal> pair,
      AccountConfig accountConfig,
      boolean out,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    super.createAndReconcileMoveLineFromPair(
        paymentSession, move, invoiceTerm, pair, accountConfig, out, paymentAmountMap);

    manageInvoicePayment(paymentSession, invoiceTerm, pair.getRight());
  }

  @Transactional
  protected void manageInvoicePayment(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, BigDecimal reconciliedAmount) {
    InvoicePayment invoicePayment = this.findInvoicePayment(paymentSession, invoiceTerm);
    if (invoicePayment != null) {
      if (invoicePayment.getAmount().subtract(reconciliedAmount).signum() == 0) {
        Invoice invoice = invoicePayment.getInvoice();
        if (invoice != null) {
          invoice.removeInvoicePaymentListItem(invoicePayment);
          invoicePayment.setInvoice(null);
        }

        invoicePayment.setPaymentSession(null);
        invoicePayment.clearInvoiceTermPaymentList();
        invoicePayment.setBankOrder(null);
        invoicePayment.setMove(null);
        if (invoicePayment.getId() != null) {
          invoicePaymentRepo.remove(invoicePayment);
        }
      } else {
        invoicePayment.setAmount(invoicePayment.getAmount().subtract(reconciliedAmount));
      }
    }
  }
}
