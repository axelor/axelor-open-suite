package com.axelor.apps.intervention.web;

import com.axelor.apps.intervention.db.RangeQuestion;
import com.axelor.apps.intervention.service.RangeQuestionService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.MapHelper;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class RangeController {

  @Transactional
  public void removeRangeQuestions(ActionRequest request, ActionResponse response) {
    List<RangeQuestion> rangeQuestionList =
        MapHelper.getCollection(request.getContext(), RangeQuestion.class, "_xRangeQuestionList");
    if (CollectionUtils.isNotEmpty(rangeQuestionList)) {
      Beans.get(RangeQuestionService.class).removeRangeQuestions(rangeQuestionList);
      response.setCanClose(true);
    }
  }
}
