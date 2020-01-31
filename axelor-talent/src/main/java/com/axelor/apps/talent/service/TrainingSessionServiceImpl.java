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

import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    for (TrainingRegister trainingRegister : trainingRegisters) {
      trainingRegisterService.complete(trainingRegister);
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
}
