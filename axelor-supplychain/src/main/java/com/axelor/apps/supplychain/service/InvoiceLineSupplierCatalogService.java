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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public interface InvoiceLineSupplierCatalogService {

  void setSupplierCatalogInfo(
      Invoice invoice, InvoiceLine invoiceLine, Map<String, Object> productInformation)
      throws AxelorException;

  void checkMinQty(
      Invoice invoice, InvoiceLine invoiceLine, ActionRequest request, ActionResponse response)
      throws AxelorException;

  Map<String, Object> updateInfoFromCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException;

  SupplierCatalog getSupplierCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException;
}
