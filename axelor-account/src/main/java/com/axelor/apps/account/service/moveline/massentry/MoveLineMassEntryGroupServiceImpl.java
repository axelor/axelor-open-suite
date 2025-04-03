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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MoveLineMassEntryGroupServiceImpl implements MoveLineMassEntryGroupService {

  protected MassEntryService massEntryService;
  protected MoveLineGroupService moveLineGroupService;
  protected MoveLineDefaultService moveLineDefaultService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected MoveLineService moveLineService;
  protected MoveLineMassEntryAttrsService moveLineMassEntryAttrsService;
  protected MoveLineMassEntryRecordService moveLineMassEntryRecordService;
  protected MoveLineCheckService moveLineCheckService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineRecordService moveLineRecordService;
  protected AnalyticAttrsService analyticAttrsService;
  protected MoveLineToolService moveLineToolService;
  protected MoveLineFinancialDiscountService moveLineFinancialDiscountService;

  @Inject
  public MoveLineMassEntryGroupServiceImpl(
      MassEntryService massEntryService,
      MoveLineGroupService moveLineGroupService,
      MoveLineDefaultService moveLineDefaultService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineService moveLineService,
      MoveLineMassEntryAttrsService moveLineMassEntryAttrsService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      MoveLineCheckService moveLineCheckService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineRecordService moveLineRecordService,
      AnalyticAttrsService analyticAttrsService,
      MoveLineToolService moveLineToolService,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService) {
    this.massEntryService = massEntryService;
    this.moveLineGroupService = moveLineGroupService;
    this.moveLineDefaultService = moveLineDefaultService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveLineService = moveLineService;
    this.moveLineMassEntryAttrsService = moveLineMassEntryAttrsService;
    this.moveLineMassEntryRecordService = moveLineMassEntryRecordService;
    this.moveLineCheckService = moveLineCheckService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveLineRecordService = moveLineRecordService;
    this.analyticAttrsService = analyticAttrsService;
    this.moveLineToolService = moveLineToolService;
    this.moveLineFinancialDiscountService = moveLineFinancialDiscountService;
  }

  public MoveLineMassEntry initializeValues(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    moveLine.setInputAction(MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE);
    moveLine =
        massEntryService.getFirstMoveLineMassEntryInformations(
            move.getMoveLineMassEntryList(), moveLine, move.getCompany());
    moveLineDefaultService.setAccountInformation(moveLine, move);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);

    return moveLine;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {

    Map<String, Object> valuesMap =
        new HashMap<>(
            moveLineGroupService.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));

    moveLineService.balanceCreditDebit(moveLine, move);
    moveLineDefaultService.setIsOtherCurrency(moveLine, move);
    moveLineMassEntryRecordService.setCurrencyRate(move, moveLine);
    moveLineDefaultService.setFinancialDiscount(moveLine);
    moveLineFinancialDiscountService.computeFinancialDiscount(moveLine, move);
    moveLineToolService.setDecimals(moveLine, move);

    valuesMap.put("inputAction", moveLine.getInputAction());
    valuesMap.put("temporaryMoveNumber", moveLine.getTemporaryMoveNumber());
    valuesMap.put("counter", moveLine.getCounter());
    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("partnerId", moveLine.getPartnerId());
    valuesMap.put("partnerSeq", moveLine.getPartnerSeq());
    valuesMap.put("partnerFullName", moveLine.getPartnerFullName());
    valuesMap.put("date", moveLine.getDate());
    valuesMap.put("dueDate", moveLine.getDueDate());
    valuesMap.put("originDate", moveLine.getOriginDate());
    valuesMap.put("origin", moveLine.getOrigin());
    valuesMap.put("moveStatusSelect", moveLine.getMoveStatusSelect());
    valuesMap.put("interbankCodeLine", moveLine.getInterbankCodeLine());
    valuesMap.put("moveDescription", moveLine.getMoveDescription());
    valuesMap.put("description", moveLine.getDescription());
    valuesMap.put("exportedDirectDebitOk", moveLine.getExportedDirectDebitOk());
    valuesMap.put("movePaymentCondition", moveLine.getMovePaymentCondition());
    valuesMap.put("movePaymentMode", moveLine.getMovePaymentMode());
    valuesMap.put("movePartnerBankDetails", moveLine.getMovePartnerBankDetails());
    valuesMap.put("account", moveLine.getAccount());
    valuesMap.put("taxLineSet", moveLine.getTaxLineSet());
    valuesMap.put("debit", moveLine.getDebit());
    valuesMap.put("credit", moveLine.getCredit());
    valuesMap.put("currencyRate", moveLine.getCurrencyRate());
    valuesMap.put("currencyAmount", moveLine.getCurrencyAmount());
    valuesMap.put("vatSystemSelect", moveLine.getVatSystemSelect());
    valuesMap.put("pfpValidatorUser", moveLine.getMovePfpValidatorUser());
    valuesMap.put("cutOffStartDate", moveLine.getCutOffStartDate());
    valuesMap.put("cutOffEndDate", moveLine.getCutOffEndDate());
    valuesMap.put("fieldsErrorList", moveLine.getFieldsErrorList());
    valuesMap.put("deliveryDate", moveLine.getDeliveryDate());
    valuesMap.put("isEdited", moveLine.getIsEdited());
    valuesMap.put("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
    valuesMap.put("currencyDecimals", moveLine.getCurrencyDecimals());
    valuesMap.put("companyCurrencyDecimals", moveLine.getCompanyCurrencyDecimals());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(moveLineGroupService.getOnNewAttrsMap(moveLine, move));
    moveLineMassEntryAttrsService.addCutOffReadonly(moveLine.getAccount(), attrsMap);
    moveLineMassEntryAttrsService.addMovePaymentModeReadOnly(attrsMap);
    moveLineMassEntryAttrsService.addPartnerBankDetailsReadOnly(moveLine, attrsMap);
    moveLineMassEntryAttrsService.addInputActionSelectionIn(move, attrsMap);
    moveLineMassEntryAttrsService.addTemporaryMoveNumberFocus(move, attrsMap);
    moveLineMassEntryAttrsService.addOriginRequired(moveLine, move.getJournal(), attrsMap);
    moveLineMassEntryAttrsService.addDescriptionRequired(move, attrsMap);

    if (move.getJournal() != null) {
      moveLineMassEntryAttrsService.addMovePaymentConditionRequired(
          move.getJournal().getJournalType(), attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    moveLineCheckService.checkDebitCredit(moveLine);
    moveLineDefaultService.cleanDebitCredit(moveLine);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);
    moveLineMassEntryRecordService.setCurrencyRate(move, moveLine);
    moveLineFinancialDiscountService.computeFinancialDiscount(moveLine, move);

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
  public Map<String, Object> getDebitOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move, LocalDate dueDate) throws AxelorException {
    moveLineRecordService.resetCredit(moveLine);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getDebitCreditOnChangeValuesMap(moveLine, move));

    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getCreditOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move, LocalDate dueDate) throws AxelorException {
    moveLineMassEntryRecordService.resetDebit(moveLine);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getDebitCreditOnChangeValuesMap(moveLine, move));

    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);

    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAccountOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move, LocalDate dueDate) throws AxelorException {
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);
    moveLineRecordService.setIsCutOffGeneratedFalse(moveLine);
    moveLineMassEntryRecordService.refreshAccountInformation(moveLine, move);
    moveLineDefaultService.setDefaultDistributionTemplate(moveLine, move);
    moveLineMassEntryRecordService.setMovePfpValidatorUser(moveLine, move.getCompany());

    Map<String, Object> valuesMap =
        new HashMap<>(
            moveLineGroupService.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);
    moveLineMassEntryRecordService.setCutOff(moveLine);
    moveLineMassEntryRecordService.fillAnalyticMoveLineMassEntryList(moveLine);

    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("cutOffStartDate", moveLine.getCutOffStartDate());
    valuesMap.put("cutOffEndDate", moveLine.getCutOffEndDate());
    valuesMap.put("isCutOffGenerated", moveLine.getCutOffEndDate());
    valuesMap.put("analyticMoveLineMassEntryList", moveLine.getAnalyticMoveLineMassEntryList());
    valuesMap.put("taxLineSet", moveLine.getTaxLineSet());
    valuesMap.put("taxEquiv", moveLine.getTaxEquiv());
    valuesMap.put("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
    valuesMap.put("invoiceTermList", moveLine.getInvoiceTermList());
    valuesMap.put("movePfpValidatorUser", moveLine.getMovePfpValidatorUser());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(
      MoveLineMassEntry moveLine, Move move) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(
            moveLineGroupService.getAnalyticDistributionTemplateOnChangeAttrsMap(moveLine, move));

    analyticAttrsService.addAnalyticAxisAttrs(
        move.getCompany(), move.getMassEntryStatusSelect(), attrsMap);
    moveLineMassEntryAttrsService.addDebitCreditFocus(
        moveLine.getAccount(), moveLine.getIsOtherCurrency(), attrsMap);
    moveLineMassEntryAttrsService.addMovePfpValidatorUserReadOnly(moveLine, attrsMap);
    moveLineMassEntryAttrsService.addMovePfpValidatorUserRequired(
        moveLine.getAccount(), move.getJournal(), move.getCompany(), attrsMap);
    moveLineMassEntryAttrsService.addCutOffReadonly(moveLine.getAccount(), attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPfpValidatorOnSelectAttrsMap(
      MoveLineMassEntry moveLine, Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineMassEntryAttrsService.addPfpValidatorUserDomain(
        moveLine.getPartner(), move.getCompany(), attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOriginDateOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    moveLineMassEntryRecordService.setCurrencyRate(move, moveLine);

    valuesMap.put("isEdited", MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_EXCEPT_VAT_SYSTEM);
    valuesMap.put("currencyRate", moveLine.getCurrencyRate());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOriginDateOnChangeAttrsMap(
      MoveLineMassEntry moveLine, Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineMassEntryAttrsService.addOriginRequired(moveLine, move.getJournal(), attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getPartnerOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    moveLineMassEntryRecordService.setPartner(moveLine, move);

    Map<String, Object> valuesMap =
        new HashMap<>(this.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));

    if (move != null) {
      moveLineMassEntryRecordService.setMovePfpValidatorUser(moveLine, move.getCompany());
      valuesMap.put("movePfpValidatorUser", moveLine.getMovePfpValidatorUser());
    }

    valuesMap.put("isEdited", MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_ALL);
    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("partnerId", moveLine.getPartnerId());
    valuesMap.put("partnerSeq", moveLine.getPartnerSeq());
    valuesMap.put("partnerFullName", moveLine.getPartnerFullName());
    valuesMap.put("movePartnerBankDetails", moveLine.getMovePartnerBankDetails());
    valuesMap.put("vatSystemSelect", moveLine.getVatSystemSelect());
    valuesMap.put("taxLineSet", moveLine.getTaxLineSet());
    valuesMap.put("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
    valuesMap.put("currencyCode", moveLine.getCurrencyCode());
    valuesMap.put("movePaymentMode", moveLine.getMovePaymentMode());
    valuesMap.put("movePaymentCondition", moveLine.getMovePaymentCondition());
    valuesMap.put("account", moveLine.getAccount());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnChangeAttrsMap(MoveLineMassEntry moveLine) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineMassEntryAttrsService.addMovePfpValidatorUserReadOnly(moveLine, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getInputActionOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move) {
    Map<String, Object> valuesMap = new HashMap<>();

    moveLine = moveLineMassEntryRecordService.setInputAction(moveLine, move);
    this.setAllMoveLineValuesMap(moveLine, valuesMap);

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getInputActionOnChangeAttrsMap(
      boolean isCounterpartLine, MoveLineMassEntry moveLine) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    moveLineMassEntryAttrsService.addReadonly(isCounterpartLine, moveLine.getAccount(), attrsMap);
    moveLineMassEntryAttrsService.addRequired(isCounterpartLine, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getTemporaryMoveNumberOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move) {
    Map<String, Object> valuesMap = new HashMap<>();

    moveLine =
        massEntryService.getFirstMoveLineMassEntryInformations(
            move.getMoveLineMassEntryList(), moveLine, move.getCompany());

    this.setAllMoveLineValuesMap(moveLine, valuesMap);
    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getTemporaryMoveNumberOnChangeAttrsMap(Move move) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())) {
      moveLineMassEntryAttrsService.addInputActionReadonly(false, attrsMap);
      moveLineMassEntryAttrsService.addTemporaryMoveNumberFocus(attrsMap);
    }

    return attrsMap;
  }

  protected void setAllMoveLineValuesMap(
      MoveLineMassEntry moveLine, Map<String, Object> valuesMap) {
    valuesMap.put("temporaryMoveNumber", moveLine.getTemporaryMoveNumber());
    valuesMap.put("counter", moveLine.getCounter());
    valuesMap.put("date", moveLine.getDate());
    valuesMap.put("origin", moveLine.getOrigin());
    valuesMap.put("originDate", moveLine.getOriginDate());
    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("partnerId", moveLine.getPartnerId());
    valuesMap.put("partnerSeq", moveLine.getPartnerSeq());
    valuesMap.put("partnerFullName", moveLine.getPartnerFullName());
    valuesMap.put("moveDescription", moveLine.getMoveDescription());
    valuesMap.put("movePaymentCondition", moveLine.getMovePaymentCondition());
    valuesMap.put("movePaymentMode", moveLine.getMovePaymentMode());
    valuesMap.put("movePartnerBankDetails", moveLine.getMovePartnerBankDetails());
    valuesMap.put("account", moveLine.getAccount());
    valuesMap.put("taxLineSet", moveLine.getTaxLineSet());
    valuesMap.put("description", moveLine.getDescription());
    valuesMap.put("debit", moveLine.getDebit());
    valuesMap.put("credit", moveLine.getCredit());
    valuesMap.put("currencyRate", moveLine.getCurrencyRate());
    valuesMap.put("currencyAmount", moveLine.getCurrencyAmount());
    valuesMap.put("moveStatusSelect", moveLine.getMoveStatusSelect());
    valuesMap.put("vatSystemSelect", moveLine.getVatSystemSelect());
    valuesMap.put("movePfpValidatorUser", moveLine.getMovePfpValidatorUser());
    valuesMap.put("cutOffStartDate", moveLine.getCutOffStartDate());
    valuesMap.put("cutOffEndDate", moveLine.getCutOffEndDate());
    valuesMap.put("currencyDecimals", moveLine.getCurrencyDecimals());
    valuesMap.put("companyCurrencyDecimals", moveLine.getCompanyCurrencyDecimals());
    valuesMap.put("isEdited", moveLine.getIsEdited());
    valuesMap.put("fieldsErrorList", moveLine.getFieldsErrorList());
    valuesMap.put("analyticDistributionTemplate", moveLine.getAnalyticDistributionTemplate());
    valuesMap.put("axis1AnalyticAccount", moveLine.getAxis1AnalyticAccount());
    valuesMap.put("axis2AnalyticAccount", moveLine.getAxis2AnalyticAccount());
    valuesMap.put("axis3AnalyticAccount", moveLine.getAxis3AnalyticAccount());
    valuesMap.put("axis4AnalyticAccount", moveLine.getAxis4AnalyticAccount());
    valuesMap.put("axis5AnalyticAccount", moveLine.getAxis5AnalyticAccount());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
    valuesMap.put("inputAction", moveLine.getInputAction());
  }

  @Override
  public Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move) throws AxelorException {
    Map<String, Object> valuesMap =
        moveLineGroupService.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move);
    moveLineMassEntryRecordService.fillAnalyticMoveLineMassEntryList(moveLine);

    valuesMap.put("analyticMoveLineMassEntryList", moveLine.getAnalyticMoveLineMassEntryList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAnalyticDistributionTemplateOnChangeLightValuesMap(
      MoveLineMassEntry moveLine) throws AxelorException {
    Map<String, Object> valuesMap =
        moveLineGroupService.getAnalyticDistributionTemplateOnChangeLightValuesMap(moveLine);
    moveLineMassEntryRecordService.fillAnalyticMoveLineMassEntryList(moveLine);

    valuesMap.put("analyticMoveLineMassEntryList", moveLine.getAnalyticMoveLineMassEntryList());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAnalyticAxisOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    Map<String, Object> valuesMap =
        moveLineGroupService.getAnalyticAxisOnChangeValuesMap(moveLine, move);
    moveLineMassEntryRecordService.fillAnalyticMoveLineMassEntryList(moveLine);

    valuesMap.put("analyticMoveLineMassEntryList", moveLine.getAnalyticMoveLineMassEntryList());

    return valuesMap;
  }
}
