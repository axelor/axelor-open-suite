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
import com.axelor.apps.base.AxelorException;
import com.axelor.db.Model;
import java.math.BigDecimal;
import java.util.List;

public interface CommonInvoiceService {

  /**
   * This method return amount% of total if isPercent and amount if not.
   *
   * @param model : model in process
   * @param amount
   * @param isPercent
   * @param total
   * @throws AxelorException if there is a inconsistency with amount and total. (For example if
   *     total is 0 and amount is not)
   * @return amount% of total if isPercent and amount if not
   */
  BigDecimal computeAmountToInvoicePercent(
      Model model, BigDecimal amount, boolean isPercent, BigDecimal total) throws AxelorException;

  BigDecimal computeSumInvoices(List<Invoice> invoices);
}
