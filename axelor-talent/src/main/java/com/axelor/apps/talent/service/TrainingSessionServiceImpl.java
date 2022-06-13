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
package com.axelor.apps.talent.service;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class TrainingSessionServiceImpl implements TrainingSessionService {

  @Inject private TrainingSessionRepository trainingSessionRepo;

  @Inject private TrainingRegisterService trainingRegisterService;

  @Inject private TrainingRegisterRepository trainingRegisterRepo;

  @Transactional
  @Override
  public void closeSession(TrainingSession trainingSession) {

    trainingSession.setStatusSelect(2);

    List<TrainingRegister> trainingRegisters =
        trainingRegisterRepo.all().filter("self.trainingSession = ?1", trainingSession).fetch();

    if (CollectionUtils.isNotEmpty(trainingRegisters)) {
      for (TrainingRegister trainingRegister : trainingRegisters) {
        trainingRegisterService.complete(trainingRegister);
      }
    }

    trainingSessionRepo.save(trainingSession);
  }

  @Override
  public String computeFullName(TrainingSession trainingSession) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    return trainingSession.getFromDate().format(formatter)
        + " - "
        + trainingSession.getToDate().format(formatter);
  }

  @Override
  @Transactional
  public void updateAllRating(TrainingSession trainingSession) {

    BigDecimal overallRatingToApply = trainingSession.getOverallRatingToApply();

    trainingSession.setRating(overallRatingToApply);

    if (CollectionUtils.isNotEmpty(trainingSession.getTrainingRegisterList())) {
      for (TrainingRegister register : trainingSession.getTrainingRegisterList()) {
        register.setRating(overallRatingToApply);
      }
    }

    trainingSessionRepo.save(trainingSession);
  }

  @Override
  @Transactional
  public void cancel(TrainingSession trainingSession) {

    trainingSession.setStatusSelect(3);

    if (CollectionUtils.isNotEmpty(trainingSession.getTrainingRegisterList())) {
      for (TrainingRegister register : trainingSession.getTrainingRegisterList()) {
        register.setStatusSelect(3);
      }
    }

    trainingSessionRepo.save(trainingSession);
  }

  @Override
  @Transactional
  public TrainingSession updateTraingRegisterTraining(TrainingSession trainingSession) {

    Training training = trainingSession.getTraining();

    if (CollectionUtils.isNotEmpty(trainingSession.getTrainingRegisterList())) {
      for (TrainingRegister register : trainingSession.getTrainingRegisterList()) {
        register.setTraining(training);

        if (CollectionUtils.isNotEmpty(register.getEventList())) {
          for (Event event : register.getEventList()) {
            event.setSubject(training.getName());
            Beans.get(EventRepository.class).save(event);
          }
        }
      }
    }

    return trainingSession;
  }
}
