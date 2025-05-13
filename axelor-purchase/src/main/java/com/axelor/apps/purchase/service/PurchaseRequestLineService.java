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
import java.math.BigDecimal;
import java.util.Map;

public interface PurchaseRequestLineService {

  public void createPurchaseRequestLine(
      PurchaseRequest purchaseRequest,
      Product product,
      String productTitle,
      Unit unit,
      BigDecimal quantity);

  public BigDecimal getDefaultQuantity(PurchaseRequestLine purchaseRequestLine);

  public Map<String, Object> getProductInformation(PurchaseRequestLine purchaseRequestLine);
}
