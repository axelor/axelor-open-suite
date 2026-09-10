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
package com.axelor.apps.quality.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservedQtyServiceQualityImplTest {

  private AppQualityService appQualityService;
  private NonCompliantReceptionService nonCompliantReceptionService;
  private ReservedQtyServiceQualityImpl service;
  private SupplyChainConfig supplyChainConfig;
  private StockMoveLine stockMoveLine;

  @BeforeEach
  void setUp() {
    appQualityService = mock(AppQualityService.class);
    when(appQualityService.isApp("quality")).thenReturn(true);
    nonCompliantReceptionService = mock(NonCompliantReceptionService.class);
    service =
        new ReservedQtyServiceQualityImpl(
            mock(StockLocationLineService.class),
            mock(StockMoveLineRepository.class),
            mock(UnitConversionService.class),
            mock(SupplyChainConfigService.class),
            mock(AppBaseService.class),
            mock(StockLocationLineFetchService.class),
            appQualityService,
            nonCompliantReceptionService);
    supplyChainConfig = new SupplyChainConfig();
    supplyChainConfig.setAutoAllocateOnReceipt(true);
    stockMoveLine = new StockMoveLine();
  }

  @Test
  void quarantinedLineIsNeverAutoAllocated() {
    when(nonCompliantReceptionService.isRedirectedToQuarantine(stockMoveLine)).thenReturn(true);

    assertFalse(service.isAutoAllocateOnReceipt(stockMoveLine, supplyChainConfig));
  }

  @Test
  void otherLinesFollowTheSupplychainConfiguration() {
    when(nonCompliantReceptionService.isRedirectedToQuarantine(stockMoveLine)).thenReturn(false);

    assertTrue(service.isAutoAllocateOnReceipt(stockMoveLine, supplyChainConfig));
    supplyChainConfig.setAutoAllocateOnReceipt(false);
    assertFalse(service.isAutoAllocateOnReceipt(stockMoveLine, supplyChainConfig));
  }

  @Test
  void appOffFollowsTheSupplychainConfiguration() {
    when(appQualityService.isApp("quality")).thenReturn(false);
    when(nonCompliantReceptionService.isRedirectedToQuarantine(stockMoveLine)).thenReturn(true);

    assertTrue(service.isAutoAllocateOnReceipt(stockMoveLine, supplyChainConfig));
  }
}
