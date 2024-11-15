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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.project.db.AllocationLine;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectMenuController {

  public void viewAllocationLines(ActionRequest request, ActionResponse response) {

    ActionView.ActionViewBuilder actionViewBuilder =
        ActionView.define(I18n.get("Allocation lines"))
            .model(AllocationLine.class.getName())
            .add("grid", "allocation-line-sprint-grid")
            .add("form", "allocation-line-form")
            .domain("self.sprint = :sprint and self.allocationPeriod in :allocationPeriodSet");

    response.setView(actionViewBuilder.map());
  }
}
