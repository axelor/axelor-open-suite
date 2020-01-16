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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class StockMoveLineController {

  @Inject AppBaseService appBaseService;

  public List<StockMoveLine> updateQty(
      List<StockMoveLine> moveLines,
      BigDecimal oldKitQty,
      BigDecimal newKitQty,
      boolean isRealQty) {

    BigDecimal qty = BigDecimal.ZERO;
    int scale = appBaseService.getNbDecimalDigitForQty();

    if (moveLines != null) {
      if (newKitQty.compareTo(BigDecimal.ZERO) != 0) {
        for (StockMoveLine line : moveLines) {
          qty =
              (line.getQty().divide(oldKitQty, scale, RoundingMode.HALF_EVEN)).multiply(newKitQty);
          line.setQty(qty.setScale(scale, RoundingMode.HALF_EVEN));
          line.setRealQty(qty.setScale(scale, RoundingMode.HALF_EVEN));
        }
      } else {
        for (StockMoveLine line : moveLines) {
          line.setQty(qty.setScale(scale, RoundingMode.HALF_EVEN));
          line.setRealQty(qty.setScale(scale, RoundingMode.HALF_EVEN));
        }
      }
    }

    return moveLines;
  }

  public List<StockMoveLine> updateRealQty(
      List<StockMoveLine> moveLines,
      BigDecimal oldKitQty,
      BigDecimal newKitQty,
      boolean isRealQty) {

    BigDecimal qty = BigDecimal.ZERO;
    int scale = appBaseService.getNbDecimalDigitForQty();

    if (moveLines != null) {
      if (newKitQty.compareTo(BigDecimal.ZERO) != 0) {
        for (StockMoveLine line : moveLines) {
          qty =
              (line.getRealQty().divide(oldKitQty, scale, RoundingMode.HALF_EVEN))
                  .multiply(newKitQty);
          line.setRealQty(qty.setScale(scale, RoundingMode.HALF_EVEN));
        }
      } else {
        for (StockMoveLine line : moveLines) {
          line.setRealQty(qty.setScale(scale, RoundingMode.HALF_EVEN));
        }
      }
    }

    return moveLines;
  }
}
