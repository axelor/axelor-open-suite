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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoiceLineAnalyticService {

  public InvoiceLine selectDefaultDistributionTemplate(InvoiceLine invoiceLine)
      throws AxelorException;

  InvoiceLine clearAnalyticAccounting(InvoiceLine invoiceLine);

  InvoiceLine analyzeInvoiceLine(InvoiceLine invoiceLine, Invoice invoice) throws AxelorException;

  List<AnalyticMoveLine> getAndComputeAnalyticDistribution(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException;

  List<AnalyticMoveLine> computeAnalyticDistribution(InvoiceLine invoiceLine);

  List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine);

  boolean validateAnalyticMoveLines(List<AnalyticMoveLine> analyticMoveLineList);
}
