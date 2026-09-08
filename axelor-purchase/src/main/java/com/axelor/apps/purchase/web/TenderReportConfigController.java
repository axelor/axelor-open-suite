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
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.TenderReportConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TenderReportConfigController {

  public void addMetaFieldLines(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> metaFieldList =
          ((List<Map<String, Object>>) context.get("metaFieldList"))
              .stream()
                  .filter(item -> Boolean.TRUE.equals(item.get("selected")))
                  .collect(Collectors.toList());
      if (ObjectUtils.isEmpty(metaFieldList)) {
        response.setAlert(
            I18n.get(PurchaseExceptionMessage.TENDER_REPORT_CONFIG_NO_FIELD_SELECTED));
        return;
      }

      if (ObjectUtils.isEmpty(context.get("_configId"))) {
        return;
      }

      List<Long> ids =
          metaFieldList.stream()
              .map(item -> Long.valueOf(item.get("id").toString()))
              .collect(Collectors.toList());
      Long configId = Long.valueOf(context.get("_configId").toString());
      Beans.get(TenderReportConfigService.class).addMetaFieldLines(configId, ids);

      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void addCustomFieldLines(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> customFieldList =
          ((List<Map<String, Object>>) context.get("customFieldList"))
              .stream()
                  .filter(item -> Boolean.TRUE.equals(item.get("selected")))
                  .collect(Collectors.toList());
      if (ObjectUtils.isEmpty(customFieldList)) {
        response.setAlert(
            I18n.get(PurchaseExceptionMessage.TENDER_REPORT_CONFIG_NO_FIELD_SELECTED));
        return;
      }

      if (ObjectUtils.isEmpty(context.get("_configId"))) {
        return;
      }

      List<Long> ids =
          customFieldList.stream()
              .map(item -> Long.valueOf(item.get("id").toString()))
              .collect(Collectors.toList());
      Long configId = Long.valueOf(context.get("_configId").toString());
      Beans.get(TenderReportConfigService.class).addCustomFieldLines(configId, ids);

      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
