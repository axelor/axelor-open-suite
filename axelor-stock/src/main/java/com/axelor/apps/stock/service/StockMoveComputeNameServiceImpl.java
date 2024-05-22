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
package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockMove;
import com.google.common.base.Strings;
import java.util.Objects;

public class StockMoveComputeNameServiceImpl implements StockMoveComputeNameService {

  @Override
  public String computeName(StockMove stockMove) {
    return computeName(stockMove, null);
  }

  @Override
  public String computeName(StockMove stockMove, String name) {
    Objects.requireNonNull(stockMove);
    StringBuilder nameBuilder = new StringBuilder();

    if (Strings.isNullOrEmpty(name)) {
      if (!Strings.isNullOrEmpty(stockMove.getStockMoveSeq())) {
        nameBuilder.append(stockMove.getStockMoveSeq());
      }
    } else {
      nameBuilder.append(name);
    }

    if (stockMove.getPartner() != null
        && !Strings.isNullOrEmpty(stockMove.getPartner().getFullName())) {
      if (nameBuilder.length() > 0) {
        nameBuilder.append(" - ");
      }

      nameBuilder.append(stockMove.getPartner().getFullName());
    }

    return nameBuilder.toString();
  }
}
