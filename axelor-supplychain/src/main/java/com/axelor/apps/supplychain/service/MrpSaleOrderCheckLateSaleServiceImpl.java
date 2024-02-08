/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.google.inject.Inject;
import java.time.LocalDate;

public class MrpSaleOrderCheckLateSaleServiceImpl implements MrpSaleOrderCheckLateSaleService {

  protected AppBaseService appBaseService;

  @Inject
  public MrpSaleOrderCheckLateSaleServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean checkLateSalesParameter(SaleOrderLine saleOrderLine, MrpLineType mrpLineType) {
    // Determine deliveryDate
    LocalDate deliveryDate = saleOrderLine.getEstimatedShippingDate();
    if (deliveryDate == null) {
      deliveryDate = saleOrderLine.getSaleOrder().getEstimatedShippingDate();
    }
    if (deliveryDate == null) {
      deliveryDate = saleOrderLine.getDesiredDeliveryDate();
    }

    // Determine if a line should be created
    if (deliveryDate == null && !mrpLineType.getIncludeElementWithoutDate()) {
      return false;
    }
    LocalDate todayDate = appBaseService.getTodayDate(saleOrderLine.getSaleOrder().getCompany());
    if (mrpLineType.getLateSalesSelect() == MrpLineTypeRepository.LATE_SALES_EXCLUDED) {
      if (deliveryDate != null && deliveryDate.isBefore(todayDate)) {
        return false;
      }
    } else if (mrpLineType.getLateSalesSelect() == MrpLineTypeRepository.LATE_SALES_ONLY) {
      if (deliveryDate == null || !deliveryDate.isBefore(todayDate)) {
        return false;
      }
    }

    return true;
  }
}
