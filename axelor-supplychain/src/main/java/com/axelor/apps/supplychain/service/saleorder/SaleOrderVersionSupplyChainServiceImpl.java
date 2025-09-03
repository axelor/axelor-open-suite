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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionServiceImpl;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnLineChangeService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderVersionSupplyChainServiceImpl extends SaleOrderVersionServiceImpl
    implements SaleOrderVersionService {

  @Inject
  public SaleOrderVersionSupplyChainServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      AppBaseService appBaseService,
      SaleOrderOnLineChangeService saleOrderOnLineChangeService,
      AppSaleService appSaleService) {
    super(
        saleOrderRepository,
        saleOrderLineRepository,
        appBaseService,
        saleOrderOnLineChangeService,
        appSaleService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createNewVersion(SaleOrder saleOrder) {
    saleOrder.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    saleOrder.setInvoicingState(SaleOrderRepository.INVOICING_STATE_NOT_INVOICED);
    super.createNewVersion(saleOrder);
  }
}
