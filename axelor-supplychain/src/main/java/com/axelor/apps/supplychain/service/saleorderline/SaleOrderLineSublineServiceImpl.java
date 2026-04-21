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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineSublineServiceImpl implements SaleOrderLineSublineService {

  @Inject
  public SaleOrderLineSublineServiceImpl() {}

  @Override
  public List<SaleOrderLine> collectAllLinesRecursively(List<SaleOrderLine> saleOrderLines) {
    List<SaleOrderLine> result = new ArrayList<>();
    List<SaleOrderLine> sorted =
        saleOrderLines.stream()
            .sorted(Comparator.comparing(SaleOrderLine::getSequence))
            .collect(Collectors.toList());
    for (SaleOrderLine line : sorted) {
      result.add(line);
      if (!CollectionUtils.isEmpty(line.getSubSaleOrderLineList())) {
        result.addAll(collectAllLinesRecursively(line.getSubSaleOrderLineList()));
      }
    }
    return result;
  }

  @Override
  public boolean hasAnyManagedChild(SaleOrderLine saleOrderLine) {
    if (CollectionUtils.isEmpty(saleOrderLine.getSubSaleOrderLineList())) {
      return false;
    }
    for (SaleOrderLine child : saleOrderLine.getSubSaleOrderLineList()) {
      if (child.getManagedInStockMove()) {
        return true;
      }
      if (hasAnyManagedChild(child)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void linkParentStockMoveLines(
      StockMove stockMove, Map<Long, StockMoveLine> saleOrderLineToStockMoveLine) {
    if (stockMove.getStockMoveLineList() == null) {
      return;
    }
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      SaleOrderLine sol = stockMoveLine.getSaleOrderLine();
      if (sol != null) {
        if (sol.getLevelIndicator() != null) {
          stockMoveLine.setLevelIndicator(sol.getLevelIndicator());
        }
        if (sol.getParentSaleOrderLine() != null) {
          StockMoveLine parentSml =
              saleOrderLineToStockMoveLine.get(sol.getParentSaleOrderLine().getId());
          if (parentSml != null) {
            stockMoveLine.setParentStockMoveLine(parentSml);
          }
        }
      }
    }
  }
}
