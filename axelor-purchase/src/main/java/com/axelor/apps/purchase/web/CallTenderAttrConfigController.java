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
package com.axelor.apps.purchase.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.CallTenderAttrConfig;
import com.axelor.apps.purchase.db.repo.CallTenderAttrConfigRepository;
import com.axelor.apps.purchase.service.CallTenderAttrConfigService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public class CallTenderAttrConfigController {

  @SuppressWarnings("unchecked")
  public void fillDefaults(ActionRequest request, ActionResponse response) {
    try {
      Context ctx = request.getContext();
      Long id = extractConfigId(ctx.get("callTenderAttrConfig"));
      if (id == null) {
        return;
      }
      CallTenderAttrConfig config = Beans.get(CallTenderAttrConfigRepository.class).find(id);
      if (config == null) {
        return;
      }
      ObjectMapper mapper = new ObjectMapper();
      Object attrsObj = ctx.get("attrs");
      String attrsJson;
      if (attrsObj == null) {
        attrsJson = null;
      } else if (attrsObj instanceof String) {
        attrsJson = (String) attrsObj;
      } else if (attrsObj instanceof Map) {
        attrsJson = mapper.writeValueAsString(attrsObj);
      } else {
        attrsJson = attrsObj.toString();
      }
      String seeded =
          Beans.get(CallTenderAttrConfigService.class).buildDefaultAttrs(config, attrsJson);
      response.setValue("attrs", seeded);
      if (seeded != null && !seeded.isEmpty()) {
        Map<String, Object> seededMap = mapper.readValue(seeded, Map.class);
        for (Map.Entry<String, Object> entry : seededMap.entrySet()) {
          response.setValue("$attrs_" + entry.getKey(), entry.getValue());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected Long extractConfigId(Object configRef) {
    if (configRef == null) {
      return null;
    }
    if (configRef instanceof CallTenderAttrConfig) {
      return ((CallTenderAttrConfig) configRef).getId();
    }
    if (configRef instanceof Map) {
      Object idObj = ((Map<?, ?>) configRef).get("id");
      return idObj == null ? null : Long.valueOf(idObj.toString());
    }
    return null;
  }
}
