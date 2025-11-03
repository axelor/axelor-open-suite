/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.IndicatorAlert;
import com.axelor.apps.base.db.IndicatorResultLine;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class IndicatorResultLineController {

  public void openRelatedRecord(ActionRequest request, ActionResponse response) {
    try {
      IndicatorResultLine resultLine = getResultLine(request);

      if (resultLine == null) {
        return;
      }

      MetaModel metaModel = resultLine.getMetaModel();
      Long relatedId = resultLine.getRelatedId();

      if (metaModel == null || relatedId == null) {
        return;
      }
      response.setView(
          ActionView.define(I18n.get(metaModel.getName()))
              .model(metaModel.getFullName())
              .param("popup", Boolean.TRUE.toString())
              .param("show-toolbar", Boolean.FALSE.toString())
              .param("show-confirm", Boolean.FALSE.toString())
              .param("popup-save", Boolean.FALSE.toString())
              .param("forceEdit", Boolean.TRUE.toString())
              .context("_showRecord", relatedId)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private IndicatorResultLine getResultLine(ActionRequest request) {
    if (IndicatorResultLine.class.equals(request.getContext().getContextClass())) {
      return request.getContext().asType(IndicatorResultLine.class);
    }
    if (IndicatorAlert.class.equals(request.getContext().getContextClass())) {
      return Optional.ofNullable(request.getContext().asType(IndicatorAlert.class))
          .map(IndicatorAlert::getResultLine)
          .orElse(null);
    }
    return null;
  }
}
