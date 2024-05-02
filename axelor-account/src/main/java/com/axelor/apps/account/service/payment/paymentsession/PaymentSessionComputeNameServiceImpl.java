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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DateService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class PaymentSessionComputeNameServiceImpl implements PaymentSessionComputeNameService {

  protected DateService dateService;

  @Inject
  public PaymentSessionComputeNameServiceImpl(DateService dateService) {
    this.dateService = dateService;
  }

  @Override
  public String computeName(PaymentSession paymentSession) throws AxelorException {
    StringBuilder name = new StringBuilder("Session");
    User createdBy = paymentSession.getCreatedBy();
    if (ObjectUtils.notEmpty(paymentSession.getPaymentMode())) {
      name.append(" " + paymentSession.getPaymentMode().getName());
    }
    if (ObjectUtils.notEmpty(paymentSession.getCreatedOn())) {
      name.append(
          String.format(
              " %s %s",
              I18n.get(ITranslation.PAYMENT_SESSION_COMPUTE_NAME_ON_THE),
              paymentSession.getCreatedOn().format(dateService.getDateTimeFormat())));
    }
    if (ObjectUtils.notEmpty(createdBy)) {
      name.append(
          String.format(
              " %s %s",
              I18n.get(ITranslation.PAYMENT_SESSION_COMPUTE_NAME_BY), createdBy.getName()));
    }
    return name.toString();
  }
}
