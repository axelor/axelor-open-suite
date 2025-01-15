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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseRequestLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

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
      purchaseRequestLine.setProductTitle(product.getName());
    } else {
      purchaseRequestLine.setProductTitle(productTitle);
      purchaseRequestLine.setNewProduct(true);
    }
    if (unit == null && product != null) {
      unit = product.getUnit();
    }
    purchaseRequestLine.setUnit(unit);
    purchaseRequestLine.setQuantity(quantity);
    purchaseRequest.addPurchaseRequestLineListItem(purchaseRequestLine);
    purchaseRequestLineRepository.save(purchaseRequestLine);
  }
}
