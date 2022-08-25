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
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
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

public class FixedAssetLineMoveServiceImpl implements FixedAssetLineMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected FixedAssetLineRepository fixedAssetLineRepo;

  protected MoveCreateService moveCreateService;

  protected MoveRepository moveRepo;

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  @Inject
  public FixedAssetLineMoveServiceImpl(
      FixedAssetLineRepository fixedAssetLineRepo,
      MoveCreateService moveCreateService,
      MoveRepository moveRepo,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    this.fixedAssetLineRepo = fixedAssetLineRepo;
    this.moveCreateService = moveCreateService;
    this.moveRepo = moveRepo;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realize(FixedAssetLine fixedAssetLine) throws AxelorException {

    generateMove(fixedAssetLine);

    fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_REALIZED);

    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();
    BigDecimal residualValue = fixedAsset.getResidualValue();
    fixedAsset.setResidualValue(residualValue.subtract(fixedAssetLine.getDepreciation()));

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
  }

  @Transactional(rollbackOn = {Exception.class})
  private void generateMove(FixedAssetLine fixedAssetLine) throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();
    String origin = fixedAsset.getReference();

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
            null);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<>();

      Account debitLineAccount = fixedAsset.getFixedAssetCategory().getChargeAccount();
      Account creditLineAccount = fixedAsset.getFixedAssetCategory().getDepreciationAccount();
      if (debitLineAccount == null || creditLineAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.FIXED_ASSET_CATEGORY_MISSING_DEBIT_OR_CREDIT_ACCOUNT),
            fixedAsset.getFixedAssetCategory().getName());
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
  public void generateDisposalMove(FixedAssetLine fixedAssetLine) throws AxelorException {

    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();
    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAssetLine.getDepreciationDate();
    String origin = fixedAsset.getReference();

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
            null);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      Account chargeAccount = fixedAsset.getFixedAssetCategory().getChargeAccount();
      Account depreciationAccount = fixedAsset.getFixedAssetCategory().getDepreciationAccount();
      if (chargeAccount == null || depreciationAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.FIXED_ASSET_CATEGORY_MISSING_CHARGE_OR_DEPRECIATION_ACCOUNT),
            fixedAsset.getFixedAssetCategory().getName());
      }
      Account purchaseAccount = fixedAsset.getPurchaseAccount();
      if (purchaseAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.FIXED_ASSET_MISSING_PURCHASE_ACCOUNT));
      }
      BigDecimal chargeAmount = fixedAssetLine.getResidualValue();
      BigDecimal cumulativeDepreciationAmount = fixedAssetLine.getCumulativeDepreciation();

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
      moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine);
    }
  }
}
