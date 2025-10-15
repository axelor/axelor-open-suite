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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.service.saleorder.packaging.SaleOrderPackagingDimensionService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class PackagingLineServiceImpl implements PackagingLineService {

  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;

  @Inject
  public PackagingLineServiceImpl(
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService) {
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
  }

  @Override
  public void updateStockMoveSet(PackagingLine packagingLine, boolean add) throws AxelorException {
    StockMoveLine stockMoveLine = packagingLine.getStockMoveLine();
    Packaging packaging = packagingLine.getPackaging();
    if (stockMoveLine == null || packaging == null) {
      return;
    }
    StockMove stockMove = stockMoveLine.getStockMove();
    if (stockMove == null) {
      return;
    }
    if (add) {
      addStockMove(packaging, stockMove);
      updateStockMoveGrossMass(packagingLine, stockMove, add);
    } else {
      Packaging root = getRootPackaging(packaging);
      if (!isStockMoveReferenced(root, stockMove, packagingLine)) {
        removeStockMove(packaging, stockMove);
        updateStockMoveGrossMass(packagingLine, stockMove, add);
      }
    }
  }

  protected Packaging getRootPackaging(Packaging packaging) {
    while (packaging != null && packaging.getParentPackaging() != null) {
      packaging = packaging.getParentPackaging();
    }
    return packaging;
  }

  protected void addStockMove(Packaging packaging, StockMove stockMove) {
    while (packaging != null) {
      if (!packaging.getStockMoveSet().contains(stockMove)) {
        packaging.getStockMoveSet().add(stockMove);
        updateStockMovePackagingInfo(packaging, stockMove, true);
      }
      packaging = packaging.getParentPackaging();
    }
  }

  protected void removeStockMove(Packaging packaging, StockMove stockMove) {
    while (packaging != null) {
      if (packaging.getStockMoveSet().contains(stockMove)) {
        packaging.getStockMoveSet().remove(stockMove);
        updateStockMovePackagingInfo(packaging, stockMove, false);
      }
      packaging = packaging.getParentPackaging();
    }
  }

  protected boolean isStockMoveReferenced(
      Packaging packaging, StockMove stockMove, PackagingLine excludedLine) {
    if (CollectionUtils.isNotEmpty(packaging.getPackagingLineList())) {
      boolean found =
          packaging.getPackagingLineList().stream()
              .filter(line -> !line.equals(excludedLine))
              .anyMatch(
                  line ->
                      line.getStockMoveLine() != null
                          && line.getStockMoveLine().getStockMove() != null
                          && line.getStockMoveLine().getStockMove().equals(stockMove));
      if (found) {
        return true;
      }
    }
    if (CollectionUtils.isNotEmpty(packaging.getChildrenPackagingList())) {
      for (Packaging childPackaging : packaging.getChildrenPackagingList()) {
        if (isStockMoveReferenced(childPackaging, stockMove, excludedLine)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void updateQtyRemainingToPackage(PackagingLine packagingLine, boolean add) {
    StockMoveLine stockMoveLine = packagingLine.getStockMoveLine();
    if (stockMoveLine == null) {
      return;
    }
    if (add) {
      stockMoveLine.setQtyRemainingToPackage(
          stockMoveLine.getQtyRemainingToPackage().subtract(packagingLine.getQty()));
    } else {
      stockMoveLine.setQtyRemainingToPackage(
          stockMoveLine.getQtyRemainingToPackage().add(packagingLine.getQty()));
    }
  }

  @Override
  public void updatePackagingMass(PackagingLine packagingLine, boolean add) throws AxelorException {
    Packaging packaging = packagingLine.getPackaging();
    BigDecimal[] mass = computePackagingLineMass(packagingLine);
    updatePackagingMass(packaging, mass[0], mass[1], add);
  }

  @Override
  public BigDecimal[] computePackagingLineMass(PackagingLine packagingLine) throws AxelorException {
    Product product = getPackagingLineProduct(packagingLine);
    if (product == null) {
      return new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO};
    }
    BigDecimal qty = packagingLine.getQty();

    BigDecimal grossMass =
        saleOrderPackagingDimensionService
            .getConvertedWeight(product.getGrossMass(), product)
            .multiply(qty)
            .setScale(3, RoundingMode.HALF_UP);

    BigDecimal netMass =
        saleOrderPackagingDimensionService
            .getConvertedWeight(product.getNetMass(), product)
            .multiply(qty)
            .setScale(3, RoundingMode.HALF_UP);

    return new BigDecimal[] {grossMass, netMass};
  }

  protected Product getPackagingLineProduct(PackagingLine packagingLine) {
    Product product =
        Optional.ofNullable(packagingLine.getStockMoveLine())
            .map(StockMoveLine::getProduct)
            .orElse(null);
    if (product == null) {
      product =
          Optional.ofNullable(packagingLine.getSaleOrderLine())
              .map(SaleOrderLine::getProduct)
              .orElse(null);
    }
    return product;
  }

  protected void updatePackagingMass(
      Packaging packaging, BigDecimal grossMass, BigDecimal netMass, boolean add) {
    while (packaging != null) {
      if (add) {
        packaging.setTotalGrossMass(packaging.getTotalGrossMass().add(grossMass));
        packaging.setTotalNetMass(packaging.getTotalNetMass().add(netMass));
      } else {
        packaging.setTotalGrossMass(packaging.getTotalGrossMass().subtract(grossMass));
        packaging.setTotalNetMass(packaging.getTotalNetMass().subtract(netMass));
      }
      packaging = packaging.getParentPackaging();
    }
  }

  protected void updateStockMovePackagingInfo(
      Packaging packaging, StockMove stockMove, boolean add) {
    int packagingLevelSelect = packaging.getPackagingLevelSelect();
    BigDecimal grossMass = stockMove.getGrossMass();
    BigDecimal packagingMass = packaging.getGrossMass();

    if (add) {
      if (packagingLevelSelect == ProductRepository.PACKAGING_LEVEL_BOX) {
        stockMove.setNumOfPackages(stockMove.getNumOfPackages() + 1);
      } else if (packagingLevelSelect == ProductRepository.PACKAGING_LEVEL_PALLET) {
        stockMove.setNumOfPalettes(stockMove.getNumOfPalettes() + 1);
      }
      stockMove.setGrossMass(grossMass.add(packagingMass));
    } else {
      if (packagingLevelSelect == ProductRepository.PACKAGING_LEVEL_BOX) {
        stockMove.setNumOfPackages(stockMove.getNumOfPackages() - 1);
      } else if (packagingLevelSelect == ProductRepository.PACKAGING_LEVEL_PALLET) {
        stockMove.setNumOfPalettes(stockMove.getNumOfPalettes() - 1);
      }
      stockMove.setGrossMass(grossMass.subtract(packagingMass));
    }
  }

  protected void updateStockMoveGrossMass(
      PackagingLine packagingLine, StockMove stockMove, boolean add) throws AxelorException {
    Product product =
        Optional.of(packagingLine.getStockMoveLine()).map(StockMoveLine::getProduct).orElse(null);
    if (product == null) {
      return;
    }
    BigDecimal grossMass = stockMove.getGrossMass();
    BigDecimal productMass =
        saleOrderPackagingDimensionService
            .getConvertedWeight(product.getGrossMass(), product)
            .multiply(packagingLine.getQty())
            .setScale(3, RoundingMode.HALF_UP);
    if (add) {
      stockMove.setGrossMass(grossMass.add(productMass));
    } else {
      stockMove.setGrossMass(grossMass.subtract(productMass));
    }
  }
}
