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
package com.axelor.apps.account.service.invoice.generator.line;

import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;

/** Classe de création de ligne de facture abstraite. Chaine de responsabilité. */
public abstract class InvoiceLineChain extends InvoiceLineGenerator {

  protected InvoiceLineChain next;

  protected InvoiceLineChain() {}

  public InvoiceLineChain setNext(InvoiceLineChain abstractInvoiceLine) {

    next = abstractInvoiceLine;
    return abstractInvoiceLine;
  }
}
