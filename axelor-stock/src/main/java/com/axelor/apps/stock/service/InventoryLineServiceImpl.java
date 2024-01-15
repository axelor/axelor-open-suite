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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class InventoryLineServiceImpl implements InventoryLineService {

  protected StockConfigService stockConfigService;
  protected InventoryLineRepository inventoryLineRepository;
  protected StockLocationLineService stockLocationLineService;
  protected ProductCompanyService productCompanyService;

  @Inject
  public InventoryLineServiceImpl(
      StockConfigService stockConfigService,
      InventoryLineRepository inventoryLineRepository,
      StockLocationLineService stockLocationLineService,
      ProductCompanyService productCompanyService) {
    this.stockConfigService = stockConfigService;
    this.inventoryLineRepository = inventoryLineRepository;
    this.stockLocationLineService = stockLocationLineService;
    this.productCompanyService = productCompanyService;
  }

  @Override
  public InventoryLine createInventoryLine(
      Inventory inventory,
      Product product,
      BigDecimal currentQty,
      String rack,
      TrackingNumber trackingNumber)
      throws AxelorException {

    return createInventoryLine(
        inventory, product, currentQty, rack, trackingNumber, null, null, null, null);
  }

  @Override
  public InventoryLine createInventoryLine(
      Inventory inventory,
      Product product,
      BigDecimal currentQty,
      String rack,
      TrackingNumber trackingNumber,
      BigDecimal realQty,
      String description,
      StockLocation stockLocation,
      StockLocation detailsStockLocation)
      throws AxelorException {
    InventoryLine inventoryLine = new InventoryLine();
    inventoryLine.setInventory(inventory);
    inventoryLine.setProduct(product);
    inventoryLine.setRack(rack);
    inventoryLine.setCurrentQty(currentQty);
    inventoryLine.setTrackingNumber(trackingNumber);
    inventoryLine.setRealQty(realQty);
    inventoryLine.setDescription(description);
    inventoryLine.setStockLocation(stockLocation);
    if (stockLocation == null) {
      inventoryLine.setStockLocation(detailsStockLocation);
    }
    this.compute(inventoryLine, inventory);

    return inventoryLine;
  }

  @Override
  public InventoryLine updateInventoryLine(InventoryLine inventoryLine, Inventory inventory) {

    StockLocation stockLocation =
        Optional.ofNullable(inventoryLine.getStockLocation()).orElse(inventory.getStockLocation());
    Product product = inventoryLine.getProduct();

    if (product != null) {
      StockLocationLine stockLocationLine =
          stockLocationLineService.getOrCreateStockLocationLine(stockLocation, product);

      if (stockLocationLine != null) {
        inventoryLine.setCurrentQty(stockLocationLine.getCurrentQty());
        inventoryLine.setRack(stockLocationLine.getRack());
        if (inventoryLine.getTrackingNumber() != null) {
          inventoryLine.setCurrentQty(
              Beans.get(StockLocationLineRepository.class)
                  .all()
                  .filter(
                      "self.product = :product and self.detailsStockLocation = :stockLocation and self.trackingNumber = :trackingNumber")
                  .bind("product", inventoryLine.getProduct())
                  .bind("stockLocation", stockLocation)
                  .bind("trackingNumber", inventoryLine.getTrackingNumber())
                  .fetchStream()
                  .map(it -> it.getCurrentQty())
                  .reduce(BigDecimal.ZERO, (a, b) -> a.add(b)));
        }
      } else {
        inventoryLine.setCurrentQty(null);
        inventoryLine.setRack(null);
      }
    }

    return inventoryLine;
  }

  @Override
  public InventoryLine compute(InventoryLine inventoryLine, Inventory inventory)
      throws AxelorException {

    StockLocation stockLocation = inventory.getStockLocation();
    Product product = inventoryLine.getProduct();
    StockLocationLine stockLocationLine =
        stockLocationLineService.getStockLocationLine(stockLocation, product);

    if (product != null) {
      inventoryLine.setUnit(product.getUnit());

      BigDecimal gap =
          inventoryLine.getRealQty() != null
              ? inventoryLine
                  .getRealQty()
                  .subtract(inventoryLine.getCurrentQty())
                  .setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;
      inventoryLine.setGap(gap);

      BigDecimal price;
      int inventoryValuationTypeSelect =
          stockConfigService
              .getStockConfig(stockLocation.getCompany())
              .getInventoryValuationTypeSelect();

      BigDecimal productAvgPrice =
          (BigDecimal)
              productCompanyService.get(
                  product, "avgPrice", inventory.getStockLocation().getCompany());

      switch (inventoryValuationTypeSelect) {
        case StockConfigRepository.VALUATION_TYPE_WAP_VALUE:
          price = productAvgPrice;
          break;
        case StockConfigRepository.VALUATION_TYPE_ACCOUNTING_VALUE:
          price = product.getCostPrice();
          break;
        case StockConfigRepository.VALUATION_TYPE_SALE_VALUE:
          price = product.getSalePrice();
          break;
        case StockConfigRepository.VALUATION_TYPE_PURCHASE_VALUE:
          price = product.getPurchasePrice();
          break;
        case StockConfigRepository.VALUATION_TYPE_WAP_STOCK_LOCATION_VALUE:
          if (stockLocationLine != null) {
            price = stockLocationLine.getAvgPrice();
          } else {
            price = productAvgPrice;
          }
          break;
        default:
          price = productAvgPrice;
          break;
      }

      inventoryLine.setGapValue(gap.multiply(price).setScale(2, RoundingMode.HALF_UP));
      inventoryLine.setRealValue(
          inventoryLine.getRealQty() != null
              ? inventoryLine.getRealQty().multiply(price).setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO);
    }

    return inventoryLine;
  }

  @Override
  public BigDecimal getCurrentQty(StockLocation stockLocation, Product product) {
    BigDecimal currentQty = BigDecimal.ZERO;

    if (stockLocation != null && product != null) {
      StockLocationLine stockLocationLine =
          stockLocationLineService.getStockLocationLine(stockLocation, product);
      if (stockLocationLine != null) {
        currentQty = stockLocationLine.getCurrentQty();
      }
    }
    return currentQty;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInventoryLine(
      InventoryLine inventoryLine, BigDecimal realQty, String description) throws AxelorException {
    inventoryLine.setRealQty(realQty);
    if (description != null) {
      inventoryLine.setDescription(description);
    }
    this.compute(inventoryLine, inventoryLine.getInventory());
    inventoryLineRepository.save(inventoryLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InventoryLine addLine(
      Inventory inventory,
      Product product,
      TrackingNumber trackingNumber,
      String rack,
      BigDecimal realQty)
      throws AxelorException {
    InventoryLine inventoryLine =
        createInventoryLine(
            inventory,
            product,
            getCurrentQty(inventory.getStockLocation(), product),
            rack,
            trackingNumber,
            null,
            null,
            inventory.getStockLocation(),
            null);
    updateInventoryLine(inventoryLine, realQty, null);
    return inventoryLine;
  }
}
