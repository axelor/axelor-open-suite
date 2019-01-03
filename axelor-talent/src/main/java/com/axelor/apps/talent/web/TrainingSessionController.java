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
package com.axelor.apps.talent.web;

import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.apps.talent.service.TrainingSessionService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TrainingSessionController {

  @Inject private TrainingSessionService trainingSessionService;

  @Inject private TrainingSessionRepository trainingSessionRepo;

  public void closeSession(ActionRequest request, ActionResponse response) {

    TrainingSession trainingSession = request.getContext().asType(TrainingSession.class);
    trainingSession = trainingSessionRepo.find(trainingSession.getId());

    trainingSessionService.closeSession(trainingSession);

    response.setReload(true);
  }
}
