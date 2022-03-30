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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunitySaleOrderSupplychainServiceImpl extends OpportunitySaleOrderServiceImpl {

  @Inject protected SaleOrderCreateService saleOrderCreateService;

  @Inject protected SaleOrderRepository saleOrderRepo;

  @Inject protected AppBaseService appBaseService;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException {
    SaleOrder saleOrder = super.createSaleOrderFromOpportunity(opportunity);

    // Adding supplychain behaviour
    // Set default invoiced and delivered partners and address in case of partner delegations
    if (Beans.get(AppSupplychainService.class).getAppSupplychain().getActivatePartnerRelations()) {
      Beans.get(SaleOrderSupplychainService.class)
          .setDefaultInvoicedAndDeliveredPartnersAndAddresses(saleOrder);
    }

    saleOrderRepo.save(saleOrder);

    return saleOrder;
  }
}
