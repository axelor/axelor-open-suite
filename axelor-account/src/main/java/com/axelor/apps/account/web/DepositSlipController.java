/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class DepositSlipController {

  public void loadPayments(ActionRequest request, ActionResponse response) {
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    depositSlip = Beans.get(DepositSlipRepository.class).find(depositSlip.getId());
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    try {
      depositSlipService.loadPayments(depositSlip);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void publish(ActionRequest request, ActionResponse response) {
    DepositSlip depositSlip = request.getContext().asType(DepositSlip.class);
    depositSlip = Beans.get(DepositSlipRepository.class).find(depositSlip.getId());
    DepositSlipService depositSlipService = Beans.get(DepositSlipService.class);

    try {
      String fileLink = depositSlipService.publish(depositSlip);
      response.setReload(true);
      response.setView(
          ActionView.define(depositSlipService.getFilename(depositSlip))
              .add("html", fileLink)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
