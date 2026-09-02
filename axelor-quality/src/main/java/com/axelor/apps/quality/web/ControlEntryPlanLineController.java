/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.quality.db.*;
import com.axelor.apps.quality.db.repo.ControlEntryPlanLineRepository;
import com.axelor.apps.quality.db.repo.ControlTypeRepository;
import com.axelor.apps.quality.service.ControlEntryPlanLineService;
import com.axelor.apps.quality.service.ControlTypeFieldValueService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Singleton;
import java.util.*;

@Singleton
public class ControlEntryPlanLineController {

  public void controlConformity(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ControlEntryPlanLine controlEntryPlanLine =
        request.getContext().asType(ControlEntryPlanLine.class);

    if (controlEntryPlanLine.getId() != null) {
      Beans.get(ControlEntryPlanLineService.class)
          .conformityEvalWithUpdate(
              Beans.get(ControlEntryPlanLineRepository.class).find(controlEntryPlanLine.getId()));
      response.setReload(true);
    }
  }

  /**
   * Rebuilds the reference values of a control plan line when its control type changes. Works on an
   * unsaved line: the generated values are pushed back to the form.
   */
  public void syncPlanValues(ActionRequest request, ActionResponse response) {

    ControlEntryPlanLine controlEntryPlanLine =
        request.getContext().asType(ControlEntryPlanLine.class);

    ControlType controlType = controlEntryPlanLine.getControlType();
    if (controlType != null && controlType.getId() != null) {
      controlType = Beans.get(ControlTypeRepository.class).find(controlType.getId());
    }

    response.setValue(
        "planValueList",
        Beans.get(ControlTypeFieldValueService.class)
            .syncPlanValues(controlType, controlEntryPlanLine.getPlanValueList()));
  }
}
