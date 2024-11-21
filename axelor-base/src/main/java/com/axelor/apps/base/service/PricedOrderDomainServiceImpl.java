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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.interfaces.PricedOrder;

public class PricedOrderDomainServiceImpl implements PricedOrderDomainService {

  @Override
  public String getPartnerDomain(PricedOrder pricedOrder, String domain, int invoiceTypeSelect) {
    StringBuilder newDomain = new StringBuilder(domain);
    if (pricedOrder != null) {
      if (pricedOrder.getCurrency() != null) {
        newDomain.append(
            String.format(" AND self.currency.id = %d", pricedOrder.getCurrency().getId()));
      }
      if (invoiceTypeSelect == PriceListRepository.TYPE_SALE) {
        if (pricedOrder.getPriceList() != null) {
          newDomain.append(
              String.format(
                  " AND %d IN self.salePartnerPriceList.priceListSet.id",
                  pricedOrder.getPriceList().getId()));
        } else {
          newDomain.append(" AND self.salePartnerPriceList is NULL");
        }
      } else if (invoiceTypeSelect == PriceListRepository.TYPE_PURCHASE) {
        if (pricedOrder.getPriceList() != null) {
          newDomain.append(
              String.format(
                  " AND %d IN self.purchasePartnerPriceList.priceListSet.id",
                  pricedOrder.getPriceList().getId()));
        } else {
          newDomain.append(" AND self.purchasePartnerPriceList is NULL");
        }
      }
    }
    return newDomain.toString();
  }
}
