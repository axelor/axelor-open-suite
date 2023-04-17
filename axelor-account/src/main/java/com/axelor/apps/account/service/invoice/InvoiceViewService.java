/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface InvoiceViewService {

  static String computeInvoiceGridName(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return "invoice-grid";
    } else {
      return "invoice-supplier-grid";
    }
  }

  static String computeInvoiceFilterName(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return "customer-invoices-filters";
    } else {
      return "supplier-invoices-filters";
    }
  }
}
