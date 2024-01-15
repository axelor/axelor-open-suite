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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.i18n.I18n;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
  protected BankDetailsService bankDetailsService;
  protected FixedAssetDateService fixedAssetDateService;
  private Batch batch;

  @Inject
  public FixedAssetDerogatoryLineMoveServiceImpl(
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      BatchRepository batchRepository,
      BankDetailsService bankDetailsService,
      FixedAssetDateService fixedAssetDateService) {
    this.fixedAssetDerogatoryLineRepository = fixedAssetDerogatoryLineRepository;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.batchRepository = batchRepository;
    this.bankDetailsService = bankDetailsService;
    this.fixedAssetDateService = fixedAssetDateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
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
              false,
              false,
              null);
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
    BigDecimal amount;
    if (fixedAssetDerogatoryLine.getIncomeDepreciationAmount().signum() != 0) {
      amount = fixedAssetDerogatoryLine.getIncomeDepreciationAmount().abs();
    } else {
      amount = fixedAssetDerogatoryLine.getDerogatoryAmount().abs();
    }
    if (fixedAssetDerogatoryLine.getFixedAsset().getGrossValue().signum() < 0) {
      amount = amount.negate();
    }
    return amount;
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
      Boolean isSimulated,
      Boolean isDisposal,
      LocalDate disposalDate)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetDerogatoryLine.getDepreciationDate();
    if (!isDisposal) {
      int periodicityTypeSelect = fixedAsset.getPeriodicityTypeSelect();
      if (periodicityTypeSelect == FixedAssetRepository.PERIODICITY_TYPE_MONTH) {
        date = date.with(TemporalAdjusters.lastDayOfMonth());
      } else {
        date =
            fixedAssetDateService.computeLastDayOfFiscalYear(company, date, periodicityTypeSelect);
      }
    } else {
      date = disposalDate;
    }

    String origin =
        fixedAsset.getFixedAssetSeq() != null
            ? fixedAsset.getFixedAssetSeq()
            : fixedAsset.getReference();
    log.debug(
        "Creating an fixed asset derogatory line specific accounting entry {} (Company : {}, Journal : {})",
        fixedAsset.getReference(),
        company.getName(),
        journal.getCode());

    BankDetails companyBankDetails = null;
    if (company != null) {
      companyBankDetails =
          bankDetailsService.getDefaultCompanyBankDetails(company, null, partner, null);
    }

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
            fixedAsset.getName(),
            companyBankDetails);

    if (move != null) {

      if (isSimulated) {
        move.setStatusSelect(MoveRepository.STATUS_SIMULATED);
      }
      List<MoveLine> moveLines = new ArrayList<>();

      if (creditLineAccount == null || debitLineAccount == null) {
        List<String> missingAccounts = new ArrayList<>();
        FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
        if (fixedAssetCategory.getCapitalDepreciationDerogatoryAccount() == null) {
          missingAccounts.add(
              I18n.get(AccountExceptionMessage.CAPITAL_DEPRECIATION_DEROGATORY_ACCOUNT));
        }
        if (fixedAssetCategory.getExpenseDepreciationDerogatoryAccount() == null) {
          missingAccounts.add(
              I18n.get(AccountExceptionMessage.EXPENSE_DEPRECIATION_DEROGATORY_ACCOUNT));
        }
        if (fixedAssetCategory.getIncomeDepreciationDerogatoryAccount() == null) {
          missingAccounts.add(
              I18n.get(AccountExceptionMessage.INCOME_DEPRECIATION_DEROGATORY_ACCOUNT));
        }
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
            Joiner.on(", ").join(missingAccounts));
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

  @Transactional(rollbackOn = {Exception.class})
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
              true,
              false,
              null));
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
