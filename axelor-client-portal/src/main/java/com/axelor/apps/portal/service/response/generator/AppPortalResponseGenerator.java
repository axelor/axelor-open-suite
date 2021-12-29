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
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Arrays;

public class AppPortalResponseGenerator extends ResponseGenerator {

  @Inject private AppService appService;

  @Override
  public void init() {
    modelFields.addAll(
        Arrays.asList(
            "manageEShop",
            "manageQuotations",
            "isPurchasesActivated",
            "isDMSActivated",
            "isTaskEditorActivated"));
    extraFieldMap.put("isAti", this::getIsAti);
    extraFieldMap.put("isQuotationRequestActive", this::getIsQuotationRequestActive);
    if (appService.isApp("subscription")) {
      modelFields.add("isSubscriptionActivated");
    }
    if (appService.isApp("tenant-portal")) {
      modelFields.add("isTenantPortalActivated");
    }
    classType = AppPortal.class;
  }

  private Object getIsAti(Object object) {

    Integer ati = Beans.get(AppBaseService.class).getAppBase().getProductInAtiSelect();
    if (ati != null && (ati == 2 || ati == 4)) {
      return true;
    }

    return false;
  }

  private Object getIsQuotationRequestActive(Object object) {

    AppPortal appPortal = (AppPortal) object;
    return appPortal.getManageQuotations();
  }
}
