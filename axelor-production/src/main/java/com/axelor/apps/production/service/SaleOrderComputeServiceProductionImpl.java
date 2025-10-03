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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.apps.supplychain.service.invoice.AdvancePaymentRefundService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderAdvancePaymentFetchService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderComputeServiceSupplychainImpl;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderComputeServiceProductionImpl extends SaleOrderComputeServiceSupplychainImpl {

  @Inject
  public SaleOrderComputeServiceProductionImpl(
      SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SubSaleOrderLineComputeService subSaleOrderLineComputeService,
      AppSaleService appSaleService,
      AdvancePaymentRefundService refundService,
      SaleOrderAdvancePaymentFetchService saleOrderAdvancePaymentFetchService) {
    super(
        saleOrderLineCreateTaxLineService,
        saleOrderLineComputeService,
        saleOrderLinePackService,
        subSaleOrderLineComputeService,
        appSaleService,
        refundService,
        saleOrderAdvancePaymentFetchService);
  }

  @Override
  public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrder;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      subSaleOrderLineComputeService.computeSumSubLineList(saleOrderLine, saleOrder);
    }

    return saleOrder;
  }
}
