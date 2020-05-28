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
package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.Pack;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface InvoiceServiceSupplychain {

  /**
   * Calculate pack total in invoice lines
   *
   * @param invoice
   */
  public void computePackTotal(Invoice invoice);

  /**
   * Reset pack total in invoice lines
   *
   * @param invoice
   */
  public void resetPackTotal(Invoice invoice);

  /**
   * Convert PackLines of pack into InvoiceLines.
   *
   * @param invoice
   * @param pack
   * @param packQty
   * @throws AxelorException
   */
  public Invoice addPack(Invoice invoice, Pack pack, BigDecimal packQty) throws AxelorException;
}
