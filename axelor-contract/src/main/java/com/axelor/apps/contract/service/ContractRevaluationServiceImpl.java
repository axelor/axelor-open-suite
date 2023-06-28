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
