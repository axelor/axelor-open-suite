/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.account.service.reconcile.foreignexchange.ForeignExchangeGapToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentVoucherConfirmService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ReconcileService reconcileService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveCutOffService moveCutOffService;
  protected MoveLineCreateService moveLineCreateService;
  protected PaymentService paymentService;
  protected PaymentModeService paymentModeService;
  protected PaymentVoucherSequenceService paymentVoucherSequenceService;
  protected PaymentVoucherControlService paymentVoucherControlService;
  protected PaymentVoucherToolService paymentVoucherToolService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected PayVoucherElementToPayRepository payVoucherElementToPayRepo;
  protected PaymentVoucherRepository paymentVoucherRepository;
  protected CurrencyService currencyService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermRepository invoiceTermRepository;
  protected MoveLineFinancialDiscountService moveLineFinancialDiscountService;
  protected FinancialDiscountService financialDiscountService;
  protected CurrencyScaleService currencyScaleService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected ForeignExchangeGapToolService foreignExchangeGapToolService;
  protected AppBaseService appBaseService;

  @Inject
  public PaymentVoucherConfirmService(
      ReconcileService reconcileService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveCutOffService moveCutOffService,
      MoveLineCreateService moveLineCreateService,
      PaymentService paymentService,
      PaymentModeService paymentModeService,
      PaymentVoucherSequenceService paymentVoucherSequenceService,
      PaymentVoucherControlService paymentVoucherControlService,
      PaymentVoucherToolService paymentVoucherToolService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      PayVoucherElementToPayRepository payVoucherElementToPayRepo,
      PaymentVoucherRepository paymentVoucherRepository,
      CurrencyService currencyService,
      InvoiceTermService invoiceTermService,
      InvoiceTermRepository invoiceTermRepository,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      FinancialDiscountService financialDiscountService,
      CurrencyScaleService currencyScaleService,
      InvoicePaymentRepository invoicePaymentRepository,
      ForeignExchangeGapToolService foreignExchangeGapToolService,
      AppBaseService appBaseService) {

    this.reconcileService = reconcileService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveCutOffService = moveCutOffService;
    this.moveLineCreateService = moveLineCreateService;
    this.paymentService = paymentService;
    this.paymentModeService = paymentModeService;
    this.paymentVoucherSequenceService = paymentVoucherSequenceService;
    this.paymentVoucherControlService = paymentVoucherControlService;
    this.paymentVoucherToolService = paymentVoucherToolService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.payVoucherElementToPayRepo = payVoucherElementToPayRepo;
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.currencyService = currencyService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermRepository = invoiceTermRepository;
    this.moveLineFinancialDiscountService = moveLineFinancialDiscountService;
    this.financialDiscountService = financialDiscountService;
    this.currencyScaleService = currencyScaleService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.foreignExchangeGapToolService = foreignExchangeGapToolService;
    this.appBaseService = appBaseService;
  }

  /**
   * Confirms the payment voucher if the selected lines PiToPay 2nd O2M belongs to different
   * companies -> error I - Payment with an amount If we pay a classical moveLine (invoice, reject
   * ..) -> just create a payment If we pay a schedule 2 payments are created 1st reconciled with
   * the invoice and the second reconciled with the schedule II - Payment with an excess Payment If
   * we pay a moveLine having the same account, we just reconcile If we pay a with different account
   * -> 1- switch money to the good account 2- reconcile then
   *
   * @param paymentVoucher
   */
  @Transactional(rollbackOn = {Exception.class})
  public void confirmPaymentVoucher(PaymentVoucher paymentVoucher) throws AxelorException {
    log.debug("In confirmPaymentVoucherService ....");
    this.checkInvoiceTermsPfpStatus(paymentVoucher);
    paymentVoucherSequenceService.setReference(paymentVoucher);

    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    Company company = paymentVoucher.getCompany();
    BankDetails companyBankDetails = paymentVoucher.getCompanyBankDetails();

    boolean isPaymentValueForCollection = isPaymentValueForCollection(paymentVoucher);
    Journal journal =
        paymentModeService.getPaymentModeJournal(
            paymentMode, company, companyBankDetails, isPaymentValueForCollection);
    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(
            paymentMode, company, companyBankDetails, isPaymentValueForCollection);

    paymentVoucherControlService.checkPaymentVoucherField(
        paymentVoucher, company, paymentModeAccount, journal);

    if (paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
        && !journal.getExcessPaymentOk()) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_AMOUNT_EXCEEDING),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    // TODO VEIRIFER QUE LES ELEMENTS A PAYER NE CONCERNE QU'UNE SEULE DEVISE

    // TODO RECUPERER DEVISE DE LA PREMIERE DETTE
    // Currency currencyToPay = null;

    createMoveAndConfirm(paymentVoucher, journal, paymentModeAccount, isPaymentValueForCollection);

    paymentVoucherSequenceService.setReceiptNo(paymentVoucher, company, journal);
    paymentVoucherRepository.save(paymentVoucher);
  }

  protected void checkInvoiceTermsPfpStatus(PaymentVoucher paymentVoucher) throws AxelorException {
    if (paymentVoucher.getPayVoucherElementToPayList().stream()
        .map(PayVoucherElementToPay::getInvoiceTerm)
        .filter(
            it -> {
              try {
                return invoiceTermService.getPfpValidatorUserCondition(
                    it.getInvoice(), it.getMoveLine());
              } catch (AxelorException e) {
                throw new RuntimeException(e);
              }
            })
        .anyMatch(
            it ->
                it.getPfpValidateStatusSelect() != InvoiceTermRepository.PFP_STATUS_VALIDATED
                    && it.getPfpValidateStatusSelect()
                        != InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_PFP_NOT_VALIDATED));
    }
  }

  protected void invoiceSetHasPendingPayments(PaymentVoucher paymentVoucher) {

    paymentVoucher.getPayVoucherElementToPayList().stream()
        .forEach(payVoucherElementToPay -> invoiceSetHasPendingPayment(payVoucherElementToPay));
  }

  protected void invoiceSetHasPendingPayment(PayVoucherElementToPay payVoucherElementToPay) {

    Invoice invoice = payVoucherElementToPay.getMoveLine().getMove().getInvoice();
    boolean hasPendingPayments =
        payVoucherElementToPay.getRemainingAmountAfterPayment().signum() <= 0;
    invoice.setHasPendingPayments(hasPendingPayments);
  }

  protected boolean isPaymentValueForCollection(PaymentVoucher paymentVoucher) {

    if (paymentVoucher.getBankEntryGenWithoutValEntryCollectionOk()) {
      return false;
    }

    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    if (!Objects.equals(
        PaymentModeRepository.ACCOUNTING_TRIGGER_VALUE_FOR_COLLECTION,
        paymentMode.getAccountingTriggerSelect())) {
      return false;
    }

    return true;
  }

  public void createMoveAndConfirm(PaymentVoucher paymentVoucher) throws AxelorException {

    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    Company company = paymentVoucher.getCompany();
    BankDetails companyBankDetails = getBankDetails(paymentVoucher);

    boolean isPaymentValueForCollection = isPaymentValueForCollection(paymentVoucher);
    Journal journal =
        paymentModeService.getPaymentModeJournal(
            paymentMode, company, companyBankDetails, isPaymentValueForCollection);
    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(
            paymentMode, company, companyBankDetails, isPaymentValueForCollection);

    createMoveAndConfirm(paymentVoucher, journal, paymentModeAccount, isPaymentValueForCollection);
  }

  public void createMoveAndConfirm(
      PaymentVoucher paymentVoucher, boolean isPaymentValueForCollection) throws AxelorException {

    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    Company company = paymentVoucher.getCompany();
    BankDetails companyBankDetails = getBankDetails(paymentVoucher);

    Journal journal =
        paymentModeService.getPaymentModeJournal(
            paymentMode, company, companyBankDetails, isPaymentValueForCollection);
    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(
            paymentMode, company, companyBankDetails, isPaymentValueForCollection);

    createMoveAndConfirm(paymentVoucher, journal, paymentModeAccount, isPaymentValueForCollection);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void valueForCollectionMoveToGeneratedMove(PaymentVoucher paymentVoucher, LocalDate date)
      throws AxelorException {

    Move valueForCollectionMove = paymentVoucher.getValueForCollectionMove();
    if (Objects.isNull(valueForCollectionMove)) {
      return;
    }

    Account valueForCollectionAccount = paymentVoucher.getValueForCollectionAccount();
    Optional<MoveLine> optionalValueForCollectionMoveLine =
        extractMoveLineFromValueForCollectionMove(
            valueForCollectionMove, valueForCollectionAccount);
    if (!optionalValueForCollectionMoveLine.isPresent()) {
      return;
    }

    Partner payerPartner = paymentVoucher.getPartner();
    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    Company company = paymentVoucher.getCompany();
    // use depositBankDetails if not null
    BankDetails bankDetails =
        paymentVoucher.getDepositBankDetails() != null
            ? paymentVoucher.getDepositBankDetails()
            : paymentVoucher.getCompanyBankDetails();
    Journal journal =
        paymentModeService.getPaymentModeJournal(paymentMode, company, bankDetails, false);
    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(paymentMode, company, bankDetails, false);

    Move move =
        moveCreateService.createMoveWithPaymentVoucher(
            journal,
            company,
            paymentVoucher,
            payerPartner,
            date,
            paymentVoucher.getPaymentMode(),
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            paymentVoucher.getRef(),
            journal.getDescriptionIdentificationOk() ? journal.getDescriptionModel() : null,
            bankDetails);

    move.setPaymentVoucher(paymentVoucher);
    move.setTradingName(paymentVoucher.getTradingName());
    setMove(paymentVoucher, move, false);

    int moveLineNo = 1;

    boolean isDebitToPay = paymentVoucherToolService.isDebitToPay(paymentVoucher);
    MoveLine moveLineToPaymentModeAccount =
        moveLineCreateService.createMoveLine(
            move,
            payerPartner,
            paymentModeAccount,
            paymentVoucher.getPaidAmount(),
            isDebitToPay,
            date,
            moveLineNo++,
            paymentVoucher.getRef(),
            null);

    move.addMoveLineListItem(moveLineToPaymentModeAccount);

    MoveLine moveLineToValueForCollectionAccount =
        moveLineCreateService.createMoveLine(
            move,
            payerPartner,
            valueForCollectionAccount,
            paymentVoucher.getPaidAmount(),
            !isDebitToPay,
            date,
            moveLineNo++,
            paymentVoucher.getRef(),
            null);

    move.addMoveLineListItem(moveLineToValueForCollectionAccount);

    MoveLine valueForCollectionMoveLine = optionalValueForCollectionMoveLine.get();
    Reconcile reconcile;

    if (isDebitToPay) {
      reconcile =
          reconcileService.createReconcile(
              valueForCollectionMoveLine,
              moveLineToValueForCollectionAccount,
              paymentVoucher.getPaidAmount(),
              true);
    } else {
      reconcile =
          reconcileService.createReconcile(
              moveLineToValueForCollectionAccount,
              valueForCollectionMoveLine,
              paymentVoucher.getPaidAmount(),
              true);
    }
    if (reconcile != null) {
      reconcileService.confirmReconcile(reconcile, true, true);
    }

    moveCutOffService.autoApplyCutOffDates(move);
    moveValidateService.accounting(move);
  }

  protected Optional<MoveLine> extractMoveLineFromValueForCollectionMove(
      Move move, Account account) {
    Predicate<? super MoveLine> filter;
    if (account == null) {
      filter =
          moveLine ->
              Objects.equals(
                  AccountTypeRepository.TYPE_CASH,
                  Optional.ofNullable(moveLine)
                      .map(MoveLine::getAccount)
                      .map(Account::getAccountType)
                      .map(AccountType::getTechnicalTypeSelect)
                      .orElse(""));
    } else {
      filter = moveLine -> Objects.equals(account, moveLine.getAccount());
    }
    return move.getMoveLineList().stream().filter(filter).findFirst();
  }

  /**
   * Confirm payment voucher and create move.
   *
   * @param paymentVoucher
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void createMoveAndConfirm(
      PaymentVoucher paymentVoucher,
      Journal journal,
      Account paymentModeAccount,
      boolean valueForCollection)
      throws AxelorException {

    boolean scheduleToBePaid = false;
    boolean hasFinancialDiscount = false;
    Partner payerPartner = paymentVoucher.getPartner();
    Company company = paymentVoucher.getCompany();
    LocalDate paymentDate = paymentVoucher.getPaymentDate();

    // If paid by a moveline check if all the lines selected have the same account +
    // company
    // Excess payment
    boolean allRight =
        paymentVoucherControlService.checkIfSameAccount(
            paymentVoucher.getPayVoucherElementToPayList(), paymentVoucher.getMoveLine());

    // Check if allright=true (means companies and accounts in lines are all the
    // same and same as in
    // move line selected for paying
    log.debug("allRight : {}", allRight);

    if (allRight) {
      scheduleToBePaid =
          this.toPayWithExcessPayment(
              paymentVoucher.getPayVoucherElementToPayList(),
              paymentVoucher.getMoveLine(),
              scheduleToBePaid,
              paymentDate);
    }

    if (paymentVoucher.getMoveLine() == null
        || (paymentVoucher.getMoveLine() != null && !allRight)
        || (scheduleToBePaid && !allRight && paymentVoucher.getMoveLine() != null)) {

      // Manage all the cases in the same way. As if a move line (Excess payment) is
      // selected, we
      // cancel it first
      Move move =
          moveCreateService.createMoveWithPaymentVoucher(
              journal,
              company,
              paymentVoucher,
              payerPartner,
              paymentDate,
              paymentVoucher.getPaymentMode(),
              null,
              MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
              MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
              paymentVoucher.getRef(),
              journal.getDescriptionIdentificationOk() ? journal.getDescriptionModel() : null,
              paymentVoucher.getCompanyBankDetails());

      move.setPaymentVoucher(paymentVoucher);
      move.setTradingName(paymentVoucher.getTradingName());
      setMove(paymentVoucher, move, valueForCollection);
      // Create move lines for payment lines
      BigDecimal paidLineTotal = BigDecimal.ZERO;
      BigDecimal financialDiscountTotalAmount = BigDecimal.ZERO;
      BigDecimal companyPaidAmount = BigDecimal.ZERO;
      int moveLineNo = 1;

      boolean isDebitToPay = paymentVoucherToolService.isDebitToPay(paymentVoucher);

      for (PayVoucherElementToPay payVoucherElementToPay :
          this.getPayVoucherElementToPayList(paymentVoucher)) {
        MoveLine moveLineToPay = payVoucherElementToPay.getMoveLine();
        log.debug("PV moveLineToPay debit : {}", moveLineToPay.getDebit());
        log.debug("PV moveLineToPay amountPaid : {}", moveLineToPay.getAmountPaid());

        BigDecimal amountToPay = payVoucherElementToPay.getAmountToPayCurrency();
        if (payVoucherElementToPay.getApplyFinancialDiscount()) {
          amountToPay =
              amountToPay
                  .add(payVoucherElementToPay.getFinancialDiscountAmount())
                  .add(payVoucherElementToPay.getFinancialDiscountTaxAmount());
        }

        if (amountToPay.compareTo(BigDecimal.ZERO) > 0) {
          paidLineTotal = paidLineTotal.add(amountToPay);
          MoveLine payMoveLine =
              this.payMoveLine(
                  move,
                  moveLineNo++,
                  payerPartner,
                  moveLineToPay,
                  amountToPay,
                  payVoucherElementToPay,
                  isDebitToPay,
                  paymentDate);

          companyPaidAmount =
              companyPaidAmount.add(payMoveLine.getDebit().max(payMoveLine.getCredit()));

          if (payVoucherElementToPay.getApplyFinancialDiscount()
              && payVoucherElementToPay.getFinancialDiscount() != null) {
            hasFinancialDiscount = true;
            boolean financialDiscountVat =
                payVoucherElementToPay.getFinancialDiscountTaxAmount().signum() > 0;

            moveLineNo =
                this.createFinancialDiscountMoveLines(
                    move,
                    paymentVoucher,
                    payVoucherElementToPay,
                    company,
                    payerPartner,
                    moveLineToPay,
                    payVoucherElementToPay.getFinancialDiscountAmount(),
                    paymentDate,
                    moveLineNo,
                    isDebitToPay,
                    financialDiscountVat);

            BigDecimal financialDiscountAmount =
                payVoucherElementToPay
                    .getFinancialDiscountAmount()
                    .add(payVoucherElementToPay.getFinancialDiscountTaxAmount());

            financialDiscountTotalAmount =
                financialDiscountTotalAmount.add(financialDiscountAmount);
            paidLineTotal = paidLineTotal.subtract(financialDiscountAmount);
          }
        }
      }
      // Create move line for the payment amount
      MoveLine moveLine = null;

      BigDecimal currencyRate =
          ObjectUtils.isEmpty(move.getMoveLineList())
              ? currencyService.getCurrencyConversionRate(
                  paymentVoucher.getCurrency(),
                  company.getCurrency(),
                  appBaseService.getTodayDate(company))
              : move.getMoveLineList().get(0).getCurrencyRate();
      // cancelling the moveLine (excess payment) by creating the balance of all the
      // payments
      // on the same account as the moveLine (excess payment)
      // in the else case we create a classical balance on the bank account of the
      // payment mode

      if (paymentVoucher.getRemainingAmount().signum() > 0) {
        companyPaidAmount =
            companyPaidAmount.add(
                currencyService.getAmountCurrencyConvertedUsingExchangeRate(
                    paymentVoucher.getRemainingAmount(), currencyRate, company.getCurrency()));
      }

      if (hasFinancialDiscount) {
        companyPaidAmount =
            companyPaidAmount.subtract(
                currencyService.getAmountCurrencyConvertedUsingExchangeRate(
                    financialDiscountTotalAmount, currencyRate, company.getCurrency()));
      }

      if (paymentVoucher.getMoveLine() != null) {
        moveLine =
            moveLineCreateService.createMoveLine(
                move,
                paymentVoucher.getPartner(),
                paymentVoucher.getMoveLine().getAccount(),
                paymentVoucher.getPaidAmount(),
                companyPaidAmount,
                currencyRate,
                isDebitToPay,
                paymentDate,
                null,
                paymentDate,
                moveLineNo++,
                paymentVoucher.getRef(),
                null);
        Reconcile reconcile =
            reconcileService.createReconcile(
                moveLine, paymentVoucher.getMoveLine(), moveLine.getDebit(), !isDebitToPay);
        if (reconcile != null) {
          reconcileService.confirmReconcile(reconcile, true, true);
        }
      } else {

        moveLine =
            moveLineCreateService.createMoveLine(
                move,
                payerPartner,
                paymentModeAccount,
                paymentVoucher.getPaidAmount(),
                companyPaidAmount,
                currencyRate,
                isDebitToPay,
                paymentDate,
                null,
                paymentDate,
                moveLineNo++,
                paymentVoucher.getRef(),
                null);
      }
      move.getMoveLineList().add(moveLine);
      // Check if the paid amount is > paid lines total
      // Then Use Excess payment on old invoices / moveLines
      if (paymentVoucher.getPaidAmount().compareTo(paidLineTotal) > 0) {
        BigDecimal remainingPaidAmount = paymentVoucher.getRemainingAmount();

        // TODO rajouter le process d'imputation automatique
        // if(paymentVoucher.getHasAutoInput()) {
        //
        // List<MoveLine> debitMoveLines =
        // Lists.newArrayList(pas.getDebitLinesToPay(contractLine,
        // paymentVoucher.getPaymentScheduleToPay()));
        // pas.createExcessPaymentWithAmount(debitMoveLines, remainingPaidAmount,
        // move, moveLineNo,
        // paymentVoucher.getPayerPartner(), company, contractLine, null,
        // paymentDate, updateCustomerAccount);
        // }
        // else {

        Account partnerAccount =
            Beans.get(AccountCustomerService.class)
                .getPartnerAccount(
                    payerPartner, company, paymentVoucherToolService.isPurchase(paymentVoucher));

        moveLine =
            moveLineCreateService.createMoveLine(
                move,
                paymentVoucher.getPartner(),
                partnerAccount,
                remainingPaidAmount,
                !isDebitToPay,
                paymentDate,
                moveLineNo++,
                paymentVoucher.getRef(),
                null);
        move.getMoveLineList().add(moveLine);

        if (isDebitToPay) {
          reconcileService.canBeZeroBalance(null, moveLine);
          // reconcileService.balanceCredit(moveLine);
        }
      }
      moveCutOffService.autoApplyCutOffDates(move);
      moveValidateService.accounting(move);
      setMove(paymentVoucher, move, valueForCollection);
    }
    paymentVoucher.setStatusSelect(PaymentVoucherRepository.STATUS_CONFIRMED);

    deleteUnPaidLines(paymentVoucher);
  }

  protected void setMove(PaymentVoucher paymentVoucher, Move move, boolean valueForCollection) {
    if (valueForCollection) {
      paymentVoucher.setValueForCollectionMove(move);
      return;
    }
    paymentVoucher.setGeneratedMove(move);
  }

  /*
   * Get companyBankDetails from paymentVoucher. If companyBankDetails is null,
   * get default from company, paymentMode, partner and operationTypeSelect and
   * fill companyBankDetails in paymentVoucher.
   *
   * @return
   */
  protected BankDetails getBankDetails(PaymentVoucher paymentVoucher) throws AxelorException {

    BankDetails companyBankDetails = paymentVoucher.getCompanyBankDetails();
    if (Objects.nonNull(companyBankDetails)) {
      return companyBankDetails;
    }

    companyBankDetails =
        Beans.get(BankDetailsService.class)
            .getDefaultCompanyBankDetails(
                paymentVoucher.getCompany(),
                paymentVoucher.getPaymentMode(),
                paymentVoucher.getPartner(),
                paymentVoucher.getOperationTypeSelect());
    paymentVoucher.setCompanyBankDetails(companyBankDetails);

    return companyBankDetails;
  }

  protected int createFinancialDiscountMoveLines(
      Move move,
      PaymentVoucher paymentVoucher,
      PayVoucherElementToPay payVoucherElementToPay,
      Company company,
      Partner payerPartner,
      MoveLine moveLineToPay,
      BigDecimal financialDiscountAmount,
      LocalDate paymentDate,
      int moveLineNo,
      boolean isDebitToPay,
      boolean financialDiscountVat)
      throws AxelorException {
    boolean isPurchase =
        paymentVoucher.getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || paymentVoucher.getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_REFUND;
    Account financialDiscountAccount =
        financialDiscountService.getFinancialDiscountAccount(company, isPurchase);
    String invoiceName = this.getInvoiceName(moveLineToPay, payVoucherElementToPay);
    Map<String, Pair<BigDecimal, BigDecimal>> financialDiscountTaxMap =
        moveLineFinancialDiscountService.getFinancialDiscountTaxMap(moveLineToPay);
    Map<String, Integer> vatSystemTaxMap =
        moveLineFinancialDiscountService.getVatSystemTaxMap(moveLineToPay.getMove());
    Map<String, Account> accountTaxMap =
        moveLineFinancialDiscountService.getAccountTaxMap(moveLineToPay.getMove());

    moveLineFinancialDiscountService.createFinancialDiscountMoveLine(
        move,
        payerPartner,
        financialDiscountTaxMap,
        vatSystemTaxMap,
        accountTaxMap,
        financialDiscountAccount,
        invoiceName,
        null,
        financialDiscountAmount,
        payVoucherElementToPay.getFinancialDiscountTaxAmount(),
        paymentDate,
        moveLineNo,
        isDebitToPay,
        financialDiscountVat);

    moveCutOffService.autoApplyCutOffDates(move);

    return moveLineNo;
  }

  public void deleteUnPaidLines(PaymentVoucher paymentVoucher) {

    if (paymentVoucher.getPayVoucherElementToPayList() == null) {
      return;
    }

    paymentVoucher.getPayVoucherDueElementList().clear();

    List<PayVoucherElementToPay> payVoucherElementToPayToRemove = new ArrayList<>();

    for (PayVoucherElementToPay payVoucherElementToPay :
        paymentVoucher.getPayVoucherElementToPayList()) {

      if (payVoucherElementToPay.getAmountToPay().compareTo(BigDecimal.ZERO) == 0
          && payVoucherElementToPay.getMoveLineGenerated() == null) {

        payVoucherElementToPayToRemove.add(payVoucherElementToPay);
      }
    }

    paymentVoucher.getPayVoucherElementToPayList().removeAll(payVoucherElementToPayToRemove);
  }

  /**
   * Récupérer les éléments à payer dans le bon ordre
   *
   * @return
   */
  public List<? extends PayVoucherElementToPay> getPayVoucherElementToPayList(
      PaymentVoucher paymentVoucher) {

    return payVoucherElementToPayRepo
        .all()
        .filter("self.paymentVoucher = ?1 ORDER by self.sequence ASC", paymentVoucher)
        .fetch();
  }

  /**
   * If paid by a moveline check if all the lines selected have the same account + company Excess
   * payment Check if allright=true (means companies and accounts in lines are all the same and same
   * as in move line selected for paying
   *
   * @param payVoucherElementToPayList Liste des paiement a réaliser
   * @param creditMoveLine Le trop-perçu
   * @param scheduleToBePaid
   * @return Une échéance doit-elle être payée?
   * @throws AxelorException
   */
  public boolean toPayWithExcessPayment(
      List<PayVoucherElementToPay> payVoucherElementToPayList,
      MoveLine creditMoveLine,
      boolean scheduleToBePaid,
      LocalDate paymentDate)
      throws AxelorException {
    boolean scheduleToBePaid2 = scheduleToBePaid;

    List<MoveLine> debitMoveLines = new ArrayList<MoveLine>();
    for (PayVoucherElementToPay payVoucherElementToPay : payVoucherElementToPayList) {

      debitMoveLines.add(payVoucherElementToPay.getMoveLine());
    }
    List<MoveLine> creditMoveLines = new ArrayList<MoveLine>();
    creditMoveLines.add(creditMoveLine);
    paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLines);
    return scheduleToBePaid2;
  }

  /**
   * @param paymentMove
   * @param moveLineSeq
   * @param payerPartner
   * @param moveLineToPay
   * @param amountToPay
   * @param payVoucherElementToPay
   * @return
   * @throws AxelorException
   */
  public MoveLine payMoveLine(
      Move paymentMove,
      int moveLineSeq,
      Partner payerPartner,
      MoveLine moveLineToPay,
      BigDecimal amountToPay,
      PayVoucherElementToPay payVoucherElementToPay,
      boolean isDebitToPay,
      LocalDate paymentDate)
      throws AxelorException {
    String invoiceName = this.getInvoiceName(moveLineToPay, payVoucherElementToPay);

    InvoiceTerm invoiceTerm = payVoucherElementToPay.getInvoiceTerm();
    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(
            invoiceTerm.getCurrency(), invoiceTerm.getCompanyCurrency(), paymentDate);
    BigDecimal currencyAmount = payVoucherElementToPay.getAmountToPayCurrency();

    if (payVoucherElementToPay.getApplyFinancialDiscount()) {
      currencyAmount = currencyAmount.add(payVoucherElementToPay.getFinancialDiscountTotalAmount());
    }

    BigDecimal invoiceTermCurrencyRate =
        currencyService.getCurrencyConversionRate(
            invoiceTerm.getCurrency(),
            invoiceTerm.getCompanyCurrency(),
            Optional.of(invoiceTerm)
                .map(InvoiceTerm::getInvoice)
                .map(Invoice::getInvoiceDate)
                .orElse(Optional.ofNullable(moveLineToPay).map(MoveLine::getDate).orElse(null)));
    BigDecimal companyAmountToPay =
        currencyScaleService.getCompanyScaledValue(
            payVoucherElementToPay.getPaymentVoucher(), currencyAmount.multiply(currencyRate));

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            paymentMove,
            payerPartner,
            moveLineToPay.getAccount(),
            amountToPay,
            companyAmountToPay,
            currencyRate,
            !isDebitToPay,
            paymentDate,
            paymentDate,
            paymentDate,
            moveLineSeq,
            invoiceName,
            null);

    paymentMove.addMoveLineListItem(moveLine);
    payVoucherElementToPay.setMoveLineGenerated(moveLine);
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(
        paymentMove, moveLine, paymentDate, false);

    companyAmountToPay =
        this.computeForeignExchangeCompanyAmount(
            currencyRate,
            invoiceTermCurrencyRate,
            companyAmountToPay,
            currencyAmount,
            invoiceTerm,
            moveLineToPay.getMove().getCompany());

    Reconcile reconcile =
        reconcileService.createReconcile(moveLineToPay, moveLine, companyAmountToPay, true);
    if (reconcile != null) {
      log.debug("Reconcile : : : {}", reconcile);
      reconcileService.confirmReconcile(reconcile, true, true);

      manageFinancialDiscountOnInvoicePayment(
          reconcile,
          Optional.of(moveLineToPay).map(MoveLine::getMove).map(Move::getInvoice).orElse(null),
          payVoucherElementToPay);
    }

    return moveLine;
  }

  protected String getInvoiceName(
      MoveLine moveLineToPay, PayVoucherElementToPay payVoucherElementToPay) {
    if (moveLineToPay.getMove().getInvoice() != null) {
      return moveLineToPay.getMove().getInvoice().getInvoiceId();
    } else {
      return payVoucherElementToPay.getPaymentVoucher().getRef();
    }
  }

  protected void manageFinancialDiscountOnInvoicePayment(
      Reconcile reconcile, Invoice invoice, PayVoucherElementToPay payVoucherElementToPay) {
    if (invoice == null
        || payVoucherElementToPay == null
        || !payVoucherElementToPay.getApplyFinancialDiscount()) {
      return;
    }

    InvoicePayment invoicePayment =
        invoicePaymentRepository.findByReconcileAndInvoice(reconcile, invoice);
    if (invoicePayment == null) {
      return;
    }

    invoicePayment.setApplyFinancialDiscount(true);
    invoicePayment.setFinancialDiscount(payVoucherElementToPay.getFinancialDiscount());
    invoicePayment.setFinancialDiscountTotalAmount(
        payVoucherElementToPay.getFinancialDiscountTotalAmount());
    invoicePayment.setFinancialDiscountTaxAmount(
        payVoucherElementToPay.getFinancialDiscountTaxAmount());
    invoicePayment.setFinancialDiscountAmount(payVoucherElementToPay.getFinancialDiscountAmount());
    invoicePayment.setAmount(
        invoicePayment.getAmount().subtract(invoicePayment.getFinancialDiscountTotalAmount()));

    if (ObjectUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      return;
    }

    LocalDate deadlineDate =
        invoicePayment.getInvoiceTermPaymentList().stream()
            .map(InvoiceTermPayment::getInvoiceTerm)
            .map(InvoiceTerm::getFinancialDiscountDeadlineDate)
            .min(LocalDate::compareTo)
            .orElse(null);
    invoicePayment.setFinancialDiscountDeadlineDate(deadlineDate);
  }

  protected BigDecimal computeForeignExchangeCompanyAmount(
      BigDecimal paymentCurrencyRate,
      BigDecimal invoiceTermCurrencyRate,
      BigDecimal companyAmount,
      BigDecimal currencyAmount,
      InvoiceTerm invoiceTerm,
      Company company)
      throws AxelorException {
    if (paymentCurrencyRate.compareTo(invoiceTermCurrencyRate) > 0
        && !foreignExchangeGapToolService.checkForeignExchangeAccounts(company)) {
      return currencyService.getAmountCurrencyConvertedAtDate(
          invoiceTerm.getCurrency(),
          invoiceTerm.getCompanyCurrency(),
          currencyAmount,
          invoiceTerm.getInvoice().getInvoiceDate());
    }

    return companyAmount;
  }
}
