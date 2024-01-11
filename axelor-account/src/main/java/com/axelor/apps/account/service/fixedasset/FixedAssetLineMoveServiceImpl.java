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
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetLineMoveServiceImpl implements FixedAssetLineMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetRepository fixedAssetRepo;

  protected FixedAssetLineRepository fixedAssetLineRepo;

  protected MoveCreateService moveCreateService;

  protected MoveLineCreateService moveLineCreateService;

  protected MoveRepository moveRepo;

  protected MoveLineRepository moveLineRepo;

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;

  protected FixedAssetLineService fixedAssetLineService;

  protected MoveValidateService moveValidateService;

  protected BatchRepository batchRepository;

  protected BankDetailsService bankDetailsService;

  protected FixedAssetDateService fixedAssetDateService;

  private Batch batch;

  @Inject
  public FixedAssetLineMoveServiceImpl(
      FixedAssetLineRepository fixedAssetLineRepo,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo,
      MoveLineRepository moveLineRepo,
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService,
      FixedAssetRepository fixedAssetRepo,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      FixedAssetLineService fixedAssetLineService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      BatchRepository batchRepository,
      BankDetailsService bankDetailsService,
      FixedAssetDateService fixedAssetDateService) {
    this.fixedAssetLineRepo = fixedAssetLineRepo;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
    this.moveLineRepo = moveLineRepo;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.fixedAssetRepo = fixedAssetRepo;
    this.batchRepository = batchRepository;
    this.bankDetailsService = bankDetailsService;
    this.fixedAssetDateService = fixedAssetDateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realize(
      FixedAssetLine fixedAssetLine, boolean isBatch, boolean generateMove, boolean isDisposal)
      throws AxelorException {

    if (fixedAssetLine == null
        || fixedAssetLine.getStatusSelect() != FixedAssetLineRepository.STATUS_PLANNED) {
      return;
    }
    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);
    if (fixedAsset == null) {
      return;
    }

    if (!isBatch && !isPreviousLineRealized(fixedAssetLine, fixedAsset)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_LINE_PREVIOUS_NOT_REALIZED));
    }
    if (fixedAssetLine.getTypeSelect() != FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      if (generateMove) {
        Move depreciationAccountMove = generateMove(fixedAssetLine, false, isDisposal);
        if (fixedAssetLine.getIsSimulated() && depreciationAccountMove != null) {
          this.moveValidateService.accounting(depreciationAccountMove);
        }
        fixedAssetLine.setDepreciationAccountMove(depreciationAccountMove);
      }
      Move impairementAccountMove = generateImpairementAccountMove(fixedAssetLine, false);
      if (fixedAssetLine.getIsSimulated() && impairementAccountMove != null) {
        this.moveValidateService.accounting(impairementAccountMove);
      }
      fixedAssetLine.setImpairmentAccountMove(impairementAccountMove);
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

    fixedAsset.setCorrectedAccountingValue(BigDecimal.ZERO);
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
        realize(economicFixedAssetLine, isBatch, generateMove, false);
      }
      if (fiscalFixedAssetLine != null) {
        realize(fiscalFixedAssetLine, isBatch, generateMove, false);
      }

      if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
        FixedAssetDerogatoryLine fixedAssetDerogatoryLine =
            fixedAsset.getFixedAssetDerogatoryLineList().stream()
                .filter(line -> line.getDepreciationDate().equals(depreciationDate))
                .findAny()
                .orElse(null);
        if (fixedAssetDerogatoryLine != null) {
          if (batch != null) {
            fixedAssetDerogatoryLineMoveService.setBatch(batch);
          }
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
          realize(ifrsFixedAssetLine, isBatch, generateMove, false);
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateImpairementAccountMove(FixedAssetLine fixedAssetLine, boolean isSimulated)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();
    String origin =
        fixedAsset.getFixedAssetSeq() != null
            ? fixedAsset.getFixedAssetSeq()
            : fixedAsset.getReference();
    BigDecimal correctedAccountingValue = fixedAssetLine.getCorrectedAccountingValue();
    BigDecimal impairmentValue = fixedAssetLine.getImpairmentValue();
    BigDecimal accountingValue = fixedAssetLine.getAccountingValue();

    log.debug(
        "Creating an fixed asset line specific accounting entry {} (Company : {}, Journal : {})",
        fixedAsset.getReference(),
        company.getName(),
        journal.getCode());

    if (correctedAccountingValue != null
        && (correctedAccountingValue.signum() != 0)
        && impairmentValue != null
        && (impairmentValue.signum() != 0)) {

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

        FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
        Account debitLineAccount;
        Account creditLineAccount;
        BigDecimal amount;
        if ((impairmentValue.signum() > 0 && accountingValue.signum() > 0)
            || (impairmentValue.signum() < 0 && accountingValue.signum() < 0)) {
          if (fixedAssetCategory.getProvisionFixedAssetAccount() == null
              || fixedAssetCategory.getAppProvisionFixedAssetAccount() == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
                I18n.get("Appropriation Provision Tangible Fixed Asset Account")
                    + " / "
                    + I18n.get("Provision Tangible Fixed Asset Account"));
          }
          debitLineAccount = fixedAssetCategory.getAppProvisionFixedAssetAccount();
          creditLineAccount = fixedAssetCategory.getProvisionFixedAssetAccount();
        } else {
          if (fixedAssetCategory.getProvisionFixedAssetAccount() == null
              || fixedAssetCategory.getWbProvisionFixedAssetAccount() == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
                I18n.get("Provision Tangible Fixed Asset Account")
                    + " / "
                    + I18n.get("Written-back provision tangible fixed asset account"));
          }
          debitLineAccount = fixedAssetCategory.getProvisionFixedAssetAccount();
          creditLineAccount = fixedAssetCategory.getWbProvisionFixedAssetAccount();
        }
        amount = impairmentValue.abs();
        if (accountingValue.signum() < 0) {
          amount = amount.negate();
        }

        MoveLine debitMoveLine =
            moveLineCreateService.createMoveLine(
                move,
                partner,
                debitLineAccount,
                amount,
                true,
                date,
                1,
                origin,
                fixedAsset.getName());
        moveLines.add(debitMoveLine);

        List<AnalyticMoveLine> analyticDebitMoveLineList =
            CollectionUtils.isEmpty(debitMoveLine.getAnalyticMoveLineList())
                ? new ArrayList<>()
                : new ArrayList<>(debitMoveLine.getAnalyticMoveLineList());
        debitMoveLine.clearAnalyticMoveLineList();

        this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

        if (CollectionUtils.isEmpty(debitMoveLine.getAnalyticMoveLineList())) {
          debitMoveLine.setAnalyticMoveLineList(analyticDebitMoveLineList);
        }

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

        List<AnalyticMoveLine> analyticCreditMoveLineList =
            CollectionUtils.isEmpty(creditMoveLine.getAnalyticMoveLineList())
                ? new ArrayList<>()
                : new ArrayList<>(creditMoveLine.getAnalyticMoveLineList());
        creditMoveLine.clearAnalyticMoveLineList();

        this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

        if (CollectionUtils.isEmpty(creditMoveLine.getAnalyticMoveLineList())) {
          creditMoveLine.setAnalyticMoveLineList(analyticCreditMoveLineList);
        }

        move.getMoveLineList().addAll(moveLines);
        if (batch != null) {
          move.addBatchSetItem(batchRepository.find(batch.getId()));
        }
        return moveRepo.save(move);
      }
    }
    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Move generateMove(FixedAssetLine fixedAssetLine, boolean isSimulated, boolean isDisposal)
      throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();

    if (!isDisposal) {
      int periodicityTypeSelect = fixedAsset.getPeriodicityTypeSelect();
      if (periodicityTypeSelect == FixedAssetRepository.PERIODICITY_TYPE_MONTH) {
        date = date.with(TemporalAdjusters.lastDayOfMonth());
      } else {
        date =
            fixedAssetDateService.computeLastDayOfFiscalYear(company, date, periodicityTypeSelect);
      }
    }

    String origin =
        fixedAsset.getFixedAssetSeq() != null
            ? fixedAsset.getFixedAssetSeq()
            : fixedAsset.getReference();

    log.debug(
        "Creating an fixed asset line specific accounting entry {} (Company : {}, Journal : {})",
        origin,
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

      FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
      Account debitLineAccount;
      Account creditLineAccount;
      if (fixedAssetLine.getTypeSelect() == FixedAssetLineRepository.TYPE_SELECT_IFRS) {
        debitLineAccount = fixedAssetCategory.getIfrsChargeAccount();
        creditLineAccount = fixedAssetCategory.getIfrsDepreciationAccount();
        if (debitLineAccount == null || creditLineAccount == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(
                  AccountExceptionMessage.IMMO_FIXED_ASSET_GENERATE_MOVE_CATEGORY_ACCOUNTS_MISSING),
              I18n.get("IFRS Charge Account") + " / " + I18n.get("IFRS Depreciation Account"));
        }
      } else {
        debitLineAccount = fixedAssetCategory.getChargeAccount();
        creditLineAccount = fixedAssetCategory.getDepreciationAccount();
        if (debitLineAccount == null || creditLineAccount == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(
                  AccountExceptionMessage.IMMO_FIXED_ASSET_GENERATE_MOVE_CATEGORY_ACCOUNTS_MISSING),
              I18n.get("Charge account") + " / " + I18n.get("Depreciation account"));
        }
      }
      BigDecimal amount = fixedAssetLine.getDepreciation();

      MoveLine debitMoveLine =
          moveLineCreateService.createMoveLine(
              move, partner, debitLineAccount, amount, true, date, 1, origin, fixedAsset.getName());
      moveLines.add(debitMoveLine);

      List<AnalyticMoveLine> analyticDebitMoveLineList =
          CollectionUtils.isEmpty(debitMoveLine.getAnalyticMoveLineList())
              ? new ArrayList<>()
              : new ArrayList<>(debitMoveLine.getAnalyticMoveLineList());
      debitMoveLine.clearAnalyticMoveLineList();

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

      if (CollectionUtils.isEmpty(debitMoveLine.getAnalyticMoveLineList())) {
        debitMoveLine.setAnalyticMoveLineList(analyticDebitMoveLineList);
      }
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

      List<AnalyticMoveLine> analyticCreditMoveLineList =
          CollectionUtils.isEmpty(creditMoveLine.getAnalyticMoveLineList())
              ? new ArrayList<>()
              : new ArrayList<>(creditMoveLine.getAnalyticMoveLineList());
      creditMoveLine.clearAnalyticMoveLineList();

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

      if (CollectionUtils.isEmpty(creditMoveLine.getAnalyticMoveLineList())) {
        creditMoveLine.setAnalyticMoveLineList(analyticCreditMoveLineList);
      }
      move.getMoveLineList().addAll(moveLines);
      if (batch != null) {
        move.addBatchSetItem(batchRepository.find(batch.getId()));
      }
    }

    return moveRepo.save(move);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateDisposalMove(
      FixedAsset fixedAsset,
      FixedAssetLine fixedAssetLine,
      int transferredReason,
      LocalDate disposalDate)
      throws AxelorException {
    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    String origin =
        fixedAsset.getFixedAssetSeq() != null
            ? fixedAsset.getFixedAssetSeq()
            : fixedAsset.getReference();
    int moveLineSequenceCounter = 0;

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
            disposalDate,
            disposalDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET,
            origin,
            fixedAsset.getName(),
            companyBankDetails);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      Account depreciationAccount = fixedAsset.getFixedAssetCategory().getDepreciationAccount();
      Account purchaseAccount = fixedAsset.getPurchaseAccount();
      BigDecimal chargeAmount =
          fixedAssetLine != null
              ? fixedAssetLine.getAccountingValue()
              : fixedAsset.getAccountingValue();
      BigDecimal cumulativeDepreciationAmount =
          fixedAssetLine != null ? fixedAssetLine.getCumulativeDepreciation() : null;
      if (chargeAmount.signum() != 0) {
        Account chargeAccount;
        if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_CESSION
            || transferredReason == FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION) {
          if (fixedAsset.getFixedAssetCategory().getRealisedAssetsValueAccount() == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(
                    AccountExceptionMessage
                        .IMMO_FIXED_ASSET_GENERATE_DISPOSAL_MOVE_CATEGORY_ACCOUNTS_MISSING),
                I18n.get("Realised Assets Value Account"));
          }
          chargeAccount = fixedAsset.getFixedAssetCategory().getRealisedAssetsValueAccount();
        } else {
          if (fixedAsset.getFixedAssetCategory().getApproExtraordDepreciationExpenseAccount()
              == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(
                    AccountExceptionMessage
                        .IMMO_FIXED_ASSET_GENERATE_DISPOSAL_MOVE_CATEGORY_ACCOUNTS_MISSING),
                I18n.get("Account for appropriation to extraordinary depreciations"));
          }
          chargeAccount =
              fixedAsset.getFixedAssetCategory().getApproExtraordDepreciationExpenseAccount();
        }
        MoveLine chargeAccountDebitMoveLine =
            moveLineCreateService.createMoveLine(
                move,
                partner,
                chargeAccount,
                chargeAmount,
                true,
                disposalDate,
                ++moveLineSequenceCounter,
                origin,
                fixedAsset.getName());
        moveLines.add(chargeAccountDebitMoveLine);

        List<AnalyticMoveLine> analyticMoveLineList =
            CollectionUtils.isEmpty(chargeAccountDebitMoveLine.getAnalyticMoveLineList())
                ? new ArrayList<>()
                : new ArrayList<>(chargeAccountDebitMoveLine.getAnalyticMoveLineList());
        chargeAccountDebitMoveLine.clearAnalyticMoveLineList();

        this.addAnalyticToMoveLine(
            fixedAsset.getAnalyticDistributionTemplate(), chargeAccountDebitMoveLine);
        if (CollectionUtils.isEmpty(chargeAccountDebitMoveLine.getAnalyticMoveLineList())) {
          chargeAccountDebitMoveLine.setAnalyticMoveLineList(analyticMoveLineList);
        }
      }
      if (chargeAmount.signum() == 0
          && (cumulativeDepreciationAmount == null || cumulativeDepreciationAmount.signum() > 0)) {
        cumulativeDepreciationAmount = fixedAsset.getGrossValue();
      }
      if (cumulativeDepreciationAmount != null && cumulativeDepreciationAmount.signum() > 0) {
        MoveLine deprecationAccountDebitMoveLine =
            moveLineCreateService.createMoveLine(
                move,
                partner,
                depreciationAccount,
                cumulativeDepreciationAmount,
                true,
                disposalDate,
                ++moveLineSequenceCounter,
                origin,
                fixedAsset.getName());
        moveLines.add(deprecationAccountDebitMoveLine);
        this.addAnalyticToMoveLine(
            fixedAsset.getAnalyticDistributionTemplate(), deprecationAccountDebitMoveLine);
      }

      MoveLine creditMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              partner,
              purchaseAccount,
              fixedAsset.getGrossValue(),
              false,
              disposalDate,
              ++moveLineSequenceCounter,
              origin,
              fixedAsset.getName());
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
      moveLine = moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine);
      moveLineRepo.save(moveLine);
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
    String origin =
        fixedAsset.getFixedAssetSeq() != null
            ? fixedAsset.getFixedAssetSeq()
            : fixedAsset.getReference();

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
            disposalDate,
            disposalDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET,
            origin,
            fixedAsset.getName(),
            companyBankDetails);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      Account creditAccountOne =
          fixedAsset.getFixedAssetCategory().getRealisedAssetsIncomeAccount();
      List<AccountManagement> creditAccountTwoList =
          taxLine.getTax().getAccountManagementList().stream()
              .filter(
                  accountManagement ->
                      accountManagement.getCompany().equals(fixedAsset.getCompany()))
              .collect(Collectors.toList());
      Account creditAccountTwo =
          !CollectionUtils.isEmpty(creditAccountTwoList)
              ? creditAccountTwoList.get(0).getSaleAccount()
              : null;
      if (creditAccountTwo != null) {
        if (creditAccountTwo.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_GOODS) {
          creditAccountTwo = creditAccountTwoList.get(0).getSaleTaxVatSystem1Account();
        } else if (creditAccountTwo.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_SERVICE) {
          creditAccountTwo = creditAccountTwoList.get(0).getSaleTaxVatSystem2Account();
        }
      }
      BigDecimal creditAmountTwo =
          disposalAmount
              .multiply(taxLine.getValue().divide(new BigDecimal(100)))
              .setScale(FixedAssetServiceImpl.CALCULATION_SCALE, RoundingMode.HALF_UP);
      creditAmountTwo =
          creditAmountTwo.setScale(FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);
      Account debitAccount = fixedAsset.getFixedAssetCategory().getDebtReceivableAccount();
      BigDecimal debitAmount = disposalAmount.add(creditAmountTwo);

      if (creditAccountOne == null
          || (creditAccountTwo == null && creditAmountTwo.compareTo(BigDecimal.ZERO) > 0)
          || debitAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(
                AccountExceptionMessage
                    .IMMO_FIXED_ASSET_GENERATE_SALE_MOVE_CATEGORY_ACCOUNTS_MISSING),
            I18n.get("Realised Assets Income Account")
                + " / "
                + I18n.get("Debt Receivable Account")
                + " / "
                + I18n.get("Sale account of tax config"));
      }

      MoveLine creditMoveLine1 =
          moveLineCreateService.createMoveLine(
              move,
              partner,
              creditAccountOne,
              disposalAmount,
              false,
              disposalDate,
              1,
              origin,
              fixedAsset.getName());
      creditMoveLine1.setTaxLine(taxLine);
      moveLines.add(creditMoveLine1);

      List<AnalyticMoveLine> analyticCredit1MoveLineList =
          CollectionUtils.isEmpty(creditMoveLine1.getAnalyticMoveLineList())
              ? new ArrayList<>()
              : new ArrayList<>(creditMoveLine1.getAnalyticMoveLineList());
      creditMoveLine1.clearAnalyticMoveLineList();
      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine1);
      if (CollectionUtils.isEmpty(creditMoveLine1.getAnalyticMoveLineList())) {
        creditMoveLine1.setAnalyticMoveLineList(analyticCredit1MoveLineList);
      }
      if (creditAmountTwo.compareTo(BigDecimal.ZERO) > 0) {
        MoveLine creditMoveLine2 =
            moveLineCreateService.createMoveLine(
                move,
                partner,
                creditAccountTwo,
                creditAmountTwo,
                false,
                disposalDate,
                1,
                origin,
                fixedAsset.getName());
        creditMoveLine2.setTaxLine(taxLine);
        moveLines.add(creditMoveLine2);
        List<AnalyticMoveLine> analyticCerdit2MoveLineList =
            CollectionUtils.isEmpty(creditMoveLine2.getAnalyticMoveLineList())
                ? new ArrayList<>()
                : new ArrayList<>(creditMoveLine2.getAnalyticMoveLineList());
        creditMoveLine2.clearAnalyticMoveLineList();
        this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine2);
        if (CollectionUtils.isEmpty(creditMoveLine2.getAnalyticMoveLineList())) {
          creditMoveLine2.setAnalyticMoveLineList(analyticCerdit2MoveLineList);
        }
      }

      MoveLine debitMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              partner,
              debitAccount,
              debitAmount,
              true,
              disposalDate,
              2,
              origin,
              fixedAsset.getName());
      moveLines.add(debitMoveLine);

      List<AnalyticMoveLine> analyticDebitMoveLineList =
          CollectionUtils.isEmpty(debitMoveLine.getAnalyticMoveLineList())
              ? new ArrayList<>()
              : new ArrayList<>(debitMoveLine.getAnalyticMoveLineList());
      debitMoveLine.clearAnalyticMoveLineList();
      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);
      if (CollectionUtils.isEmpty(debitMoveLine.getAnalyticMoveLineList())) {
        debitMoveLine.setAnalyticMoveLineList(analyticDebitMoveLineList);
      }

      move.getMoveLineList().addAll(moveLines);
    }
    moveRepo.save(move);
    fixedAsset.setSaleAccountMove(move);
    fixedAssetRepo.save(fixedAsset);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void simulate(FixedAssetLine fixedAssetLine) throws AxelorException {
    Objects.requireNonNull(fixedAssetLine);
    if (fixedAssetLine.getIsSimulated()) {
      return;
    }
    if (fixedAssetLine.getTypeSelect() != FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      Move impairementAccountMove = generateImpairementAccountMove(fixedAssetLine, true);
      fixedAssetLine.setImpairmentAccountMove(impairementAccountMove);
      if (impairementAccountMove == null) {
        fixedAssetLine.setDepreciationAccountMove(generateMove(fixedAssetLine, true, false));
      }
    }

    fixedAssetLine.setIsSimulated(true);
    fixedAssetLineRepo.save(fixedAssetLine);
    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);
    if (fixedAsset != null) {
      simulateOthersLine(fixedAsset, fixedAssetLine.getDepreciationDate());
    }
  }

  @Override
  public void simulateOthersLine(FixedAsset fixedAsset, LocalDate depreciationDate)
      throws AxelorException {
    Objects.requireNonNull(fixedAsset);
    Objects.requireNonNull(depreciationDate);
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
        simulate(economicFixedAssetLine);
      }
      if (fiscalFixedAssetLine != null) {
        simulate(fiscalFixedAssetLine);
      }

      if (depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
        FixedAssetDerogatoryLine fixedAssetDerogatoryLine =
            fixedAsset.getFixedAssetDerogatoryLineList().stream()
                .filter(line -> line.getDepreciationDate().equals(depreciationDate))
                .findAny()
                .orElse(null);
        if (fixedAssetDerogatoryLine != null) {
          fixedAssetDerogatoryLineMoveService.simulate(fixedAssetDerogatoryLine);
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
          simulate(ifrsFixedAssetLine);
        }
      }
    }
  }

  @Override
  public boolean canSimulate(FixedAssetLine fixedAssetLine) throws AxelorException {
    Objects.requireNonNull(fixedAssetLine);

    FixedAsset fixedAsset = fixedAssetLineService.getFixedAsset(fixedAssetLine);
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
