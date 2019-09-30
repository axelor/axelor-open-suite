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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.AlarmEngineBatch;
import com.axelor.apps.base.db.repo.AlarmEngineBatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.alarm.AlarmEngineBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AlarmEngineBatchController {

  public void launch(ActionRequest request, ActionResponse response) {

    AlarmEngineBatch alarmEngineBatch = request.getContext().asType(AlarmEngineBatch.class);
    alarmEngineBatch = Beans.get(AlarmEngineBatchRepository.class).find(alarmEngineBatch.getId());

    response.setFlash(Beans.get(AlarmEngineBatchService.class).run(alarmEngineBatch).getComments());
    response.setReload(true);
  }

  // WS
  public void run(ActionRequest request, ActionResponse response) throws AxelorException {

    AlarmEngineBatch alarmEngineBatch =
        Beans.get(AlarmEngineBatchRepository.class).findByCode("code");

    if (alarmEngineBatch == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(IExceptionMessage.ALARM_ENGINE_BATCH_5)
                  + " "
                  + request.getContext().get("code")));
    } else {
      Map<String, Object> mapData = new HashMap<String, Object>();
      mapData.put(
          "anomaly", Beans.get(AlarmEngineBatchService.class).run(alarmEngineBatch).getAnomaly());
      response.setData(mapData);
    }
  }
}
