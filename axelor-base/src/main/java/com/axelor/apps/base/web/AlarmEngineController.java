/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.AlarmEngine;
import com.axelor.apps.base.db.repo.AlarmEngineRepository;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AlarmEngineController {

  @SuppressWarnings("rawtypes")
  @Inject
  private AlarmEngineService aes;

  @SuppressWarnings("unchecked")
  public void validateQuery(ActionRequest request, ActionResponse response) {

    AlarmEngine alarmEngine = request.getContext().asType(AlarmEngine.class);

    try {
      if (alarmEngine.getQuery() != null) {
        aes.results(
            alarmEngine.getQuery(), Class.forName(alarmEngine.getMetaModel().getFullName()));
      }
    } catch (Exception e) {
      response.setValue(
          "query",
          alarmEngine.getId() != null
              ? Beans.get(AlarmEngineRepository.class).find(alarmEngine.getId()).getQuery()
              : null);
      TraceBackService.trace(response, e);
    }
  }
}
