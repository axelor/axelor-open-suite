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

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

public class FixedAssetDerogatoryLineServiceImpl implements FixedAssetDerogatoryLineService {

  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;

  protected FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository;

  protected FixedAssetLineToolService fixedAssetLineToolService;

  @Inject
  public FixedAssetDerogatoryLineServiceImpl(
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService,
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository,
      FixedAssetLineToolService fixedAssetLineToolService) {
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
    this.fixedAssetDerogatoryLineRepository = fixedAssetDerogatoryLineRepository;
    this.fixedAssetLineToolService = fixedAssetLineToolService;
  }

  @Override
  public FixedAssetDerogatoryLine createFixedAssetDerogatoryLine(
      LocalDate depreciationDate,
      BigDecimal depreciationAmount,
      BigDecimal fiscalDepreciationAmount,
      BigDecimal derogatoryAmount,
      BigDecimal incomeDepreciationAmount,
      BigDecimal derogatoryBalanceAmount,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine fiscalFixedAssetLine,
      int statusSelect) {

    FixedAssetDerogatoryLine fixedAssetDerogatoryLine = new FixedAssetDerogatoryLine();
    fixedAssetDerogatoryLine.setStatusSelect(statusSelect);
    fixedAssetDerogatoryLine.setDepreciationDate(depreciationDate);
    fixedAssetDerogatoryLine.setDepreciationAmount(depreciationAmount);
    fixedAssetDerogatoryLine.setFiscalDepreciationAmount(fiscalDepreciationAmount);
    fixedAssetDerogatoryLine.setDerogatoryAmount(derogatoryAmount);
    fixedAssetDerogatoryLine.setIncomeDepreciationAmount(incomeDepreciationAmount);
    fixedAssetDerogatoryLine.setDerogatoryBalanceAmount(derogatoryBalanceAmount);
    fixedAssetDerogatoryLine.setFixedAssetLine(fixedAssetLine);
    fixedAssetDerogatoryLine.setFiscalFixedAssetLine(fiscalFixedAssetLine);

    return fixedAssetDerogatoryLine;
  }

  @Override
  public void computeDerogatoryBalanceAmount(
      List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList) {
    if (fixedAssetDerogatoryLineList != null) {
      fixedAssetDerogatoryLineList.sort(
          (line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
      FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine = null;
      for (FixedAssetDerogatoryLine line : fixedAssetDerogatoryLineList) {
        line.setDerogatoryBalanceAmount(
            computeDerogatoryBalanceAmount(
                previousFixedAssetDerogatoryLine,
                line.getDerogatoryAmount(),
                line.getIncomeDepreciationAmount()));
        previousFixedAssetDerogatoryLine = line;
      }
    }
  }

  @Override
  public List<FixedAssetDerogatoryLine> computePlannedFixedAssetDerogatoryLineList(
      FixedAsset fixedAsset) {
    LinkedHashMap<LocalDate, List<FixedAssetLine>> dateFixedAssetLineGrouped =
        fixedAssetLineToolService.groupAndSortByDateFixedAssetLine(fixedAsset);

    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList = new ArrayList<>();
    FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine = null;

    for (Entry<LocalDate, List<FixedAssetLine>> entry : dateFixedAssetLineGrouped.entrySet()) {

      FixedAssetDerogatoryLine derogatoryLine =
          computePlannedDerogatoryLine(
              extractLineWithType(entry.getValue(), FixedAssetLineRepository.TYPE_SELECT_ECONOMIC),
              extractLineWithType(entry.getValue(), FixedAssetLineRepository.TYPE_SELECT_FISCAL),
              previousFixedAssetDerogatoryLine,
              entry.getKey());
      derogatoryLine.setFixedAsset(fixedAsset);
      fixedAssetDerogatoryLineList.add(derogatoryLine);
      previousFixedAssetDerogatoryLine = derogatoryLine;
    }
    return fixedAssetDerogatoryLineList;
  }

  @Override
  public FixedAssetDerogatoryLine computePlannedDerogatoryLine(
      FixedAssetLine economicFixedAssetLine,
      FixedAssetLine fiscalFixedAssetLine,
      FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine,
      LocalDate date) {
    // Initialisation of fiscal and economic depreciation
    BigDecimal depreciationAmount = BigDecimal.ZERO;
    if (economicFixedAssetLine != null) {
      depreciationAmount =
          economicFixedAssetLine.getDepreciation() == null
              ? BigDecimal.ZERO
              : economicFixedAssetLine.getDepreciation();
    }

    BigDecimal fiscalDepreciationAmount = BigDecimal.ZERO;
    if (fiscalFixedAssetLine != null) {
      fiscalDepreciationAmount =
          fiscalFixedAssetLine.getDepreciation() == null
              ? BigDecimal.ZERO
              : fiscalFixedAssetLine.getDepreciation();
    }

    BigDecimal derogatoryAmount = null;
    BigDecimal incomeDepreciationAmount = null;

    // If fiscal depreciation is greater than economic depreciation then we fill
    // derogatoryAmount, else incomeDepreciation.
    if (fiscalDepreciationAmount.abs().compareTo(depreciationAmount.abs()) > 0) {
      derogatoryAmount = fiscalDepreciationAmount.subtract(depreciationAmount);
    } else {
      incomeDepreciationAmount = depreciationAmount.subtract(fiscalDepreciationAmount);
    }

    BigDecimal derogatoryBalanceAmount =
        computeDerogatoryBalanceAmount(
            previousFixedAssetDerogatoryLine, derogatoryAmount, incomeDepreciationAmount);
    return createFixedAssetDerogatoryLine(
        date,
        depreciationAmount,
        fiscalDepreciationAmount,
        derogatoryAmount,
        incomeDepreciationAmount,
        derogatoryBalanceAmount,
        null,
        null,
        FixedAssetLineRepository.STATUS_PLANNED);
  }

  protected FixedAssetLine extractLineWithType(List<FixedAssetLine> fixedAssetLineList, int type) {
    if (fixedAssetLineList != null) {
      return fixedAssetLineList.stream()
          .filter(fixedAssetLine -> fixedAssetLine.getTypeSelect() == type)
          .findAny()
          .orElse(null);
    }
    return null;
  }

  protected BigDecimal computeDerogatoryBalanceAmount(
      FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine,
      BigDecimal derogatoryAmount,
      BigDecimal incomeDepreciationAmount) {
    BigDecimal derogatoryBalanceAmount;
    BigDecimal previousDerogatoryBalanceAmount =
        previousFixedAssetDerogatoryLine == null
            ? BigDecimal.ZERO
            : previousFixedAssetDerogatoryLine.getDerogatoryBalanceAmount();
    if (derogatoryAmount == null || derogatoryAmount.signum() == 0) {
      derogatoryBalanceAmount =
          BigDecimal.ZERO.subtract(incomeDepreciationAmount).add(previousDerogatoryBalanceAmount);
    } else {
      derogatoryBalanceAmount =
          derogatoryAmount.subtract(BigDecimal.ZERO).add(previousDerogatoryBalanceAmount);
    }
    return derogatoryBalanceAmount;
  }

  @Override
  public void multiplyLinesBy(
      List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLine, BigDecimal prorata) {

    if (fixedAssetDerogatoryLine != null) {
      fixedAssetDerogatoryLine.forEach(line -> multiplyLineBy(line, prorata));
    }
  }

  protected void multiplyLineBy(FixedAssetDerogatoryLine line, BigDecimal prorata) {

    line.setDepreciationAmount(
        prorata
            .multiply(line.getDepreciationAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setFiscalDepreciationAmount(
        prorata
            .multiply(line.getFiscalDepreciationAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setDerogatoryAmount(
        prorata
            .multiply(line.getDerogatoryAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setIncomeDepreciationAmount(
        prorata
            .multiply(line.getIncomeDepreciationAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setDerogatoryBalanceAmount(
        prorata
            .multiply(line.getDerogatoryBalanceAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if firstPlannedDerogatoryLine is null
   */
  @Override
  public void generateDerogatoryCessionMove(
      FixedAssetDerogatoryLine firstPlannedDerogatoryLine,
      FixedAssetDerogatoryLine lastRealizedDerogatoryLine,
      LocalDate disposalDate)
      throws AxelorException {
    Objects.requireNonNull(firstPlannedDerogatoryLine);
    Account creditAccount;
    Account debitAccount;
    if (lastRealizedDerogatoryLine == null) {
      creditAccount = computeCessionCreditAccount(firstPlannedDerogatoryLine);
      debitAccount = computeCessionDebitAccount(firstPlannedDerogatoryLine);
    } else {
      creditAccount = computeCessionCreditAccount(lastRealizedDerogatoryLine);
      debitAccount = computeCessionDebitAccount(lastRealizedDerogatoryLine);
    }

    BigDecimal lastDerogatoryBalanceAmount =
        lastRealizedDerogatoryLine == null
            ? BigDecimal.ZERO
            : lastRealizedDerogatoryLine.getDerogatoryBalanceAmount();
    BigDecimal amount =
        firstPlannedDerogatoryLine
            .getDerogatoryBalanceAmount()
            .subtract(lastDerogatoryBalanceAmount)
            .abs();
    if (amount.signum() == 0) {
      return;
    }
    firstPlannedDerogatoryLine.setDerogatoryDepreciationMove(
        fixedAssetDerogatoryLineMoveService.generateMove(
            firstPlannedDerogatoryLine,
            creditAccount,
            debitAccount,
            amount,
            false,
            true,
            disposalDate));
    firstPlannedDerogatoryLine.setStatusSelect(FixedAssetLineRepository.STATUS_REALIZED);
  }

  protected Account computeCessionDebitAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getDerogatoryBalanceAmount().compareTo(BigDecimal.ZERO) >= 0) {
      return fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
  }

  protected Account computeCessionCreditAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {

    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getDerogatoryBalanceAmount().compareTo(BigDecimal.ZERO) >= 0) {
      return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getIncomeDepreciationDerogatoryAccount();
  }

  @Transactional
  @Override
  public void copyFixedAssetDerogatoryLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset) {
    if (newFixedAsset.getFixedAssetDerogatoryLineList() == null
        && fixedAsset.getFixedAssetDerogatoryLineList() != null) {
      fixedAsset
          .getFixedAssetDerogatoryLineList()
          .forEach(
              line -> {
                FixedAssetDerogatoryLine copy =
                    fixedAssetDerogatoryLineRepository.copy(line, false);
                copy.setFixedAsset(newFixedAsset);
                newFixedAsset.addFixedAssetDerogatoryLineListItem(
                    fixedAssetDerogatoryLineRepository.save(copy));
              });
    }
  }
  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if fixedAssetDerogatoryLineList is null
   */
  @Override
  public void clear(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList) {
    Objects.requireNonNull(fixedAssetDerogatoryLineList);
    fixedAssetDerogatoryLineList.forEach(
        line -> {
          remove(line);
        });
    fixedAssetDerogatoryLineList.clear();
  }

  @Override
  @Transactional
  public void remove(FixedAssetDerogatoryLine line) {
    Objects.requireNonNull(line);
    fixedAssetDerogatoryLineRepository.remove(line);
  }

  @Override
  public void filterListByStatus(
      List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList, int status) {
    List<FixedAssetDerogatoryLine> derogatoryLinesToRemove = new ArrayList<>();
    if (fixedAssetDerogatoryLineList != null) {
      fixedAssetDerogatoryLineList.stream()
          .filter(line -> line.getStatusSelect() == status)
          .forEach(line -> derogatoryLinesToRemove.add(line));
      fixedAssetDerogatoryLineList.removeIf(line -> line.getStatusSelect() == status);
    }
    clear(derogatoryLinesToRemove);
  }
}
