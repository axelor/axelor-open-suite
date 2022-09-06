/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
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
      BatchRepository batchRepository) {
    this.fixedAssetLineRepo = fixedAssetLineRepo;
    this.fixedAssetRepo = fixedAssetRepo;
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
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realize(FixedAssetLine fixedAssetLine, boolean isBatch, boolean generateMove)
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
        Move depreciationAccountMove = generateMove(fixedAssetLine, false);
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
          realize(ifrsFixedAssetLine, isBatch, generateMove);
        }
      }
    }
  }

  @Transactional
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

    log.debug(
        "Creating an fixed asset line specific accounting entry {} (Company : {}, Journal : {})",
        fixedAsset.getReference(),
        company.getName(),
        journal.getCode());

    if (correctedAccountingValue != null
        && (correctedAccountingValue.signum() != 0)
        && impairmentValue != null
        && (impairmentValue.signum() != 0)) {

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

        FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
        Account debitLineAccount;
        Account creditLineAccount;
        BigDecimal amount;
        if (impairmentValue.compareTo(BigDecimal.ZERO) > 0) {
          if (fixedAssetCategory.getProvisionTangibleFixedAssetAccount() == null
              || fixedAssetCategory.getWbProvisionTangibleFixedAssetAccount() == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
                I18n.get("Charge account")
                    + " / "
                    + I18n.get("Provision Tangible Fixed Asset Account"));
          }
          debitLineAccount = fixedAssetCategory.getChargeAccount();
          creditLineAccount = fixedAssetCategory.getProvisionTangibleFixedAssetAccount();
        } else {
          if (fixedAssetCategory.getProvisionTangibleFixedAssetAccount() == null
              || fixedAssetCategory.getWbProvisionTangibleFixedAssetAccount() == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CATEGORY_ACCOUNTS_MISSING),
                I18n.get("Provision Tangible Fixed Asset Account")
                    + " / "
                    + I18n.get("WB Provision Tangible Fixed Asset Account"));
          }
          debitLineAccount = fixedAssetCategory.getProvisionTangibleFixedAssetAccount();
          creditLineAccount = fixedAssetCategory.getWbProvisionTangibleFixedAssetAccount();
        }
        amount = impairmentValue.abs();

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

        this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

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

        this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

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
  protected Move generateMove(FixedAssetLine fixedAssetLine, boolean isSimulated)
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

    log.debug(
        "Creating an fixed asset line specific accounting entry {} (Company : {}, Journal : {})",
        origin,
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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine);

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
            fixedAsset.getName());

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
      if (chargeAmount.signum() > 0) {
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
          chargeAccount = fixedAsset.getFixedAssetCategory().getChargeAccount();
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

        this.addAnalyticToMoveLine(
            fixedAsset.getAnalyticDistributionTemplate(), chargeAccountDebitMoveLine);
      }
      if (chargeAmount.signum() == 0
          && (cumulativeDepreciationAmount == null || cumulativeDepreciationAmount.signum() > 0)) {
        cumulativeDepreciationAmount = fixedAsset.getGrossValue();
      }
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
            fixedAsset.getName());

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      Account creditAccountOne =
          fixedAsset.getFixedAssetCategory().getRealisedAssetsIncomeAccount();
      BigDecimal denominator = BigDecimal.ONE.add(taxLine.getValue());
      BigDecimal creditAmountOne =
          disposalAmount.divide(
              denominator, FixedAssetServiceImpl.CALCULATION_SCALE, RoundingMode.HALF_UP);
      List<Account> creditAccountTwoList =
          taxLine.getTax().getAccountManagementList().stream()
              .filter(
                  accountManagement ->
                      accountManagement
                          .getCompany()
                          .getName()
                          .equals(fixedAsset.getCompany().getName()))
              .map(accountManagement -> accountManagement.getSaleAccount())
              .collect(Collectors.toList());
      Account creditAccountTwo =
          !CollectionUtils.isEmpty(creditAccountTwoList) ? creditAccountTwoList.get(0) : null;
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
              creditAmountOne,
              false,
              disposalDate,
              1,
              origin,
              fixedAsset.getName());
      moveLines.add(creditMoveLine1);

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine1);
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
        moveLines.add(creditMoveLine2);

        this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), creditMoveLine2);
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

      this.addAnalyticToMoveLine(fixedAsset.getAnalyticDistributionTemplate(), debitMoveLine);

      move.getMoveLineList().addAll(moveLines);
    }
    moveRepo.save(move);
    fixedAsset.setSaleAccountMove(move);
    fixedAssetRepo.save(fixedAsset);
  }

  @Transactional
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
        fixedAssetLine.setDepreciationAccountMove(generateMove(fixedAssetLine, true));
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
