/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.ServiceType;
import com.axelor.apps.account.db.repo.ServiceTypeRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ServiceTypeController {
  public void checkDuplicateCode(ActionRequest request, ActionResponse response) {
    try {
      ServiceType serviceType = request.getContext().asType(ServiceType.class);
      String code = serviceType.getCode();
      Long id = serviceType.getId();

      if (ObjectUtils.isEmpty(code)) {
        response.setAttr("$duplicateCode", "value", null);
        return;
      }
      Query<ServiceType> query = Beans.get(ServiceTypeRepository.class).all();

      String queryStr = "lower(self.code) = lower(:code)";
      if (id != null) {
        queryStr += " AND self.id != :id";
        query.bind("id", id);
      }
      query.filter(queryStr).bind("code", code);
      if (query.count() > 0) {
        response.setAttr(
            "$duplicateCode",
            "value",
            String.format(I18n.get(IExceptionMessage.SERVICE_TYPE_DUPLICATE_CODE), code));
      } else {
        response.setAttr("$duplicateCode", "value", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
