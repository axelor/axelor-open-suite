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
package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.InterventionRange;
import com.axelor.apps.intervention.db.repo.AnswerValueRepository;
import com.axelor.apps.intervention.db.repo.InterventionQuestionRepository;
import com.axelor.apps.intervention.db.repo.InterventionRangeRepository;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class InterventionRangeServiceImpl implements InterventionRangeService {

  protected final InterventionRangeRepository interventionRangeRepository;
  protected final InterventionQuestionRepository interventionQuestionRepository;
  protected final InterventionRepository interventionRepository;
  protected final AnswerValueRepository answerValueRepository;

  @Inject
  public InterventionRangeServiceImpl(
      InterventionRangeRepository interventionRangeRepository,
      InterventionQuestionRepository interventionQuestionRepository,
      InterventionRepository interventionRepository,
      AnswerValueRepository answerValueRepository) {
    this.interventionRangeRepository = interventionRangeRepository;
    this.interventionQuestionRepository = interventionQuestionRepository;
    this.interventionRepository = interventionRepository;
    this.answerValueRepository = answerValueRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void deleteInterventionRanges(List<Long> interventionRangeIds) {
    for (Long interventionRangeId : interventionRangeIds) {
      InterventionRange interventionRange = interventionRangeRepository.find(interventionRangeId);
      List<InterventionQuestion> interventionQuestionList =
          interventionRange.getInterventionQuestionList();
      interventionQuestionList.clear();
      interventionRangeRepository.save(interventionRange);
    }

    JPA.em()
        .createQuery("DELETE FROM InterventionRange self WHERE self.id IN :ids")
        .setParameter("ids", interventionRangeIds)
        .executeUpdate();
  }
}
