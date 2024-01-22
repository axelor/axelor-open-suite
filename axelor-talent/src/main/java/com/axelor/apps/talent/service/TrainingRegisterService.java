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

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.talent.db.Training;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface TrainingRegisterService {

  public Event plan(TrainingRegister trainingRegister);

  public void complete(TrainingRegister trainingRegister);

  public void cancel(TrainingRegister trainingRegister);

  public Training updateTrainingRating(Training training, Long excludeId);

  public TrainingSession updateSessionRating(TrainingSession trainingSession, Long excludeId);

  public void updateEventCalendar(TrainingRegister trainingRegister);

  public String computeFullName(TrainingRegister trainingRegister);

  public String massTrainingRegisterCreation(
      ArrayList<LinkedHashMap<String, Object>> employeeList, TrainingSession trainingSession);
}
