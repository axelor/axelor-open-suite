/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.OpportunitySaleOrderServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderServiceSupplychainImpl extends OpportunitySaleOrderServiceImpl {

  @Inject private SaleOrderServiceSupplychainImpl saleOrderServiceSupplychainImpl;

  @Inject protected GeneralService generalService;

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException {
    Currency currency;
    if (opportunity.getCurrency() != null) {
      currency = opportunity.getCurrency();
    } else if (opportunity.getPartner() != null && opportunity.getPartner().getCurrency() != null) {
      currency = opportunity.getPartner().getCurrency();
    } else {
      currency = opportunity.getCompany().getCurrency();
    }

    SaleOrder saleOrder =
        saleOrderServiceSupplychainImpl.createSaleOrder(
            opportunity.getUser(),
            opportunity.getCompany(),
            null,
            currency,
            null,
            opportunity.getName(),
            null,
            null,
            generalService.getTodayDate(),
            opportunity.getPartner().getSalePriceList(),
            opportunity.getPartner(),
            opportunity.getTeam());

    saleOrderRepo.save(saleOrder);

    opportunity.setSaleOrder(saleOrder);
    opportunityRepository.save(opportunity);

    return saleOrder;
  }
}
