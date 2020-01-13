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
package com.axelor.apps.account.web;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.debtrecovery.PayerQualityService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class AppAccountController {

  public void payerQualityProcess(ActionRequest request, ActionResponse response) {

    try {
      Beans.get(PayerQualityService.class).payerQualityProcess();
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateAccountConfigurations(ActionRequest request, ActionResponse response) {

    Beans.get(AppAccountService.class).generateAccountConfigurations();

    response.setReload(true);
  }
}
