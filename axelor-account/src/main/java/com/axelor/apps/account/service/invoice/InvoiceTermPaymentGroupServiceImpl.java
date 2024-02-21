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

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.service.invoice.attributes.InvoiceTermPaymentAttrsService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceTermPaymentGroupServiceImpl implements InvoiceTermPaymentGroupService {

  protected InvoiceTermPaymentAttrsService invoiceTermPaymentAttrsService;

  @Inject
  public InvoiceTermPaymentGroupServiceImpl(
      InvoiceTermPaymentAttrsService invoiceTermPaymentAttrsService) {
    this.invoiceTermPaymentAttrsService = invoiceTermPaymentAttrsService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(InvoiceTermPayment invoiceTermPayment) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();

    if (invoiceTerm != null) {
      invoiceTermPaymentAttrsService.addIsMultiCurrency(invoiceTerm, attrsMap);
      invoiceTermPaymentAttrsService.addPaidAmountScale(invoiceTerm, attrsMap);
      invoiceTermPaymentAttrsService.addCompanyPaidAmountScale(invoiceTerm, attrsMap);
      invoiceTermPaymentAttrsService.addFinancialDiscountAmountScale(invoiceTerm, attrsMap);
    }

    return attrsMap;
  }
}
