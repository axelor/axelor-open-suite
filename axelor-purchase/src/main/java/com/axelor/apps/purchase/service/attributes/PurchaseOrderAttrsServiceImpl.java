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
package com.axelor.apps.purchase.service.attributes;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.CurrencyScaleServicePurchase;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class PurchaseOrderAttrsServiceImpl implements PurchaseOrderAttrsService {

  protected CurrencyScaleServicePurchase currencyScaleServicePurchase;

  @Inject
  public PurchaseOrderAttrsServiceImpl(CurrencyScaleServicePurchase currencyScaleServicePurchase) {
    this.currencyScaleServicePurchase = currencyScaleServicePurchase;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void setPurchaseOrderLineScale(
      PurchaseOrder purchaseOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleServicePurchase.getScale(purchaseOrder);

    this.addAttr("purchaseOrderLineList.exTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("purchaseOrderLineList.inTaxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public void setPurchaseOrderLineTaxScale(
      PurchaseOrder purchaseOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleServicePurchase.getScale(purchaseOrder);

    this.addAttr("purchaseOrderLineTaxList.inTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("purchaseOrderLineTaxList.exTaxBase", "scale", currencyScale, attrsMap);
    this.addAttr("purchaseOrderLineTaxList.taxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public Map<String, Map<String, Object>> onChangePurchaseOrderLine(PurchaseOrder purchaseOrder) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (purchaseOrder != null && ObjectUtils.notEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      this.setPurchaseOrderLineScale(purchaseOrder, attrsMap);
      this.setPurchaseOrderLineTaxScale(purchaseOrder, attrsMap);
    }

    return attrsMap;
  }
}
