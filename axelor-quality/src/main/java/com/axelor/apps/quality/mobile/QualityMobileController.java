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
package com.axelor.apps.quality.mobile;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QualityControl;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.service.QualityControlService;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class QualityMobileController {

  /**
   * This method is used in mobile application.
   *
   * @param request
   * @param response
   *     <p>POST: /open-suite-webapp/ws/action Content-Type: application/json Fields: id
   *     <p>payload : { "action":
   *     "com.axelor.apps.quality.mobile.QualityMobileController:sendEmail", "data": { "context":
   *     {"id": 1} } }
   */
  public void sendEmail(ActionRequest request, ActionResponse response) {
    try {
      boolean automaticMail = Beans.get(AppQualityService.class).getAppQuality().getAutomaticMail();

      if (request.getRawContext().get("id") == null || !automaticMail) {
        return;
      }
      QualityControl qualityControl =
          Beans.get(QualityControlRepository.class)
              .find(Long.valueOf(request.getRawContext().get("id").toString()));

      Beans.get(QualityControlService.class).sendEmail(qualityControl);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
