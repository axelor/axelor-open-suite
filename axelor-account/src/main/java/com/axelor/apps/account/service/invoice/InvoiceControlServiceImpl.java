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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InvoiceControlServiceImpl implements InvoiceControlService {

  protected InvoiceRepository invoiceRepository;

  @Inject
  public InvoiceControlServiceImpl(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  public Boolean isDuplicate(Invoice invoice) {
    Objects.requireNonNull(invoice);
    if (invoice.getOriginDate() != null
        && !Strings.isNullOrEmpty(invoice.getSupplierInvoiceNb())
        && invoice.getPartner() != null) {

      StringBuilder query =
          new StringBuilder(
              "self.supplierInvoiceNb = :supplierInvoiceNb AND self.partner = :partnerId AND YEAR(self.originDate) = :yearOriginDate AND self.statusSelect != :statusSelect AND self.operationTypeSelect = :operationTypeSelect");
      Map<String, Object> params = new HashMap<String, Object>();

      params.put("supplierInvoiceNb", invoice.getSupplierInvoiceNb());
      params.put("partnerId", invoice.getPartner().getId());
      params.put("yearOriginDate", invoice.getOriginDate().getYear());
      params.put("statusSelect", InvoiceRepository.STATUS_CANCELED);
      params.put("operationTypeSelect", invoice.getOperationTypeSelect());

      if (invoice.getId() != null) {
        query.append(" AND self.id != :invoiceId");
        params.put("invoiceId", invoice.getId());
      }

      return invoiceRepository.all().filter(query.toString()).bind(params).count() > 0;
    }

    return false;
  }
}
