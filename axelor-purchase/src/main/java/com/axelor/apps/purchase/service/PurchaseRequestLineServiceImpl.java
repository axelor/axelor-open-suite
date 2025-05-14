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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseRequestLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PurchaseRequestLineServiceImpl implements PurchaseRequestLineService {

  protected PurchaseRequestLineRepository purchaseRequestLineRepository;

  @Inject
  public PurchaseRequestLineServiceImpl(
      PurchaseRequestLineRepository purchaseRequestLineRepository) {
    this.purchaseRequestLineRepository = purchaseRequestLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createPurchaseRequestLine(
      PurchaseRequest purchaseRequest,
      Product product,
      String productTitle,
      Unit unit,
      BigDecimal quantity) {
    PurchaseRequestLine purchaseRequestLine = new PurchaseRequestLine();
    if (product != null) {
      purchaseRequestLine.setProduct(product);
      getProductInformation(purchaseRequestLine);
    } else {
      purchaseRequestLine.setProductTitle(productTitle);
      purchaseRequestLine.setNewProduct(true);
    }
    if (unit != null) {
      purchaseRequestLine.setUnit(unit);
    }
    if (quantity.compareTo(BigDecimal.ZERO) == 0) {
      purchaseRequestLine.setQuantity(getDefaultQuantity(purchaseRequestLine));
    } else {
      purchaseRequestLine.setQuantity(quantity);
    }
    purchaseRequest.addPurchaseRequestLineListItem(purchaseRequestLine);
    purchaseRequestLineRepository.save(purchaseRequestLine);
  }

  @Override
  public BigDecimal getDefaultQuantity(PurchaseRequestLine purchaseRequestLine) {
    return BigDecimal.ONE;
  }

  @Override
  public Map<String, Object> getProductInformation(PurchaseRequestLine purchaseRequestLine) {
    Product product = purchaseRequestLine.getProduct();
    if (product == null) {
      return null;
    }
    Unit unit = product.getPurchasesUnit() != null ? product.getPurchasesUnit() : product.getUnit();
    Map<String, Object> values = new HashMap<>();
    purchaseRequestLine.setUnit(unit);
    purchaseRequestLine.setProductTitle(product.getName());
    values.put("unit", purchaseRequestLine.getUnit());
    values.put("productTitle", purchaseRequestLine.getProductTitle());
    return values;
  }
}
