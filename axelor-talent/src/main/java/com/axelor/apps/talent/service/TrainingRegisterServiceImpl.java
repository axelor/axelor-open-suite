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
package com.axelor.apps.talent.service;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainingRegisterServiceImpl implements TrainingRegisterService {

  private final Logger log = LoggerFactory.getLogger(TrainingRegisterService.class);

  @Inject protected TrainingRegisterRepository trainingRegisterRepo;

  @Inject protected EventRepository eventRepo;

  @Inject protected TrainingRepository trainingRepo;

  @Inject protected TrainingSessionRepository trainingSessionRepo;

  @Transactional
  @Override
  public Event plan(TrainingRegister trainingRegister) {

    trainingRegister.setStatusSelect(1);

    trainingRegisterRepo.save(trainingRegister);

    Event event = generateMeeting(trainingRegister);

    return eventRepo.save(event);
  }

  protected Event generateMeeting(TrainingRegister trainingRegister) {

    Event event = new Event();
    event.setTypeSelect(EventRepository.TYPE_MEETING);
    event.setStartDateTime(trainingRegister.getFromDate());
    event.setEndDateTime(trainingRegister.getToDate());
    event.setSubject(trainingRegister.getTraining().getName());
    event.setUser(trainingRegister.getEmployee().getUser());
    if (trainingRegister.getTrainingSession() != null) {
      event.setLocation(trainingRegister.getTrainingSession().getLocation());
    }
    return event;
  }

  @Transactional
  @Override
  public void complete(TrainingRegister trainingRegister) {

    trainingRegister.setStatusSelect(2);

    trainingRegister
        .getEmployee()
        .getSkillSet()
        .addAll(trainingRegister.getTraining().getSkillSet());

    trainingRegisterRepo.save(trainingRegister);
  }

  @Transactional
  @Override
  public Training updateTrainingRating(Training training, Long excludeId) {

    String query = "self.training = ?1";

    if (excludeId != null) {
      query += " AND self.id != " + excludeId;
    }

    List<TrainingRegister> trainingTrs = trainingRegisterRepo.all().filter(query, training).fetch();

    long totalTrainingsRating = trainingTrs.stream().mapToLong(tr -> tr.getRatingSelect()).sum();
    int totalTrainingSize = trainingTrs.size();

    log.debug("Training: {}", training.getName());
    log.debug("Total trainings TR: {}", totalTrainingSize);
    log.debug("Total ratings:: training: {}", totalTrainingsRating);

    double avgRating = totalTrainingSize == 0 ? 0 : totalTrainingsRating / totalTrainingSize;

    log.debug("Avg training rating : {}", avgRating);

    training.setRating(new BigDecimal(avgRating));

    return trainingRepo.save(training);
  }

  @Transactional
  @Override
  public TrainingSession updateSessionRating(TrainingSession session, Long excludeId) {

    String query = "self.trainingSession = ?1";
    if (excludeId != null) {
      query += " AND self.id != " + excludeId;
    }

    List<TrainingRegister> sessionTrs = trainingRegisterRepo.all().filter(query, session).fetch();

    long totalSessionsRating = sessionTrs.stream().mapToLong(tr -> tr.getRatingSelect()).sum();
    int totalSessionSize = sessionTrs.size();

    double avgRating = totalSessionSize == 0 ? 0 : totalSessionsRating / totalSessionSize;

    log.debug("Avg session rating : {}", avgRating);

    session.setRating(new BigDecimal(avgRating));
    session.setNbrRegistered(totalSessionSize);

    return trainingSessionRepo.save(session);
  }

  @Transactional
  @Override
  public void cancel(TrainingRegister trainingRegister) {

    trainingRegister.setStatusSelect(3);

    trainingRegisterRepo.save(trainingRegister);
  }
}
