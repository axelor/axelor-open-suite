/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineCheckBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineRecordBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MoveLineGroupBudgetServiceImpl extends MoveLineGroupBankPaymentServiceImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public MoveLineGroupBudgetServiceImpl(
      MoveLineService moveLineService,
      MoveLineDefaultService moveLineDefaultService,
      MoveLineRecordService moveLineRecordService,
      MoveLineAttrsService moveLineAttrsService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineCheckService moveLineCheckService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineToolService moveLineToolService,
      MoveToolService moveToolService,
      AnalyticLineService analyticLineService,
      MoveAttrsService moveAttrsService,
      AnalyticAttrsService analyticAttrsService,
      MoveCutOffService moveCutOffService,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      MoveLineCheckBankPaymentService moveLineCheckBankPaymentService,
      MoveLineRecordBankPaymentService moveLineRecordBankPaymentService,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService,
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AnalyticAxisService analyticAxisService) {
    super(
        moveLineService,
        moveLineDefaultService,
        moveLineRecordService,
        moveLineAttrsService,
        moveLineComputeAnalyticService,
        moveLineCheckService,
        moveLineInvoiceTermService,
        moveLineToolService,
        moveToolService,
        analyticLineService,
        moveAttrsService,
        analyticAttrsService,
        moveCutOffService,
        moveLineFinancialDiscountService,
        moveLineCheckBankPaymentService,
        moveLineRecordBankPaymentService,
        fiscalPositionService,
        taxService,
        analyticAxisService);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadMoveAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = super.getOnLoadMoveAttrsMap(moveLine, move);
    if (move != null && appBudgetService.isApp("budget")) {
      boolean condition = budgetToolsService.checkBudgetKeyAndRoleForMove(move);
      this.addAttr("budgetPanel", "readonly", condition, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = super.getOnLoadAttrsMap(moveLine, move);
    if (move != null && appBudgetService.isApp("budget")) {
      boolean condition = budgetToolsService.checkBudgetKeyAndRoleForMove(move);
      this.addAttr("budgetPanel", "readonly", condition, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {

    Map<String, Map<String, Object>> attrsMap = super.getOnNewAttrsMap(moveLine, move);

    if (move != null && appBudgetService.isApp("budget")) {
      boolean condition = budgetToolsService.checkBudgetKeyAndRoleForMove(move);
      this.addAttr("budgetPanel", "readonly", condition, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Object> valuesMap = super.getOnNewValuesMap(moveLine, move);

    if (move != null) {
      valuesMap.put("budgetFromDate", move.getBudgetFromDate());
      valuesMap.put("budgetToDate", move.getBudgetToDate());
    }

    return valuesMap;
  }

  @Override
  public Map<String, Object> getDebitOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {

    Map<String, Object> valuesMap = super.getDebitOnChangeValuesMap(moveLine, move, dueDate);

    addBudgetRemainingAmountToAllocate(valuesMap, moveLine);

    return valuesMap;
  }

  @Override
  public Map<String, Object> getCreditOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {

    Map<String, Object> valuesMap = super.getCreditOnChangeValuesMap(moveLine, move, dueDate);

    addBudgetRemainingAmountToAllocate(valuesMap, moveLine);

    return valuesMap;
  }

  @Override
  public Map<String, Object> getCurrencyAmountRateOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {
    Map<String, Object> valuesMap =
        super.getCurrencyAmountRateOnChangeValuesMap(moveLine, move, dueDate);

    addBudgetRemainingAmountToAllocate(valuesMap, moveLine);

    return valuesMap;
  }

  @Override
  public Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Object> valuesMap = super.getDebitCreditOnChangeValuesMap(moveLine, move);

    addBudgetRemainingAmountToAllocate(valuesMap, moveLine);

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAccountOnChangeValuesMap(
      MoveLine moveLine,
      Move move,
      LocalDate cutOffStartDate,
      LocalDate cutOffEndDate,
      LocalDate dueDate)
      throws AxelorException {

    Map<String, Object> valuesMap =
        super.getAccountOnChangeValuesMap(moveLine, move, cutOffStartDate, cutOffEndDate, dueDate);
    valuesMap.put("budget", null);
    valuesMap.put("budgetDistributionList", new ArrayList<>());

    return valuesMap;
  }

  protected void addBudgetRemainingAmountToAllocate(
      Map<String, Object> valuesMap, MoveLine moveLine) {
    valuesMap.put(
        "budgetRemainingAmountToAllocate",
        budgetToolsService.getBudgetRemainingAmountToAllocate(
            moveLine.getBudgetDistributionList(), moveLine.getDebit().max(moveLine.getCredit())));
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }
}
