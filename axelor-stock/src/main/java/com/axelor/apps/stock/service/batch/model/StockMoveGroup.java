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
package com.axelor.apps.stock.service.batch.model;

import java.time.LocalDate;
import java.util.Objects;

public class StockMoveGroup {

  private final LocalDate realDate;
  private final Long idFromStockLocation;
  private final Long idToStockLocation;
  private final int statusSelect;

  public StockMoveGroup(
      LocalDate realDate, Long fromStockLocation, Long toStockLocation, int statusSelect) {
    this.realDate = realDate;
    this.idFromStockLocation = Objects.requireNonNull(fromStockLocation);
    this.idToStockLocation = Objects.requireNonNull(toStockLocation);
    this.statusSelect = statusSelect;
  }

  public LocalDate getRealDate() {
    return realDate;
  }

  public Long getFromStockLocation() {
    return idFromStockLocation;
  }

  public Long getToStockLocation() {
    return idToStockLocation;
  }

  public int getStatusSelect() {
    return statusSelect;
  }
}
