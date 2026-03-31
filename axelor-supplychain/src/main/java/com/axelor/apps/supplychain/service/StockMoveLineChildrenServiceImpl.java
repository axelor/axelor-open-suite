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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class StockMoveLineChildrenServiceImpl implements StockMoveLineChildrenService {

  protected final StockMoveLineRepository stockMoveLineRepository;
  protected final AppBaseService appBaseService;

  @Inject
  public StockMoveLineChildrenServiceImpl(
      StockMoveLineRepository stockMoveLineRepository, AppBaseService appBaseService) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public List<StockMoveLine> propagateRealQtyInList(List<StockMoveLine> stockMoveLineList) {
    if (CollectionUtils.isEmpty(stockMoveLineList)) {
      return stockMoveLineList;
    }
    Map<Long, StockMoveLine> lineById = new HashMap<>();
    for (StockMoveLine line : stockMoveLineList) {
      if (line.getId() != null) {
        lineById.put(line.getId(), line);
      }
    }
    int scale = appBaseService.getNbDecimalDigitForQty();
    for (StockMoveLine line : stockMoveLineList) {
      if (!Boolean.TRUE.equals(line.getRealQtyChangedByUser())) {
        continue;
      }
      // Consume the flag so it does not retrigger on the next onChange
      line.setRealQtyChangedByUser(false);
      if (!CollectionUtils.isEmpty(line.getChildStockMoveLineList())
          && line.getQty().signum() != 0) {
        propagateToDescendants(line, lineById, scale);
      }
    }
    return stockMoveLineList;
  }

  protected void propagateToDescendants(
      StockMoveLine parent, Map<Long, StockMoveLine> lineById, int scale) {
    List<StockMoveLine> children = parent.getChildStockMoveLineList();
    if (CollectionUtils.isEmpty(children) || parent.getQty().signum() == 0) {
      return;
    }
    for (StockMoveLine child : children) {
      StockMoveLine childInList = child.getId() != null ? lineById.get(child.getId()) : null;
      if (childInList == null) {
        continue;
      }
      BigDecimal newRealQty =
          parent
              .getRealQty()
              .multiply(childInList.getQty())
              .divide(parent.getQty(), scale, RoundingMode.HALF_UP);
      childInList.setRealQty(newRealQty);
      // Replicate action-stock-move-line-record-total-net-mass
      if (childInList.getNetMass() != null) {
        childInList.setTotalNetMass(childInList.getNetMass().multiply(newRealQty));
      }
      // Replicate action-stock-move-line-record-set-qty-remaining-to-package
      childInList.setQtyRemainingToPackage(newRealQty);
      propagateToDescendants(childInList, lineById, scale);
    }
  }

  @Override
  public List<StockMoveLine> applyRatioToChildren(List<StockMoveLine> children, BigDecimal ratio) {
    int scale = appBaseService.getNbDecimalDigitForQty();
    for (StockMoveLine child : children) {
      child.setRealQty(child.getRealQty().multiply(ratio).setScale(scale, RoundingMode.HALF_UP));

      // Grandchildren may not be loaded in context — fetch from DB (read-only)
      List<StockMoveLine> grandchildren = child.getChildStockMoveLineList();
      if (grandchildren == null && child.getId() != null) {
        grandchildren =
            stockMoveLineRepository
                .all()
                .filter("self.parentStockMoveLine.id = ?1", child.getId())
                .fetch();
        child.setChildStockMoveLineList(grandchildren);
      }
      if (!CollectionUtils.isEmpty(grandchildren)) {
        applyRatioToChildren(grandchildren, ratio);
      }
    }
    return children;
  }
}
