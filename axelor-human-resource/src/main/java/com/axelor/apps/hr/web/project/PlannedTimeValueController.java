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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.hr.service.project.PlannedTimeValueService;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PlannedTimeValueController {

  public void checkIfExists(ActionRequest request, ActionResponse response) {

    PlannedTimeValue plannedTimeValue = request.getContext().asType(PlannedTimeValue.class);
    if (Beans.get(PlannedTimeValueService.class).checkIfExists(plannedTimeValue)) {
      response.setAlert(I18n.get("This planned time value already exists."));
      response.setValue("plannedTime", null);
    }
  }
}
