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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
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
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetDerogatoryLineMoveServiceImpl
    implements FixedAssetDerogatoryLineMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveRepository moveRepo;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected MoveValidateService moveValidateService;
  protected BatchRepository batchRepository;
  private Batch batch;

  @Inject
  public FixedAssetDerogatoryLineMoveServiceImpl(
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      BatchRepository batchRepository) {
    this.fixedAssetDerogatoryLineRepository = fixedAssetDerogatoryLineRepository;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.batchRepository = batchRepository;
  }

  @Override
  @Transactional
  public void realize(
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine, boolean isBatch, boolean generateMove)
      throws AxelorException {
    log.debug("Computing action 'realize' on " + fixedAssetDerogatoryLine);

    if (fixedAssetDerogatoryLine == null
        || fixedAssetDerogatoryLine.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED) {
      return;
    }
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAsset == null) {
      return;
    }

    if (!isBatch && !isPreviousLineRealized(fixedAssetDerogatoryLine, fixedAsset)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_LINE_PREVIOUS_NOT_REALIZED));
    }

    BigDecimal derogatoryAmount = fixedAssetDerogatoryLine.getDerogatoryAmount();
    BigDecimal incomeDepreciationAmount = fixedAssetDerogatoryLine.getIncomeDepreciationAmount();
    BigDecimal derogatoryBalanceAmount = fixedAssetDerogatoryLine.getDerogatoryBalanceAmount();
    // If derogatoryAmount or incomeDepreciationAmount or derogatoryBalanceAmount are different than
    // 0
    // Normally, they should be greater or equal to 0. But using signum gives better visibility.
    if ((derogatoryAmount != null && derogatoryAmount.signum() != 0)
        || (incomeDepreciationAmount != null && incomeDepreciationAmount.signum() != 0)
        || (derogatoryBalanceAmount != null && derogatoryBalanceAmount.signum() != 0)) {

      BigDecimal amount = computeAmount(fixedAssetDerogatoryLine);
      Move deragotaryDepreciationMove =
          generateMove(
              fixedAssetDerogatoryLine,
              computeCreditAccount(fixedAssetDerogatoryLine),
              computeDebitAccount(fixedAssetDerogatoryLine),
              amount,
              false);
      if (fixedAssetDerogatoryLine.getIsSimulated() && deragotaryDepreciationMove != null) {
        this.moveValidateService.accounting(deragotaryDepreciationMove);
      }
      fixedAssetDerogatoryLine.setDerogatoryDepreciationMove(deragotaryDepreciationMove);
    }
    fixedAssetDerogatoryLine.setStatusSelect(FixedAssetLineRepository.STATUS_REALIZED);
    fixedAssetDerogatoryLineRepository.save(fixedAssetDerogatoryLine);

    if (fixedAssetDerogatoryLine.getFixedAsset() != null) {
      fixedAssetLineMoveService.realizeOthersLines(
          fixedAssetDerogatoryLine.getFixedAsset(),
          fixedAssetDerogatoryLine.getDepreciationDate(),
          isBatch,
          generateMove);
    }
  }

  protected boolean isPreviousLineRealized(
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine, FixedAsset fixedAsset) {
    List<FixedAssetDerogatoryLine> fixedAssetLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    fixedAssetLineList.sort(
        (line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
    for (int i = 0; i < fixedAssetLineList.size(); i++) {
      if (fixedAssetDerogatoryLine
          .getDepreciationDate()
          .equals(fixedAssetLineList.get(i).getDepreciationDate())) {
        if (i > 0) {
          if (fixedAssetLineList.get(i - 1).getStatusSelect()
              != FixedAssetLineRepository.STATUS_REALIZED) {
            return false;
          }
          return true;
        }
        return true;
      }
    }
    return true;
  }

  protected BigDecimal computeAmount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    // When calling this fonction, incomeDepreciationAmount and derogatoryAmount are not supposed to
    // be both different to 0.
    // Because when computed, only one of theses values is filled. But they can be both equals to 0.
    if (fixedAssetDerogatoryLine.getIncomeDepreciationAmount().signum() != 0) {
      return fixedAssetDerogatoryLine.getIncomeDepreciationAmount().abs();
    }
    return fixedAssetDerogatoryLine.getDerogatoryAmount().abs();
  }

  protected Account computeDebitAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getIncomeDepreciationAmount().signum() != 0) {
      return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
  }

  protected Account computeCreditAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getIncomeDepreciationAmount().signum() != 0) {
      return fixedAsset.getFixedAssetCategory().getIncomeDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move generateMove(
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine,
      Account creditLineAccount,
      Account debitLineAccount,
      BigDecimal amount,
      Boolean isSimulated)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetDerogatoryLine.getDepreciationDate();
    String origin =
        fixedAsset.getFixedAssetSeq() != null
            ? fixedAsset.getFixedAssetSeq()
            : fixedAsset.getReference();
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
            date,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET,
            origin,
            fixedAsset.getName());

    if (move != null) {

      if (isSimulated) {
        move.setStatusSelect(MoveRepository.STATUS_SIMULATED);
      }
      List<MoveLine> moveLines = new ArrayList<>();

      if (creditLineAccount == null || debitLineAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
            I18n.get(AccountExceptionMessage.CAPITAL_DEPRECIATION_DEROGATORY_ACCOUNT));
      }
      MoveLine debitMoveLine =
          moveLineCreateService.createMoveLine(
              move, partner, debitLineAccount, amount, true, date, 1, origin, fixedAsset.getName());
      moveLines.add(debitMoveLine);
      MoveLine creditMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              partner,
              creditLineAccount,
              amount,
              false,
              date,
              2,
              origin,
              fixedAsset.getName());
      moveLines.add(creditMoveLine);
      move.getMoveLineList().addAll(moveLines);
      if (batch != null) {
        move.addBatchSetItem(batchRepository.find(batch.getId()));
      }
    }

    return moveRepo.save(move);
  }

  @Transactional
  @Override
  public void simulate(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) throws AxelorException {
    Objects.requireNonNull(fixedAssetDerogatoryLine);

    if (fixedAssetDerogatoryLine.getIsSimulated()) {
      return;
    }

    BigDecimal derogatoryAmount = fixedAssetDerogatoryLine.getDerogatoryAmount();
    BigDecimal incomeDepreciationAmount = fixedAssetDerogatoryLine.getIncomeDepreciationAmount();
    BigDecimal derogatoryBalanceAmount = fixedAssetDerogatoryLine.getDerogatoryBalanceAmount();
    // If derogatoryAmount or incomeDepreciationAmount or derogatoryBalanceAmount are different than
    // 0
    // Normally, they should be greater or equal to 0. But using signum gives better visibility.
    if ((derogatoryAmount != null && derogatoryAmount.signum() != 0)
        || (incomeDepreciationAmount != null && incomeDepreciationAmount.signum() != 0)
        || (derogatoryBalanceAmount != null && derogatoryBalanceAmount.signum() != 0)) {

      BigDecimal amount = computeAmount(fixedAssetDerogatoryLine);
      fixedAssetDerogatoryLine.setDerogatoryDepreciationMove(
          generateMove(
              fixedAssetDerogatoryLine,
              computeCreditAccount(fixedAssetDerogatoryLine),
              computeDebitAccount(fixedAssetDerogatoryLine),
              amount,
              true));
    }

    fixedAssetDerogatoryLine.setIsSimulated(true);
    fixedAssetDerogatoryLineRepository.save(fixedAssetDerogatoryLine);

    if (fixedAssetDerogatoryLine.getFixedAsset() != null) {
      fixedAssetLineMoveService.simulateOthersLine(
          fixedAssetDerogatoryLine.getFixedAsset(), fixedAssetDerogatoryLine.getDepreciationDate());
    }
  }

  @Override
  public boolean canSimulate(FixedAssetDerogatoryLine fixedAssetLine) throws AxelorException {
    Objects.requireNonNull(fixedAssetLine);

    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();
    if (fixedAsset != null && fixedAsset.getJournal() != null) {
      return fixedAsset.getJournal().getAuthorizeSimulatedMove();
    }
    return false;
  }

  @Override
  public void setBatch(Batch batch) {
    this.batch = batch;
  }
}
