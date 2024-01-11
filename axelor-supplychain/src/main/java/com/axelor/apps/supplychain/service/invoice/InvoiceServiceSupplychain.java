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
package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoiceServiceSupplychain {

  /**
   * Calculate pack total in invoice lines.
   *
   * @param invoice
   */
  public void computePackTotal(Invoice invoice);

  /**
   * Reset pack total in invoice lines.
   *
   * @param invoice
   */
  public void resetPackTotal(Invoice invoice);

  /**
   * To update product qty with pack header qty.
   *
   * @param invoice
   * @return {@link Invoice}
   * @throws AxelorException
   */
  public Invoice updateProductQtyWithPackHeaderQty(Invoice invoice) throws AxelorException;

  public void swapStockMoveInvoices(List<Invoice> invoiceList, Invoice newInvoice);
}
