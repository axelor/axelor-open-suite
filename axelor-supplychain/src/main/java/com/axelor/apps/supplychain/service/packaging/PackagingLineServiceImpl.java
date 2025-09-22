/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.db.repo.PackagingLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PackagingLineServiceImpl implements PackagingLineService {

  protected final PackagingLineRepository packagingLineRepository;

  @Inject
  public PackagingLineServiceImpl(PackagingLineRepository packagingLineRepository) {
    this.packagingLineRepository = packagingLineRepository;
  }

  @Override
  public void addPackagingLines(Packaging packaging, List<StockMoveLine> stockMoveLineList) {
    if (packaging == null || CollectionUtils.isEmpty(stockMoveLineList)) {
      return;
    }
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      createPackagingLine(packaging, stockMoveLine);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createPackagingLine(Packaging packaging, StockMoveLine stockMoveLine) {
    PackagingLine packagingLine = new PackagingLine();
    packagingLine.setPackaging(packaging);
    packagingLine.setStockMoveLine(stockMoveLine);
    packagingLine.setQty(stockMoveLine.getQtyRemainingToPackage());
    packagingLineRepository.save(packagingLine);
  }
}
