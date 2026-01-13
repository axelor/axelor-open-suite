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
package com.axelor.apps.supplychain.rest.dto;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.sale.rest.dto.SaleOrderPostRequest;
import com.axelor.utils.api.ObjectFinder;

public class SaleOrderSupplychainPostRequest extends SaleOrderPostRequest {

  private Long paymentModeId;
  private Long paymentConditionId;

  public Long getPaymentModeId() {
    return paymentModeId;
  }

  public void setPaymentModeId(Long paymentModeId) {
    this.paymentModeId = paymentModeId;
  }

  public Long getPaymentConditionId() {
    return paymentConditionId;
  }

  public void setPaymentConditionId(Long paymentConditionId) {
    this.paymentConditionId = paymentConditionId;
  }

  public PaymentMode fetchPaymentMode() {
    if (paymentModeId == null || paymentModeId == 0L) {
      return null;
    }
    return ObjectFinder.find(PaymentMode.class, paymentModeId, ObjectFinder.NO_VERSION);
  }

  public PaymentCondition fetchPaymentCondition() {
    if (paymentConditionId == null || paymentConditionId == 0L) {
      return null;
    }
    return ObjectFinder.find(PaymentCondition.class, paymentConditionId, ObjectFinder.NO_VERSION);
  }
}
