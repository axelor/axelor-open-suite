/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetLineMoveServiceImpl implements FixedAssetLineMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetRepository fixedAssetRepo;

  protected FixedAssetLineRepository fixedAssetLineRepo;

  protected MoveCreateService moveCreateService;

  protected MoveRepository moveRepo;

  protected MoveLineService moveLineService;

  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;

  @Inject
  public FixedAssetLineMoveServiceImpl(
      FixedAssetLineRepository fixedAssetLineRepo,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo,
      MoveLineService moveLineService,
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService,
      FixedAssetRepository fixedAssetRepo) {
    this.fixedAssetLineRepo = fixedAssetLineRepo;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
    this.moveLineService = moveLineService;
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
    this.fixedAssetRepo = fixedAssetRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realize(FixedAssetLine fixedAssetLine, boolean isBatch, boolean generateMove)
      throws AxelorException {

    if (fixedAssetLine == null
        || fixedAssetLine.getStatusSelect() != FixedAssetLineRepository.STATUS_PLANNED) {
      return;
    }
    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();
    if (fixedAsset == null) {
      return;
    }
    if (!isBatch && !isPreviousLineRealized(fixedAssetLine, fixedAsset)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_LINE_PREVIOUS_NOT_REALIZED));
    }
    if (fixedAssetLine.getTypeSelect() != FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      if (generateMove) {
        generateMove(fixedAssetLine);
      }
      generateImpairementAccountMove(fixedAssetLine);
    }

    if (fixedAssetLine.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED
        && fixedAssetLine.getTypeSelect() == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC) {
      BigDecimal accountingValue = fixedAsset.getAccountingValue();
      fixedAsset.setAccountingValue(accountingValue.subtract(fixedAssetLine.getDepreciation()));
    }

    fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_REALIZED);

    FixedAssetLine plannedFixedAssetLine =
        fixedAsset.getFixedAssetLineList().stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .findAny()
            .orElse(null);

    if (plannedFixedAssetLine == null
        && fixedAsset.getDisposalValue().compareTo(BigDecimal.ZERO) == 0) {
      fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DEPRECIATED);
    }

    fixedAssetLineRepo.save(fixedAssetLine);

    if (fixedAsset != null) {
      realizeOthersLines(fixedAsset, fixedAssetLine.getDepreciationDate(), isBatch, generateMove);
    }
  }

  protected boolean isPreviousLineRealized(FixedAssetLine fixedAssetLine, FixedAsset fixedAsset) {
    List<FixedAssetLine> fixedAssetLineList = fixedAsset.getFixedAssetLineList();
    fixedAssetLineList.sort(
        (line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
    for (int i = 0; i < fixedAssetLineList.size(); i++) {
      if (fixedAssetLine
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
  /**
   * Method that may computes action "realize" on lines of fiscalFixedAssetLineList,
   * fixedAssetLineList and fixedAssetDerogatoryLineList that matches the same depreciation date. It
   * will compute depending on the fixedAsset.depreciationPlanSelect
   *
   * @param fixedAsset
   * @param depreciationDate
   * @throws AxelorException
   */
  @Override
  public void realizeOthersLines(
      FixedAsset fixedAsset, LocalDate depreciationDate, boolean isBatch, boolean generateMove)
      throws AxelorException {
    String depreciationPlanSelect = fixedAsset.getDepreciationPlanSelect();

    if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC)
        && depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_FISCAL)) {

      FixedAssetLine economicFixedAssetLine =
          fixedAsset.getFixedAssetLineList().stream()
              .filter(line -> line.getDepreciationDate().equals(depreciationDate))
              .findAny()
              .orElse(null);
      FixedAssetLine fiscalFixedAssetLine =
          fixedAsset.getFiscalFixedAssetLineList().stream()
              .filter(line -> line.getDepreciationDate().equals(depreciationDate))
              .findAny()
              .orElse(null);
      if (economicFixedAssetLine != null) {
        realize(economicFixedAssetLine, isBatch, generateMove);
      }
      if (fiscalFixedAssetLine != null) {
        realize(fiscalFixedAssetLine, isBatch, generateMove);
      }

      if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
        FixedAssetDerogatoryLine fixedAssetDerogatoryLine =
            fixedAsset.getFixedAssetDerogatoryLineList().stream()
                .filter(line -> line.getDepreciationDate().equals(depreciationDate))
                .findAny()
                .orElse(null);
        if (fixedAssetDerogatoryLine != null) {
          fixedAssetDerogatoryLineMoveService.realize(
              fixedAssetDerogatoryLine, isBatch, generateMove);
        }
      }
    }
    if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_IFRS)) {
      if (fixedAsset.getIfrsFixedAssetLineList() != null) {
        FixedAssetLine ifrsFixedAssetLine =
            fixedAsset.getIfrsFixedAssetLineList().stream()
                .filter(line -> line.getDepreciationDate().equals(depreciationDate))
                .findAny()
                .orElse(null);
        if (ifrsFixedAssetLine != null) {
          realize(ifrsFixedAssetLine, isBatch, generateMove);
        }
      }
    }
  }

  @Transactional
  private void generateImpairementAccountMove(FixedAssetLine fixedAssetLine)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();

    log.debug(
        "Creating an fixed asset line specific accounting entry {} (Company : {}, Journal : {})",
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
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET);
    BigDecimal correctedAccountingValue = fixedAssetLine.getCorrectedAccountingValue();
    BigDecimal impairmentValue = fixedAssetLine.getImpairmentValue();
    if (move != null
        && correctedAccountingValue != null
        && (correctedAccountingValue.signum() != 0)
        && impairmentValue != null
        && (impairmentValue.signum() != 0)) {
      List<MoveLine> moveLines = new ArrayList<>();

      String origin = fixedAsset.getReference();
      FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
      Account debitLineAccount;
      Account creditLineAccount;
      BigDecimal amount;
      if (impairmentValue.compareTo(BigDecimal.ZERO) > 0) {
        if (fixedAssetCategory.getProvisionTangibleFixedAssetAccount() == null
            || fixedAssetCategory.getWbProvisionTangibleFixedAssetAccount() == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
              "provisionTangibleFixedAssetAccount/wbProvisionTangibleFixedAssetAccount");
        }

        debitLineAccount = fixedAssetCategory.getChargeAccount();
        creditLineAccount = fixedAssetCategory.getProvisionTangibleFixedAssetAccount();
      } else {
        debitLineAccount = fixedAssetCategory.getProvisionTangibleFixedAssetAccount();
        creditLineAccount = fixedAssetCategory.getWbProvisionTangibleFixedAssetAccount();
      }
      amount = impairmentValue.abs();
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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

      move.getMoveLineList().addAll(moveLines);
      moveRepo.save(move);
      fixedAssetLine.setImpairmentAccountMove(move);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  private void generateMove(FixedAssetLine fixedAssetLine) throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();

    log.debug(
        "Creating an fixed asset line specific accounting entry {} (Company : {}, Journal : {})",
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
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET);
    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<>();

      String origin = fixedAsset.getReference();
      FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
      Account debitLineAccount;
      Account creditLineAccount;
      if (fixedAssetLine.getTypeSelect() == FixedAssetLineRepository.TYPE_SELECT_IFRS) {
        debitLineAccount = fixedAssetCategory.getIfrsChargeAccount();
        creditLineAccount = fixedAssetCategory.getIfrsDepreciationAccount();
        if (debitLineAccount == null || creditLineAccount == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_GENERATE_MOVE_CATEGORY_ACCOUNTS_MISSING),
              "ifrsChargeAccount/ifrsDepreciationAccount");
        }
      } else {
        debitLineAccount = fixedAssetCategory.getChargeAccount();
        creditLineAccount = fixedAssetCategory.getDepreciationAccount();
        if (debitLineAccount == null || creditLineAccount == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_GENERATE_MOVE_CATEGORY_ACCOUNTS_MISSING),
              "chargeAccount/depreciationAccount");
        }
      }
      BigDecimal amount = fixedAssetLine.getDepreciation();

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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

      move.getMoveLineList().addAll(moveLines);
    }

    moveRepo.save(move);

    fixedAssetLine.setDepreciationAccountMove(move);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateDisposalMove(FixedAssetLine fixedAssetLine, int transferredReason)
      throws AxelorException {

    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();
    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();

    // Creating move
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            partner,
            date,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      String origin = fixedAsset.getReference();
      Account chargeAccount;
      Account depreciationAccount = fixedAsset.getFixedAssetCategory().getDepreciationAccount();
      Account purchaseAccount = fixedAsset.getPurchaseAccount();
      BigDecimal chargeAmount = fixedAssetLine.getAccountingValue();
      BigDecimal cumulativeDepreciationAmount = fixedAssetLine.getCumulativeDepreciation();
      if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_CESSION
          || transferredReason == FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION) {
        if (fixedAsset.getFixedAssetCategory().getRealisedAssetsValueAccount() == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(
                  IExceptionMessage
                      .IMMO_FIXED_ASSET_GENERATE_DISPOSAL_MOVE_CATEGORY_ACCOUNTS_MISSING),
              "RealisedAssetsValueAccount");
        }
        chargeAccount = fixedAsset.getFixedAssetCategory().getRealisedAssetsValueAccount();
      } else {
        chargeAccount = fixedAsset.getFixedAssetCategory().getChargeAccount();
      }

      // Creating accounting debit move line for charge account
      MoveLine chargeAccountDebitMoveLine =
          new MoveLine(
              move,
              partner,
              chargeAccount,
              date,
              null,
              1,
              chargeAmount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(chargeAccountDebitMoveLine);

      this.addAnalyticToMoveLine(
          fixedAsset.getAnalyticDistributionTemplate(), chargeAccountDebitMoveLine);

      // Creating accounting debit move line for deprecation account
      MoveLine deprecationAccountDebitMoveLine =
          new MoveLine(
              move,
              partner,
              depreciationAccount,
              date,
              null,
              1,
              cumulativeDepreciationAmount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(deprecationAccountDebitMoveLine);

      this.addAnalyticToMoveLine(
          fixedAsset.getAnalyticDistributionTemplate(), deprecationAccountDebitMoveLine);

      // Creating accounting credit move line
      MoveLine creditMoveLine =
          new MoveLine(
              move,
              partner,
              purchaseAccount,
              date,
              null,
              2,
              BigDecimal.ZERO,
              fixedAsset.getGrossValue(),
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(creditMoveLine);

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

      move.getMoveLineList().addAll(moveLines);
    }
    moveRepo.save(move);
    fixedAsset.setDisposalMove(move);
  }

  @Transactional
  protected void addAnalyticToMoveLine(
      AnalyticDistributionTemplate analyticDistributionTemplate, MoveLine moveLine) {
    if (analyticDistributionTemplate != null
        && moveLine.getAccount().getAnalyticDistributionAuthorized()) {
      moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
      moveLineService.computeAnalyticDistribution(moveLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateSaleMove(
      FixedAsset fixedAsset, TaxLine taxLine, BigDecimal disposalAmount, LocalDate disposalDate)
      throws AxelorException {

    Company company = fixedAsset.getCompany();
    Journal journal = company.getAccountConfig().getCustomerSalesJournal();
    Partner partner = fixedAsset.getPartner();
    // Creating move
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            partner,
            disposalDate,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      String origin = fixedAsset.getReference();
      Account creditAccountOne =
          fixedAsset.getFixedAssetCategory().getRealisedAssetsIncomeAccount();
      BigDecimal denominator = BigDecimal.ONE.add(taxLine.getValue());
      BigDecimal creditAmountOne =
          disposalAmount.divide(
              denominator, FixedAssetServiceImpl.CALCULATION_SCALE, RoundingMode.HALF_UP);
      Account creditAccountTwo =
          taxLine.getTax().getAccountManagementList().stream()
              .filter(
                  accountManagement ->
                      accountManagement.getCompany().getName().equals(company.getName()))
              .map(accountManagement -> accountManagement.getSaleAccount())
              .findFirst()
              .orElse(null);
      BigDecimal creditAmountTwo =
          creditAmountOne
              .multiply(taxLine.getValue())
              .setScale(FixedAssetServiceImpl.CALCULATION_SCALE, RoundingMode.HALF_UP);
      creditAmountOne =
          creditAmountOne.setScale(FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);
      creditAmountTwo =
          creditAmountTwo.setScale(FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);
      Account debitAccount = fixedAsset.getFixedAssetCategory().getDebtReceivableAccount();
      BigDecimal debitAmount = disposalAmount;

      if (creditAccountOne == null || creditAccountTwo == null || debitAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(
                IExceptionMessage.IMMO_FIXED_ASSET_GENERATE_SALE_MOVE_CATEGORY_ACCOUNTS_MISSING),
            "realisedAssetsIncomeAccount / debtReceivableAccount / taxLine.tax.AccountManagementList.saleAccount");
      }

      MoveLine creditMoveLine1 =
          new MoveLine(
              move,
              partner,
              creditAccountOne,
              disposalDate,
              null,
              1,
              BigDecimal.ZERO,
              creditAmountOne,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              disposalDate);
      moveLines.add(creditMoveLine1);

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine1);

      MoveLine creditMoveLine2 =
          new MoveLine(
              move,
              partner,
              creditAccountTwo,
              disposalDate,
              null,
              1,
              BigDecimal.ZERO,
              creditAmountTwo,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              disposalDate);
      moveLines.add(creditMoveLine2);

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine2);

      MoveLine debitMoveLine =
          new MoveLine(
              move,
              partner,
              debitAccount,
              disposalDate,
              null,
              2,
              debitAmount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              disposalDate);
      moveLines.add(debitMoveLine);

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

      move.getMoveLineList().addAll(moveLines);
    }
    moveRepo.save(move);
    fixedAsset.setSaleAccountMove(move);
    fixedAssetRepo.save(fixedAsset);
  }
}
