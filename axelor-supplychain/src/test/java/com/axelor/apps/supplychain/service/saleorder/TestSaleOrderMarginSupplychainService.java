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
package com.axelor.apps.supplychain.service.saleorder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.studio.db.AppSale;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSaleOrderMarginSupplychainService {

  private SaleOrderMarginSupplychainServiceImpl saleOrderMarginService;

  @BeforeEach
  void prepare() {
    AppSaleService appSaleService = mock(AppSaleService.class);
    when(appSaleService.getAppSale()).thenReturn(new AppSale());
    when(appSaleService.isApp("supplychain")).thenReturn(true);
    saleOrderMarginService =
        new SaleOrderMarginSupplychainServiceImpl(
            appSaleService,
            mock(CurrencyService.class),
            mock(ProductCompanyService.class),
            mock(CurrencyScaleService.class));
  }

  @Test
  void testComputeMarginSaleOrderWithoutShippingCost() {
    SaleOrder saleOrder = createSaleOrder(BigDecimal.ZERO);

    saleOrderMarginService.computeMarginSaleOrder(saleOrder);

    assertMargin(saleOrder, "600", "400", "40", "66.67");
  }

  @Test
  void testComputeMarginSaleOrderWithShippingCost() {
    SaleOrder saleOrder = createSaleOrder(new BigDecimal("100"));

    saleOrderMarginService.computeMarginSaleOrder(saleOrder);

    assertMargin(saleOrder, "700", "300", "30", "42.86");
  }

  protected SaleOrder createSaleOrder(BigDecimal shippingCost) {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setProduct(new Product());
    saleOrderLine.setExTaxTotal(new BigDecimal("1000"));
    saleOrderLine.setCompanyExTaxTotal(new BigDecimal("1000"));
    saleOrderLine.setSubTotalCostPrice(new BigDecimal("600"));
    saleOrderLine.setSubTotalGrossMargin(new BigDecimal("400"));

    SaleOrder saleOrder = new SaleOrder();
    saleOrder.addSaleOrderLineListItem(saleOrderLine);
    saleOrder.setShippingCost(shippingCost);
    return saleOrder;
  }

  protected void assertMargin(
      SaleOrder saleOrder,
      String totalCostPrice,
      String totalGrossMargin,
      String marginRate,
      String markup) {
    Assertions.assertEquals(0, new BigDecimal("1000").compareTo(saleOrder.getAccountedRevenue()));
    Assertions.assertEquals(
        0, new BigDecimal(totalCostPrice).compareTo(saleOrder.getTotalCostPrice()));
    Assertions.assertEquals(
        0, new BigDecimal(totalGrossMargin).compareTo(saleOrder.getTotalGrossMargin()));
    Assertions.assertEquals(0, new BigDecimal(marginRate).compareTo(saleOrder.getMarginRate()));
    Assertions.assertEquals(0, new BigDecimal(markup).compareTo(saleOrder.getMarkup()));
  }
}
