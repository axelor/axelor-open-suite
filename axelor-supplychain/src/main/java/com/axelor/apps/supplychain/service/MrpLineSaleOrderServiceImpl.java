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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderSplitService;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.studio.db.AppSale;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MrpLineSaleOrderServiceImpl implements MrpLineSaleOrderService {

  protected final AppSaleService appSaleService;
  protected final SaleOrderSplitService saleOrderSplitService;
  protected final UnitConversionService unitConversionService;

  @Inject
  public MrpLineSaleOrderServiceImpl(
      AppSaleService appSaleService,
      SaleOrderSplitService saleOrderSplitService,
      UnitConversionService unitConversionService) {
    this.appSaleService = appSaleService;
    this.saleOrderSplitService = saleOrderSplitService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public BigDecimal getSoMrpLineQty(SaleOrderLine saleOrderLine, Unit unit, MrpLineType mrpLineType)
      throws AxelorException {
    AppSale appSale = appSaleService.getAppSale();
    boolean isSplitQuotationEnabled = appSale.getIsQuotationAndOrderSplitEnabled();
    BigDecimal qty = saleOrderLine.getQty().subtract(saleOrderLine.getDeliveredQty());
    List<Integer> statusList = StringHelper.getIntegerList(mrpLineType.getStatusSelect());
    boolean isStatusCompatible =
        CollectionUtils.isNotEmpty(statusList)
            && statusList.contains(SaleOrderRepository.STATUS_FINALIZED_QUOTATION)
            && statusList.contains(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    if (isSplitQuotationEnabled
        && isStatusCompatible
        && saleOrderLine.getSaleOrder().getStatusSelect()
            == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      qty = saleOrderSplitService.getQtyToOrderLeft(saleOrderLine);
    }

    if (!unit.equals(saleOrderLine.getUnit())) {
      qty =
          unitConversionService.convert(
              saleOrderLine.getUnit(),
              unit,
              qty,
              saleOrderLine.getQty().scale(),
              saleOrderLine.getProduct());
    }
    return qty;
  }
}
