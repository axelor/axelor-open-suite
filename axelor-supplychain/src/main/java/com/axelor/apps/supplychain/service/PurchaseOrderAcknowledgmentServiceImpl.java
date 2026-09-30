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

    BigDecimal acknowledgedQty = computeAcknowledgedQty(acknowledgmentList);

    return new AcknowledgmentData(
        maxDeliveryDate, acknowledgedQty.compareTo(purchaseOrderLine.getQty()) != 0);
  }

  @Override
  public Map<LocalDate, BigDecimal> computeAmountByDeliveryDate(
      PurchaseOrderLine purchaseOrderLine, BigDecimal invoicedAmount) {
    if (purchaseOrderLine == null) {
      return Collections.emptyMap();
    }

    BigDecimal orderedQty = purchaseOrderLine.getQty();
    if (orderedQty.signum() == 0) {
      return Collections.emptyMap();
    }

    List<PurchaseOrderAcknowledgment> acknowledgmentList =
        purchaseOrderLine.getPurchaseOrderAcknowledgmentList();
    if (CollectionUtils.isEmpty(acknowledgmentList)) {
      return Collections.emptyMap();
    }

    List<PurchaseOrderAcknowledgment> deliveredAcknowledgmentList =
        acknowledgmentList.stream()
            .filter(ack -> ack.getDeliveryDate() != null)
            .filter(ack -> ack.getQty() != null && ack.getQty().signum() > 0)
            .sorted(Comparator.comparing(PurchaseOrderAcknowledgment::getDeliveryDate))
            .toList();
    if (deliveredAcknowledgmentList.isEmpty()) {
      return Collections.emptyMap();
    }

    BigDecimal acknowledgedQty = computeAcknowledgedQty(deliveredAcknowledgmentList);

    BigDecimal acknowledgedAmount =
        purchaseOrderLine
            .getInTaxTotal()
            .multiply(acknowledgedQty)
            .divide(orderedQty, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    BigDecimal amountToSpread =
        acknowledgedAmount
            .subtract(invoicedAmount == null ? BigDecimal.ZERO : invoicedAmount)
            .max(BigDecimal.ZERO);

    return splitAmountByAcknowledgment(
        amountToSpread, deliveredAcknowledgmentList, acknowledgedQty);
  }

  protected BigDecimal computeAcknowledgedQty(
      List<PurchaseOrderAcknowledgment> acknowledgmentList) {
    return acknowledgmentList.stream()
        .map(PurchaseOrderAcknowledgment::getQty)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Spreads the amount over the delivery dates of the acknowledgments, pro rata of their confirmed
   * quantities. The last acknowledgment takes the remainder so that the shares add up to the amount
   * exactly.
   */
  protected Map<LocalDate, BigDecimal> splitAmountByAcknowledgment(
      BigDecimal amount,
      List<PurchaseOrderAcknowledgment> acknowledgmentList,
      BigDecimal acknowledgedQty) {
    Map<LocalDate, BigDecimal> amountByDeliveryDate = new LinkedHashMap<>();
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
                .divide(
                    acknowledgedQty,
                    AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                    RoundingMode.HALF_UP);
        remainingAmount = remainingAmount.subtract(share);
      }
      amountByDeliveryDate.merge(acknowledgment.getDeliveryDate(), share, BigDecimal::add);
    }
    return amountByDeliveryDate;
  }
}
