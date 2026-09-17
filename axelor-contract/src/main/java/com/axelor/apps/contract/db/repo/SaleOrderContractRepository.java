/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.service.app.AppContractService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderCopyService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOrderingStatusService;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;

public class SaleOrderContractRepository extends SaleOrderSupplychainRepository {

  @Inject
  public SaleOrderContractRepository(
      SaleOrderCopyService saleOrderCopyService,
      SaleOrderOrderingStatusService saleOrderOrderingStatusService,
      AppBaseService appBaseService) {
    super(saleOrderCopyService, saleOrderOrderingStatusService, appBaseService);
  }

  @Override
  protected boolean isPackManagementEnabled(SaleOrder saleOrder) {
    if (super.isPackManagementEnabled(saleOrder)) {
      return true;
    }
    return appBaseService.isApp("contract")
        && saleOrder.getContract() != null
        && Beans.get(AppContractService.class).getAppContract().getIsPackManagement();
  }
}
