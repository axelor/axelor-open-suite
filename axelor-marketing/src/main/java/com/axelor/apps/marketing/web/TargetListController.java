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
package com.axelor.apps.marketing.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.marketing.db.TargetList;
import com.axelor.apps.marketing.service.TargetListService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

/**
 * This controller class use to get filtered Partners and Leads.
 *
 * @author axelor
 */
@Singleton
public class TargetListController {

  public void openFilteredLeads(ActionRequest request, ActionResponse response) {

    TargetList targetList = request.getContext().asType(TargetList.class);
    String leadFilters = null;

    try {
      leadFilters = Beans.get(TargetListService.class).getLeadQuery(targetList);

      if (leadFilters != null) {
        response.setView(
            ActionView.define(I18n.get("Leads"))
                .model(Lead.class.getName())
                .add("grid", "lead-grid")
                .add("form", "lead-form")
                .domain(leadFilters)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openFilteredPartners(ActionRequest request, ActionResponse response) {

    TargetList targetList = request.getContext().asType(TargetList.class);
    String partnerFilters = null;

    try {
      partnerFilters = Beans.get(TargetListService.class).getPartnerQuery(targetList);

      if (partnerFilters != null) {
        response.setView(
            ActionView.define(I18n.get("Partners"))
                .model(Partner.class.getName())
                .add("grid", "partner-grid")
                .add("form", "partner-form")
                .domain(partnerFilters)
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
