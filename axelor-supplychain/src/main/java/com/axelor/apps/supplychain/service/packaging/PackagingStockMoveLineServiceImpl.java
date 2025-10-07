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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class PackagingStockMoveLineServiceImpl implements PackagingStockMoveLineService {

  @Override
  public void updateQtyRemainingToPackage(LogisticalForm logisticalForm) throws AxelorException {
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(packagingList)) {
      return;
    }
    for (Packaging packaging : packagingList) {
      updateQtyRemainingToPackage(packaging);
    }
  }

  @Override
  public void updateQtyRemainingToPackage(Packaging packaging) throws AxelorException {
    List<PackagingLine> packagingLineList = new ArrayList<>();
    Packaging rootPackaging = getRootPackaging(packaging);
    getPackagingLineList(rootPackaging, packagingLineList);
    updateQtyRemainingToPackage(packagingLineList);
  }

  protected Packaging getRootPackaging(Packaging packaging) {
    Packaging current = packaging;
    while (current != null && current.getParentPackaging() != null) {
      current = current.getParentPackaging();
    }
    return current;
  }

  protected void getPackagingLineList(Packaging packaging, List<PackagingLine> packagingLineList) {
    if (packaging == null) {
      return;
    }
    if (CollectionUtils.isNotEmpty(packaging.getPackagingLineList())) {
      packagingLineList.addAll(packaging.getPackagingLineList());
    }
    if (CollectionUtils.isNotEmpty(packaging.getChildrenPackagingList())) {
      for (Packaging childPackaging : packaging.getChildrenPackagingList()) {
        getPackagingLineList(childPackaging, packagingLineList);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateQtyRemainingToPackage(List<PackagingLine> packagingLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(packagingLineList)) {
      return;
    }
    Map<StockMoveLine, BigDecimal> map =
        packagingLineList.stream()
            .filter(line -> line.getStockMoveLine() != null)
            .collect(
                Collectors.groupingBy(
                    PackagingLine::getStockMoveLine,
                    Collectors.mapping(
                        PackagingLine::getQty,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

    for (Map.Entry<StockMoveLine, BigDecimal> entry : map.entrySet()) {
      StockMoveLine stockMoveLine = entry.getKey();
      BigDecimal qtyPacked = entry.getValue();

      BigDecimal qtyRemaining =
          stockMoveLine.getRealQty().subtract(qtyPacked).setScale(3, RoundingMode.HALF_UP);

      if (qtyRemaining.signum() < 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.INVALID_PACKAGING_LINE_QTY));
      }
      stockMoveLine.setQtyRemainingToPackage(qtyRemaining);
    }
  }
}
