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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.attributes.InvoiceLineAttrsService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Map;

public class InvoiceLineGroupServiceImpl implements InvoiceLineGroupService {

  protected InvoiceLineAttrsService invoiceLineAttrsService;

  @Inject
  public InvoiceLineGroupServiceImpl(InvoiceLineAttrsService invoiceLineAttrsService) {
    this.invoiceLineAttrsService = invoiceLineAttrsService;
  }

  @Override
  public void setInvoiceLineScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    if (invoice != null && ObjectUtils.notEmpty(invoice.getInvoiceLineList())) {
      invoiceLineAttrsService.addInTaxPriceScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.addExTaxTotalScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.addInTaxTotalScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.addCoefficientScale(invoice, attrsMap, prefix);
    }
  }

  @Override
  public void setInvoiceLineTaxLineSetDomain(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    if (invoice != null) {
      invoiceLineAttrsService.addTaxLineSetDomain(invoice.getOperationTypeSelect(), attrsMap);
    }
  }
}
