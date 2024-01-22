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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.shiro.util.CollectionUtils;

@RequestScoped
public class InvoicePaymentCreateServiceImpl implements InvoicePaymentCreateService {

  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected CurrencyService currencyService;
  protected AppBaseService appBaseService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceService invoiceService;
  protected InvoiceVisibilityService invoiceVisibilityService;

  @Inject
  public InvoicePaymentCreateServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentToolService invoicePaymentToolService,
      CurrencyService currencyService,
      AppBaseService appBaseService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermService invoiceTermService,
      InvoiceService invoiceService,
      InvoiceVisibilityService invoiceVisibilityService) {

    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.currencyService = currencyService;
    this.appBaseService = appBaseService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceService = invoiceService;
    this.invoiceVisibilityService = invoiceVisibilityService;
  }

  /**
   * @param amount
   * @param paymentDate
   * @param currency
   * @param paymentMode
   * @param invoice
   * @param typeSelect 1 : Advance Payment 2 : Payment 3 : Refund invoice 4 : Other
   * @return
   */
  public InvoicePayment createInvoicePayment(
      Invoice invoice,
      BigDecimal amount,
      LocalDate paymentDate,
      Currency currency,
      PaymentMode paymentMode,
      int typeSelect) {
    return new InvoicePayment(
        amount,
        paymentDate,
        currency,
        paymentMode,
        invoice,
        typeSelect,
        InvoicePaymentRepository.STATUS_DRAFT);
  }

  /**
   * @param invoice
   * @param amount
   * @param paymentMove
   * @return
   */
  public InvoicePayment createInvoicePayment(Invoice invoice, BigDecimal amount, Move paymentMove)
      throws AxelorException {

    LocalDate paymentDate = paymentMove.getDate();
    int typePaymentMove = this.determineType(paymentMove);

    Currency currency = paymentMove.getCurrency();
    if (currency == null) {
      currency = paymentMove.getCompanyCurrency();
    }

    PaymentMode paymentMode = null;
    InvoicePayment invoicePayment;

    if (typePaymentMove != InvoicePaymentRepository.TYPE_REFUND_INVOICE
        && typePaymentMove != InvoicePaymentRepository.TYPE_INVOICE) {
      paymentMode = paymentMove.getPaymentMode();
    }

    invoicePayment =
        this.createInvoicePayment(
            invoice, amount, paymentDate, currency, paymentMode, typePaymentMove);

    invoicePayment.setMove(paymentMove);
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);

    PaymentVoucher paymentVoucher = paymentMove.getPaymentVoucher();
    PaymentSession paymentSession = paymentMove.getPaymentSession();

    if (paymentVoucher != null) {
      invoicePayment.setCompanyBankDetails(paymentVoucher.getCompanyBankDetails());
    } else if (paymentSession != null) {
      invoicePayment.setCompanyBankDetails(paymentSession.getBankDetails());
    } else if (invoice.getSchedulePaymentOk() && invoice.getPaymentSchedule() != null) {
      BankDetails companyBankDetails = invoice.getPaymentSchedule().getCompanyBankDetails();
      invoicePayment.setCompanyBankDetails(companyBankDetails);
    }

    if (paymentVoucher != null
        && paymentMode != null
        && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_CHEQUE) {
      invoicePayment.setChequeNumber(paymentVoucher.getChequeNumber());
      invoicePayment.setDescription(paymentVoucher.getRef());
    }

    computeAdvancePaymentImputation(invoicePayment, paymentMove, invoice.getOperationTypeSelect());
    invoice.addInvoicePaymentListItem(invoicePayment);
    invoicePaymentToolService.updateAmountPaid(invoice);
    invoicePaymentRepository.save(invoicePayment);

    return invoicePayment;
  }

  protected int determineType(Move move) {

    Invoice invoice = move.getInvoice();
    if (move.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_IRRECOVERABLE) {
      return InvoicePaymentRepository.TYPE_IRRECOVERABLE_DEBT;
    }
    if (invoice != null) {
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
          || invoice.getOperationTypeSelect()
              == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
        return InvoicePaymentRepository.TYPE_INVOICE;
      } else {
        return InvoicePaymentRepository.TYPE_REFUND_INVOICE;
      }
    } else if (move.getPaymentVoucher() != null || move.getPaymentSession() != null) {
      return InvoicePaymentRepository.TYPE_PAYMENT;
    } else {
      return InvoicePaymentRepository.TYPE_OTHER;
    }
  }

  protected void computeAdvancePaymentImputation(
      InvoicePayment invoicePayment, Move paymentMove, int operationTypeSelect) {

    // check if the payment is an advance payment imputation
    Invoice advanceInvoice =
        findAvancePaymentInvoiceFromPaymentInvoice(paymentMove, operationTypeSelect);
    if (advanceInvoice != null) {
      List<InvoicePayment> invoicePaymentList = advanceInvoice.getInvoicePaymentList();
      if (invoicePaymentList != null && !invoicePaymentList.isEmpty()) {
        // set right type
        invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_ADV_PAYMENT_IMPUTATION);

        // create link between advance payment and its imputation
        InvoicePayment advancePayment = advanceInvoice.getInvoicePaymentList().get(0);
        advancePayment.setImputedBy(invoicePayment);
        invoicePaymentRepository.save(advancePayment);
      }
    }
  }

  /**
   * We try to get to the status of the invoice from the reconcile to see if this move was created
   * from a payment for an advance payment invoice.
   *
   * @param move
   * @return the found advance invoice if the move is from a payment that comes from this invoice.
   *     null in other cases
   */
  protected Invoice findAvancePaymentInvoiceFromPaymentInvoice(Move move, int operationTypeSelect) {
    List<MoveLine> moveLineList = move.getMoveLineList();
    if (moveLineList == null || moveLineList.size() < 2) {
      return null;
    }
    for (MoveLine moveLine : moveLineList) {
      // search for the reconcile between the debit line
      if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
          || moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
        Invoice invoice =
            findAdvancePaymentInvoiceFromPaymentMoveLine(moveLine, operationTypeSelect);
        if (invoice == null) {
          continue;
        } else {
          return invoice;
        }
      }
    }
    return null;
  }

  protected Invoice findAdvancePaymentInvoiceFromPaymentMoveLine(
      MoveLine moveLine, int operationTypeSelect) {
    Reconcile reconcile =
        findReconcileFromMoveLine(
            moveLine, operationTypeSelect != InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);
    if (reconcile == null) {
      return null;
    }
    // in the reconcile, search for the credit line to get the
    // associated payment
    MoveLine candidateMoveLine;
    if (operationTypeSelect == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
      candidateMoveLine = reconcile.getDebitMoveLine();
    } else {
      candidateMoveLine = reconcile.getCreditMoveLine();
    }

    if (candidateMoveLine == null || candidateMoveLine.getMove() == null) {
      return null;
    }
    Move candidatePaymentMove = candidateMoveLine.getMove();
    InvoicePayment invoicePayment =
        invoicePaymentRepository
            .all()
            .filter("self.move = :_move")
            .bind("_move", candidatePaymentMove)
            .fetchOne();
    // if the invoice linked to the payment is an advance
    // payment, then return true.
    if (invoicePayment != null
        && invoicePayment.getInvoice() != null
        && invoicePayment.getInvoice().getOperationSubTypeSelect()
            == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      return invoicePayment.getInvoice();
    }
    return null;
  }

  protected Reconcile findReconcileFromMoveLine(MoveLine moveLine, boolean fromDebitMoveLine) {
    StringBuilder filterString = new StringBuilder();
    if (fromDebitMoveLine) {
      filterString.append("self.debitMoveLine = ?");
    } else {
      filterString.append("self.creditMoveLine = ?");
    }
    Reconcile reconcile =
        Beans.get(ReconcileRepository.class)
            .all()
            .filter(filterString.toString(), moveLine)
            .fetchOne();
    return reconcile;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InvoicePayment createAndAddInvoicePayment(Invoice invoice, BankDetails companyBankDetails)
      throws AxelorException {
    InvoicePayment invoicePayment = this.createInvoicePayment(invoice, companyBankDetails, null);

    invoice.addInvoicePaymentListItem(invoicePayment);

    return invoicePayment;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InvoicePayment createInvoicePayment(
      Invoice invoice, BankDetails companyBankDetails, LocalDate paymentDate)
      throws AxelorException {
    InvoicePayment invoicePayment =
        createInvoicePayment(
            invoice,
            invoice.getInTaxTotal().subtract(invoice.getAmountPaid()),
            paymentDate == null ? appBaseService.getTodayDate(invoice.getCompany()) : paymentDate,
            invoice.getCurrency(),
            invoice.getPaymentMode(),
            InvoicePaymentRepository.TYPE_PAYMENT);
    invoice.addInvoicePaymentListItem(invoicePayment);
    invoicePayment.setCompanyBankDetails(companyBankDetails);
    invoiceTermPaymentService.createInvoicePaymentTerms(invoicePayment, null);
    invoicePayment = invoicePaymentRepository.save(invoicePayment);

    if (invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_PENDING) {
      invoiceTermService.updateInvoiceTermsPaidAmount(invoicePayment);
    }

    return invoicePayment;
  }

  @Transactional
  public InvoicePayment createInvoicePayment(
      List<InvoiceTerm> invoiceTermList,
      PaymentMode paymentMode,
      BankDetails companyBankDetails,
      LocalDate paymentDate,
      LocalDate bankDepositDate,
      String chequeNumber,
      PaymentSession paymentSession) {
    if (CollectionUtils.isEmpty(invoiceTermList)) {
      return null;
    }

    Invoice invoice = invoiceTermList.get(0).getInvoice();
    Currency currency = invoice.getCurrency();
    BigDecimal amountRemaining =
        invoiceTermList.stream()
            .map(InvoiceTerm::getAmountRemaining)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    InvoicePayment invoicePayment =
        createInvoicePayment(
            invoice,
            amountRemaining,
            paymentDate,
            currency,
            paymentMode,
            InvoicePaymentRepository.TYPE_PAYMENT);

    invoicePayment.setCompanyBankDetails(companyBankDetails);
    invoicePayment.setBankDepositDate(bankDepositDate);
    invoicePayment.setChequeNumber(chequeNumber);
    invoicePayment.setPaymentSession(paymentSession);
    invoicePayment.setManualChange(true);
    invoice.addInvoicePaymentListItem(invoicePayment);

    invoiceTermPaymentService.initInvoiceTermPayments(invoicePayment, invoiceTermList);
    invoicePaymentToolService.computeFinancialDiscount(invoicePayment);

    invoicePayment.setAmount(
        invoicePayment.getAmount().subtract(invoicePayment.getFinancialDiscountTotalAmount()));

    return invoicePaymentRepository.save(invoicePayment);
  }

  @Override
  public InvoicePayment createInvoicePayment(
      Invoice invoice,
      InvoiceTerm invoiceTerm,
      PaymentMode paymentMode,
      BankDetails companyBankDetails,
      LocalDate paymentDate,
      LocalDate bankDepositDate,
      String chequeNumber) {
    return this.createInvoicePayment(
        Collections.singletonList(invoiceTerm),
        paymentMode,
        companyBankDetails,
        paymentDate,
        bankDepositDate,
        chequeNumber,
        null);
  }

  @Override
  public InvoicePayment createInvoicePayment(
      Invoice invoice,
      InvoiceTerm invoiceTerm,
      PaymentMode paymentMode,
      BankDetails companyBankDetails,
      LocalDate paymentDate,
      PaymentSession paymentSession) {
    return this.createInvoicePayment(
        Collections.singletonList(invoiceTerm),
        paymentMode,
        companyBankDetails,
        paymentDate,
        null,
        null,
        paymentSession);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<InvoicePayment> createMassInvoicePayment(
      List<Long> invoiceList,
      PaymentMode paymentMode,
      BankDetails companyBankDetails,
      LocalDate paymentDate,
      LocalDate bankDepositDate,
      String chequeNumber)
      throws AxelorException {
    InvoiceRepository invoiceRepository = Beans.get(InvoiceRepository.class);
    List<InvoicePayment> invoicePaymentList = new ArrayList<>();

    for (Long invoiceId : invoiceList) {
      Invoice invoice = invoiceRepository.find(invoiceId);

      this.createInvoicePayment(
          invoice,
          invoicePaymentList,
          paymentMode,
          companyBankDetails,
          paymentDate,
          bankDepositDate,
          chequeNumber,
          false);
      this.createInvoicePayment(
          invoice,
          invoicePaymentList,
          paymentMode,
          companyBankDetails,
          paymentDate,
          bankDepositDate,
          chequeNumber,
          true);

      invoicePaymentToolService.updateAmountPaid(invoice);
    }

    return invoicePaymentList;
  }

  public void createInvoicePayment(
      Invoice invoice,
      List<InvoicePayment> invoicePaymentList,
      PaymentMode paymentMode,
      BankDetails companyBankDetails,
      LocalDate paymentDate,
      LocalDate bankDepositDate,
      String chequeNumber,
      boolean holdback) {
    List<InvoiceTerm> invoiceTermList =
        invoice.getInvoiceTermList().stream()
            .filter(
                it ->
                    !it.getIsPaid()
                        && it.getAmountRemaining().signum() > 0
                        && it.getIsHoldBack() == holdback)
            .collect(Collectors.toList());

    InvoicePayment invoicePayment =
        this.createInvoicePayment(
            invoiceTermList,
            paymentMode,
            companyBankDetails,
            paymentDate,
            bankDepositDate,
            chequeNumber,
            null);

    if (invoicePayment != null) {
      invoicePaymentList.add(invoicePayment);

      if (!invoice.getInvoicePaymentList().contains(invoicePayment)) {
        invoice.addInvoicePaymentListItem(invoicePayment);
      }
    }
  }

  @Override
  public List<Long> getInvoiceIdsToPay(List<Long> invoiceIdList) throws AxelorException {
    Company company = null;
    Currency currency = null;
    List<Long> invoiceToPay = new ArrayList<>();

    for (Long invoiceId : invoiceIdList) {
      Invoice invoice = Beans.get(InvoiceRepository.class).find(invoiceId);

      if ((invoice.getStatusSelect() != InvoiceRepository.STATUS_VENTILATED
              && invoice.getOperationSubTypeSelect()
                  != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
          || (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
              && invoice.getStatusSelect() != InvoiceRepository.STATUS_VALIDATED)) {

        continue;
      }

      if (invoice.getAmountRemaining().compareTo(BigDecimal.ZERO) <= 0
          || !invoiceService.checkInvoiceTerms(invoice)) {

        continue;
      }

      if (company == null) {
        company = invoice.getCompany();
      }

      if (currency == null) {
        currency = invoice.getCurrency();
      }

      if (invoice.getCompany() == null || !invoice.getCompany().equals(company)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_MERGE_ERROR_COMPANY));
      }

      if (invoice.getCurrency() == null || !invoice.getCurrency().equals(currency)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_MERGE_ERROR_CURRENCY));
      }

      if (invoiceVisibilityService.getPfpCondition(invoice)
          && invoice.getPfpValidateStatusSelect() != InvoiceRepository.PFP_STATUS_VALIDATED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_MASS_PAYMENT_ERROR_PFP_LITIGATION));
      }

      invoiceToPay.add(invoiceId);
    }

    return invoiceToPay;
  }
}
