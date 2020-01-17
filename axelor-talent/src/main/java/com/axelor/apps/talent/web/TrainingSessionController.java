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
package com.axelor.apps.talent.web;

import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.apps.talent.service.TrainingSessionService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class TrainingSessionController {

  public void closeSession(ActionRequest request, ActionResponse response) {

    TrainingSession trainingSession = request.getContext().asType(TrainingSession.class);
    trainingSession = Beans.get(TrainingSessionRepository.class).find(trainingSession.getId());

    Beans.get(TrainingSessionService.class).closeSession(trainingSession);

    response.setReload(true);
  }
}
