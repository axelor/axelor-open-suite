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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentConditionController {

  public void alertModification(ActionRequest request, ActionResponse response) {
    PaymentCondition paymentCondition = request.getContext().asType(PaymentCondition.class);
    try {
      if (paymentCondition.getId() != null) {
        long invoiceCount =
            Beans.get(InvoiceRepository.class)
                .all()
                .filter("self.paymentCondition.id = ?1", paymentCondition.getId())
                .count();

        long moveCount =
            Beans.get(MoveRepository.class)
                .all()
                .filter("self.paymentCondition.id = ?1", paymentCondition.getId())
                .count();

        if (invoiceCount > 0 || moveCount > 0) {
          response.setAlert(I18n.get(AccountExceptionMessage.PAYMENT_CONDITION_LINKED_OBJECTS));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
