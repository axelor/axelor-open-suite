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
package com.axelor.apps.talent.db.repo;

import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.exception.TalentExceptionMessage;
import com.axelor.apps.talent.service.TrainingSessionService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import javax.validation.ValidationException;

public class TrainingSessionTalentRepository extends TrainingSessionRepository {

  @Inject private TrainingSessionService trainingSessionService;

  @Override
  public TrainingSession save(TrainingSession trainingSession) {

    if (trainingSession.getFromDate().isAfter(trainingSession.getToDate())) {
      throw new ValidationException(I18n.get(TalentExceptionMessage.INVALID_DATE_RANGE));
    }

    trainingSession.setFullName(trainingSessionService.computeFullName(trainingSession));

    return super.save(trainingSession);
  }
}
