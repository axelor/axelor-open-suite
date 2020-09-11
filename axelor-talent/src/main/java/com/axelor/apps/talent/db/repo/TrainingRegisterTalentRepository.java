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
package com.axelor.apps.talent.db.repo;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.exception.IExceptionMessage;
import com.axelor.apps.talent.service.TrainingRegisterService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import javax.validation.ValidationException;

public class TrainingRegisterTalentRepository extends TrainingRegisterRepository {

  @Inject private TrainingRegisterService trainingRegisterService;

  @Inject private EventRepository eventRepo;

  @Override
  public TrainingRegister save(TrainingRegister trainingRegister) {

    if (trainingRegister.getFromDate().isAfter(trainingRegister.getToDate())) {
      throw new ValidationException(I18n.get(IExceptionMessage.INVALID_DATE_RANGE));
    }

    TrainingSession trainingSession = trainingRegister.getTrainingSession();
    if (trainingSession != null
        && (trainingSession.getFromDate().isAfter(trainingRegister.getFromDate())
            || trainingSession.getToDate().isBefore(trainingRegister.getToDate()))) {

      throw new ValidationException(I18n.get(IExceptionMessage.INVALID_TR_DATE));
    }

    trainingRegister.setFullName(trainingRegisterService.computeFullName(trainingRegister));

    trainingRegister = super.save(trainingRegister);

    trainingRegisterService.updateTrainingRating(trainingRegister.getTraining(), null);

    if (trainingRegister.getTrainingSession() != null) {
      trainingRegisterService.updateSessionRating(trainingRegister.getTrainingSession(), null);
    }

    refresh(trainingRegister);

    return trainingRegister;
  }

  @Override
  public void remove(TrainingRegister trainingRegister) {

    Training training = trainingRegister.getTraining();
    TrainingSession session = trainingRegister.getTrainingSession();

    List<Event> eventList = trainingRegister.getEventList();

    for (Event event : eventList) {
      eventRepo.remove(event);
    }

    super.remove(trainingRegister);

    trainingRegisterService.updateTrainingRating(training, null);

    if (session != null) {
      trainingRegisterService.updateSessionRating(session, null);
    }
  }
}
