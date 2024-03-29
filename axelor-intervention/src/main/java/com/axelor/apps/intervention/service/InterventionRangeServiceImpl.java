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
