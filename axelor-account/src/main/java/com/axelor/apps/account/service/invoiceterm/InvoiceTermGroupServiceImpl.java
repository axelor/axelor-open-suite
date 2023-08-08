package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceTermGroupServiceImpl implements InvoiceTermGroupService {

  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermAttrsService invoiceTermAttrsService;
  protected InvoiceTermPfpService invoiceTermPfpService;

  @Inject
  public InvoiceTermGroupServiceImpl(
      InvoiceTermService invoiceTermService,
      InvoiceTermAttrsService invoiceTermAttrsService,
      InvoiceTermPfpService invoiceTermPfpService) {
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermAttrsService = invoiceTermAttrsService;
    this.invoiceTermPfpService = invoiceTermPfpService;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(InvoiceTerm invoiceTerm) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();
    invoiceTerm = invoiceTermService.initInvoiceTermWithParents(invoiceTerm);
    invoiceTermService.setPfpStatus(invoiceTerm, null);

    valuesMap.put("invoice", invoiceTerm.getInvoice());
    valuesMap.put("moveLine", invoiceTerm.getMoveLine());
    valuesMap.put("paymentMode", invoiceTerm.getPaymentMode());
    valuesMap.put("bankDetails", invoiceTerm.getBankDetails());
    valuesMap.put("sequence", invoiceTerm.getSequence());
    valuesMap.put("isCustomized", invoiceTerm.getIsCustomized());
    valuesMap.put("isPaid", invoiceTerm.getIsPaid());
    valuesMap.put("percentage", invoiceTerm.getPercentage());
    valuesMap.put("amount", invoiceTerm.getAmount());
    valuesMap.put("amountRemaining", invoiceTerm.getAmountRemaining());
    valuesMap.put("companyAmount", invoiceTerm.getCompanyAmount());
    valuesMap.put("companyAmountRemaining", invoiceTerm.getCompanyAmountRemaining());
    valuesMap.put("dueDate", invoiceTerm.getDueDate());
    valuesMap.put("isHoldBack", invoiceTerm.getIsHoldBack());
    valuesMap.put("company", invoiceTerm.getCompany());
    valuesMap.put("partner", invoiceTerm.getPartner());
    valuesMap.put("currency", invoiceTerm.getCurrency());
    valuesMap.put("origin", invoiceTerm.getOrigin());
    valuesMap.put("originDate", invoiceTerm.getOriginDate());
    putFinancialDiscountFields(invoiceTerm, valuesMap);
    valuesMap.put("pfpValidateStatusSelect", invoiceTerm.getPfpValidateStatusSelect());

    return valuesMap;
  }

  @Override
  public Map<String, Object> getOnLoadValuesMap(InvoiceTerm invoiceTerm) {

    Map<String, Object> valuesMap = this.checkPfpValidatorUser(invoiceTerm);

    valuesMap.put("$isMultiCurrency", invoiceTermService.isMultiCurrency(invoiceTerm));

    valuesMap.put(
        "$showFinancialDiscount", invoiceTermService.setShowFinancialDiscount(invoiceTerm));
    valuesMap.put("$invoiceTermMoveFile", invoiceTermService.getLinkedDmsFile(invoiceTerm));

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAmountOnChangeValuesMap(InvoiceTerm invoiceTerm) {
    Map<String, Object> valuesMap = new HashMap<>();

    invoiceTermService.computeCustomizedPercentage(invoiceTerm);
    invoiceTermService.computeFinancialDiscount(invoiceTerm);

    valuesMap.put("percentage", invoiceTerm.getPercentage());
    valuesMap.put("amountRemaining", invoiceTerm.getAmountRemaining());
    valuesMap.put("companyAmount", invoiceTerm.getCompanyAmount());
    valuesMap.put("companyAmountRemaining", invoiceTerm.getCompanyAmountRemaining());
    putFinancialDiscountFields(invoiceTerm, valuesMap);
    valuesMap.put(
        "isCustomized",
        invoiceTerm.getPaymentConditionLine() == null
            || invoiceTerm
                    .getPercentage()
                    .compareTo(invoiceTerm.getPaymentConditionLine().getPaymentPercentage())
                != 0);

    return valuesMap;
  }

  protected void putFinancialDiscountFields(
      InvoiceTerm invoiceTerm, Map<String, Object> valuesMap) {
    valuesMap.put("applyFinancialDiscount", invoiceTerm.getApplyFinancialDiscount());
    valuesMap.put("financialDiscount", invoiceTerm.getFinancialDiscount());
    valuesMap.put("financialDiscountDeadlineDate", invoiceTerm.getFinancialDiscountDeadlineDate());
    valuesMap.put("financialDiscountAmount", invoiceTerm.getFinancialDiscountAmount());
    valuesMap.put(
        "remainingAmountAfterFinDiscount", invoiceTerm.getRemainingAmountAfterFinDiscount());
    valuesMap.put(
        "amountRemainingAfterFinDiscount", invoiceTerm.getAmountRemainingAfterFinDiscount());
    valuesMap.put(
        "$showFinancialDiscount", invoiceTermService.setShowFinancialDiscount(invoiceTerm));
  }

  @Override
  public Map<String, Object> checkPfpValidatorUser(InvoiceTerm invoiceTerm) {
    Map<String, Object> valuesMap = new HashMap<>();
    valuesMap.put(
        "$isValidPfpValidatorUser",
        invoiceTermPfpService.isPfpValidatorUser(invoiceTerm, AuthUtils.getUser()));

    valuesMap.put(
        "$isSelectedPfpValidatorEqualsPartnerPfpValidator",
        invoiceTerm
            .getPfpValidatorUser()
            .equals(
                invoiceTermService.getPfpValidatorUser(
                    invoiceTerm.getPartner(), invoiceTerm.getCompany())));

    valuesMap.put(
        "$isPaymentConditionFree", invoiceTermService.isPaymentConditionFree(invoiceTerm));

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(InvoiceTerm invoiceTerm) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    invoiceTermAttrsService.hideActionAndPfpPanel(invoiceTerm, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(InvoiceTerm invoiceTerm) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    invoiceTermAttrsService.changeAmountsTitle(invoiceTerm, attrsMap);

    invoiceTermAttrsService.hideActionAndPfpPanel(invoiceTerm, attrsMap);

    return attrsMap;
  }
}
