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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PaymentConditionServiceImpl implements PaymentConditionService {
  @Override
  public void checkPaymentCondition(PaymentCondition paymentCondition) throws AxelorException {
    if (paymentCondition == null) {
      return;
    }

    List<PaymentConditionLine> paymentConditionLineList =
        paymentCondition.getPaymentConditionLineList();
    if (CollectionUtils.isEmpty(paymentConditionLineList)
        || checkPercentage(paymentConditionLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_CONDITION_CONFIGURATION_ERROR),
          paymentCondition.getName());
    }
  }

  protected boolean checkPercentage(List<PaymentConditionLine> paymentConditionLineList) {
    return paymentConditionLineList.stream()
            .map(PaymentConditionLine::getPaymentPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .compareTo(BigDecimal.valueOf(100))
        != 0;
  }
}
