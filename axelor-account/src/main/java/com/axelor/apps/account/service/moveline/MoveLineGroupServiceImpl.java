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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MoveLineGroupServiceImpl implements MoveLineGroupService {
  protected MoveLineService moveLineService;
  protected MoveLineDefaultService moveLineDefaultService;
  protected MoveLineRecordService moveLineRecordService;
  protected MoveLineAttrsService moveLineAttrsService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected MoveLineCheckService moveLineCheckService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineToolService moveLineToolService;
  protected MoveToolService moveToolService;
  protected AnalyticLineService analyticLineService;

  @Inject
  public MoveLineGroupServiceImpl(
      MoveLineService moveLineService,
      MoveLineDefaultService moveLineDefaultService,
      MoveLineRecordService moveLineRecordService,
      MoveLineAttrsService moveLineAttrsService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineCheckService moveLineCheckService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineToolService moveLineToolService,
      MoveToolService moveToolService,
      AnalyticLineService analyticLineService) {
    this.moveLineService = moveLineService;
    this.moveLineDefaultService = moveLineDefaultService;
    this.moveLineRecordService = moveLineRecordService;
    this.moveLineAttrsService = moveLineAttrsService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveLineCheckService = moveLineCheckService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveLineToolService = moveLineToolService;
    this.moveToolService = moveToolService;
    this.analyticLineService = analyticLineService;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    moveLineDefaultService.setFieldsFromParent(moveLine, move);
    moveLineDefaultService.setAccountInformation(moveLine, move);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));

    moveLineDefaultService.setFieldsFromFirstMoveLine(moveLine, move);
    moveLineService.balanceCreditDebit(moveLine, move);
    moveLineDefaultService.setIsOtherCurrency(moveLine, move);
    moveLineRecordService.setCurrencyFields(moveLine, move);
    moveLineDefaultService.setFinancialDiscount(moveLine);
    moveLineService.computeFinancialDiscount(moveLine);
    moveLineRecordService.setCounter(moveLine, move);

    valuesMap.put("counter", moveLine.getCounter());
    valuesMap.put("account", moveLine.getAccount());
    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("date", moveLine.getDate());
    valuesMap.put("dueDate", moveLine.getDueDate());
    valuesMap.put("originDate", moveLine.getOriginDate());
    valuesMap.put("origin", moveLine.getOrigin());
    valuesMap.put("description", moveLine.getDescription());
    valuesMap.put("credit", moveLine.getCredit());
    valuesMap.put("debit", moveLine.getDebit());
    valuesMap.put("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
    valuesMap.put("taxLine", moveLine.getTaxLine());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
    valuesMap.put("interbankCodeLine", moveLine.getInterbankCodeLine());
    valuesMap.put("exportedDirectDebitOk", moveLine.getExportedDirectDebitOk());
    valuesMap.put("isOtherCurrency", moveLine.getIsOtherCurrency());
    valuesMap.put("currencyRate", moveLine.getCurrencyRate());
    valuesMap.put("currencyAmount", moveLine.getCurrencyAmount());
    valuesMap.put("financialDiscount", moveLine.getFinancialDiscount());
    valuesMap.put("financialDiscountRate", moveLine.getFinancialDiscountRate());
    valuesMap.put("financialDiscountTotalAmount", moveLine.getFinancialDiscountTotalAmount());
    valuesMap.put("remainingAmountAfterFinDiscount", moveLine.getRemainingAmountAfterFinDiscount());
    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));

    moveLineAttrsService.addAnalyticAxisAttrs(move, attrsMap);
    moveLineAttrsService.addDescriptionRequired(move, attrsMap);
    moveLineAttrsService.addTaxLineRequired(move, moveLine, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {

    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));

    moveLineAttrsService.addShowTaxAmount(moveLine, attrsMap);
    moveLineAttrsService.addInvoiceTermListPercentageWarningText(moveLine, attrsMap);

    if (move != null) {
      moveLineAttrsService.addAnalyticDistributionTypeSelect(move, attrsMap);
      moveLineAttrsService.addReadonly(moveLine, move, attrsMap);
      moveLineAttrsService.addDescriptionRequired(move, attrsMap);
      moveLineAttrsService.addAnalyticAxisAttrs(move, attrsMap);
      moveLineAttrsService.addTaxLineRequired(move, moveLine, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadMoveAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));

    moveLineAttrsService.addInvoiceTermListPercentageWarningText(moveLine, attrsMap);
    moveLineAttrsService.addShowTaxAmount(moveLine, attrsMap);

    if (move != null) {
      moveLineAttrsService.addReadonly(moveLine, move, attrsMap);
      moveLineAttrsService.addDescriptionRequired(move, attrsMap);
      moveLineAttrsService.addAnalyticAxisAttrs(move, attrsMap);
      moveLineAttrsService.addValidatePeriod(move, attrsMap);
      moveLineAttrsService.addAnalyticDistributionTypeSelect(move, attrsMap);
      moveLineAttrsService.addShowAnalyticDistributionPanel(move, moveLine, attrsMap);
      moveLineAttrsService.addTaxLineRequired(move, moveLine, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(
      MoveLine moveLine, Move move) throws AxelorException {
    moveLineComputeAnalyticService.clearAnalyticAccounting(moveLine);
    moveLineCheckService.checkAnalyticByTemplate(moveLine);
    moveLineComputeAnalyticService.createAnalyticDistributionWithTemplate(moveLine, move);
    analyticLineService.printAnalyticAccount(moveLine, move.getCompany());

    if (moveLine.getAnalyticDistributionTemplate() == null) {
      moveLineComputeAnalyticService.clearAnalyticAccounting(moveLine);
    }

    moveLineCheckService.checkAnalyticAxes(moveLine);

    return createAnalyticValuesMap(moveLine);
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnChangeAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addAnalyticAccountRequired(moveLine, move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    moveLineCheckService.checkDebitCredit(moveLine);
    moveLineDefaultService.cleanDebitCredit(moveLine);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);
    moveLineRecordService.setCurrencyFields(moveLine, move);
    moveLineService.computeFinancialDiscount(moveLine);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("credit", moveLine.getCredit());
    valuesMap.put("debit", moveLine.getDebit());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
    valuesMap.put("currencyRate", moveLine.getCurrencyRate());
    valuesMap.put("currencyAmount", moveLine.getCurrencyAmount());
    valuesMap.put("financialDiscountRate", moveLine.getFinancialDiscountRate());
    valuesMap.put("financialDiscountTotalAmount", moveLine.getFinancialDiscountTotalAmount());
    valuesMap.put("remainingAmountAfterFinDiscount", moveLine.getRemainingAmountAfterFinDiscount());
    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getDebitCreditInvoiceTermOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {
    Map<String, Object> valuesMap =
        new HashMap<>(this.getDebitCreditOnChangeValuesMap(moveLine, move));

    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

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
    moveLineRecordService.setCutOffDates(moveLine, cutOffStartDate, cutOffEndDate);
    moveLineRecordService.setIsCutOffGeneratedFalse(moveLine);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);
    moveLineRecordService.refreshAccountInformation(moveLine, move);
    moveLineDefaultService.setDefaultDistributionTemplate(moveLine, move);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));

    moveLineRecordService.setParentFromMove(moveLine, move);
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("cutOffStartDate", moveLine.getCutOffStartDate());
    valuesMap.put("cutOffEndDate", moveLine.getCutOffEndDate());
    valuesMap.put("isCutOffGenerated", moveLine.getCutOffEndDate());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
    valuesMap.put("taxLine", moveLine.getTaxLine());
    valuesMap.put("taxEquiv", moveLine.getTaxEquiv());
    valuesMap.put("taxLineBeforeReverse", moveLine.getTaxLineBeforeReverse());
    valuesMap.put("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());
    valuesMap.put("vatSystemSelect", moveLine.getVatSystemSelect());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));

    moveLineAttrsService.addPartnerReadonly(moveLine, move, attrsMap);
    moveLineAttrsService.addAnalyticAxisAttrs(move, attrsMap);
    moveLineAttrsService.addTaxLineRequired(move, moveLine, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getAnalyticAxisOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    moveLineComputeAnalyticService.clearAnalyticAccountingIfEmpty(moveLine);
    moveLineComputeAnalyticService.analyzeMoveLine(moveLine, move.getCompany());
    moveLineComputeAnalyticService.clearAnalyticAccountingIfEmpty(moveLine);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticAxisOnChangeAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addAnalyticAccountRequired(moveLine, move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDateOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    computeDateOnChangeValues(moveLine, move);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("originDate", moveLine.getOriginDate());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());

    return valuesMap;
  }

  @Override
  public void computeDateOnChangeValues(MoveLine moveLine, Move move) throws AxelorException {
    moveLineRecordService.setOriginDate(moveLine);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);
    moveLineToolService.checkDateInPeriod(move, moveLine);
  }

  @Override
  public Map<String, Object> getCurrencyAmountRateOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {
    moveLineRecordService.setDebitCredit(moveLine);
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("debit", moveLine.getDebit());
    valuesMap.put("credit", moveLine.getCredit());
    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAccountOnSelectAttrsMap(
      Journal journal, Company company) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addAccountDomain(journal, company, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnSelectAttrsMap(MoveLine moveLine, Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addPartnerDomain(move, attrsMap);
    moveLineAttrsService.addPartnerReadonly(moveLine, move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnSelectAttrsMap(
      Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addAnalyticDistributionTemplateDomain(move, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOnLoadAnalyticDistributionValuesMap(Move move)
      throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    if (move != null) {
      valuesMap.put(
          "$validatePeriod",
          moveToolService.isTemporarilyClosurePeriodManage(
              move.getPeriod(), move.getJournal(), AuthUtils.getUser()));
    }

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAnalyticDistributionAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addShowAnalyticDistributionPanel(move, moveLine, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDebitOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {
    moveLineRecordService.resetCredit(moveLine);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getDebitCreditOnChangeValuesMap(moveLine, move));

    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getCreditOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException {
    moveLineRecordService.resetDebit(moveLine);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getDebitCreditOnChangeValuesMap(moveLine, move));

    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getPartnerOnChangeValuesMap(MoveLine moveLine) {
    moveLineInvoiceTermService.updateInvoiceTermsParentFields(moveLine);
    moveLineRecordService.resetPartnerFields(moveLine);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("partnerId", moveLine.getPartnerId());
    valuesMap.put("partnerSeq", moveLine.getPartnerSeq());
    valuesMap.put("partnerFullName", moveLine.getPartnerFullName());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAnalyticDistributionTemplateOnChangeLightValuesMap(
      MoveLine moveLine) {
    analyticLineService.checkAnalyticLineForAxis(moveLine);

    return createAnalyticValuesMap(moveLine);
  }

  @Override
  public Map<String, Object> getAnalyticMoveLineOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException {
    analyticLineService.printAnalyticAccount(moveLine, move.getCompany());

    return createAnalyticValuesMap(moveLine);
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticMoveLineOnChangeAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineAttrsService.addAnalyticAccountRequired(moveLine, move, attrsMap);

    return attrsMap;
  }

  protected Map<String, Object> createAnalyticValuesMap(MoveLine moveLine) {
    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("axis1AnalyticAccount", moveLine.getAxis1AnalyticAccount());
    valuesMap.put("axis2AnalyticAccount", moveLine.getAxis2AnalyticAccount());
    valuesMap.put("axis3AnalyticAccount", moveLine.getAxis3AnalyticAccount());
    valuesMap.put("axis4AnalyticAccount", moveLine.getAxis4AnalyticAccount());
    valuesMap.put("axis5AnalyticAccount", moveLine.getAxis5AnalyticAccount());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());

    return valuesMap;
  }
}
