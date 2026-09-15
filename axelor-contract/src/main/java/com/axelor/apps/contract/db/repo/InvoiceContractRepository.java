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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.service.app.AppContractService;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import jakarta.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceContractRepository extends InvoiceSupplychainRepository {

  protected AppBaseService appBaseService;
  protected AppContractService appContractService;

  @Inject
  public InvoiceContractRepository(
      AppBaseService appBaseService, AppContractService appContractService) {
    this.appBaseService = appBaseService;
    this.appContractService = appContractService;
  }

  @Override
  protected boolean isPackManagementEnabled(Invoice invoice) {
    if (super.isPackManagementEnabled(invoice)) {
      return true;
    }
    return appBaseService.isApp("contract")
        && CollectionUtils.isNotEmpty(invoice.getContractSet())
        && appContractService.getAppContract().getIsPackManagement();
  }
}
