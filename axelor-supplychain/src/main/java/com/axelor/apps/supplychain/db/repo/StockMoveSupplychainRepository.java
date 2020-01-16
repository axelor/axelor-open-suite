/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import java.math.BigDecimal;

public class StockMoveSupplychainRepository extends StockMoveManagementRepository {

  @Override
  public StockMove copy(StockMove entity, boolean deep) {

    StockMove copy = super.copy(entity, deep);

    copy.setInvoiceSet(null);
    copy.setOriginTypeSelect(null);
    if (copy.getStockMoveLineList() != null) {
      for (StockMoveLine stockMoveLine : copy.getStockMoveLineList()) {
        stockMoveLine.setReservedQty(BigDecimal.ZERO);
        stockMoveLine.setQtyInvoiced(null);
      }
    }
    copy.setReservationDateTime(null);
    copy.setInvoicingStatusSelect(StockMoveRepository.STATUS_NOT_INVOICED);

    return copy;
  }
}
