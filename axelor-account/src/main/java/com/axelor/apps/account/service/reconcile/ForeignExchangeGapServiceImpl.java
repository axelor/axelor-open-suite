package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ForeignExchangeGapServiceImpl implements ForeignExchangeGapService {

  protected AccountConfigService accountConfigService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;

  @Inject
  public ForeignExchangeGapServiceImpl(
      AccountConfigService accountConfigService,
      MoveLineCreateService moveLineCreateService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService) {
    this.accountConfigService = accountConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
  }

  @Override
  public Move manageForeignExchangeGap(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    // We only run the process if currency rate between the two moveLines are different
    if (debitMoveLine != null
        && creditMoveLine != null
        && !debitMoveLine.getCurrencyRate().equals(creditMoveLine.getCurrencyRate())) {
      boolean isDebit = this.isDebit(creditMoveLine, debitMoveLine);
      BigDecimal amountReconciled = reconcile.getAmount();

      BigDecimal currencyAmount = BigDecimal.ZERO;
      BigDecimal moveLineRate = BigDecimal.ONE;
      if (creditMoveLine.getAmountRemaining().abs().compareTo(amountReconciled) == 0) {
        currencyAmount = creditMoveLine.getCurrencyAmount();
        moveLineRate = debitMoveLine.getCurrencyRate();
      } else if (debitMoveLine.getAmountRemaining().abs().compareTo(amountReconciled) == 0) {
        currencyAmount = debitMoveLine.getCurrencyAmount();
        moveLineRate = creditMoveLine.getCurrencyRate();
      }

      BigDecimal foreignExchangeGapAmount =
          amountReconciled.subtract(currencyAmount.abs().multiply(moveLineRate));
      boolean isGain = this.isGain(creditMoveLine, debitMoveLine, isDebit);

      return this.createForeignExchangeGapMove(
          reconcile, foreignExchangeGapAmount, isGain, isDebit);
    }

    return null;
  }

  protected Move createForeignExchangeGapMove(
      Reconcile reconcile, BigDecimal foreignExchangeAmount, boolean isGain, boolean isDebit)
      throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(reconcile.getCompany());
    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    Account gainsAccount = accountConfig.getForeignExchangeGainsAccount();
    Account lossesAccount = accountConfig.getForeignExchangeLossesAccount();
    // TODO Add control if one of this two accounts are NULL

    Account debitAccount =
        (isDebit && isGain)
            ? gainsAccount
            : (!isDebit && !isGain) ? lossesAccount : accountConfig.getCustomerAccount();
    Account creditAccount =
        (isDebit && !isGain)
            ? lossesAccount
            : (!isDebit && isGain) ? gainsAccount : accountConfig.getCustomerAccount();

    /* Working conditions according to spec do delete
    if (isDebit) {
        if (isGain) {
            debitAccount = gainsAccount;
        } else {
            creditAccount = lossesAccount;
        }
    } else {
        if (isGain) {
            creditAccount = gainsAccount; // OK
        } else {
            debitAccount = lossesAccount; // OK
        }
    }
    */

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

  protected boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit) {
    return isDebit
        ? creditMoveLine.getCurrencyRate().compareTo(debitMoveLine.getCurrencyRate()) > 0
        : debitMoveLine.getCurrencyRate().compareTo(creditMoveLine.getCurrencyRate()) < 0;
  }

  protected boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine) {
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
}
