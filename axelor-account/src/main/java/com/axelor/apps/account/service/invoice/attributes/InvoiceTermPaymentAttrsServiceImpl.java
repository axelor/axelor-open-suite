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
package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceTermPaymentAttrsServiceImpl implements InvoiceTermPaymentAttrsService {

  protected CurrencyScaleService currencyScaleService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public InvoiceTermPaymentAttrsServiceImpl(
      CurrencyScaleService currencyScaleService, InvoiceTermService invoiceTermService) {
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermService = invoiceTermService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addIsMultiCurrency(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "$isMultiCurrency", "value", invoiceTermService.isMultiCurrency(invoiceTerm), attrsMap);
  }

  @Override
  public void addPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("paidAmount", "scale", currencyScaleService.getScale(invoiceTerm), attrsMap);
  }

  @Override
  public void addCompanyPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "companyPaidAmount", "scale", currencyScaleService.getCompanyScale(invoiceTerm), attrsMap);
  }

  @Override
  public void addFinancialDiscountAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("paidAmount", "scale", currencyScaleService.getScale(invoiceTerm), attrsMap);
  }
}
