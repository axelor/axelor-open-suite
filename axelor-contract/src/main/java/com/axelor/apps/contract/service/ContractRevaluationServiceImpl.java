/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.RevaluationFormula;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

  @Inject
  public ContractRevaluationServiceImpl(
      ContractRepository contractRepository,
      RevaluationFormulaService revaluationFormulaService,
      ContractLineService contractLineService) {
    this.contractRepository = contractRepository;
    this.revaluationFormulaService = revaluationFormulaService;
    this.contractLineService = contractLineService;
  }

  public Contract applyFormula(Contract contract) throws AxelorException, ScriptException {
    contract = contractRepository.find(contract.getId());
    List<ContractLine> contractLineList =
        contract.getCurrentContractVersion().getContractLineList();
    StringJoiner newUnitPrices = new StringJoiner(", ");
    StringJoiner allP0 = new StringJoiner(", ");
    StringJoiner allPf = new StringJoiner(", ");
    if (CollectionUtils.isNotEmpty(contractLineList)) {
      for (ContractLine contractLine : contractLineList) {
        if (contractLine.getIsToRevaluate()) {
          applyFormulaToContractLine(contract, contractLine, allP0, allPf);
          newUnitPrices.add(contractLine.getPrice().toString());
        }
        contractLineService.compute(contractLine, contract, contractLine.getProduct());
        contractLineService.computeTotal(contractLine);
      }
    }
    return contract;
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
}
