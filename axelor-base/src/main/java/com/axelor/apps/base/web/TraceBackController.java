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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.TraceBack;
import com.axelor.common.Inflector;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TraceBackController {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Show reference view.
   *
   * @param request
   * @param response
   */
  public void showReference(ActionRequest request, ActionResponse response) {
    TraceBack traceBack = request.getContext().asType(TraceBack.class);

    if (Strings.isNullOrEmpty(traceBack.getRef())) {
      return;
    }

    Class<?> modelClass = JPA.model(traceBack.getRef());
    final Inflector inflector = Inflector.getInstance();
    String viewName = inflector.dasherize(modelClass.getSimpleName());

    LOG.debug("Showing anomaly reference ::: {}", viewName);

    ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Reference"));
    actionViewBuilder.model(traceBack.getRef());

    if (traceBack.getRefId() != null) {
      actionViewBuilder.context("_showRecord", traceBack.getRefId());
    } else {
      actionViewBuilder.add("grid", String.format("%s-grid", viewName));
    }

    actionViewBuilder.add("form", String.format("%s-form", viewName));
    response.setView(actionViewBuilder.map());
  }
}
