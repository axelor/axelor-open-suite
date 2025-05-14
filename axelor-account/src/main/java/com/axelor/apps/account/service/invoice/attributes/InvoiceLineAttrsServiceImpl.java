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
package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineAttrsServiceImpl implements InvoiceLineAttrsService {

  protected CurrencyScaleService currencyScaleService;
  protected AppBaseService appBaseService;

  @Inject
  public InvoiceLineAttrsServiceImpl(
      CurrencyScaleService currencyScaleService, AppBaseService appBaseService) {
    this.currencyScaleService = currencyScaleService;
    this.appBaseService = appBaseService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  protected String computeField(String field, String prefix) {
    return String.format("%s%s", prefix, field);
  }

  @Override
  public void addInTaxPriceScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("inTaxPrice", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("exTaxTotal", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("inTaxTotal", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addCompanyExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("companyExTaxTotal", prefix),
        "scale",
        currencyScaleService.getCompanyScale(invoice),
        attrsMap);
  }

  @Override
  public void addCompanyInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("companyInTaxTotal", prefix),
        "scale",
        currencyScaleService.getCompanyScale(invoice),
        attrsMap);
  }

  @Override
  public void addCoefficientScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("coefficient", prefix),
        "scale",
        appBaseService.getNbDecimalDigitForUnitPrice() + 2,
        attrsMap);
  }

  @Override
  public void addTaxLineSetDomain(
      int operationTypeSelect, Map<String, Map<String, Object>> attrsMap) {
    String domain = "(self.endDate > :__date__ OR self.endDate IS NULL) ";
    if (operationTypeSelect == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
        || operationTypeSelect == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND) {
      domain += " AND self.tax.isNonDeductibleTax = false ";
    }

    this.addAttr("taxLineSet", "domain", domain, attrsMap);
  }
}
