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

  public static final String REVALUATION_INDEX_1 = "ind1i";
  public static final String REVALUATION_INDEX_2 = "ind2i";
  public static final String REVALUATION_NEXT_INDEX_1 = "ind1f";
  public static final String REVALUATION_NEXT_INDEX_2 = "ind2f";
  public static final String REVALUATION_PREVIOUS_YEAR_INDEX_1 = "ind1py";
  public static final String REVALUATION_PREVIOUS_YEAR_INDEX_2 = "ind2py";
  public static final String REVALUATION_ORIGINAL_PRICE = "p0";
  public static final String REVALUATION_LAST_REVALUATED_PRICE = "pf";

  @Inject
  public RevaluationFormulaService(IndexRevaluationService indexRevaluationService) {
    this.indexRevaluationService = indexRevaluationService;
  }

  public void checkFormula(RevaluationFormula revaluationFormula) throws ScriptException {
    String formula = revaluationFormula.getFormula();
    if (formula == null) {
      return;
    }

    formula = formula.toLowerCase();
    if (formula.contains(REVALUATION_INDEX_1)) {
      formula = formula.replace(REVALUATION_INDEX_1, "1");
    }
    if (formula.contains(REVALUATION_INDEX_2)) {
      formula = formula.replace(REVALUATION_INDEX_2, "1");
    }
    if (formula.contains(REVALUATION_NEXT_INDEX_1)) {
      formula = formula.replace(REVALUATION_NEXT_INDEX_1, "1");
    }
    if (formula.contains(REVALUATION_NEXT_INDEX_2)) {
      formula = formula.replace(REVALUATION_NEXT_INDEX_2, "1");
    }
    if (formula.contains(REVALUATION_PREVIOUS_YEAR_INDEX_1)) {
      formula = formula.replace(REVALUATION_PREVIOUS_YEAR_INDEX_1, "1");
    }
    if (formula.contains(REVALUATION_PREVIOUS_YEAR_INDEX_2)) {
      formula = formula.replace(REVALUATION_PREVIOUS_YEAR_INDEX_2, "1");
    }
    if (formula.contains(REVALUATION_ORIGINAL_PRICE)) {
      formula = formula.replace(REVALUATION_ORIGINAL_PRICE, "1");
    }
    if (formula.contains(REVALUATION_LAST_REVALUATED_PRICE)) {
      formula = formula.replace(REVALUATION_LAST_REVALUATED_PRICE, "1");
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

    if (formula.contains(REVALUATION_INDEX_1) && index1 != null && date1 != null) {
      ind1i = indexRevaluationService.getIndexValue(index1, date1).getValue();
      formula = formula.replace(REVALUATION_INDEX_1, ind1i.toString());
    }
    if (formula.contains(REVALUATION_INDEX_2) && index2 != null && date2 != null) {
      ind2i = indexRevaluationService.getIndexValue(index2, date2).getValue();
      formula = formula.replace(REVALUATION_INDEX_2, ind2i.toString());
    }
    if (formula.contains(REVALUATION_NEXT_INDEX_1) && index1 != null) {
      ind1f = indexRevaluationService.getIndexValue(index1, nextRevaluationDate).getValue();
      formula = formula.replace(REVALUATION_NEXT_INDEX_1, ind1f.toString());
    }
    if (formula.contains(REVALUATION_NEXT_INDEX_2) && index2 != null) {
      ind2f = indexRevaluationService.getIndexValue(index2, nextRevaluationDate).getValue();
      formula = formula.replace(REVALUATION_NEXT_INDEX_2, ind2f.toString());
    }
    if (formula.contains(REVALUATION_PREVIOUS_YEAR_INDEX_1) && index1 != null) {
      ind1nm1 =
          indexRevaluationService.getLastYearIndexValue(index1, nextRevaluationDate).getValue();
      formula = formula.replace(REVALUATION_PREVIOUS_YEAR_INDEX_1, ind1nm1.toString());
    }
    if (formula.contains(REVALUATION_PREVIOUS_YEAR_INDEX_2) && index2 != null) {
      ind2nm1 =
          indexRevaluationService.getLastYearIndexValue(index2, nextRevaluationDate).getValue();
      formula = formula.replace(REVALUATION_PREVIOUS_YEAR_INDEX_2, ind2nm1.toString());
    }
    if (formula.contains(REVALUATION_ORIGINAL_PRICE) && initialPrice != null) {
      formula = formula.replace(REVALUATION_ORIGINAL_PRICE, initialPrice.toString());
    }
    if (formula.contains(REVALUATION_LAST_REVALUATED_PRICE) && price != null) {
      formula = formula.replace(REVALUATION_LAST_REVALUATED_PRICE, price.toString());
    }

    if (function != null) {
      function.apply(
          new FormulaProcessingResults(
              ind1i, ind2i, ind1f, ind2f, ind1nm1, ind2nm1, initialPrice, price));
    }

    return formula;
  }
}
