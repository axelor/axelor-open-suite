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
package com.axelor.apps.base.tracking;

import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class GlobalTrackingLogController {

  public void showGlobalTrackingLogsInWizard(ActionRequest request, ActionResponse response) {
    try {

      Context context = request.getContext();
      boolean showLines = context.get("metaModel") != null && context.get("metaField") != null;

      response.setAttr("globalTrackingLogDashlet", "hidden", showLines);
      response.setAttr("globalTrackingLogLineDashlet", "hidden", !showLines);

      response.setAttr(
          showLines ? "globalTrackingLogLineDashlet" : "globalTrackingLogDashlet", "refresh", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
