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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.repo.StockMoveLineSupplychainRepository;
import com.axelor.inject.Beans;

public class StockMoveLineProductionRepository extends StockMoveLineSupplychainRepository {

  @Override
  public StockMoveLine copy(StockMoveLine entity, boolean deep) {
    StockMoveLine copy = super.copy(entity, deep);

    if (!Beans.get(AppBaseService.class).isApp("production")) {
      return copy;
    }

    if (!deep) {
      copy.setProducedManufOrder(null);
      copy.setConsumedManufOrder(null);
      copy.setConsumedOperationOrder(null);
      copy.setResidualManufOrder(null);
    }
    return copy;
  }
}
