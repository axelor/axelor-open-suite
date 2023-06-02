package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.moveline.MoveLineAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultService;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MoveLineMassEntryGroupServiceImpl implements MoveLineMassEntryGroupService {

  protected MassEntryService massEntryService;
  protected MoveLineGroupService moveLineGroupService;
  protected MoveLineAttrsService moveLineAttrsService;
  protected MoveLineDefaultService moveLineDefaultService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected MoveLineService moveLineService;
  protected MoveLineMassEntryAttrsService moveLineMassEntryAttrsService;
  protected MoveLineMassEntryRecordService moveLineMassEntryRecordService;
  protected MoveLineCheckService moveLineCheckService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineRecordService moveLineRecordService;

  @Inject
  public MoveLineMassEntryGroupServiceImpl(
      MassEntryService massEntryService,
      MoveLineGroupService moveLineGroupService,
      MoveLineAttrsService moveLineAttrsService,
      MoveLineDefaultService moveLineDefaultService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineService moveLineService,
      MoveLineMassEntryAttrsService moveLineMassEntryAttrsService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      MoveLineCheckService moveLineCheckService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineRecordService moveLineRecordService) {
    this.massEntryService = massEntryService;
    this.moveLineGroupService = moveLineGroupService;
    this.moveLineAttrsService = moveLineAttrsService;
    this.moveLineDefaultService = moveLineDefaultService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveLineService = moveLineService;
    this.moveLineMassEntryAttrsService = moveLineMassEntryAttrsService;
    this.moveLineMassEntryRecordService = moveLineMassEntryRecordService;
    this.moveLineCheckService = moveLineCheckService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveLineRecordService = moveLineRecordService;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {

    moveLine.setInputAction(MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE);
    moveLine =
        massEntryService.getFirstMoveLineMassEntryInformations(
            move.getMoveLineMassEntryList(), moveLine);
    moveLineDefaultService.setAccountInformation(moveLine, move);
    moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine, move);

    Map<String, Object> valuesMap =
        new HashMap<>(
            moveLineGroupService.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));

    moveLineService.balanceCreditDebit(moveLine, move);
    moveLineDefaultService.setIsOtherCurrency(moveLine, move);
    moveLineMassEntryRecordService.setCurrencyRate(move, moveLine);
    moveLineDefaultService.setFinancialDiscount(moveLine);
    moveLineService.computeFinancialDiscount(moveLine);

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
    valuesMap.put("taxLine", moveLine.getTaxLine());
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

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap =
        new HashMap<>(moveLineGroupService.getOnNewAttrsMap(moveLine, move));
    moveLineMassEntryAttrsService.addCutOffReadOnly(moveLine.getAccount(), attrsMap);
    moveLineMassEntryAttrsService.addMovePaymentModeReadOnly(attrsMap);
    moveLineMassEntryAttrsService.addPartnerBankDetailsReadOnly(moveLine, attrsMap);
    moveLineMassEntryAttrsService.addInputActionSelectionIn(move, attrsMap);
    moveLineMassEntryAttrsService.addTemporaryMoveNumberFocus(move, attrsMap);

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
    moveLineRecordService.refreshAccountInformation(moveLine, move);
    moveLineDefaultService.setDefaultDistributionTemplate(moveLine, move);
    moveLineMassEntryRecordService.setMovePfpValidatorUser(moveLine, move.getCompany());

    Map<String, Object> valuesMap =
        new HashMap<>(
            moveLineGroupService.getAnalyticDistributionTemplateOnChangeValuesMap(moveLine, move));
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, dueDate, false);
    moveLineMassEntryRecordService.setCutOff(moveLine);

    valuesMap.put("partner", moveLine.getPartner());
    valuesMap.put("cutOffStartDate", moveLine.getCutOffStartDate());
    valuesMap.put("cutOffEndDate", moveLine.getCutOffEndDate());
    valuesMap.put("isCutOffGenerated", moveLine.getCutOffEndDate());
    valuesMap.put("analyticMoveLineList", moveLine.getAnalyticMoveLineList());
    valuesMap.put("taxLine", moveLine.getTaxLine());
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

    moveLineAttrsService.addAnalyticAxisAttrs(move, attrsMap);
    moveLineMassEntryAttrsService.addDebitCreditFocus(
        moveLine.getAccount(), moveLine.getIsOtherCurrency(), attrsMap);
    moveLineMassEntryAttrsService.addMovePfpValidatorUserReadOnly(moveLine, attrsMap);
    moveLineMassEntryAttrsService.addMovePfpValidatorUserRequired(moveLine.getAccount(), attrsMap);
    moveLineMassEntryAttrsService.addCutOffReadOnly(moveLine.getAccount(), attrsMap);

    return attrsMap;
  }
}
