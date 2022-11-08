/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.interfaces.PricedOrder;

public class PricedOrderDomainServiceImpl implements PricedOrderDomainService {

  @Override
  public String getPartnerDomain(PricedOrder pricedOrder, String domain) {
    StringBuilder newDomain = new StringBuilder(domain);
    if (pricedOrder != null) {
      if (pricedOrder.getCurrency() != null) {
        newDomain.append(
            String.format(" AND self.currency.id = %d", pricedOrder.getCurrency().getId()));
      }
      if (pricedOrder.getPriceList() != null) {
        newDomain.append(
            String.format(
                " AND %d IN self.salePartnerPriceList.priceListSet.id",
                pricedOrder.getPriceList().getId()));
      } else {
        newDomain.append(" AND self.salePartnerPriceList is NULL");
      }
      if (pricedOrder.getFiscalPosition() != null) {
        newDomain.append(
            String.format(
                " AND self.fiscalPosition.id = %s", pricedOrder.getFiscalPosition().getId()));
      } else {
        newDomain.append(" AND self.fiscalPosition is NULL");
      }
    }
    return newDomain.toString();
  }
}
