/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import com.axelor.inject.Beans;

public class InvoiceProjectRepository extends InvoiceSupplychainRepository {

  @Override
  public void remove(Invoice entity) {

    if (Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      Beans.get(InvoicingProjectRepository.class)
          .all()
          .filter("self.invoice.id = ?", entity.getId())
          .remove();
    }

    super.remove(entity);
  }
}
