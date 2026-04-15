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
import com.axelor.apps.supplychain.db.PurchaseOrderAcknowledgment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderAcknowledgmentServiceImpl implements PurchaseOrderAcknowledgmentService {

  private static final AcknowledgmentData EMPTY_ACKNOWLEDGMENT_DATA =
      new AcknowledgmentData(null, false);

  @Override
  public AcknowledgmentData computeAcknowledgmentData(PurchaseOrderLine purchaseOrderLine) {
    if (purchaseOrderLine == null) {
      return EMPTY_ACKNOWLEDGMENT_DATA;
    }

    List<PurchaseOrderAcknowledgment> acknowledgmentList =
        purchaseOrderLine.getPurchaseOrderAcknowledgmentList();

    if (CollectionUtils.isEmpty(acknowledgmentList)) {
      return EMPTY_ACKNOWLEDGMENT_DATA;
    }

    LocalDate maxDeliveryDate =
        acknowledgmentList.stream()
            .map(PurchaseOrderAcknowledgment::getDeliveryDate)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);

    BigDecimal acknowledgedQty =
        acknowledgmentList.stream()
            .map(PurchaseOrderAcknowledgment::getQty)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new AcknowledgmentData(
        maxDeliveryDate, acknowledgedQty.compareTo(purchaseOrderLine.getQty()) > 0);
  }
}
