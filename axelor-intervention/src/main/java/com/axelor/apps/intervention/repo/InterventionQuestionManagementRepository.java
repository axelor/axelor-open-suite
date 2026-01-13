/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.repo;

import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.repo.InterventionQuestionRepository;
import com.axelor.apps.intervention.service.InterventionService;
import com.axelor.apps.intervention.service.helper.InterventionQuestionHelper;
import com.axelor.inject.Beans;

public class InterventionQuestionManagementRepository extends InterventionQuestionRepository {

  @Override
  public InterventionQuestion save(InterventionQuestion entity) {
    InterventionQuestion question = super.save(entity);

    question.setIsAnswered(InterventionQuestionHelper.isAnswered(question));
    question.setState(InterventionQuestionHelper.state(question));

    all()
        .filter("self.conditionalInterventionQuestion.id = :id")
        .bind("id", question.getId())
        .fetch()
        .forEach(q -> q.setState(InterventionQuestionHelper.state(q)));

    Beans.get(InterventionService.class)
        .computeTag(entity.getInterventionRange().getIntervention().getId());

    return super.save(question);
  }
}
