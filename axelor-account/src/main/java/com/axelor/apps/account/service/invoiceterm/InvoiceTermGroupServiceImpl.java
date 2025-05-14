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
package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceTermGroupServiceImpl implements InvoiceTermGroupService {

  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermAttrsService invoiceTermAttrsService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected CurrencyScaleService currencyScaleService;
  protected InvoiceTermRecordService invoiceTermRecordService;
  protected InvoiceTermPfpToolService invoiceTermPfpToolService;

  @Inject
  public InvoiceTermGroupServiceImpl(
      InvoiceTermService invoiceTermService,
      InvoiceTermAttrsService invoiceTermAttrsService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermRecordService invoiceTermRecordService,
      InvoiceTermPfpToolService invoiceTermPfpToolService) {
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermAttrsService = invoiceTermAttrsService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermRecordService = invoiceTermRecordService;
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(InvoiceTerm invoiceTerm) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();
    invoiceTerm = invoiceTermService.initInvoiceTermWithParents(invoiceTerm);
    invoiceTermService.setPfpStatus(invoiceTerm, null);
    invoiceTermFinancialDiscountService.computeFinancialDiscount(invoiceTerm);

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
    valuesMap.put(
        "$isPaymentConditionFree", invoiceTermService.isPaymentConditionFree(invoiceTerm));
    valuesMap.put("$isMultiCurrency", invoiceTermService.isMultiCurrency(invoiceTerm));
    valuesMap.put("$companyCurrencyScale", currencyScaleService.getCompanyScale(invoiceTerm));

    return valuesMap;
  }

  @Override
  public Map<String, Object> getOnLoadValuesMap(InvoiceTerm invoiceTerm) {

    Map<String, Object> valuesMap = this.checkPfpValidatorUser(invoiceTerm);

    valuesMap.put("$isMultiCurrency", invoiceTermService.isMultiCurrency(invoiceTerm));
    valuesMap.put("$companyCurrencyScale", currencyScaleService.getCompanyScale(invoiceTerm));

    valuesMap.put(
        "$showFinancialDiscount", invoiceTermService.setShowFinancialDiscount(invoiceTerm));
    valuesMap.put("$invoiceTermMoveFile", invoiceTermService.getLinkedDmsFile(invoiceTerm));

    return valuesMap;
  }

  @Override
  public Map<String, Object> getAmountOnChangeValuesMap(InvoiceTerm invoiceTerm) {
    Map<String, Object> valuesMap = new HashMap<>();

    invoiceTermService.computeCustomizedPercentage(invoiceTerm);
    invoiceTermFinancialDiscountService.computeFinancialDiscount(invoiceTerm);

    valuesMap.put("percentage", invoiceTerm.getPercentage());
    valuesMap.put("amountRemaining", invoiceTerm.getAmountRemaining());
    valuesMap.put("companyAmount", invoiceTerm.getCompanyAmount());
    valuesMap.put("companyAmountRemaining", invoiceTerm.getCompanyAmountRemaining());
    putFinancialDiscountFields(invoiceTerm, valuesMap);
    valuesMap.put("isCustomized", invoiceTermRecordService.computeIsCustomized(invoiceTerm));

    return valuesMap;
  }

  @Override
  public Map<String, Object> getPercentageOnChangeValuesMap(InvoiceTerm invoiceTerm) {
    Map<String, Object> valuesMap = new HashMap<>();

    invoiceTermService.setCustomizedAmounts(invoiceTerm);
    invoiceTermFinancialDiscountService.computeFinancialDiscount(invoiceTerm);

    valuesMap.put("amount", invoiceTerm.getAmount());
    valuesMap.put("amountRemaining", invoiceTerm.getAmountRemaining());
    valuesMap.put("companyAmount", invoiceTerm.getCompanyAmount());
    valuesMap.put("companyAmountRemaining", invoiceTerm.getCompanyAmountRemaining());
    putFinancialDiscountFields(invoiceTerm, valuesMap);
    valuesMap.put("isCustomized", invoiceTermRecordService.computeIsCustomized(invoiceTerm));

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
        invoiceTermPfpToolService.isPfpValidatorUser(invoiceTerm, AuthUtils.getUser()));

    if (invoiceTerm.getPfpValidatorUser() != null) {
      valuesMap.put(
          "$isSelectedPfpValidatorEqualsPartnerPfpValidator",
          invoiceTermPfpToolService.checkPfpValidatorUser(invoiceTerm));
    }

    valuesMap.put(
        "$isPaymentConditionFree", invoiceTermService.isPaymentConditionFree(invoiceTerm));

    return valuesMap;
  }

  @Override
  public Map<String, Object> setPfpValidatorUserDomainValuesMap(InvoiceTerm invoiceTerm) {
    Map<String, Object> valuesMap = new HashMap<>();

    if (invoiceTerm.getPfpValidatorUser() == null
        || invoiceTerm.getPfpValidateStatusSelect() != InvoiceTermRepository.PFP_STATUS_AWAITING) {
      return valuesMap;
    }

    valuesMap.put(
        "$isSelectedPfpValidatorEqualsPartnerPfpValidator",
        invoiceTermPfpToolService.checkPfpValidatorUser(invoiceTerm));

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

  @Override
  public Map<String, Map<String, Object>> setPfpValidatorUserDomainAttrsMap(
      InvoiceTerm invoiceTerm) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    invoiceTermAttrsService.setPfpValidatorUserDomainAttrsMap(invoiceTerm, attrsMap);

    return attrsMap;
  }
}
