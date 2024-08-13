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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import java.util.List;
import java.util.Optional;

public interface ManufOrderGetStockMoveService {
  StockMove getProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  StockMove getResidualStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  StockMove getConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  List<StockMove> getResidualOutStockMoveLineList(ManufOrder manufOrder) throws AxelorException;

  List<StockMove> getNonResidualOutStockMoveLineList(ManufOrder manufOrder) throws AxelorException;

  Optional<StockMove> getPlannedStockMove(List<StockMove> stockMoveList);
}
