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
package com.axelor.apps.gdpr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.gdpr.service.GdprSearchEngineService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GdprSearchEngineController {

  /**
   * search in Partner and Lead object with fields
   *
   * @param request
   * @param response
   */
  public void searchObject(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    // keep only filled fields
    Map<String, Object> searchParams = new HashMap<>();
    Optional.ofNullable(context.get("__firstName"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("firstName", str));
    Optional.ofNullable(context.get("__lastName"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("lastName", str));
    Optional.ofNullable(context.get("__email"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("email", str));
    Optional.ofNullable(context.get("__phone"))
        .map(Object::toString)
        .ifPresent(str -> searchParams.put("phone", str));

    if (searchParams.isEmpty()) {
      response.setAlert(I18n.get("Please enter at least one field."));
    } else {
      List<Map<String, Object>> resultList = new ArrayList<>();
      try {
        resultList = Beans.get(GdprSearchEngineService.class).searchObject(searchParams);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
        response.setError(e.getMessage());
      }
      response.setValue("__searchResults", resultList);
    }
  }

  /**
   * fill reference field
   *
   * @param request
   * @param response
   */
  public void fillReferenceWithData(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      List<Map<String, Object>> resultList =
          (List<Map<String, Object>>) context.get("__searchResults");
      Map<String, Object> selectedObject =
          Beans.get(GdprSearchEngineService.class).checkSelectedObject(resultList);
      response.setValue("modelSelect", selectedObject.get("typeClass").toString());
      response.setValue("modelId", selectedObject.get("objectId"));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
