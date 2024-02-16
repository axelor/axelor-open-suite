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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.utils.junit.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestInventoryLineService extends BaseTest {

  protected final LoaderHelper loaderHelper;
  protected final StockLocationRepository stockLocationRepository;

  protected final InventoryLineService inventoryLineService;

  protected final ProductRepository productRepository;

  public TestInventoryLineService() {
    this.inventoryLineService = new InventoryLineServiceImpl(null, null, null, null);
    this.loaderHelper = Beans.get(LoaderHelper.class);
    this.stockLocationRepository = new StockLocationRepository();
    this.productRepository = new ProductRepository();
  }

  @BeforeEach
  void setUpStockLocation() {
    loaderHelper.importCsv("data/stock-stock-location.xml");
  }

  @Test
  void testInventoryLineIsPresentInStockLocation() {

    StockLocation stockLocation =
        stockLocationRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1L)
            .fetchOne();
    Product product =
        productRepository.all().filter("self.importId = :importId").bind("importId", 1L).fetchOne();

    InventoryLine inventoryLine = new InventoryLine();

    inventoryLine.setStockLocation(stockLocation);
    inventoryLine.setProduct(product);

    Assertions.assertTrue(inventoryLineService.isPresentInStockLocation(inventoryLine));
  }

  @Test
  void testInventoryLineIsNotPresentInStockLocation() {

    StockLocation stockLocation =
        stockLocationRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1L)
            .fetchOne();
    Product product = new Product();
    product.setCode("Test");
    product.setName("Test");

    InventoryLine inventoryLine = new InventoryLine();

    inventoryLine.setStockLocation(stockLocation);
    inventoryLine.setProduct(product);

    Assertions.assertFalse(inventoryLineService.isPresentInStockLocation(inventoryLine));
  }

  @Test
  void testInventoryLineNPEPresentInStockLocation() {

    Assertions.assertThrows(
        NullPointerException.class, () -> inventoryLineService.isPresentInStockLocation(null));
  }
}
