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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.PurchaseOrderAcknowledgment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

  @Override
  public Map<LocalDate, BigDecimal> splitAmountByDeliveryDate(
      PurchaseOrderLine purchaseOrderLine, BigDecimal amount) {
    if (purchaseOrderLine == null || amount == null || amount.signum() == 0) {
      return Collections.emptyMap();
    }

    List<PurchaseOrderAcknowledgment> acknowledgmentList =
        Optional.ofNullable(purchaseOrderLine.getPurchaseOrderAcknowledgmentList())
            .orElse(Collections.emptyList())
            .stream()
            .filter(ack -> ack.getDeliveryDate() != null)
            .filter(ack -> ack.getQty() != null && ack.getQty().signum() > 0)
            .sorted(Comparator.comparing(PurchaseOrderAcknowledgment::getDeliveryDate))
            .toList();
    if (acknowledgmentList.isEmpty()) {
      return Collections.emptyMap();
    }

    BigDecimal totalQty =
        acknowledgmentList.stream()
            .map(PurchaseOrderAcknowledgment::getQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
    BigDecimal remainingAmount = amount;
    for (int i = 0; i < acknowledgmentList.size(); i++) {
      PurchaseOrderAcknowledgment acknowledgment = acknowledgmentList.get(i);
      BigDecimal share;
      if (i == acknowledgmentList.size() - 1) {
        share = remainingAmount;
      } else {
        share =
            amount
                .multiply(acknowledgment.getQty())
                .divide(totalQty, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        remainingAmount = remainingAmount.subtract(share);
      }
      result.merge(acknowledgment.getDeliveryDate(), share, BigDecimal::add);
    }
    return result;
  }
}
