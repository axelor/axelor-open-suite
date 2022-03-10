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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Recording;
import com.axelor.apps.base.db.repo.RecordingRepository;
import com.axelor.apps.base.service.RecordingService;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;
import wslite.json.JSONException;

public class RecordingController {

  public void stopRecording(ActionRequest request, ActionResponse response) {

    try {
      Recording recording =
          Beans.get(RecordingRepository.class)
              .find(request.getContext().asType(Recording.class).getId());

      if (recording != null) {
        String warning = Beans.get(RecordingService.class).stopRecording(recording);
        if (StringUtils.notBlank(warning)) {
          response.setNotify(warning);
        }
        response.setReload(true);
      }
    } catch (JSONException | InterruptedException | IOException | AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
