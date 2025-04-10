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
package com.axelor.apps.sale.service.saleorderline.view;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewService {

  String HIDDEN_ATTR = "hidden";
  String TITLE_ATTR = "title";
  String SCALE_ATTR = "scale";
  String SELECTION_IN_ATTR = "selection-in";
  String READONLY_ATTR = "readonly";

  Map<String, Map<String, Object>> getOnNewAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  Map<String, Map<String, Object>> getDiscountTypeSelectOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  Map<String, Map<String, Object>> getQtyOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  Map<String, Map<String, Object>> hidePriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> getDiscountAmountTitle(SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> getPriceAndQtyScale();

  Map<String, Map<String, Object>> focusProduct();
}
