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
package com.axelor.apps.sale.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
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
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.studio.db.AppBase;
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
    AppBaseService appBaseService = mock(AppBaseService.class);
    AppBase appBase = mock(AppBase.class);
    when(appBaseService.getAppBase()).thenReturn(appBase);
    when(appBase.getEnablePricingScale()).thenReturn(false);
    return new SaleOrderLineComputeServiceImpl(
        mock(TaxService.class),
        currencyScaleService,
        mock(ProductCompanyService.class),
        createSaleOrderMarginService(appSaleService),
        currencyService,
        priceListService,
        mock(SaleOrderLinePackService.class),
        createSaleOrderLineCostPriceComputeService(appSaleService),
        appBaseService,
        mock(SaleOrderLinePricingService.class));
  }

  protected MarginComputeService createSaleOrderMarginService(AppSaleService appSaleService) {
    return new MarginComputeServiceImpl(appSaleService, currencyService, currencyScaleService);
  }

  protected SaleOrderLineCostPriceComputeService createSaleOrderLineCostPriceComputeService(
      AppSaleService appSaleService) {
    return new SaleOrderLineCostPriceComputeServiceImpl(
        appSaleService,
        mock(ProductCompanyService.class),
        currencyScaleService,
        mock(SaleOrderLineProductService.class));
  }

  protected SubSaleOrderLineComputeService createSubSaleOrderLineComputeService(
      AppSaleService appSaleService) {
    return new SubSaleOrderLineComputeServiceImpl(
        createSaleOrderLineComputeService(appSaleService),
        appSaleService,
        currencyScaleService,
        mock(SaleOrderLinePriceService.class),
        mock(SaleOrderLineProductService.class));
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

  @Test
  void testApplyFixedGlobalDiscountOverPreexistingPercentLineDiscount() throws AxelorException {
    // reporter scenario: line 200 with a manual 10% discount, line 300, then global fixed 50
    saleOrder.setSaleOrderLineList(new ArrayList<>());
    SaleOrderLine line200 =
        createSaleOrderLine(
            BigDecimal.valueOf(200).setScale(SCALE_VALUE, RoundingMode.HALF_UP), BigDecimal.ONE);
    line200.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_PERCENT);
    line200.setDiscountAmount(BigDecimal.TEN);
    SaleOrderLine line300 =
        createSaleOrderLine(
            BigDecimal.valueOf(300).setScale(SCALE_VALUE, RoundingMode.HALF_UP), BigDecimal.ONE);
    saleOrder.addSaleOrderLineListItem(line200);
    saleOrder.addSaleOrderLineListItem(line300);

    saleOrder.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_FIXED);
    saleOrder.setDiscountAmount(BigDecimal.valueOf(50));
    saleOrderGlobalDiscountService.applyGlobalDiscountOnLines(saleOrder);

    Assertions.assertEquals(
        PriceListLineRepository.AMOUNT_TYPE_FIXED, line200.getDiscountTypeSelect());
    Assertions.assertEquals(
        0, BigDecimal.valueOf(20).compareTo(line200.getDiscountAmount()), "line 200 -> fixed 20");
    Assertions.assertEquals(
        PriceListLineRepository.AMOUNT_TYPE_FIXED, line300.getDiscountTypeSelect());
    Assertions.assertEquals(
        0, BigDecimal.valueOf(30).compareTo(line300.getDiscountAmount()), "line 300 -> fixed 30");
  }

  @Test
  void testResetGlobalDiscountOnLinesClearsLineDiscounts() throws AxelorException {
    // a global discount is applied: lines carry a derived discount
    saleOrder.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_PERCENT);
    saleOrder.setDiscountAmount(BigDecimal.TEN);
    saleOrderGlobalDiscountService.applyGlobalDiscountOnLines(saleOrder);

    // the global discount is removed: lines must be reset and totals recomputed at full price
    saleOrder.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    saleOrder.setDiscountAmount(BigDecimal.ZERO);
    saleOrderGlobalDiscountService.resetGlobalDiscountOnLines(saleOrder);
    saleOrderComputeService.computeSaleOrder(saleOrder);

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Assertions.assertEquals(
          PriceListLineRepository.AMOUNT_TYPE_NONE, saleOrderLine.getDiscountTypeSelect());
      Assertions.assertEquals(
          0,
          BigDecimal.ZERO.compareTo(saleOrderLine.getDiscountAmount()),
          "line discount amount should be reset to zero");
    }
    Assertions.assertEquals(
        BigDecimal.valueOf(102).setScale(SCALE_VALUE, RoundingMode.HALF_UP),
        saleOrder.getExTaxTotal());
  }
}
