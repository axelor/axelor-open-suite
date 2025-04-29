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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
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
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class SaleOrderLineComputeSupplychainServiceImpl extends SaleOrderLineComputeServiceImpl {

  protected AppBaseService appBaseService;
  protected AppSupplychainService appSupplychainService;
  protected AppAccountService appAccountService;
  protected AnalyticLineModelService analyticLineModelService;
  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;

  @Inject
  public SaleOrderLineComputeSupplychainServiceImpl(
      TaxService taxService,
      CurrencyScaleService currencyScaleService,
      ProductCompanyService productCompanyService,
      MarginComputeService marginComputeService,
      CurrencyService currencyService,
      PriceListService priceListService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineCostPriceComputeService saleOrderLineCostPriceComputeService,
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppAccountService appAccountService,
      AnalyticLineModelService analyticLineModelService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain) {
    super(
        taxService,
        currencyScaleService,
        productCompanyService,
        marginComputeService,
        currencyService,
        priceListService,
        saleOrderLinePackService,
        saleOrderLineCostPriceComputeService);
    this.appBaseService = appBaseService;
    this.appSupplychainService = appSupplychainService;
    this.appAccountService = appAccountService;
    this.analyticLineModelService = analyticLineModelService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
  }

  @Override
  public Map<String, Object> updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {

    Map<String, Object> saleOrderLineMap =
        super.updateProductQty(saleOrderLine, saleOrder, oldQty, newQty);

    BigDecimal qty = saleOrderLine.getQty();
    qty =
        qty.divide(oldQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
            .multiply(newQty)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    saleOrderLine.setQty(qty);

    qty =
        saleOrderLineServiceSupplyChain.checkInvoicedOrDeliveredOrderQty(saleOrderLine, saleOrder);
    saleOrderLine.setQty(qty);
    saleOrderLineMap.put("qty", qty);

    if (!appSupplychainService.isApp("supplychain")
        || saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      return saleOrderLineMap;
    }
    if (appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, null);
      analyticLineModelService.computeAnalyticDistribution(analyticLineModel);
    }
    if (appSupplychainService.getAppSupplychain().getManageStockReservation()
        && (saleOrderLine.getRequestedReservedQty().compareTo(qty) > 0
            || saleOrderLine.getIsQtyRequested())) {
      saleOrderLine.setRequestedReservedQty(BigDecimal.ZERO.max(qty));
      saleOrderLineMap.put("requestedReservedQty", saleOrderLine.getRequestedReservedQty());
    }
    return saleOrderLineMap;
  }
}
