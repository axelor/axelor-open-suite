/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.common.Inflector;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalTrackingLogLineController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void showGlobalTrackingLogLineReference(ActionRequest request, ActionResponse response) {
    try {

      GlobalTrackingLogLine globalTrackingLogLine =
          request.getContext().asType(GlobalTrackingLogLine.class);
      GlobalTrackingLog globalTrackingLog = globalTrackingLogLine.getGlobalTrackingLog();

      if (globalTrackingLog == null) {
        return;
      }
      Class<?> modelClass = JPA.model(globalTrackingLog.getMetaModel().getFullName());
      final Inflector inflector = Inflector.getInstance();
      String viewName = inflector.dasherize(modelClass.getSimpleName());

      LOG.debug("Showing Tracking Log reference ::: {}", viewName);

      ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Reference"));
      actionViewBuilder.model(globalTrackingLog.getMetaModel().getFullName());

      if (globalTrackingLog.getRelatedId() != null) {
        actionViewBuilder.context("_showRecord", globalTrackingLog.getRelatedId());
      } else {
        actionViewBuilder.add("grid", String.format("%s-grid", viewName));
      }

      actionViewBuilder.add("form", String.format("%s-form", viewName));
      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
