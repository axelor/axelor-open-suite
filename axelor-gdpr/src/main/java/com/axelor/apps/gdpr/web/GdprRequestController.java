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
package com.axelor.apps.gdpr.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.service.response.GdprResponseService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GdprRequestController {

  public void generateResponse(ActionRequest request, ActionResponse response) {
    try {
      GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
      gdprRequest = Beans.get(GDPRRequestRepository.class).find(gdprRequest.getId());
      Beans.get(GdprResponseService.class).generateResponse(gdprRequest);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void sendResponse(ActionRequest request, ActionResponse response) {
    try {
      GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
      gdprRequest = Beans.get(GDPRRequestRepository.class).find(gdprRequest.getId());
      Beans.get(GdprResponseService.class).sendResponse(gdprRequest);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
