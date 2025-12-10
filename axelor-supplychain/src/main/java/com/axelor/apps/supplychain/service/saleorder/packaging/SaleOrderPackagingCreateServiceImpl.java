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
package com.axelor.apps.supplychain.service.saleorder.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.db.repo.PackagingLineRepository;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class SaleOrderPackagingCreateServiceImpl implements SaleOrderPackagingCreateService {

  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;
  protected PackagingRepository packagingRepository;
  protected PackagingLineRepository packagingLineRepository;

  @Inject
  public SaleOrderPackagingCreateServiceImpl(
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService,
      PackagingRepository packagingRepository,
      PackagingLineRepository packagingLineRepository) {
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
    this.packagingRepository = packagingRepository;
    this.packagingLineRepository = packagingLineRepository;
  }

  @Override
  public void createPackaging(
      Product selectedBox,
      Map<Product, Pair<SaleOrderLine, BigDecimal>> boxContents,
      Map<Product, Pair<SaleOrderLine, BigDecimal>> lineQtyMap,
      SaleOrder saleOrder)
      throws AxelorException {

    for (Map.Entry<Product, Pair<SaleOrderLine, BigDecimal>> entry : boxContents.entrySet()) {
      Product product = entry.getKey();
      Pair<SaleOrderLine, BigDecimal> pair = entry.getValue();
      BigDecimal remainingQty = lineQtyMap.get(product).getRight().subtract(pair.getRight());
      lineQtyMap.put(product, Pair.of(pair.getLeft(), remainingQty));
    }
    Packaging packaging = createPackaging(selectedBox, saleOrder);

    for (Map.Entry<Product, Pair<SaleOrderLine, BigDecimal>> entry : boxContents.entrySet()) {
      Product product = entry.getKey();
      if (product.getIsPackaging()) {
        updatePackagings(product, saleOrder, packaging);
      } else {
        Pair<SaleOrderLine, BigDecimal> pair = entry.getValue();
        createPackagingLine(packaging, pair.getLeft(), pair.getRight());
      }
    }
    packagingRepository.save(packaging);
  }

  protected void createPackagingLine(
      Packaging packaging, SaleOrderLine saleOrderLine, BigDecimal qty) {
    PackagingLine packagingLine = new PackagingLine();
    packagingLine.setPackaging(packaging);
    packagingLine.setSaleOrderLine(saleOrderLine);
    packagingLine.setQty(qty);
    packaging.addPackagingLineListItem(packagingLine);
    packagingLineRepository.save(packagingLine);
  }

  protected Packaging createPackaging(Product packageUsed, SaleOrder saleOrder) {
    Packaging packaging = new Packaging();
    packaging.setPackageUsed(packageUsed);
    packaging.setSaleOrder(saleOrder);
    packaging.setPackagingLevelSelect(packageUsed.getPackagingLevelSelect());
    return packagingRepository.save(packaging);
  }

  protected void updatePackagings(
      Product packageUsed, SaleOrder saleOrder, Packaging parentPackaging) {
    List<Packaging> packagingList =
        packagingRepository
            .all()
            .filter("self.packageUsed = :packageUsed AND self.saleOrder = :saleOrder")
            .bind("packageUsed", packageUsed)
            .bind("saleOrder", saleOrder)
            .fetch();

    for (Packaging packaging : packagingList) {
      packaging.setParentPackaging(parentPackaging);
      packaging.setSaleOrder(null);
      parentPackaging.addChildrenPackagingListItem(packaging);
      packagingRepository.save(packaging);
    }
  }
}
