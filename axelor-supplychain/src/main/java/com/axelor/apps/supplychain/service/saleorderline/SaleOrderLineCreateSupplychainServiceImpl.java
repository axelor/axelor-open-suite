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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineCreateServiceImpl;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderLineCreateSupplychainServiceImpl extends SaleOrderLineCreateServiceImpl {

  protected AnalyticLineModelService analyticLineModelService;
  protected SupplyChainConfigService supplyChainConfigService;
  protected ReservedQtyService reservedQtyService;

  @Inject
  public SaleOrderLineCreateSupplychainServiceImpl(
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      AnalyticLineModelService analyticLineModelService,
      SupplyChainConfigService supplyChainConfigService,
      ReservedQtyService reservedQtyService) {
    super(appSaleService, appBaseService, saleOrderLineComputeService, saleOrderLinePackService);
    this.analyticLineModelService = analyticLineModelService;
    this.supplyChainConfigService = supplyChainConfigService;
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  public SaleOrderLine createSaleOrderLine(
      PackLine packLine,
      SaleOrder saleOrder,
      BigDecimal packQty,
      BigDecimal conversionRate,
      Integer sequence)
      throws AxelorException {

    SaleOrderLine soLine =
        super.createSaleOrderLine(packLine, saleOrder, packQty, conversionRate, sequence);

    if (soLine != null && soLine.getProduct() != null) {
      soLine.setSaleSupplySelect(soLine.getProduct().getSaleSupplySelect());

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(soLine, null);
      analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);

      if (ObjectUtils.notEmpty(soLine.getAnalyticMoveLineList())) {
        soLine
            .getAnalyticMoveLineList()
            .forEach(analyticMoveLine -> analyticMoveLine.setSaleOrderLine(soLine));
      }
      SupplyChainConfig supplyChainConfig =
          supplyChainConfigService.getSupplyChainConfig(saleOrder.getCompany());

      if (supplyChainConfig.getAutoRequestReservedQty()) {
        reservedQtyService.requestQty(soLine);
      }
    }
    return soLine;
  }
}
