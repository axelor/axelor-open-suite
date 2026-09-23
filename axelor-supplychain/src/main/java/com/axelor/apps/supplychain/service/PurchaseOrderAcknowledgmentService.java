/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface PurchaseOrderAcknowledgmentService {

  AcknowledgmentData computeAcknowledgmentData(PurchaseOrderLine purchaseOrderLine);

  /**
   * Splits an amount of the purchase order line over the confirmed delivery dates of its
   * acknowledgments, pro rata of the confirmed quantities. Acknowledgments without a delivery date
   * or without a positive quantity are ignored. Returns an empty map when no acknowledgment
   * qualifies.
   */
  Map<LocalDate, BigDecimal> splitAmountByDeliveryDate(
      PurchaseOrderLine purchaseOrderLine, BigDecimal amount);

  record AcknowledgmentData(LocalDate maxDeliveryDate, boolean qtyExceeded) {}
}
