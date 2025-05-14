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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.interfaces.GlobalDiscounterLine;
import com.axelor.apps.base.service.discount.GlobalDiscountAbstractService;
import com.google.inject.Inject;
import java.util.List;

public class InvoiceGlobalDiscountServiceImpl extends GlobalDiscountAbstractService
    implements InvoiceGlobalDiscountService {

  protected final InvoiceService invoiceService;
  protected final InvoiceLineService invoiceLineService;

  @Inject
  public InvoiceGlobalDiscountServiceImpl(
      InvoiceService invoiceService, InvoiceLineService invoiceLineService) {
    this.invoiceService = invoiceService;
    this.invoiceLineService = invoiceLineService;
  }

  @Override
  protected void compute(GlobalDiscounter globalDiscounter) throws AxelorException {
    Invoice invoice = getInvoice(globalDiscounter);
    invoice
        .getInvoiceLineList()
        .forEach(
            invoiceLine -> {
              try {
                invoiceLineService.compute(invoice, invoiceLine);
              } catch (AxelorException e) {
                throw new RuntimeException(e);
              }
            });
    invoiceService.compute(invoice);
  }

  @Override
  protected List<? extends GlobalDiscounterLine> getGlobalDiscounterLines(
      GlobalDiscounter globalDiscounter) {
    return getInvoice(globalDiscounter).getInvoiceLineList();
  }

  protected Invoice getInvoice(GlobalDiscounter globalDiscounter) {
    Invoice invoice = null;
    if (globalDiscounter instanceof Invoice) {
      invoice = (Invoice) globalDiscounter;
    }
    return invoice;
  }
}
