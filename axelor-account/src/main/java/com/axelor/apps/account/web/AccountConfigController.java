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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.service.analytic.AccountConfigAnalyticService;
import com.axelor.apps.account.service.move.SimulatedMoveService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class AccountConfigController {

  public void deactivateSimulatedMoves(ActionRequest request, ActionResponse response) {

    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
    try {
      Beans.get(SimulatedMoveService.class).deactivateSimulatedMoves(accountConfig.getCompany());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkChangesInAnalytic(ActionRequest request, ActionResponse response) {
    try {
      AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);
      List<AnalyticAxisByCompany> initialList =
          Beans.get(AccountConfigRepository.class)
              .find(accountConfig.getId())
              .getAnalyticAxisByCompanyList();
      List<AnalyticAxisByCompany> modifiedList = accountConfig.getAnalyticAxisByCompanyList();
      Beans.get(AccountConfigAnalyticService.class)
          .checkChangesInAnalytic(initialList, modifiedList);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }
}
