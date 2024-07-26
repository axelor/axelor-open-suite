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
package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.utils.api.ResponseStructure;

public class SaleOrderResponse extends ResponseStructure {

  protected Long saleOrderId;
  protected Integer status;

  public SaleOrderResponse(SaleOrder saleOrder) {
    super(saleOrder.getVersion());
    this.saleOrderId = saleOrder.getId();
    this.status = saleOrder.getStatusSelect();
  }

  public Long getExpenseId() {
    return saleOrderId;
  }
}
