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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderManagementRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCopyService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOrderingStatusService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class SaleOrderSupplychainRepository extends SaleOrderManagementRepository {

  @Inject
  public SaleOrderSupplychainRepository(
      SaleOrderCopyService saleOrderCopyService,
      SaleOrderOrderingStatusService saleOrderOrderingStatusService) {
    super(saleOrderCopyService, saleOrderOrderingStatusService);
  }

  @Override
  public void remove(SaleOrder order) {

    Partner partner = order.getClientPartner();

    super.remove(order);

    try {
      Beans.get(AccountingSituationSupplychainService.class).updateUsedCredit(partner);
    } catch (AxelorException e) {
      e.printStackTrace();
    }
  }

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    try {
      Beans.get(SaleOrderLineAnalyticService.class).checkAnalyticAxisByCompany(saleOrder);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return super.save(saleOrder);
  }
}
