/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.mobilesettings.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.repo.BarcodeTypeConfigRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.mobilesettings.service.AppMobileSettingsService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppMobileSettings;

public class AppMobileSettingsController {

  public void generateQrCode(ActionRequest request, ActionResponse response) {
    try {
      AppMobileSettings appMobile = request.getContext().asType(AppMobileSettings.class);
      MetaFile qrCode =
          Beans.get(BarcodeGeneratorService.class)
              .createBarCode(
                  appMobile.getId(),
                  "AppMobileQrCode.png",
                  AppSettings.get().getBaseURL(),
                  Beans.get(BarcodeTypeConfigRepository.class).findByName("QR_CODE"),
                  false);
      response.setValue("qrCode", qrCode);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateBooleanConfig(ActionRequest request, ActionResponse response) {
    try {
      AppMobileSettings appMobile = request.getContext().asType(AppMobileSettings.class);
      Beans.get(AppMobileSettingsService.class).updateAllMobileConfig(appMobile);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
