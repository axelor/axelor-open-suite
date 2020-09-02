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
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.apps.talent.exception.IExceptionMessage;
import com.axelor.apps.talent.service.TrainingRegisterService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Singleton
public class TrainingRegisterController {

  @Inject private EventRepository eventRepository;

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

    if (trainingSaved != null
        && trainingSaved.getId().equals(trainingRegister.getTraining().getId())) {
      trainingRegisterService.updateTrainingRating(trainingSaved, trainingRegister.getId());
    }

    if (trainingSessionSaved != null) {
      if (trainingRegister.getTrainingSession() == null
          || trainingRegister.getTrainingSession().getId().equals(trainingSessionSaved.getId())) {
        trainingRegisterService.updateSessionRating(trainingSessionSaved, trainingRegister.getId());
      }
    }
  }

  @Transactional
  public void EventUserUpdate(ActionRequest request, ActionResponse response) {
    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);

    if (trainingRegister.getEventList() != null) {
      for (Event event : trainingRegister.getEventList()) {
        event.setUser(trainingRegister.getEmployee().getUser());
        eventRepository.save(event);
      }
    }
  }

  @Transactional
  public void EventFromDateUpdate(ActionRequest request, ActionResponse response) {
    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    if (trainingRegister.getEventList() != null) {
      for (Event event : trainingRegister.getEventList()) {
        event.setStartDateTime(trainingRegister.getFromDate());
        eventRepository.save(event);
      }
    }
  }

  @Transactional
  public void EventToDateUpdate(ActionRequest request, ActionResponse response) {
    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    if (trainingRegister.getEventList() != null) {
      for (Event event : trainingRegister.getEventList()) {
        event.setEndDateTime(trainingRegister.getToDate());
        eventRepository.save(event);
      }
    }
  }

  @Transactional
  public void EventCalenderUpdate(ActionRequest request, ActionResponse response) {
    TrainingRegister trainingRegister = request.getContext().asType(TrainingRegister.class);
    if (trainingRegister.getEventList() != null) {
      for (Event event : trainingRegister.getEventList()) {
        event.setCalendar(trainingRegister.getCalendar());
        eventRepository.save(event);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void massTrainingRegisterCreation(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    ArrayList<LinkedHashMap<String, Object>> employeeList =
        (ArrayList<LinkedHashMap<String, Object>>) context.get("employeeList");

    TrainingSession trainingSession =
        Beans.get(TrainingSessionRepository.class)
            .find(Long.parseLong(context.get("_trainingSessionId").toString()));

    String eventList =
        Beans.get(TrainingRegisterService.class)
            .massTrainingRegisterCreation(employeeList, trainingSession);

    response.setCanClose(true);

    if (!eventList.equals("()")) {
      response.setView(
          ActionView.define("Meeting")
              .model(Event.class.getCanonicalName())
              .add("grid", "event-grid")
              .add("form", "event-form")
              .param("search-filters", "event-filters")
              .domain("self.id in " + eventList)
              .map());
    } else {
      response.setAlert(IExceptionMessage.NO_EVENT_GENERATED);
    }
  }
}
