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
package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
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

  public void showReference(ActionRequest request, ActionResponse response) {
    try {
      GlobalTrackingLog globalTrackingLog = request.getContext().asType(GlobalTrackingLog.class);

      ActionView.ActionViewBuilder actionViewBuilder =
          Beans.get(GlobalTrackingLogService.class).createReferenceView(globalTrackingLog);

      if (actionViewBuilder != null) {
        response.setView(actionViewBuilder.map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
