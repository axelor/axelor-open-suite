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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestStockMoveUpdateService {

  private static StockMoveUpdateService stockMoveUpdateService;

  @BeforeAll
  static void prepare() {
    stockMoveUpdateService = new StockMoveUpdateServiceImpl(null, null, null, null, null, null);
  }

  @Test
  void testUpdateUnknownStatus() {
    StockMove stockMove = new StockMove();
    stockMove.setStatusSelect(5);
    int status = 6;
    String errorMessage = null;
    try {
      stockMoveUpdateService.updateStatus(stockMove, status);
    } catch (AxelorException e) {
      errorMessage = e.getMessage();
    }
    Assertions.assertEquals(
        "Workflow to update status to value 6 is not supported for stock move.", errorMessage);
  }
}
