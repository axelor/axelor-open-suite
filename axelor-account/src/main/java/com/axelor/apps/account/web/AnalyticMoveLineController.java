/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineComputeService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineParentService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;

public class AnalyticMoveLineController {

  public void computePercentage(ActionRequest request, ActionResponse response) {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
      AnalyticLineModel parent =
          Beans.get(AnalyticMoveLineParentService.class)
              .getModelUsingAnalyticMoveLine(analyticMoveLine, request.getContext());

      AnalyticMoveLineComputeService analyticMoveLineComputeService =
          Beans.get(AnalyticMoveLineComputeService.class);

      BigDecimal percentage =
          parent != null
              ? analyticMoveLineComputeService.computePercentage(
                  analyticMoveLine, parent.getLineAmount())
              : analyticMoveLineComputeService.computePercentage(analyticMoveLine);
      response.setValue("percentage", percentage);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on change of the analytic distribution lines of an analytic line. When the user empties
   * the distribution, the template no longer describes the line, so it is cleared as well.
   *
   * <p>The context is the parent analytic line (move, invoice, order, contract or expense line),
   * not the analytic move line itself.
   */
  public void clearDistributionTemplateIfNoLine(ActionRequest request, ActionResponse response) {
    try {
      if (ObjectUtils.isEmpty(request.getContext().get("analyticMoveLineList"))) {
        response.setValue("analyticDistributionTemplate", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
