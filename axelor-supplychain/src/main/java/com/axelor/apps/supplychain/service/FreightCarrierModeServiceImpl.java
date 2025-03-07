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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class FreightCarrierModeServiceImpl implements FreightCarrierModeService {

  protected final SaleOrderRepository saleOrderRepository;

  @Inject
  public FreightCarrierModeServiceImpl(SaleOrderRepository saleOrderRepository) {
    this.saleOrderRepository = saleOrderRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public void computeFreightCarrierMode(
      List<FreightCarrierMode> freightCarrierModeList, Long saleOrderId) throws AxelorException {
    SaleOrder saleOrder = saleOrderRepository.find(saleOrderId);
    if (saleOrder != null) {
      this.checkSelectedFreightCarrierMode(freightCarrierModeList);
      FreightCarrierMode freightCarrierMode = freightCarrierModeList.get(0);
      saleOrder.setFreightCarrierMode(freightCarrierMode);
      saleOrder.setCarrierPartner(freightCarrierMode.getCarrierPartner());

      saleOrderRepository.save(saleOrder);
    }
  }

  protected void checkSelectedFreightCarrierMode(List<FreightCarrierMode> freightCarrierModeList)
      throws AxelorException {
    if (freightCarrierModeList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(
                  SupplychainExceptionMessage.SALE_ORDER_NO_FREIGHT_CARRIER_PRICING_SELECTED)));
    }

    if (freightCarrierModeList.size() > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(
                  SupplychainExceptionMessage
                      .SALE_ORDER_MORE_THAN_ONE_FREIGHT_CARRIER_PRICING_SELECTED)));
    }
  }
}
