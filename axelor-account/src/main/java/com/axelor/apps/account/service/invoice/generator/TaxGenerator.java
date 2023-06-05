/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.generator;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import java.util.List;

/**
 * InvoiceLineTaxService est une classe implémentant l'ensemble des services pour les lignes de
 * taxes des factures.
 */
public abstract class TaxGenerator extends InvoiceLineManagement {

  protected Invoice invoice;
  protected List<InvoiceLine> invoiceLines;

  protected TaxGenerator(Invoice invoice, List<InvoiceLine> invoiceLines) {

    this.invoice = invoice;
    this.invoiceLines = invoiceLines;
  }
}
