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
package com.axelor.apps.mobile.web;

import com.axelor.apps.base.db.AppMobile;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AppMobileController {

  public void getAppMobile(ActionRequest request, ActionResponse response) {

    Map<String, Object> data = new HashMap<>();

    AppMobile appMobile = (AppMobile) Beans.get(AppService.class).getApp("mobile");

    data.put("isAppMobileEnable", appMobile.getActive());
    data.put("isSaleAppEnable", appMobile.getIsSaleAppEnable());
    data.put("isCrmAppEnable", appMobile.getIsCrmAppEnable());
    data.put("isTimesheetAppEnable", appMobile.getIsTimesheetAppEnable());
    data.put("isLeaveAppEnable", appMobile.getIsLeaveAppEnable());
    data.put("isExpenseAppEnable", appMobile.getIsExpenseAppEnable());

    data.put("offlineRecordLimit", appMobile.getOfflineRecordLimit());

    data.put("partnerSet", convertToData(appMobile.getPartnerSet()));
    data.put("partnerContactSet", convertToData(appMobile.getPartnerContactSet()));
    data.put("projectSet", convertToData(appMobile.getProjectSet()));
    data.put("leadSet", convertToData(appMobile.getLeadSet()));
    data.put("productSet", convertToData(appMobile.getProductSet()));

    response.setData(data);
  }

  private Object convertToData(Set<? extends AuditableModel> modelSet) {

    return modelSet
        .stream()
        .map(
            it ->
                new HashMap<String, Object>() {
                  {
                    put("id", it.getId());
                    put("version", it.getVersion());
                  }
                })
        .collect(Collectors.toSet());
  }
}
