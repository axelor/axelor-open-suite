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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class AdvancePaymentSaleRepository extends AdvancePaymentRepository {

  @Override
  public AdvancePayment save(AdvancePayment advancePayment) {
    try {
      SaleOrder saleOrder = advancePayment.getSaleOrder();
      Beans.get(SaleOrderComputeService.class)._computeSaleOrder(saleOrder);
      return super.save(advancePayment);
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }
}
