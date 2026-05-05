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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.config.OutSmGenerationService;
import com.axelor.common.ObjectUtils;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class SaleOrderLineQtyToDeliverServiceImpl implements SaleOrderLineQtyToDeliverService {

  protected final AppBaseService appBaseService;
  protected final OutSmGenerationService outSmGenerationService;

  @Inject
  public SaleOrderLineQtyToDeliverServiceImpl(
      AppBaseService appBaseService, OutSmGenerationService outSmGenerationService) {
    this.appBaseService = appBaseService;
    this.outSmGenerationService = outSmGenerationService;
  }

  @Override
  public void initQtyToDeliverForAll(List<SaleOrderLine> lines) {
    initQtyToDeliverForAll(lines, BigDecimal.ONE, appBaseService.getNbDecimalDigitForQty());
  }

  protected void initQtyToDeliverForAll(
      List<SaleOrderLine> lines, BigDecimal parentCumulative, int scale) {
    if (ObjectUtils.isEmpty(lines)) {
      return;
    }
    for (SaleOrderLine line : lines) {
      BigDecimal lineQty = line.getQty() != null ? line.getQty() : BigDecimal.ZERO;
      BigDecimal cumulative =
          lineQty.multiply(parentCumulative).setScale(scale, RoundingMode.HALF_UP);
      line.setQtyToDeliver(cumulative);
      initQtyToDeliverForAll(line.getSubSaleOrderLineList(), cumulative, scale);
    }
  }

  @Override
  public void initQtyToDeliverForAllIfManagedLines(SaleOrder saleOrder) {
    SupplyChainConfig config =
        Optional.ofNullable(saleOrder)
            .map(SaleOrder::getCompany)
            .map(Company::getSupplyChainConfig)
            .orElse(null);
    if (config == null || !outSmGenerationService.isOnlyForManagedLines(config)) {
      return;
    }
    initQtyToDeliverForAll(saleOrder.getSaleOrderLineList());
  }
}
