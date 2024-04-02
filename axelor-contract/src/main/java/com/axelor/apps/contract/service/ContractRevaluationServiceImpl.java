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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.RevaluationFormula;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.collections.CollectionUtils;

public class ContractRevaluationServiceImpl implements ContractRevaluationService {

  protected ContractRepository contractRepository;
  protected RevaluationFormulaService revaluationFormulaService;
  protected ContractLineService contractLineService;

  protected AppBaseService appBaseService;

  protected ContractVersionService contractVersionService;

  @Inject
  public ContractRevaluationServiceImpl(
      ContractRepository contractRepository,
      RevaluationFormulaService revaluationFormulaService,
      ContractLineService contractLineService,
      AppBaseService appBaseService,
      ContractVersionService contractVersionService) {
    this.contractRepository = contractRepository;
    this.revaluationFormulaService = revaluationFormulaService;
    this.contractLineService = contractLineService;
    this.appBaseService = appBaseService;
    this.contractVersionService = contractVersionService;
  }

  public Contract applyFormula(Contract contract) throws AxelorException, ScriptException {
    Long id = contract.getId();
    if (id != null) {
      contract = contractRepository.find(id);
    }
    ContractVersion contractVersion = contract.getCurrentContractVersion();
    BigDecimal initialYearlyExTotalRevalued = contractVersion.getYearlyExTaxTotalRevalued();
    List<ContractLine> contractLineList = contractVersion.getContractLineList();
    StringJoiner newUnitPrices = new StringJoiner(", ");
    StringJoiner allP0 = new StringJoiner(", ");
    StringJoiner allPf = new StringJoiner(", ");
    applyFormulaAndComputeContractLines(contract, contractLineList, newUnitPrices, allP0, allPf);
    contractVersionService.computeTotals(contractVersion);

    BigDecimal newYearlyExTaxTotalRevalued = contractVersion.getYearlyExTaxTotalRevalued();
    checkIfRevaluationIsNeeded(newYearlyExTaxTotalRevalued, initialYearlyExTotalRevalued, contract);
    return contract;
  }

  protected void applyFormulaAndComputeContractLines(
      Contract contract,
      List<ContractLine> contractLineList,
      StringJoiner newUnitPrices,
      StringJoiner allP0,
      StringJoiner allPf)
      throws AxelorException, ScriptException {
    if (CollectionUtils.isNotEmpty(contractLineList)) {
      for (ContractLine contractLine : contractLineList) {
        if (contractLine.getIsToRevaluate()) {
          applyFormulaToContractLine(contract, contractLine, allP0, allPf);
          newUnitPrices.add(contractLine.getPrice().toString());
        }
        contractLineService.compute(contractLine, contract, contractLine.getProduct());
        contractLineService.computeTotal(contractLine, contract);
      }
    }
  }

  protected void applyFormulaToContractLine(
      Contract contract, ContractLine contractLine, StringJoiner allP0, StringJoiner allPf)
      throws AxelorException, ScriptException {
    String formula =
        revaluationFormulaService.processingFormula(
            contractLine,
            contract,
            Optional.ofNullable(contract)
                .map(Contract::getRevaluationFormula)
                .map(RevaluationFormula::getFormula)
                .orElse(null),
            results -> {
              if (results.getInitialUnitPrice() != null) {
                allP0.add(results.getInitialUnitPrice().toString());
              }
              if (results.getPrice() != null) {
                allPf.add(results.getPrice().toString());
              }
            });
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");
    BigDecimal value = BigDecimal.valueOf((Double) engine.eval(formula));
    contractLine.setPrice(value.setScale(2, RoundingMode.HALF_UP));
  }

  @Override
  public boolean isToRevaluate(LocalDate todayDate, Contract contract) {
    LocalDate nextRevaluationDate = contract.getNextRevaluationDate();
    Duration revaluationPeriod = contract.getRevaluationPeriod();
    LocalDate computedLastRevaluationDate =
        computeNewDateWithPeriod(revaluationPeriod, contract.getLastRevaluationDate());
    LocalDate computedStartDate =
        computeNewDateWithPeriod(revaluationPeriod, contract.getStartDate());

    boolean computedLastRevaluationDateIsBeforeNextRevaluationDate =
        computedLastRevaluationDate != null
            && computedLastRevaluationDate.isBefore(nextRevaluationDate);

    boolean computedStartDateIsBeforeToday =
        computedStartDate != null && computedStartDate.isBefore(todayDate);

    return todayDate.isEqual(nextRevaluationDate)
        || (nextRevaluationDate.isBefore(todayDate)
            && (computedLastRevaluationDateIsBeforeNextRevaluationDate
                || computedStartDateIsBeforeToday));
  }

  protected LocalDate computeNewDateWithPeriod(Duration revaluationPeriod, LocalDate date) {
    if (date == null || revaluationPeriod == null) {
      return null;
    }

    int revaluationPeriodType = revaluationPeriod.getTypeSelect();
    Integer revaluationPeriodValue = revaluationPeriod.getValue();
    LocalDate computedDate = null;
    if (revaluationPeriodType == DurationRepository.TYPE_MONTH) {
      computedDate = date.plusMonths(revaluationPeriodValue);
    } else if (revaluationPeriodType == DurationRepository.TYPE_DAY) {
      computedDate = date.plusDays(revaluationPeriodValue);
    }
    return computedDate;
  }

  @Override
  public long getNumberOfContractsToRevaluateToday() {
    List<Contract> contractList = contractRepository.all().fetch();
    LocalDate todayDate = appBaseService.getTodayDate(null);
    return contractList.stream()
        .filter(
            contract ->
                contract.getIsToRevaluate()
                    && contract.getCurrentContractVersion().getStatusSelect()
                        == ContractVersionRepository.ONGOING_VERSION
                    && isToRevaluate(todayDate, contract))
        .count();
  }

  @Override
  public void checkIfRevaluationIsNeeded(
      BigDecimal newYearlyExTotalRevalued,
      BigDecimal initialYearlyExTotalRevalued,
      Contract contract)
      throws AxelorException {

    if (newYearlyExTotalRevalued.compareTo(initialYearlyExTotalRevalued) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ContractExceptionMessage.CONTRACT_REVALUATION_NOT_NEEDED),
          contract.getContractId());
    }
  }
}
