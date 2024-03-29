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
