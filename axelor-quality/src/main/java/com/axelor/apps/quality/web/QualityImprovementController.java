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
package com.axelor.apps.quality.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.QIDetectionRepository;
import com.axelor.apps.quality.service.QualityImprovementCreateService;
import com.axelor.apps.quality.service.QualityImprovementService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Map;
import java.util.Optional;

public class QualityImprovementController {

  public void setDefaultValues(ActionRequest request, ActionResponse response)
      throws AxelorException {
    response.setValue("company", AuthUtils.getUser().getActiveCompany());
    response.setValue("qiStatus", Beans.get(QualityImprovementService.class).getDefaultQIStatus());
  }

  public void createQualityImprovement(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ControlEntryRepository controlEntryRepository = Beans.get(ControlEntryRepository.class);
    QIDetectionRepository qIDetectionRepository = Beans.get(QIDetectionRepository.class);
    Context context = request.getContext();
    ControlEntry controlEntry =
        Optional.ofNullable(context.get("_controlEntryId"))
            .map(Object::toString)
            .map(Long::valueOf)
            .map(controlEntryRepository::find)
            .orElse(null);
    QIDetection qiDetection =
        Optional.ofNullable(context.get("qiDetection"))
            .map(Map.class::cast)
            .map(map -> map.get("id"))
            .map(Number.class::cast)
            .map(Number::longValue)
            .map(qIDetectionRepository::find)
            .orElse(null);

    if (controlEntry == null || qiDetection == null) {
      return;
    }

    QualityImprovement qiImprovement =
        Beans.get(QualityImprovementCreateService.class)
            .createQualityImprovementFromControlEntry(controlEntry, qiDetection);
    response.setView(
        ActionView.define(I18n.get("Quality improvement"))
            .model(QualityImprovement.class.getName())
            .add("form", "quality-improvement-form")
            .add("grid", "quality-improvement-grid")
            .context("_showRecord", qiImprovement.getId())
            .map());
    response.setCanClose(true);
  }
}
