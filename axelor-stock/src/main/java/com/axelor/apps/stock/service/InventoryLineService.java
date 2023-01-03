/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryLineService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected StockConfigService stockConfigService;
  protected InventoryLineRepository inventoryLineRepository;
  protected ProductRepository productRepository;
  protected UnitConversionService unitConversionService;

  @Inject
  public InventoryLineService(
      StockConfigService stockConfigService,
      InventoryLineRepository inventoryLineRepository,
      ProductRepository productRepository,
      UnitConversionService unitConversionService) {
    this.stockConfigService = stockConfigService;
    this.inventoryLineRepository = inventoryLineRepository;
    this.productRepository = productRepository;
    this.unitConversionService = unitConversionService;
  }

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

  public InventoryLine updateInventoryLine(InventoryLine inventoryLine, Inventory inventory) {

    StockLocation stockLocation = inventory.getStockLocation();
    Product product = inventoryLine.getProduct();

    if (product != null) {
      StockLocationLine stockLocationLine =
          Beans.get(StockLocationLineService.class)
              .getOrCreateStockLocationLine(stockLocation, product);

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

  public InventoryLine compute(InventoryLine inventoryLine, Inventory inventory)
      throws AxelorException {

    StockLocation stockLocation = inventory.getStockLocation();
    Product product = inventoryLine.getProduct();

    if (product != null) {
      inventoryLine.setUnit(product.getUnit());

      BigDecimal gap =
          inventoryLine.getRealQty() != null
              ? inventoryLine
                  .getCurrentQty()
                  .subtract(inventoryLine.getRealQty())
                  .setScale(2, RoundingMode.HALF_UP)
              : BigDecimal.ZERO;
      inventoryLine.setGap(gap);

      BigDecimal price;
      int inventoryValuationTypeSelect =
          stockConfigService
              .getStockConfig(stockLocation.getCompany())
              .getInventoryValuationTypeSelect();

      switch (inventoryValuationTypeSelect) {
        case StockConfigRepository.VALUATION_TYPE_ACCOUNTING_VALUE:
          price = product.getCostPrice();
          break;
        case StockConfigRepository.VALUATION_TYPE_SALE_VALUE:
          price = product.getSalePrice();
          break;
        default:
          price = product.getAvgPrice();
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

  public BigDecimal getCurrentQty(StockLocation stockLocation, Product product) {
    BigDecimal currentQty = BigDecimal.ZERO;

    if (stockLocation != null && product != null) {
      StockLocationLine stockLocationLine =
          Beans.get(StockLocationLineService.class).getStockLocationLine(stockLocation, product);
      if (stockLocationLine != null) {
        currentQty = stockLocationLine.getCurrentQty();
      }
    }
    return currentQty;
  }

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

  public List<InventoryLine> getInventoryLines(Product product) throws AxelorException {
    if (product != null && !product.getStockManaged()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVENTORY_PRODUCT_STOCK_UNMANAGEABLE));
    }

    return inventoryLineRepository
        .all()
        .filter("self.product.id = :_productId")
        .bind("_productId", product.getId())
        .fetch();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateStockUnit(Product product) throws AxelorException {
    if (product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.INVENTORY_PRODUCT_MISSING));
    }

    product = productRepository.find(product.getId());
    List<InventoryLine> inventoryLineList = this.getInventoryLines(product);

    LOG.debug("Update stock unit for the product: {}", product.getFullName());
    for (InventoryLine inventoryLine : inventoryLineList) {
      this.updateInventoryLineUnit(inventoryLine, product);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateInventoryLineUnit(InventoryLine inventoryLine, Product product)
      throws AxelorException {
    Unit productUnit = product.getUnit();
    Unit inventoryLineUnit = inventoryLine.getUnit();

    if (productUnit != null && !productUnit.equals(inventoryLineUnit)) {
      LOG.debug("Unit conversion start unit {}", inventoryLineUnit.getName());
      LOG.debug("Unit conversion end unit {}", productUnit.getName());
      convertCurrentQty(inventoryLine, inventoryLineUnit, productUnit, product);
      convertRealQty(inventoryLine, inventoryLineUnit, productUnit, product);
      this.compute(inventoryLine, inventoryLine.getInventory());
    }

    inventoryLineRepository.save(inventoryLine);
  }

  private void convertCurrentQty(
      InventoryLine inventoryLine, Unit startUnit, Unit endUnit, Product product)
      throws AxelorException {
    BigDecimal oldCurrentQty = inventoryLine.getCurrentQty();
    LOG.debug("Inventory old current quantity {}", oldCurrentQty);
    BigDecimal currentQty =
        unitConversionService.convert(
            startUnit, endUnit, oldCurrentQty, oldCurrentQty.scale(), product);
    LOG.debug("Inventory new current quantity {}", oldCurrentQty);
    inventoryLine.setCurrentQty(currentQty);
  }

  private void convertRealQty(
      InventoryLine inventoryLine, Unit startUnit, Unit endUnit, Product product)
      throws AxelorException {
    BigDecimal oldRealQty = inventoryLine.getRealQty();
    LOG.debug("Inventory old real quantity {}", oldRealQty);
    BigDecimal realQty =
        unitConversionService.convert(startUnit, endUnit, oldRealQty, oldRealQty.scale(), product);
    LOG.debug("Inventory new real quantity {}", realQty);
    inventoryLine.setRealQty(realQty);
  }
}
