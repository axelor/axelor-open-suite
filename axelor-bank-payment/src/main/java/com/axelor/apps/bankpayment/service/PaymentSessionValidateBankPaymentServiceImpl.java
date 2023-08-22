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

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
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
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateServiceImpl;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineRepository;
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
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
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
  protected BankOrderLineRepository bankOrderLineRepo;
  protected PaymentSessionBankOrderService paymentSessionBankOrderService;

  @Inject
  public PaymentSessionValidateBankPaymentServiceImpl(
      AppBaseService appBaseService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveComputeService moveComputeService,
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
      MoveInvoiceTermService moveInvoiceTermService,
      BankOrderService bankOrderService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderLineService bankOrderLineService,
      BankOrderLineOriginService bankOrderLineOriginService,
      BankOrderRepository bankOrderRepo,
      CurrencyService currencyService,
      AppAccountService appAccountService,
      InvoicePaymentRepository invoicePaymentRepo,
      DateService dateService,
      BankOrderLineRepository bankOrderLineRepo,
      PaymentSessionBankOrderService paymentSessionBankOrderService) {
    super(
        appBaseService,
        moveCreateService,
        moveValidateService,
        moveComputeService,
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
        moveInvoiceTermService);
    this.bankOrderService = bankOrderService;
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderLineService = bankOrderLineService;
    this.bankOrderLineOriginService = bankOrderLineOriginService;
    this.bankOrderRepo = bankOrderRepo;
    this.currencyService = currencyService;
    this.appAccountService = appAccountService;
    this.dateService = dateService;
    this.bankOrderLineRepo = bankOrderLineRepo;
    this.paymentSessionBankOrderService = paymentSessionBankOrderService;
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
      paymentSessionBankOrderService.createOrUpdateBankOrderLineFromInvoiceTerm(
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
  public boolean generatePaymentsFirst(PaymentSession paymentSession) {
    return super.generatePaymentsFirst(paymentSession)
        || (paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_ONGOING
            && paymentSession.getPaymentMode().getGenerateBankOrder()
            && paymentSession.getAccountingTriggerSelect()
                != PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE);
  }

  @Override
  @Transactional
  public InvoicePayment generatePendingPaymentFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    InvoicePayment invoicePayment =
        super.generatePendingPaymentFromInvoiceTerm(paymentSession, invoiceTerm);
    if (invoicePayment == null) {
      return null;
    }
    invoicePayment.setBankOrder(paymentSession.getBankOrder());

    return invoicePaymentRepo.save(invoicePayment);
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

  @Override
  public void createAndReconcileMoveLineFromPair(
      PaymentSession paymentSession,
      Move move,
      InvoiceTerm invoiceTerm,
      Pair<InvoiceTerm, BigDecimal> pair,
      boolean out,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    super.createAndReconcileMoveLineFromPair(
        paymentSession, move, invoiceTerm, pair, out, paymentAmountMap);

    paymentSessionBankOrderService.manageInvoicePayment(
        paymentSession, invoiceTerm, pair.getRight());
  }
}
