/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service.purchaseorderline.view;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class PurchaseOrderLineViewServiceImpl implements PurchaseOrderLineViewService {

  private static final String HIDDEN_ATTR = "hidden";

  @Override
  public Map<String, Map<String, Object>> hideDeliveryPanel(PurchaseOrderLine purchaseOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(purchaseOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");

    boolean hidePanels = !productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_STORABLE);

    attrs.put("deliveryPanel", Map.of(HIDDEN_ATTR, hidePanels));
    return attrs;
  }
}
