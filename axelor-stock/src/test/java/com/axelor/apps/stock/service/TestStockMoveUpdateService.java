/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestStockMoveUpdateService {

  private StockMoveUpdateService stockMoveUpdateService;

  @Before
  public void prepare() {
    stockMoveUpdateService = new StockMoveUpdateServiceImpl(null, null, null, null, null, null);
  }

  @Test
  public void testUpdateUnknownStatus() {
    StockMove stockMove = new StockMove();
    stockMove.setStatusSelect(5);
    int status = 6;
    String errorMessage = null;
    try {
      stockMoveUpdateService.updateStatus(stockMove, status);
    } catch (AxelorException e) {
      errorMessage = e.getMessage();
    }
    Assert.assertEquals(
        "Workflow to update status to value 6 is not supported for stock move.", errorMessage);
  }
}
