/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

public interface SaleOrderPurchaseService {

  public void createPurchaseOrders(SaleOrder saleOrder) throws AxelorException;

  public Map<Partner, List<SaleOrderLine>> splitBySupplierPartner(
      List<SaleOrderLine> saleOrderLineList) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder createPurchaseOrder(
      Partner supplierPartner, List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder)
      throws AxelorException;
}
