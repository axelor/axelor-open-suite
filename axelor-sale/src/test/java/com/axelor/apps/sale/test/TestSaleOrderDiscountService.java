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
package com.axelor.apps.sale.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.MarginComputeServiceImpl;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderGlobalDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGlobalDiscountServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.studio.db.AppSale;
import com.axelor.utils.junit.BaseTest;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSaleOrderDiscountService extends BaseTest {

  protected static final int SCALE_VALUE = 2;

  protected final PriceListService priceListService;
  protected final CurrencyService currencyService;
  protected final CurrencyScaleService currencyScaleService;
  protected SaleOrderGlobalDiscountService saleOrderGlobalDiscountService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrder saleOrder;

  @Inject
  TestSaleOrderDiscountService(
      PriceListService priceListService,
      CurrencyService currencyService,
      CurrencyScaleService currencyScaleService,
      SaleOrderGlobalDiscountService saleOrderGlobalDiscountService) {
    this.priceListService = priceListService;
    this.currencyService = currencyService;
    this.currencyScaleService = currencyScaleService;
    this.saleOrderGlobalDiscountService = saleOrderGlobalDiscountService;
  }

  @BeforeEach
  void prepare() {
    AppSale appSale = mock(AppSale.class);
    AppSaleService appSaleService = mock(AppSaleService.class);
    when(appSale.getConsiderZeroCost()).thenReturn(false);
    when(appSaleService.getAppSale()).thenReturn(appSale);
    createSaleOrderComputeService(appSaleService);
    saleOrderGlobalDiscountService =
        new SaleOrderGlobalDiscountServiceImpl(saleOrderComputeService);
    prepareSaleOrder();
  }

  protected void createSaleOrderComputeService(AppSaleService appSaleService) {
    saleOrderComputeService =
        new SaleOrderComputeServiceImpl(
            mock(SaleOrderLineCreateTaxLineService.class),
            createSaleOrderLineComputeService(appSaleService),
            mock(SaleOrderLinePackService.class),
            createSubSaleOrderLineComputeService(appSaleService),
            appSaleService);
  }

  protected SaleOrderLineComputeService createSaleOrderLineComputeService(
      AppSaleService appSaleService) {
    return new SaleOrderLineComputeServiceImpl(
        mock(TaxService.class),
        currencyScaleService,
        mock(ProductCompanyService.class),
        createSaleOrderMarginService(appSaleService),
        currencyService,
        priceListService,
        mock(SaleOrderLinePackService.class),
        createSaleOrderLineCostPriceComputeService(appSaleService));
  }

  protected MarginComputeService createSaleOrderMarginService(AppSaleService appSaleService) {
    return new MarginComputeServiceImpl(appSaleService, currencyService, currencyScaleService);
  }

  protected SaleOrderLineCostPriceComputeService createSaleOrderLineCostPriceComputeService(
      AppSaleService appSaleService) {
    return new SaleOrderLineCostPriceComputeServiceImpl(
        appSaleService, mock(ProductCompanyService.class), currencyScaleService);
  }

  protected SubSaleOrderLineComputeService createSubSaleOrderLineComputeService(
      AppSaleService appSaleService) {
    return new SubSaleOrderLineComputeServiceImpl(
        createSaleOrderLineComputeService(appSaleService), appSaleService, currencyScaleService);
  }

  protected void prepareSaleOrder() {
    saleOrder = new SaleOrder();
    saleOrder.setSaleOrderLineList(new ArrayList<>());
    saleOrder.addSaleOrderLineListItem(
        createSaleOrderLine(
            BigDecimal.valueOf(80).setScale(SCALE_VALUE, RoundingMode.HALF_UP), BigDecimal.ONE));
    saleOrder.addSaleOrderLineListItem(
        createSaleOrderLine(
            BigDecimal.valueOf(22).setScale(SCALE_VALUE, RoundingMode.HALF_UP), BigDecimal.ONE));
  }

  protected SaleOrderLine createSaleOrderLine(BigDecimal price, BigDecimal qty) {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);
    saleOrderLine.setPrice(price);
    saleOrderLine.setQty(qty);
    return saleOrderLine;
  }

  @Test
  void testApplyPercentageGlobalDiscountOnLines() throws AxelorException {
    saleOrder.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_PERCENT);
    saleOrder.setDiscountAmount(BigDecimal.TEN);
    saleOrderGlobalDiscountService.applyGlobalDiscountOnLines(saleOrder);
    Assertions.assertEquals(
        BigDecimal.valueOf(91.8).setScale(SCALE_VALUE, RoundingMode.HALF_UP),
        saleOrder.getExTaxTotal());
  }

  @Test
  void testApplyFixedGlobalDiscountOnLines() throws AxelorException {
    saleOrder.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_FIXED);
    saleOrder.setDiscountAmount(BigDecimal.TEN);
    saleOrderGlobalDiscountService.applyGlobalDiscountOnLines(saleOrder);
    Assertions.assertEquals(
        BigDecimal.valueOf(92).setScale(SCALE_VALUE, RoundingMode.HALF_UP),
        saleOrder.getExTaxTotal());
  }
}
