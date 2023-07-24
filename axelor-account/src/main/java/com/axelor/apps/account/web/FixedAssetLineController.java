/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class FixedAssetLineController {

  public void realize(ActionRequest request, ActionResponse response) {
    FixedAssetLine fixedAssetLine = request.getContext().asType(FixedAssetLine.class);
    fixedAssetLine = Beans.get(FixedAssetLineRepository.class).find(fixedAssetLine.getId());

    try {
      Beans.get(FixedAssetLineMoveService.class).realize(fixedAssetLine, false, true, false);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void simulate(ActionRequest request, ActionResponse response) {
    FixedAssetLine fixedAssetLine = request.getContext().asType(FixedAssetLine.class);
    fixedAssetLine = Beans.get(FixedAssetLineRepository.class).find(fixedAssetLine.getId());

    try {
      if (Beans.get(FixedAssetLineMoveService.class).canSimulate(fixedAssetLine)) {
        Beans.get(FixedAssetLineMoveService.class).simulate(fixedAssetLine);
      } else {
        response.setError(I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CAN_NOT_SIMULATE));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
