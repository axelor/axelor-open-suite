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
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GlobalTrackingLogLineController {
  public void showGlobalTrackingLogLineReference(ActionRequest request, ActionResponse response) {
    try {
      GlobalTrackingLogLine globalTrackingLogLine =
          request.getContext().asType(GlobalTrackingLogLine.class);
      GlobalTrackingLog globalTrackingLog = globalTrackingLogLine.getGlobalTrackingLog();

      ActionViewBuilder actionViewBuilder =
          Beans.get(GlobalTrackingLogService.class).createReferenceView(globalTrackingLog);

      if (actionViewBuilder != null) {
        response.setView(actionViewBuilder.map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
