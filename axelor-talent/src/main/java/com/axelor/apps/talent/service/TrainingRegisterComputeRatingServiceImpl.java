/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.talent.service;

import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainingRegisterComputeRatingServiceImpl
    implements TrainingRegisterComputeRatingService {

  private final Logger log = LoggerFactory.getLogger(TrainingRegisterService.class);

  protected TrainingRepository trainingRepository;
  protected TrainingSessionRepository trainingSessionRepository;

  @Inject
  public TrainingRegisterComputeRatingServiceImpl(
      TrainingRepository trainingRepository, TrainingSessionRepository trainingSessionRepository) {
    this.trainingRepository = trainingRepository;
    this.trainingSessionRepository = trainingSessionRepository;
  }

  @Transactional
  @Override
  public Training updateTrainingRating(Training training, Long excludeId) {

    String query = "self.training = ?1";

    if (excludeId != null) {
      query += " AND self.id != " + excludeId;
    }

    List<TrainingRegister> trainingTrs =
        JPA.all(TrainingRegister.class).filter(query, training).fetch();

    long totalTrainingsRating =
        trainingTrs.stream().mapToLong(tr -> tr.getRating().longValue()).sum();
    int totalTrainingSize = trainingTrs.size();

    log.debug("Training: {}", training.getName());
    log.debug("Total trainings TR: {}", totalTrainingSize);
    log.debug("Total ratings:: training: {}", totalTrainingsRating);

    double avgRating = totalTrainingSize == 0 ? 0 : totalTrainingsRating / totalTrainingSize;

    log.debug("Avg training rating : {}", avgRating);

    training.setRating(BigDecimal.valueOf(avgRating));

    return trainingRepository.save(training);
  }

  @Transactional
  @Override
  public TrainingSession updateSessionRating(TrainingSession session, Long excludeId) {

    String query = "self.trainingSession = ?1";
    if (excludeId != null) {
      query += " AND self.id != " + excludeId;
    }

    List<TrainingRegister> sessionTrs =
        JPA.all(TrainingRegister.class).filter(query, session).fetch();

    long totalSessionsRating =
        sessionTrs.stream().mapToLong(tr -> tr.getRating().longValue()).sum();
    int totalSessionSize = sessionTrs.size();

    double avgRating = totalSessionSize == 0 ? 0 : totalSessionsRating / totalSessionSize;

    log.debug("Avg session rating : {}", avgRating);

    session.setRating(BigDecimal.valueOf(avgRating));
    session.setNbrRegistered(totalSessionSize);

    return trainingSessionRepository.save(session);
  }
}
