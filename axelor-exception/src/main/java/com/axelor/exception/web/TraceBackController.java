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
package com.axelor.exception.web;

import com.axelor.common.Inflector;
import com.axelor.db.JPA;
import com.axelor.exception.db.TraceBack;
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
