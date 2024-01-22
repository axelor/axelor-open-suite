package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.db.JPA;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class BankReconciliationBalanceComputationServiceImpl
    implements BankReconciliationBalanceComputationService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected AccountService accountService;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected MoveLineRepository moveLineRepository;
  protected CurrencyService currencyService;
  protected DateService dateService;
  protected BankReconciliationComputeService bankReconciliationComputeService;

  @Inject
  public BankReconciliationBalanceComputationServiceImpl(
      BankReconciliationRepository bankReconciliationRepository,
      AccountService accountService,
      BankReconciliationLineRepository bankReconciliationLineRepository,
      MoveLineRepository moveLineRepository,
      CurrencyService currencyService,
      DateService dateService,
      BankReconciliationComputeService bankReconciliationComputeService) {
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.moveLineRepository = moveLineRepository;
    this.currencyService = currencyService;
    this.dateService = dateService;
    this.bankReconciliationComputeService = bankReconciliationComputeService;
  }

  @Override
  @Transactional
  public BankReconciliation computeBalances(BankReconciliation bankReconciliation)
      throws AxelorException {
    int limit = 10;
    int offset = 0;
    List<BankReconciliation> bankReconciliations;
    List<MoveLine> moveLines;

    BigDecimal statementReconciledLineBalance = BigDecimal.ZERO;
    BigDecimal movesReconciledLineBalance = BigDecimal.ZERO;
    BigDecimal statementUnreconciledLineBalance = BigDecimal.ZERO;
    BigDecimal movesUnreconciledLineBalance = BigDecimal.ZERO;
    BigDecimal statementOngoingReconciledBalance = BigDecimal.ZERO;
    BigDecimal movesOngoingReconciledBalance = BigDecimal.ZERO;

    bankReconciliations =
        bankReconciliationRepository
            .findByBankDetails(bankReconciliation.getBankDetails())
            .order("id")
            .fetch(limit, offset);
    if (bankReconciliations.size() != 0) {
      statementReconciledLineBalance =
          statementReconciledLineBalance.add(bankReconciliations.get(0).getStartingBalance());
      movesReconciledLineBalance =
          movesReconciledLineBalance.add(bankReconciliations.get(0).getStartingBalance());
    }
    do {
      for (BankReconciliation br : bankReconciliations) {
        for (BankReconciliationLine brl : br.getBankReconciliationLineList()) {
          statementReconciledLineBalance =
              computeStatementReconciledLineBalance(statementReconciledLineBalance, brl);
          statementUnreconciledLineBalance =
              computeStatementUnreconciledLineBalance(statementUnreconciledLineBalance, brl);
          statementOngoingReconciledBalance =
              computeStatementOngoingReconciledLineBalance(statementOngoingReconciledBalance, brl);
          movesOngoingReconciledBalance =
              computeMovesOngoingReconciledLineBalance(movesOngoingReconciledBalance, brl);
        }
      }
      offset += limit;
      JPA.clear();
      bankReconciliations =
          bankReconciliationRepository
              .findByBankDetails(bankReconciliation.getBankDetails())
              .fetch(limit, offset);
    } while (bankReconciliations.size() != 0);

    offset = 0;
    JPA.clear();
    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    moveLines = this.getMoveLines(bankReconciliation.getCashAccount(), limit, offset);

    do {
      for (MoveLine moveLine : moveLines) {
        movesReconciledLineBalance =
            computeMovesReconciledLineBalance(movesReconciledLineBalance, moveLine);
        movesUnreconciledLineBalance =
            computeMovesUnreconciledLineBalance(movesUnreconciledLineBalance, moveLine);
      }
      offset += limit;
      JPA.clear();
      bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
      moveLines = this.getMoveLines(bankReconciliation.getCashAccount(), limit, offset);

    } while (moveLines.size() != 0);
    JPA.clear();
    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }

    bankReconciliation.setStatementReconciledLineBalance(
        getConvertedAmount(statementReconciledLineBalance, bankReconciliation));
    bankReconciliation.setMovesReconciledLineBalance(
        getConvertedAmount(movesReconciledLineBalance, bankReconciliation));
    bankReconciliation.setStatementUnreconciledLineBalance(
        getConvertedAmount(statementUnreconciledLineBalance, bankReconciliation));
    bankReconciliation.setMovesUnreconciledLineBalance(
        getConvertedAmount(movesUnreconciledLineBalance, bankReconciliation));
    bankReconciliation.setStatementOngoingReconciledBalance(
        getConvertedAmount(statementOngoingReconciledBalance, bankReconciliation));
    bankReconciliation.setMovesOngoingReconciledBalance(
        getConvertedAmount(movesOngoingReconciledBalance, bankReconciliation));
    bankReconciliation.setStatementAmountRemainingToReconcile(
        getConvertedAmount(
            statementUnreconciledLineBalance.subtract(statementOngoingReconciledBalance),
            bankReconciliation));
    bankReconciliation.setMovesAmountRemainingToReconcile(
        getConvertedAmount(
            movesUnreconciledLineBalance.subtract(movesOngoingReconciledBalance),
            bankReconciliation));
    bankReconciliation.setStatementTheoreticalBalance(
        getConvertedAmount(
            statementReconciledLineBalance.add(statementOngoingReconciledBalance),
            bankReconciliation));
    bankReconciliation.setMovesTheoreticalBalance(
        getConvertedAmount(
            movesReconciledLineBalance.add(movesOngoingReconciledBalance), bankReconciliation));
    bankReconciliation = bankReconciliationComputeService.computeEndingBalance(bankReconciliation);
    return bankReconciliationRepository.save(bankReconciliation);
  }

  protected List<MoveLine> getMoveLines(Account cashAccount, int limit, int offset) {
    return moveLineRepository
        .all()
        .filter("self.account = :cashAccount AND self.move.statusSelect IN (:daybook, :accounted)")
        .bind("cashAccount", cashAccount)
        .bind("daybook", MoveRepository.STATUS_DAYBOOK)
        .bind("accounted", MoveRepository.STATUS_ACCOUNTED)
        .fetch(limit, offset);
  }

  protected BigDecimal computeMovesReconciledLineBalance(
      BigDecimal movesReconciledLineBalance, MoveLine moveLine) {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) { // Debit line
      movesReconciledLineBalance =
          movesReconciledLineBalance.add(moveLine.getBankReconciledAmount());
    } else { // Credit line
      movesReconciledLineBalance =
          movesReconciledLineBalance.subtract(moveLine.getBankReconciledAmount());
    }
    return movesReconciledLineBalance;
  }

  protected BigDecimal computeMovesUnreconciledLineBalance(
      BigDecimal movesUnreconciledLineBalance, MoveLine moveLine) {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) { // Debit line
      movesUnreconciledLineBalance =
          movesUnreconciledLineBalance.add(
              moveLine.getDebit().subtract(moveLine.getBankReconciledAmount()));
    } else { // Credit line
      movesUnreconciledLineBalance =
          movesUnreconciledLineBalance.subtract(
              moveLine.getCredit().subtract(moveLine.getBankReconciledAmount()));
    }
    return movesUnreconciledLineBalance;
  }

  protected BigDecimal computeStatementReconciledLineBalance(
      BigDecimal statementReconciledLineBalance, BankReconciliationLine brl) {
    if (brl.getIsPosted()) {

      statementReconciledLineBalance = statementReconciledLineBalance.subtract(brl.getDebit());
      statementReconciledLineBalance = statementReconciledLineBalance.add(brl.getCredit());
    }
    return statementReconciledLineBalance;
  }

  protected BigDecimal computeStatementUnreconciledLineBalance(
      BigDecimal statementUnreconciledLineBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted()) {
      statementUnreconciledLineBalance = statementUnreconciledLineBalance.subtract(brl.getDebit());
      statementUnreconciledLineBalance = statementUnreconciledLineBalance.add(brl.getCredit());
    }
    return statementUnreconciledLineBalance;
  }

  protected BigDecimal computeStatementOngoingReconciledLineBalance(
      BigDecimal statementOngoingReconciledBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      List<BankReconciliationLine> bankReconciliationLines =
          bankReconciliationLineRepository
              .all()
              .filter("self.moveLine.id = ?1", brl.getMoveLine().getId())
              .fetch();
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        statementOngoingReconciledBalance =
            statementOngoingReconciledBalance.subtract(bankReconciliationLine.getDebit());
        statementOngoingReconciledBalance =
            statementOngoingReconciledBalance.add(bankReconciliationLine.getCredit());
      }
    }
    return statementOngoingReconciledBalance;
  }

  protected BigDecimal computeMovesOngoingReconciledLineBalance(
      BigDecimal movesOngoingReconciledBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      String query = "self.postedNbr LIKE '%%s%'";
      query = query.replace("%s", brl.getPostedNbr());
      List<MoveLine> moveLines = moveLineRepository.all().filter(query).fetch();
      for (MoveLine moveLine : moveLines) {
        if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) {
          movesOngoingReconciledBalance =
              movesOngoingReconciledBalance.add(moveLine.getCredit().add(moveLine.getDebit()));
        } else {
          movesOngoingReconciledBalance =
              movesOngoingReconciledBalance.subtract(moveLine.getCredit().add(moveLine.getDebit()));
        }
      }
    }
    return movesOngoingReconciledBalance;
  }

  protected BigDecimal getConvertedAmount(BigDecimal value, BankReconciliation bankReconciliation)
      throws AxelorException {
    return currencyService.getAmountCurrencyConvertedAtDate(
        bankReconciliation.getCurrency(),
        bankReconciliation.getCompany().getCurrency(),
        value,
        dateService.date());
  }
}
