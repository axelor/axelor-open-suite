package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.RangeQuestion;
import com.axelor.apps.intervention.db.repo.RangeQuestionRepository;
import com.google.inject.Inject;
import java.util.List;

public class RangeQuestionServiceImpl implements RangeQuestionService {

  private final RangeQuestionRepository rangeQuestionRepository;

  @Inject
  public RangeQuestionServiceImpl(RangeQuestionRepository rangeQuestionRepository) {
    this.rangeQuestionRepository = rangeQuestionRepository;
  }

  @Override
  public void removeRangeQuestions(List<RangeQuestion> rangeQuestionList) {

    rangeQuestionList.forEach(rangeQuestionRepository::remove);
  }
}
