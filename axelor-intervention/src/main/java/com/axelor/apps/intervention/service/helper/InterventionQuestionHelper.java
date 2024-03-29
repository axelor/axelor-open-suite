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
package com.axelor.apps.intervention.service.helper;

import com.axelor.apps.intervention.db.AnswerValue;
import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.InterventionQuestionState;
import java.util.Optional;
import java.util.Set;

public class InterventionQuestionHelper {

  public static boolean isAnswered(InterventionQuestion question) {
    return question.getAnswerTypeSelect().equals("list") && question.getListAnswer() != null
        || question.getAnswerTypeSelect().equals("text") && question.getTextAnswer() != null
        || question.getAnswerTypeSelect().equals("date") && question.getDateAnswer() != null
        || question.getAnswerTypeSelect().equals("picture") && question.getPictureAnswer() != null
        || question.getAnswerTypeSelect().equals("measure") && question.getMeasureAnswer() != null
        || question.getAnswerTypeSelect().equals("checkbox") && question.getCheckboxAnswer() != null
        || question.getAnswerTypeSelect().equals("signature")
            && question.getSignatureAnswer() != null
        || question.getAnswerTypeSelect().equals("advancedMonitoring")
            && question.getAdvancedMonitoringAnswer() != null;
  }

  public static boolean isActive(InterventionQuestion question) {
    if (!Boolean.TRUE.equals(question.getIsConditional())) {
      return true;
    }
    Optional<InterventionQuestion> conditionalQuestion =
        Optional.ofNullable(question.getConditionalInterventionQuestion());
    Optional<Set<AnswerValue>> conditionalAnswers =
        Optional.ofNullable(question.getConditionalAnswerValueSet());
    if (!conditionalQuestion.isPresent() || !conditionalAnswers.isPresent()) {
      return false;
    }
    return conditionalQuestion.get().getListAnswer() != null
        && conditionalAnswers.get().stream()
            .anyMatch(answerValue -> answerValue.equals(conditionalQuestion.get().getListAnswer()));
  }

  public static boolean isRequired(InterventionQuestion question) {
    if (!Boolean.TRUE.equals(question.getIsConditional())) {
      return question.getIsRequired();
    }
    return isActive(question) && question.getIsRequired();
  }

  public static InterventionQuestionState state(InterventionQuestion question) {
    if (Boolean.TRUE.equals(question.getIsAnswered())) {
      return InterventionQuestionState.ANSWERED;
    }
    if (isRequired(question) && !Boolean.TRUE.equals(question.getIsAnswered())) {
      return InterventionQuestionState.NOT_ANSWERED;
    }
    if (question.getIndicationText() != null) {
      return InterventionQuestionState.INDICATION;
    }
    if (Boolean.TRUE.equals(question.getIsPrivate())) {
      return InterventionQuestionState.PRIVATE;
    }
    if (Boolean.TRUE.equals(question.getIsConditional())) {
      return InterventionQuestionState.CONDITIONAL;
    }
    return null;
  }
}
