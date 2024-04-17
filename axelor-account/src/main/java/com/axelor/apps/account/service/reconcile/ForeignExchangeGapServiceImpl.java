package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class ForeignExchangeGapServiceImpl implements ForeignExchangeGapService {

  protected AccountConfigService accountConfigService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveReverseService moveReverseService;
  protected AppBaseService appBaseService;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoiceTermService invoiceTermService;
  protected InvoicePaymentRepository invoicePaymentRepository;

  @Inject
  public ForeignExchangeGapServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineCreateService moveLineCreateService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveReverseService moveReverseService,
      AppBaseService appBaseService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermService invoiceTermService,
      InvoicePaymentRepository invoicePaymentRepository) {
    this.accountConfigService = accountConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveReverseService = moveReverseService;
    this.appBaseService = appBaseService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoiceTermService = invoiceTermService;
    this.invoicePaymentRepository = invoicePaymentRepository;
  }

  @Override
  public Move manageForeignExchangeGap(Reconcile reconcile) throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    // We only run the process if currency rate between the two moveLines are different
    if (debitMoveLine != null
        && creditMoveLine != null
        && !debitMoveLine.getCurrencyRate().equals(creditMoveLine.getCurrencyRate())) {
      boolean isDebit = this.isDebit(creditMoveLine, debitMoveLine);
      BigDecimal foreignExchangeGapAmount =
          this.getForeignExchangeGapAmount(reconcile.getAmount(), creditMoveLine, debitMoveLine);

      if (foreignExchangeGapAmount.abs().compareTo(BigDecimal.valueOf(0.01)) > 0) {
        boolean isGain = this.isGain(creditMoveLine, debitMoveLine, isDebit);

        return this.createForeignExchangeGapMove(
            reconcile, foreignExchangeGapAmount, isGain, isDebit);
      }
    }

    return null;
  }

  protected BigDecimal getForeignExchangeGapAmount(
      BigDecimal amountReconciled, MoveLine creditMoveLine, MoveLine debitMoveLine) {
    BigDecimal currencyAmount = BigDecimal.ZERO;
    BigDecimal moveLineRate = BigDecimal.ONE;

    if (creditMoveLine.getAmountRemaining().abs().compareTo(amountReconciled) == 0) {
      currencyAmount = creditMoveLine.getCurrencyAmount();
      moveLineRate = debitMoveLine.getCurrencyRate();
    } else if (debitMoveLine.getAmountRemaining().abs().compareTo(amountReconciled) == 0) {
      currencyAmount = debitMoveLine.getCurrencyAmount();
      moveLineRate = creditMoveLine.getCurrencyRate();
    }

    return amountReconciled.subtract(currencyAmount.abs().multiply(moveLineRate));
  }

  protected Move createForeignExchangeGapMove(
      Reconcile reconcile, BigDecimal foreignExchangeAmount, boolean isGain, boolean isDebit)
      throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(reconcile.getCompany());
    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    Account gainsAccount = accountConfigService.getForeignExchangeAccount(accountConfig, true);
    Account lossesAccount = accountConfigService.getForeignExchangeAccount(accountConfig, false);

    Account debitAccount =
        (isDebit && !isGain)
            ? lossesAccount
            : (!isDebit && !isGain) ? lossesAccount : creditMoveLine.getAccount();
    Account creditAccount =
        (isDebit && isGain)
            ? gainsAccount
            : (!isDebit && isGain) ? gainsAccount : debitMoveLine.getAccount();

    Move originMove = null;
    Partner partner = null;

    if (debitMoveLine.getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      originMove = debitMoveLine.getMove();
    } else {
      originMove = creditMoveLine.getMove();
    }

    if (debitMoveLine.getMove().getPartner() != null
        && debitMoveLine.getMove().getPartner().equals(creditMoveLine.getMove().getPartner())) {
      partner = debitMoveLine.getMove().getPartner();
    }

    // Move creation
    Move move =
        moveCreateService.createMove(
            miscOperationJournal,
            reconcile.getCompany(),
            null,
            partner,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            0,
            null,
            null,
            originMove != null ? originMove.getCompanyBankDetails() : null);

    // Credit move line creation
    this.miscOperationMoveCreation(
        move, originMove.getPartner(), creditAccount, foreignExchangeAmount.abs(), false, 1);
    // Debit move line creation
    this.miscOperationMoveCreation(
        move, originMove.getPartner(), debitAccount, foreignExchangeAmount.abs(), true, 2);

    moveValidateService.accounting(move);

    return move;
  }

  @Override
  public boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit) {
    return isDebit
        ? creditMoveLine.getCurrencyRate().compareTo(debitMoveLine.getCurrencyRate()) > 0
        : debitMoveLine.getCurrencyRate().compareTo(creditMoveLine.getCurrencyRate()) < 0;
  }

  @Override
  public boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine) {
    return debitMoveLine.getDate().isAfter(creditMoveLine.getDate());
  }

  protected void miscOperationMoveCreation(
      Move move, Partner partner, Account account, BigDecimal amount, boolean isDebit, int ref)
      throws AxelorException {
    MoveLine newMoveLine =
        moveLineCreateService.createMoveLine(
            move, partner, account, amount, isDebit, move.getDate(), ref, null, null);
    move.addMoveLineListItem(newMoveLine);
  }

  @Override
  public boolean checkForeignExchangeAccounts(Company company) throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Account gainsAccount = accountConfig.getForeignExchangeGainsAccount();
    Account lossesAccount = accountConfig.getForeignExchangeLossesAccount();

    if (gainsAccount == null && lossesAccount == null) {
      return false;
    }

    accountConfigService.getForeignExchangeAccount(accountConfig, true);
    accountConfigService.getForeignExchangeAccount(accountConfig, false);
    return true;
  }

  @Override
  public void unreconcileForeignExchangeMove(Reconcile reconcile) throws AxelorException {
    if (reconcile.getForeignExchangeMove() != null) {
      moveReverseService.generateReverse(
          reconcile.getForeignExchangeMove(),
          true,
          true,
          true,
          appBaseService.getTodayDate(reconcile.getCompany()));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InvoicePayment createForeignExchangeInvoicePayment(
      Reconcile newReconcile, Reconcile reconcile) throws AxelorException {
    Invoice invoice =
        reconcile.getDebitMoveLine().getMove().getInvoice() != null
            ? reconcile.getDebitMoveLine().getMove().getInvoice()
            : reconcile.getCreditMoveLine().getMove().getInvoice();
    InvoicePayment invoicePayment =
        invoicePaymentCreateService.createInvoicePayment(
            invoice,
            newReconcile.getAmount(),
            invoice.getDueDate(),
            newReconcile.getForeignExchangeMove().getCurrency(),
            invoice.getPaymentMode(),
            this.getInvoicePaymentType(reconcile));

    invoicePayment.setCompanyBankDetails(invoice.getCompanyBankDetails());
    invoicePayment.setMove(newReconcile.getForeignExchangeMove());
    invoice.addInvoicePaymentListItem(invoicePayment);
    invoiceTermPaymentService.createInvoicePaymentTerms(invoicePayment, null);
    if (!ObjectUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      for (InvoiceTermPayment invoiceTermPayment : invoicePayment.getInvoiceTermPaymentList()) {
        invoiceTermService.updateInvoiceTermsPaidAmount(
            invoicePayment, invoiceTermPayment.getInvoiceTerm(), invoiceTermPayment);
      }
    }

    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePaymentRepository.save(invoicePayment);

    return invoicePayment;
  }

  protected int getInvoicePaymentType(Reconcile reconcile) {
    boolean isDebit = this.isDebit(reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine());
    boolean isGain =
        this.isGain(reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine(), isDebit);

    return isGain
        ? InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN_PAYMENT
        : InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS_PAYMENT;
  }
}
