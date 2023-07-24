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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.ResearchRequest;
import com.axelor.apps.base.db.ResearchResultLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.research.ResearchRequestService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchRequestController {

  public void searchObject(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);

    // keep only filled fields
    Map<String, Object> searchParams = new HashMap<>();
    if (researchRequest.getResearch1() != null) {
      searchParams.put(
          researchRequest.getResearch1().getCode(),
          "%" + researchRequest.getResearch1Value() + "%");
    }
    if (researchRequest.getResearch2() != null) {
      searchParams.put(
          researchRequest.getResearch2().getCode(),
          "%" + researchRequest.getResearch2Value() + "%");
    }
    if (researchRequest.getResearch3() != null) {
      searchParams.put(
          researchRequest.getResearch3().getCode(),
          "%" + researchRequest.getResearch3Value() + "%");
    }
    if (researchRequest.getResearch4() != null) {
      searchParams.put(
          researchRequest.getResearch4().getCode(),
          "%" + researchRequest.getResearch4Value() + "%");
    }
    if (researchRequest.getDateResearch1() != null) {
      searchParams.put(
          researchRequest.getDateResearch1().getCode(), researchRequest.getDateResearch1Value());
    }

    if (searchParams.isEmpty()) {
      response.setAlert(I18n.get("Please enter at least one field."));
    } else {
      List<ResearchResultLine> resultList = new ArrayList<>();
      try {
        resultList =
            Beans.get(ResearchRequestService.class).searchObject(searchParams, researchRequest);
        response.setValue("researchResultLineList", resultList);
        response.setValue("searchDate", Beans.get(AppBaseService.class).getTodayDate(null));
      } catch (AxelorException e) {
        TraceBackService.trace(response, e, ResponseMessageType.ERROR);
      }
    }
  }

  public void getResearch1PrimaryKeyDomain(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);
    try {

      String domain =
          Beans.get(ResearchRequestService.class).getStringResearchKeyDomain(researchRequest);
      response.setAttr("research1", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getResearch2PrimaryKeyDomain(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);
    try {

      String domain =
          Beans.get(ResearchRequestService.class).getStringResearchKeyDomain(researchRequest);
      response.setAttr("research2", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getResearch3PrimaryKeyDomain(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);
    try {

      String domain =
          Beans.get(ResearchRequestService.class).getStringResearchKeyDomain(researchRequest);
      response.setAttr("research3", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getResearch4PrimaryKeyDomain(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);
    try {

      String domain =
          Beans.get(ResearchRequestService.class).getStringResearchKeyDomain(researchRequest);
      response.setAttr("research4", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getDateResearch1PrimaryKeyDomain(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);
    try {

      String domain =
          Beans.get(ResearchRequestService.class).getDateResearchKeyDomain(researchRequest);
      response.setAttr("dateResearch1", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
