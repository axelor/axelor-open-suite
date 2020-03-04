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

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.service.TrainingRegisterService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class TrainingRegisterController {

  public void plan(ActionRequest request, ActionResponse response) {

    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    trainingRegister = Beans.get(TrainingRegisterRepository.class).find(trainingRegister.getId());
    Event event = Beans.get(TrainingRegisterService.class).plan(trainingRegister);

    response.setReload(true);

    response.setView(
        ActionView.define("Meeting")
            .model(Event.class.getCanonicalName())
            .add("form", "event-form")
            .add("grid", "event-grid")
            .context("_showRecord", event.getId())
            .context("_user", trainingRegister.getEmployee().getUser())
            .map());
  }

  public void updateEventCalendar(ActionRequest request, ActionResponse response) {

    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);

    Beans.get(TrainingRegisterService.class).updateEventCalendar(trainingRegister);
  }

  public void complete(ActionRequest request, ActionResponse response) {

    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    trainingRegister = Beans.get(TrainingRegisterRepository.class).find(trainingRegister.getId());

    Beans.get(TrainingRegisterService.class).complete(trainingRegister);

    response.setReload(true);
  }

  public void cancel(ActionRequest request, ActionResponse response) {

    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    trainingRegister = Beans.get(TrainingRegisterRepository.class).find(trainingRegister.getId());

    Beans.get(TrainingRegisterService.class).cancel(trainingRegister);

    response.setReload(true);
  }

  public void updateOldRating(ActionRequest request, ActionResponse response) {

    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    TrainingRegisterService trainingRegisterService = Beans.get(TrainingRegisterService.class);

    Training trainingSaved = null;
    TrainingSession trainingSessionSaved = null;
    if (trainingRegister.getId() != null) {
      TrainingRegister trainingRegisterSaved =
          Beans.get(TrainingRegisterRepository.class).find(trainingRegister.getId());
      trainingSessionSaved = trainingRegisterSaved.getTrainingSession();
      trainingSaved = trainingRegisterSaved.getTraining();
    }

    if (trainingSaved != null && trainingSaved.getId().equals(trainingRegister.getTraining().getId())) {
      trainingRegisterService.updateTrainingRating(trainingSaved, trainingRegister.getId());
    }

    if (trainingSessionSaved != null) {
      if (trainingRegister.getTrainingSession() == null
          || trainingRegister.getTrainingSession().getId().equals(trainingSessionSaved.getId())) {
        trainingRegisterService.updateSessionRating(trainingSessionSaved, trainingRegister.getId());
      }
    }
  }
}
