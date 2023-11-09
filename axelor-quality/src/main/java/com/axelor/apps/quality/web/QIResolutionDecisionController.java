/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDecision;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.service.QIResolutionDecisionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;

public class QIResolutionDecisionController {

  public void checkQuantity(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Context parent = context.getParent();

      QIResolutionDecision qiResolutionDecision = context.asType(QIResolutionDecision.class);
      QIResolution qiResolution = null;

      if (parent == null || !parent.getContextClass().equals(QIResolution.class)) {
        qiResolution = qiResolutionDecision.getQiResolution();
      } else {
        qiResolution = parent.asType(QIResolution.class);
      }
      if (!Beans.get(QIResolutionDecisionService.class)
          .checkQuantity(qiResolutionDecision, qiResolution)) {
        response.setValue("quantity", BigDecimal.ZERO);
        response.setInfo(
            I18n.get(QualityExceptionMessage.QI_RESOLUTION_DECISION_INVALID_SUM_OF_QUANTITIES));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
