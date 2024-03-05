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
package com.axelor.apps.contract.service.attributes;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.service.CurrencyScaleServiceContract;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ContractLineAttrsServiceImpl implements ContractLineAttrsService {

  protected CurrencyScaleServiceContract currencyScaleServiceContract;
  protected AppAccountService appAccountService;

  @Inject
  public ContractLineAttrsServiceImpl(
      CurrencyScaleServiceContract currencyScaleServiceContract,
      AppAccountService appAccountService) {
    this.currencyScaleServiceContract = currencyScaleServiceContract;
    this.appAccountService = appAccountService;
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

  protected void addQtyScale(Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("qty", prefix),
        "scale",
        appAccountService.getNbDecimalDigitForQty(),
        attrsMap);
  }

  protected void addPriceScale(Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("price", prefix),
        "scale",
        appAccountService.getNbDecimalDigitForUnitPrice(),
        attrsMap);
  }

  protected void addDiscountScales(Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("discountAmount", prefix),
        "scale",
        appAccountService.getNbDecimalDigitForUnitPrice(),
        attrsMap);
    this.addAttr(
        this.computeField("priceDiscounted", prefix),
        "scale",
        appAccountService.getNbDecimalDigitForUnitPrice(),
        attrsMap);
  }

  @Override
  public Map<String, Map<String, Object>> setScaleAndPrecision(Contract contract, String prefix) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (contract != null) {
      int currencyScale = currencyScaleServiceContract.getScale(contract);

      this.addAttr(this.computeField("exTaxTotal", prefix), "scale", currencyScale, attrsMap);
      this.addAttr(this.computeField("inTaxTotal", prefix), "scale", currencyScale, attrsMap);

      this.addAttr(
          this.computeField("initialPricePerYear", prefix), "scale", currencyScale, attrsMap);
      this.addAttr(
          this.computeField("yearlyPriceRevalued", prefix), "scale", currencyScale, attrsMap);
    }

    this.addQtyScale(attrsMap, prefix);
    this.addPriceScale(attrsMap, prefix);
    this.addDiscountScales(attrsMap, prefix);

    return attrsMap;
  }
}
