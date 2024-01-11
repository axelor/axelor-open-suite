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
package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.utils.api.ResponseStructure;

public class StockIncomingMoveResponse extends ResponseStructure {
  private final long id;
  private final int typeSelect;
  private final long fromAddressId;
  private final long toStockLocationId;

  public StockIncomingMoveResponse(StockMove stockMove) {
    super(stockMove.getVersion());
    this.id = stockMove.getId();
    this.typeSelect = stockMove.getTypeSelect();
    this.fromAddressId = stockMove.getFromAddress().getId();
    this.toStockLocationId = stockMove.getToStockLocation().getId();
  }

  public long getId() {
    return id;
  }

  public int getTypeSelect() {
    return typeSelect;
  }

  public long getFromAddressId() {
    return fromAddressId;
  }

  public long getToStockLocationId() {
    return toStockLocationId;
  }
}
