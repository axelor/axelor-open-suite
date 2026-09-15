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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface ManufOrderMultiLevelPlanningService {

  /**
   * Called by the multi-level manufacturing order controller to generate all child manuf orders for
   * a given bill of material list from a given manuf order.
   *
   * @param productList list of products to filter sub-BOMs, or null for all
   * @param manufOrder the parent manufacturing order
   * @return list of generated draft child ManufOrders
   */
  List<ManufOrder> generateAllSubManufOrder(List<Product> productList, ManufOrder manufOrder)
      throws AxelorException;

  /**
   * Get the list of sub-BOM entries that can be consumed by the given BOM / manuf order.
   *
   * @param bom the parent bill of material
   * @param mo the parent manufacturing order
   * @param productList list of products to filter, or null for all
   * @return list of (BillOfMaterial, required qty) pairs
   */
  List<Pair<BillOfMaterial, BigDecimal>> getToConsumeSubBomList(
      BillOfMaterial bom, ManufOrder mo, List<Product> productList) throws AxelorException;

  /**
   * From the multi-level planning wizard, plan the selected orders and discard the others.
   *
   * @param manufOrders the list of draft MO maps from the wizard
   * @return the IDs of the generated ManufOrders
   */
  List<Long> planSelectedOrdersAndDiscardOthers(List<Map<String, Object>> manufOrders)
      throws AxelorException;

  /**
   * Get all direct children ManufOrders of the given ManufOrder.
   *
   * @param manufOrder the parent manufacturing order
   * @return list of direct child ManufOrders
   */
  List<ManufOrder> getChildrenManufOrder(ManufOrder manufOrder);
}
