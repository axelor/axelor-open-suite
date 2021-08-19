package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetDerogatoryLineMoveServiceImpl
    implements FixedAssetDerogatoryLineMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository;
  protected MoveCreateService moveCreateService;
  protected MoveRepository moveRepo;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;

  @Inject
  public FixedAssetDerogatoryLineMoveServiceImpl(
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService) {
    this.fixedAssetDerogatoryLineRepository = fixedAssetDerogatoryLineRepository;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
  }

  @Override
  @Transactional
  public void realize(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) throws AxelorException {
    log.debug("Computing action 'realize' on " + fixedAssetDerogatoryLine);

    if (fixedAssetDerogatoryLine == null
        || fixedAssetDerogatoryLine.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED) {
      return;
    }
    BigDecimal derogatoryAmount = fixedAssetDerogatoryLine.getDerogatoryAmount();
    BigDecimal incomeDepreciationAmount = fixedAssetDerogatoryLine.getIncomeDepreciationAmount();
    BigDecimal derogatoryBalanceAmount = fixedAssetDerogatoryLine.getDerogatoryBalanceAmount();
    // If derogatoryAmount or incomeDepreciationAmount or derogatoryBalanceAmount are greater than 0
    // then we proceed.
    if ((derogatoryAmount != null && derogatoryAmount.compareTo(BigDecimal.ZERO) > 0)
        || (incomeDepreciationAmount != null
            && incomeDepreciationAmount.abs().compareTo(BigDecimal.ZERO) > 0)
        || (derogatoryBalanceAmount != null
            && derogatoryBalanceAmount.compareTo(BigDecimal.ZERO) > 0)) {

      Account creditAccount = computeCreditAccount(fixedAssetDerogatoryLine);
      Account debitAccount = computeDebitAccount(fixedAssetDerogatoryLine);
      BigDecimal amount = computeAmount(fixedAssetDerogatoryLine);
      generateMove(fixedAssetDerogatoryLine, creditAccount, debitAccount, amount);
    }
    fixedAssetDerogatoryLine.setStatusSelect(FixedAssetLineRepository.STATUS_REALIZED);
    fixedAssetDerogatoryLineRepository.save(fixedAssetDerogatoryLine);

    if (fixedAssetDerogatoryLine.getFixedAsset() != null) {
      fixedAssetLineMoveService.realizeOthersLines(
          fixedAssetDerogatoryLine.getFixedAsset(),
          fixedAssetDerogatoryLine.getDepreciationDate(),
          false);
    }
  }

  private BigDecimal computeAmount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    if (fixedAssetDerogatoryLine.getDerogatoryAmount().compareTo(BigDecimal.ZERO) > 0) {
      return fixedAssetDerogatoryLine.getDerogatoryAmount().abs();
    } else if (fixedAssetDerogatoryLine
            .getIncomeDepreciationAmount()
            .abs()
            .compareTo(BigDecimal.ZERO)
        > 0) {
      return fixedAssetDerogatoryLine.getIncomeDepreciationAmount().abs();
    }
    return fixedAssetDerogatoryLine.getDerogatoryAmount().abs();
  }

  private Account computeDebitAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getDerogatoryAmount().compareTo(BigDecimal.ZERO) > 0) {
      return fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
    } else if (fixedAssetDerogatoryLine
            .getIncomeDepreciationAmount()
            .abs()
            .compareTo(BigDecimal.ZERO)
        > 0) {
      return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
  }

  private Account computeCreditAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getDerogatoryAmount().compareTo(BigDecimal.ZERO) > 0) {
      return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
    } else if (fixedAssetDerogatoryLine
            .getIncomeDepreciationAmount()
            .abs()
            .compareTo(BigDecimal.ZERO)
        > 0) {
      return fixedAsset.getFixedAssetCategory().getIncomeDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateMove(
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine,
      Account creditLineAccount,
      Account debitLineAccount,
      BigDecimal amount)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetDerogatoryLine.getDepreciationDate();

    log.debug(
        "Creating an fixed asset derogatory line specific accounting entry {} (Company : {}, Journal : {})",
        fixedAsset.getReference(),
        company.getName(),
        journal.getCode());

    // Creating move
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            partner,
            date,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<>();

      String origin = fixedAsset.getReference();

      if (creditLineAccount == null || debitLineAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
            "Expense depreciation derogatory/Capital depreciation derogatory/Income depreciation derogatory");
      }
      // Creating accounting debit move line
      MoveLine debitMoveLine =
          new MoveLine(
              move,
              partner,
              debitLineAccount,
              date,
              null,
              1,
              amount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(debitMoveLine);
      // Creating accounting debit move line
      MoveLine creditMoveLine =
          new MoveLine(
              move,
              partner,
              creditLineAccount,
              date,
              null,
              2,
              BigDecimal.ZERO,
              amount,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(creditMoveLine);
      move.getMoveLineList().addAll(moveLines);
    }

    moveRepo.save(move);
    fixedAssetDerogatoryLine.setDerogatoryDepreciationMove(move);
  }
}
