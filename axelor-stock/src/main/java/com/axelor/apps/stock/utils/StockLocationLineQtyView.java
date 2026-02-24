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
package com.axelor.apps.stock.utils;

import com.axelor.apps.base.db.Unit;
import java.math.BigDecimal;

/**
 * Lightweight projection used by JPQL constructor expressions to aggregate planned incoming and
 * outgoing quantities per unit for a given product/location context.
 */
public class StockLocationLineQtyView {

  private final Unit unit;
  private final BigDecimal incoming;
  private final BigDecimal outgoing;

  public StockLocationLineQtyView(Unit unit, BigDecimal incoming, BigDecimal outgoing) {
    this.unit = unit;
    this.incoming = incoming;
    this.outgoing = outgoing;
  }

  public Unit getUnit() {
    return unit;
  }

  public BigDecimal getIncoming() {
    return incoming;
  }

  public BigDecimal getOutgoing() {
    return outgoing;
  }
}
