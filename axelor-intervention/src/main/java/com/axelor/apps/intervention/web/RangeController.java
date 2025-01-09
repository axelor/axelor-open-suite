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
