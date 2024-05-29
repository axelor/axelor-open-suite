package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class ForeignExchangeGapServiceImpl implements ForeignExchangeGapService {

  protected AccountConfigService accountConfigService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveReverseService moveReverseService;
  protected AppBaseService appBaseService;
  protected ForeignExchangeGapToolsService foreignExchangeGapToolsService;

  @Inject
  public ForeignExchangeGapServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineCreateService moveLineCreateService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveReverseService moveReverseService,
      AppBaseService appBaseService,
      ForeignExchangeGapToolsService foreignExchangeGapToolsService) {
    this.accountConfigService = accountConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveReverseService = moveReverseService;
    this.appBaseService = appBaseService;
    this.foreignExchangeGapToolsService = foreignExchangeGapToolsService;
  }

  @Override
  public ForeignMoveToReconcile manageForeignExchangeGap(Reconcile reconcile)
      throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    // We only run the process if currency rate between the two moveLines are different and currency
    // are equals
    if (foreignExchangeGapToolsService.checkCurrencies(creditMoveLine, debitMoveLine)
        && !debitMoveLine.getCurrencyRate().equals(creditMoveLine.getCurrencyRate())
        && this.checkForeignExchangeAccounts(reconcile.getCompany())
        && foreignExchangeGapToolsService.checkIsTotalPayment(
            reconcile, creditMoveLine, debitMoveLine)) {
      BigDecimal foreignExchangeGapAmount =
          this.getForeignExchangeGapAmount(reconcile.getAmount(), creditMoveLine, debitMoveLine);

      // We only create a foreign exchange move if foreignExchangeGapAmount is greater than 0.01
      if (foreignExchangeGapAmount.compareTo(BigDecimal.valueOf(0.01)) > 0) {
        Move foreignExchangeMove =
            this.createForeignExchangeGapMove(reconcile, foreignExchangeGapAmount);
        MoveLine foreignExchangeDebitMoveLine = foreignExchangeMove.getMoveLineList().get(0);
        MoveLine foreignExchangeCreditMoveLine = foreignExchangeMove.getMoveLineList().get(1);

        MoveLine debitMoveLineToReconcile;
        MoveLine creditMoveLineToReconcile;
        if (foreignExchangeGapToolsService.isGain(creditMoveLine, debitMoveLine)) {
          debitMoveLineToReconcile = foreignExchangeDebitMoveLine;
          creditMoveLineToReconcile = creditMoveLine;
        } else {
          debitMoveLineToReconcile = debitMoveLine;
          creditMoveLineToReconcile = foreignExchangeCreditMoveLine;
        }

        boolean updateInvoiceTerms =
            foreignExchangeGapToolsService.isGain(creditMoveLine, debitMoveLine)
                == foreignExchangeGapToolsService.isDebit(creditMoveLine, debitMoveLine);
        return new ForeignMoveToReconcile(
            foreignExchangeMove,
            debitMoveLineToReconcile,
            creditMoveLineToReconcile,
            updateInvoiceTerms);
      }
    }

    return null;
  }

  protected BigDecimal getForeignExchangeGapAmount(
      BigDecimal amountReconciled, MoveLine creditMoveLine, MoveLine debitMoveLine) {
    BigDecimal currencyAmount = BigDecimal.ZERO;
    BigDecimal moveLineRate = BigDecimal.ONE;

    if (creditMoveLine.getAmountRemaining().abs().compareTo(amountReconciled) == 0) {
      currencyAmount =
          creditMoveLine
              .getAmountRemaining()
              .divide(
                  creditMoveLine.getCurrencyRate(),
                  creditMoveLine.getCurrencyDecimals(),
                  RoundingMode.HALF_UP);
      moveLineRate = debitMoveLine.getCurrencyRate();
    } else if (debitMoveLine.getAmountRemaining().abs().compareTo(amountReconciled) == 0) {
      currencyAmount =
          debitMoveLine
              .getAmountRemaining()
              .divide(
                  debitMoveLine.getCurrencyRate(),
                  debitMoveLine.getCurrencyDecimals(),
                  RoundingMode.HALF_UP);
      moveLineRate = creditMoveLine.getCurrencyRate();
    }

    return amountReconciled.subtract(currencyAmount.abs().multiply(moveLineRate)).abs();
  }

  protected Move createForeignExchangeGapMove(Reconcile reconcile, BigDecimal foreignExchangeAmount)
      throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(reconcile.getCompany());
    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    Account gainsAccount = accountConfigService.getForeignExchangeAccount(accountConfig, true);
    Account lossesAccount = accountConfigService.getForeignExchangeAccount(accountConfig, false);

    boolean paymentIsDebit = foreignExchangeGapToolsService.isDebit(creditMoveLine, debitMoveLine);
    boolean isGain = foreignExchangeGapToolsService.isGain(creditMoveLine, debitMoveLine);

    Account debitAccount =
        (paymentIsDebit && !isGain)
            ? lossesAccount
            : (!paymentIsDebit && !isGain) ? lossesAccount : creditMoveLine.getAccount();
    Account creditAccount =
        (paymentIsDebit && isGain)
            ? gainsAccount
            : (!paymentIsDebit && isGain) ? gainsAccount : debitMoveLine.getAccount();

    Invoice invoice;
    Partner partner = null;

    if (debitMoveLine.getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      invoice = creditMoveLine.getMove().getInvoice();
    } else {
      invoice = debitMoveLine.getMove().getInvoice();
    }

    if (debitMoveLine.getMove().getPartner() != null
        && debitMoveLine.getMove().getPartner().equals(creditMoveLine.getMove().getPartner())) {
      partner = debitMoveLine.getMove().getPartner();
    }

    LocalDate date = paymentIsDebit ? debitMoveLine.getDate() : creditMoveLine.getDate();
    // Move creation
    Move move =
        moveCreateService.createMove(
            miscOperationJournal,
            reconcile.getCompany(),
            null,
            partner,
            date,
            date,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            0,
            null,
            null,
            invoice != null ? invoice.getCompanyBankDetails() : null);
    move.setInvoice(invoice);

    // Debit move line creation
    this.miscOperationMoveCreation(move, partner, debitAccount, foreignExchangeAmount, true, 1);
    // Credit move line creation
    this.miscOperationMoveCreation(move, partner, creditAccount, foreignExchangeAmount, false, 2);

    moveValidateService.accounting(move);

    return move;
  }

  protected void miscOperationMoveCreation(
      Move move, Partner partner, Account account, BigDecimal amount, boolean isDebit, int ref)
      throws AxelorException {
    MoveLine newMoveLine =
        moveLineCreateService.createMoveLine(
            move, partner, account, amount, isDebit, move.getDate(), ref, null, null);
    move.addMoveLineListItem(newMoveLine);
  }

  protected boolean checkForeignExchangeAccounts(Company company) throws AxelorException {
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
}
