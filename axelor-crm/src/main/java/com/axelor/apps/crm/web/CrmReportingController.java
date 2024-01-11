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
package com.axelor.apps.crm.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.CrmReporting;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.CrmReportingService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CrmReportingController {

  public void showResults(ActionRequest request, ActionResponse response) {
    try {
      CrmReporting crmReporting = request.getContext().asType(CrmReporting.class);
      ActionViewBuilder actionViewBuilder;
      actionViewBuilder =
          Beans.get(CrmReportingService.class)
              .createActionViewBuilder(crmReporting, Class.forName(crmReporting.getTypeSelect()));
      response.setView(actionViewBuilder.map());
    } catch (ClassNotFoundException | AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void showOpportunities(ActionRequest request, ActionResponse response) {
    try {
      CrmReporting crmReporting = request.getContext().asType(CrmReporting.class);
      ActionViewBuilder actionViewBuilder;
      actionViewBuilder =
          Beans.get(CrmReportingService.class)
              .createActionViewBuilder(crmReporting, Opportunity.class);
      response.setView(actionViewBuilder.map());
    } catch (ClassNotFoundException | AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void showEvents(ActionRequest request, ActionResponse response) {
    try {
      CrmReporting crmReporting = request.getContext().asType(CrmReporting.class);
      ActionViewBuilder actionViewBuilder;
      actionViewBuilder =
          Beans.get(CrmReportingService.class).createActionViewBuilder(crmReporting, Event.class);
      response.setView(actionViewBuilder.map());
    } catch (ClassNotFoundException | AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
