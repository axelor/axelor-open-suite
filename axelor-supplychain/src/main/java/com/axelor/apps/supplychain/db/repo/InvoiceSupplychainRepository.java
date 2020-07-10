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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class InvoiceSupplychainRepository extends InvoiceManagementRepository {

  @Inject InvoiceServiceSupplychain invoiceServiceSupplychain;

  @Override
  public Invoice save(Invoice invoice) {
    try {
      if (Boolean.TRUE.equals(
          Beans.get(AppSaleService.class).getAppSale().getEnablePackManagement())) {
        invoiceServiceSupplychain.computePackTotal(invoice);
      } else {
        invoiceServiceSupplychain.resetPackTotal(invoice);
      }
      return super.save(invoice);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
