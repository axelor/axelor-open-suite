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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OperationOrderOutsourceService {

  Optional<Partner> getOutsourcePartner(OperationOrder operationOrder);

  List<PurchaseOrderLine> createPurchaseOrderLines(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder) throws AxelorException;

  Optional<PurchaseOrderLine> createPurchaseOrderLine(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder, Product product)
      throws AxelorException;

  Optional<PurchaseOrderLine> createPurchaseOrderLine(
      ManufOrder manufOrder, PurchaseOrder purchaseOrder, Product product) throws AxelorException;

  List<PurchaseOrderLine> createPurchaseOrderLines(
      ManufOrder manufOrder, Set<Product> productSet, PurchaseOrder purchaseOrder)
      throws AxelorException;

  long getOutsourcingDuration(OperationOrder operationOrder);
}
