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
package com.axelor.apps.supplychain.service.saleorderline.view;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewSupplychainService {
  Map<String, Map<String, Object>> hideSupplychainPanels(SaleOrder saleOrder);

  Map<String, Map<String, Object>> hideDeliveredQty(SaleOrder saleOrder);

  Map<String, Map<String, Object>> hideAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> setAnalyticDistributionPanelHidden(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException;

  Map<String, Map<String, Object>> setReservedQtyReadonly(SaleOrder saleOrder);

  Map<String, Map<String, Object>> setDistributionLineReadonly(SaleOrder saleOrder);
}
