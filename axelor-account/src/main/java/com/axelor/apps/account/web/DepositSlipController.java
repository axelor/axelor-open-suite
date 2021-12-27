/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.repo.DepositSlipRepository;
import com.axelor.apps.account.service.DepositSlipService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class DepositSlipController {

  @HandleExceptionResponse
  public void loadPayments(ActionRequest request, ActionResponse response) throws AxelorException {
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    depositSlip = Beans.get(DepositSlipRepository.class).find(depositSlip.getId());
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    depositSlipService.loadPayments(depositSlip);
    response.setReload(true);
  }

  @HandleExceptionResponse
  public void publish(ActionRequest request, ActionResponse response) throws AxelorException {
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    depositSlip = Beans.get(DepositSlipRepository.class).find(depositSlip.getId());
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    String fileLink = depositSlipService.publish(depositSlip);
    response.setReload(true);
    response.setView(
        ActionView.define(depositSlipService.getFilename(depositSlip)).add("html", fileLink).map());
  }
}
