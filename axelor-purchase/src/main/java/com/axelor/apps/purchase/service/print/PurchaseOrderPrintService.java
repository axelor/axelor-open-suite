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
package com.axelor.apps.purchase.service.print;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import java.io.IOException;
import java.util.List;

public interface PurchaseOrderPrintService {

  /**
   * Print a purchase order
   *
   * @return ReportSettings
   * @throws IOException
   * @throws AxelorException
   */
  String printPurchaseOrders(List<Long> ids) throws IOException;

  String printPurchaseOrder(PurchaseOrder purchaseOrder, String formatPdf) throws AxelorException;

  String getFileName(PurchaseOrder purchaseOrder);
}
