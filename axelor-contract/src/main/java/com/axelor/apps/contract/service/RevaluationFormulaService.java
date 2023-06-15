package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.IndexRevaluation;
import com.axelor.apps.contract.db.RevaluationFormula;
import com.axelor.apps.contract.service.utils.FormulaProcessingResults;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class RevaluationFormulaService {
  protected IndexRevaluationService indexRevaluationService;

  @Inject
  public RevaluationFormulaService(IndexRevaluationService indexRevaluationService) {
    this.indexRevaluationService = indexRevaluationService;
  }

  public void checkFormula(RevaluationFormula revaluationFormula) throws ScriptException {
    String formula = revaluationFormula.getFormula();
    formula = formula.toLowerCase();
    if (formula.contains("ind1i")) {
      formula = formula.replace("ind1i", "1");
    }
    if (formula.contains("ind2i")) {
      formula = formula.replace("ind2i", "1");
    }
    if (formula.contains("ind1f")) {
      formula = formula.replace("ind1f", "1");
    }
    if (formula.contains("ind2f")) {
      formula = formula.replace("ind2f", "1");
    }
    if (formula.contains("ind1py")) {
      formula = formula.replace("ind1py", "1");
    }
    if (formula.contains("ind2py")) {
      formula = formula.replace("ind2py", "1");
    }
    if (formula.contains("p0")) {
      formula = formula.replace("p0", "1");
    }
    if (formula.contains("pf")) {
      formula = formula.replace("pf", "1");
    }
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");
    engine.eval(formula);
  }

  @FunctionalInterface
  public interface Function<I1> {
    void apply(I1 results);
  }

  public String processingFormula(
      ContractLine contractLine,
      Contract contract,
      String formula,
      Function<FormulaProcessingResults> function)
      throws AxelorException {
    return processingFormula(
        contract != null ? contract.getIndex1Date() : null,
        contract != null ? contract.getIndex2Date() : null,
        contract != null ? contract.getNextRevaluationDate() : null,
        contract != null ? contract.getIndex1() : null,
        contract != null ? contract.getIndex2() : null,
        contractLine != null ? contractLine.getInitialUnitPrice() : null,
        contractLine != null ? contractLine.getPrice() : null,
        formula,
        function);
  }

  public String processingFormula(
      LocalDate date1,
      LocalDate date2,
      LocalDate nextRevaluationDate,
      IndexRevaluation index1,
      IndexRevaluation index2,
      BigDecimal initialPrice,
      BigDecimal price,
      String formula,
      Function<FormulaProcessingResults> function)
      throws AxelorException {
    if (formula == null) {
      return null;
    }

    formula = formula.toLowerCase();
    BigDecimal ind1i = null;
    BigDecimal ind2i = null;
    BigDecimal ind1f = null;
    BigDecimal ind2f = null;
    BigDecimal ind1nm1 = null;
    BigDecimal ind2nm1 = null;

    if (formula.contains("ind1i") && index1 != null && date1 != null) {
      ind1i = indexRevaluationService.getIndexValue(index1, date1).getValue();
      formula = formula.replace("ind1i", ind1i.toString());
    }
    if (formula.contains("ind2i") && index2 != null && date2 != null) {
      ind2i = indexRevaluationService.getIndexValue(index2, date2).getValue();
      formula = formula.replace("ind2i", ind2i.toString());
    }
    if (formula.contains("ind1f") && index1 != null) {
      ind1f = indexRevaluationService.getIndexValue(index1, nextRevaluationDate).getValue();
      formula = formula.replace("ind1f", ind1f.toString());
    }
    if (formula.contains("ind2f") && index2 != null) {
      ind2f = indexRevaluationService.getIndexValue(index2, nextRevaluationDate).getValue();
      formula = formula.replace("ind2f", ind2f.toString());
    }
    if (formula.contains("ind1py") && index1 != null) {
      ind1nm1 =
          indexRevaluationService.getLastYearIndexValue(index1, nextRevaluationDate).getValue();
      formula = formula.replace("ind1py", ind1nm1.toString());
    }
    if (formula.contains("ind2py") && index2 != null) {
      ind2nm1 =
          indexRevaluationService.getLastYearIndexValue(index2, nextRevaluationDate).getValue();
      formula = formula.replace("ind2py", ind2nm1.toString());
    }
    if (formula.contains("p0") && initialPrice != null) {
      formula = formula.replace("p0", initialPrice.toString());
    }
    if (formula.contains("pf") && price != null) {
      formula = formula.replace("pf", price.toString());
    }

    if (function != null) {
      function.apply(
          new FormulaProcessingResults(
              ind1i, ind2i, ind1f, ind2f, ind1nm1, ind2nm1, initialPrice, price));
    }

    return formula;
  }
}
