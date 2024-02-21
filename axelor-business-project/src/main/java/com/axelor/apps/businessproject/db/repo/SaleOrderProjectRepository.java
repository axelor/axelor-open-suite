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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.inject.Beans;

public class SaleOrderProjectRepository extends SaleOrderSupplychainRepository {
  @Override
  public SaleOrder copy(SaleOrder entity, boolean deep) {

    SaleOrder copy = super.copy(entity, deep);

    if (Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      copy.setProject(null);
      if (copy.getSaleOrderLineList() != null) {
        for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
          saleOrderLine.setProject(null);
        }
      }
    }

    return copy;
  }
}
