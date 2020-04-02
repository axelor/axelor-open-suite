/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.web;

import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.sale.service.AdvancePaymentService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AdvancePaymentController {

  @Inject private AdvancePaymentService advancePaymentService;

  public void cancelAdvancePayment(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AdvancePayment advancePayment = request.getContext().asType(AdvancePayment.class);

    advancePayment = Beans.get(AdvancePaymentRepository.class).find(advancePayment.getId());

    advancePaymentService.cancelAdvancePayment(advancePayment);

    response.setReload(true);
  }
}
