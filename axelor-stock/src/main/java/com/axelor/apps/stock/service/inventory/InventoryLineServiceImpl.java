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
package com.axelor.apps.stock.service.inventory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.cache.AxelorCache;
import com.axelor.cache.CacheBuilder;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class InventoryLineServiceImpl implements InventoryLineService {

  private final AxelorCache<ImmutablePair<Long, Long>, Boolean> presenceCache =
      CacheBuilder.newBuilder("presenceCache")
          .maximumSize(1000)
          .expireAfterWrite(Duration.ofMinutes(5))
          .build();

  private final AxelorCache<Long, Integer> valuationTypeCache =
      CacheBuilder.newBuilder("valuationTypeCache")
          .maximumSize(100)
          .expireAfterWrite(Duration.ofMinutes(5))
          .build();

  protected StockConfigService stockConfigService;
  protected InventoryLineRepository inventoryLineRepository;
  protected StockLocationLineService stockLocationLineService;
  protected ProductCompanyService productCompanyService;
  protected StockLocationRepository stockLocationRepository;
  protected StockLocationLineFetchService stockLocationLineFetchService;

  @Inject
  public InventoryLineServiceImpl(
      StockConfigService stockConfigService,
      InventoryLineRepository inventoryLineRepository,
      StockLocationLineService stockLocationLineService,
      ProductCompanyService productCompanyService,
      StockLocationRepository stockLocationRepository,
      StockLocationLineFetchService stockLocationLineFetchService) {
    this.stockConfigService = stockConfigService;
    this.inventoryLineRepository = inventoryLineRepository;
    this.stockLocationLineService = stockLocationLineService;
    this.productCompanyService = productCompanyService;
    this.stockLocationRepository = stockLocationRepository;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
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
  public InventoryLine updateInventoryLine(InventoryLine inventoryLine, Inventory inventory)
      throws AxelorException {

    StockLocation stockLocation =
        Optional.ofNullable(inventoryLine.getStockLocation()).orElse(inventory.getStockLocation());
    Product product = inventoryLine.getProduct();

    if (product == null) {
      return inventoryLine;
    }

    inventoryLine.setPrice(BigDecimal.ZERO);
    StockLocationLine stockLocationLine =
        stockLocationLineService.getOrCreateStockLocationLine(stockLocation, product);

    if (stockLocationLine != null) {
      inventoryLine.setCurrentQty(getCurrentQtyInProductUnit(stockLocationLine));
      inventoryLine.setRack(stockLocationLine.getRack());
      if (inventoryLine.getTrackingNumber() != null) {
        StockLocationLine detailLocationLine =
            stockLocationLineFetchService.getDetailLocationLine(
                stockLocation, product, inventoryLine.getTrackingNumber());
        inventoryLine.setCurrentQty(getCurrentQtyInProductUnit(detailLocationLine));
      }
    } else {
      inventoryLine.setCurrentQty(null);
      inventoryLine.setRack(null);
    }

    return inventoryLine;
  }

  @Override
  public InventoryLine compute(InventoryLine inventoryLine, Inventory inventory)
      throws AxelorException {

    Product product = inventoryLine.getProduct();

    if (product == null) {
      return inventoryLine;
    }

    StockLocation stockLocation =
        Optional.ofNullable(inventoryLine.getStockLocation()).orElse(inventory.getStockLocation());
    StockLocationLine stockLocationLine =
        stockLocationLineFetchService.getStockLocationLine(stockLocation, product);
    Company company = stockLocation.getCompany();

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
    int inventoryValuationTypeSelect = getInventoryValuationTypeSelect(company);

    BigDecimal productAvgPrice =
        (BigDecimal) productCompanyService.get(product, "avgPrice", company);

    switch (inventoryValuationTypeSelect) {
      case StockConfigRepository.VALUATION_TYPE_WAP_VALUE:
        price = productAvgPrice;
        break;
      case StockConfigRepository.VALUATION_TYPE_ACCOUNTING_VALUE:
        price = (BigDecimal) productCompanyService.get(product, "costPrice", company);
        break;
      case StockConfigRepository.VALUATION_TYPE_SALE_VALUE:
        price = (BigDecimal) productCompanyService.get(product, "salePrice", company);
        break;
      case StockConfigRepository.VALUATION_TYPE_PURCHASE_VALUE:
        price = (BigDecimal) productCompanyService.get(product, "purchasePrice", company);
        break;
      case StockConfigRepository.VALUATION_TYPE_WAP_STOCK_LOCATION_VALUE:
        if (stockLocationLine != null) {
          price = convertAvgPriceToProductUnit(stockLocationLine);
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

    return inventoryLine;
  }

  @Override
  public BigDecimal getCurrentQty(StockLocation stockLocation, Product product)
      throws AxelorException {
    BigDecimal currentQty = BigDecimal.ZERO;

    if (stockLocation != null && product != null) {
      StockLocationLine stockLocationLine =
          stockLocationLineFetchService.getStockLocationLine(stockLocation, product);
      currentQty = getCurrentQtyInProductUnit(stockLocationLine);
    }
    return currentQty;
  }

  @Override
  public BigDecimal getCurrentQtyInProductUnit(StockLocationLine stockLocationLine)
      throws AxelorException {
    if (stockLocationLine == null) {
      return BigDecimal.ZERO;
    }

    if (stockLocationLine.getUnit() == null) {
      throw missingUnitException(stockLocationLine, stockLocationLine.getProduct());
    }

    BigDecimal currentQty =
        stockLocationLineService.convertToProductUnit(
            stockLocationLine, stockLocationLine.getCurrentQty());
    return currentQty == null ? BigDecimal.ZERO : currentQty;
  }

  /**
   * The average price of a stock location line is expressed per stock location line unit, while the
   * inventory line quantities are expressed in the product unit. A price converts in the opposite
   * direction to a quantity, hence the product unit to stock location line unit conversion.
   */
  protected BigDecimal convertAvgPriceToProductUnit(StockLocationLine stockLocationLine)
      throws AxelorException {
    BigDecimal avgPrice =
        stockLocationLineService.convertFromProductUnit(
            stockLocationLine, stockLocationLine.getAvgPrice());
    return avgPrice == null ? BigDecimal.ZERO : avgPrice;
  }

  protected AxelorException missingUnitException(
      StockLocationLine stockLocationLine, Product product) {
    if (stockLocationLine.getTrackingNumber() != null) {
      return new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.DETAIL_LOCATION_LINE_MISSING_UNIT),
          stockLocationLine.getTrackingNumber().getTrackingNumberSeq(),
          stockLocationLine.getDetailsStockLocation() != null
              ? stockLocationLine.getDetailsStockLocation().getName()
              : "",
          product == null ? "" : product.getFullName());
    }
    return new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(StockExceptionMessage.LOCATION_LINE_MISSING_UNIT),
        stockLocationLine.getStockLocation() != null
            ? stockLocationLine.getStockLocation().getName()
            : "",
        product == null ? "" : product.getFullName());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInventoryLine(
      InventoryLine inventoryLine,
      BigDecimal realQty,
      String description,
      StockLocation stockLocation)
      throws AxelorException {
    inventoryLine.setRealQty(realQty);
    if (description != null) {
      inventoryLine.setDescription(description);
    }
    if (stockLocation != null) {
      inventoryLine.setStockLocation(stockLocation);
    }

    Inventory inventory = inventoryLine.getInventory();
    updateInventoryLine(inventoryLine, inventory);
    this.compute(inventoryLine, inventory);
    inventoryLineRepository.save(inventoryLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InventoryLine addLine(
      Inventory inventory,
      Product product,
      TrackingNumber trackingNumber,
      String rack,
      BigDecimal realQty,
      StockLocation stockLocation)
      throws AxelorException {

    StockLocation finalStockLocation =
        stockLocation != null ? stockLocation : inventory.getStockLocation();

    InventoryLine inventoryLine =
        createInventoryLine(
            inventory,
            product,
            getCurrentQty(finalStockLocation, product),
            rack,
            trackingNumber,
            null,
            null,
            finalStockLocation,
            null);
    updateInventoryLine(inventoryLine, realQty, null, null);
    return inventoryLine;
  }

  @Override
  public boolean isPresentInStockLocation(InventoryLine inventoryLine) {
    if (inventoryLine.getProduct() == null || inventoryLine.getStockLocation() == null) {
      return false;
    }
    ImmutablePair<Long, Long> key =
        ImmutablePair.of(
            inventoryLine.getStockLocation().getId(), inventoryLine.getProduct().getId());

    return presenceCache.get(
        key,
        k ->
            stockLocationLineFetchService.getStockLocationLine(
                    inventoryLine.getStockLocation(), inventoryLine.getProduct())
                != null);
  }

  protected int getInventoryValuationTypeSelect(Company company) {
    return valuationTypeCache.get(
        company.getId(),
        k -> {
          try {
            return stockConfigService.getStockConfig(company).getInventoryValuationTypeSelect();
          } catch (AxelorException e) {
            throw new RuntimeException(
                "Error fetching valuation type for company " + company.getId(), e);
          }
        });
  }
}
