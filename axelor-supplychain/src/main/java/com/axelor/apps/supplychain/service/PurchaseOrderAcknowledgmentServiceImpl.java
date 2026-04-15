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

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.service.PaymentConditionToolService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.PurchaseOrderAcknowledgment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Override
  public Map<LocalDate, BigDecimal> computeAcknowledgmentForecastMap(
      PurchaseOrderLine purchaseOrderLine,
      BigDecimal unitInTaxAmount,
      BigDecimal invoicedAmount,
      long estimatedDurationDays,
      PaymentCondition paymentCondition,
      LocalDate fromDate,
      LocalDate toDate) {

    List<PurchaseOrderAcknowledgment> ackList =
        purchaseOrderLine.getPurchaseOrderAcknowledgmentList();
    if (CollectionUtils.isEmpty(ackList)) {
      return Collections.emptyMap();
    }

    BigDecimal totalAckQty =
        ackList.stream()
            .map(PurchaseOrderAcknowledgment::getQty)
            .filter(q -> q != null && q.signum() > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalAckQty.signum() == 0) {
      return Collections.emptyMap();
    }

    BigDecimal netTotal =
        totalAckQty.multiply(unitInTaxAmount).subtract(invoicedAmount).max(BigDecimal.ZERO);
    if (netTotal.signum() == 0) {
      return Collections.emptyMap();
    }

    Map<LocalDate, BigDecimal> result = new HashMap<>();
    for (PurchaseOrderAcknowledgment ack : ackList) {
      BigDecimal ackQty = ack.getQty();
      if (ackQty.signum() == 0) {
        continue;
      }
      LocalDate deliveryDate = ack.getDeliveryDate();
      if (deliveryDate == null) {
        continue;
      }
      LocalDate adjustedDate =
          PaymentConditionToolService.getMaxDueDate(
              paymentCondition, deliveryDate.plusDays(estimatedDurationDays));
      if (adjustedDate == null || adjustedDate.isBefore(fromDate) || adjustedDate.isAfter(toDate)) {
        continue;
      }
      BigDecimal ackAmount =
          netTotal
              .multiply(ackQty)
              .divide(totalAckQty, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      if (ackAmount.signum() == 0) {
        continue;
      }
      result.merge(adjustedDate, ackAmount, BigDecimal::add);
    }
    return result;
  }
}
