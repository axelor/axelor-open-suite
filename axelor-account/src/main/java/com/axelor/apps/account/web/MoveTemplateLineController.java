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

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.account.service.move.template.MoveTemplateLineAnalyticService;
import com.axelor.apps.account.service.move.template.MoveTemplateLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineGroupService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class MoveTemplateLineController {
  public void accountOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);
      if (moveTemplate != null) {
        response.setAttrs(
            Beans.get(MoveLineGroupService.class)
                .getAccountOnSelectAttrsMap(moveTemplate.getJournal(), moveTemplate.getCompany()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void accountOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      MoveTemplateLineAnalyticService moveTemplateLineAnalyticService =
          Beans.get(MoveTemplateLineAnalyticService.class);

      response.setValues(
          moveTemplateLineAnalyticService.getAccountOnChangeValuesMap(
              moveTemplateLine, moveTemplate));
      response.setAttrs(
          moveTemplateLineAnalyticService.getAccountOnChangeAttrsMap(
              moveTemplateLine, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      MoveTemplateLineAnalyticService moveTemplateLineAnalyticService =
          Beans.get(MoveTemplateLineAnalyticService.class);

      response.setValues(
          moveTemplateLineAnalyticService.getAnalyticDistributionTemplateOnChangeValuesMap(
              moveTemplateLine, moveTemplate));
      response.setAttrs(
          moveTemplateLineAnalyticService.getAnalyticDistributionTemplateOnChangeAttrsMap(
              moveTemplateLine, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticAxisOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      MoveTemplateLineAnalyticService moveTemplateLineAnalyticService =
          Beans.get(MoveTemplateLineAnalyticService.class);

      response.setValues(
          moveTemplateLineAnalyticService.getAnalyticAxisOnChangeValuesMap(
              moveTemplateLine, moveTemplate));
      response.setAttrs(
          moveTemplateLineAnalyticService.getAnalyticAxisOnChangeAttrsMap(
              moveTemplateLine, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      if (moveTemplate == null) {
        return;
      }

      response.setAttrs(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAxisDomainAttrsMap(moveTemplateLine, moveTemplate.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticDistributionTemplateOnSelect(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      response.setAttrs(
          Beans.get(MoveTemplateLineAnalyticService.class)
              .getAnalyticDistributionTemplateOnSelectAttrsMap(moveTemplateLine, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void analyticMoveLineOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      MoveTemplateLineAnalyticService moveTemplateLineAnalyticService =
          Beans.get(MoveTemplateLineAnalyticService.class);

      response.setValues(
          moveTemplateLineAnalyticService.getAnalyticMoveLineOnChangeValuesMap(
              moveTemplateLine, moveTemplate));
      response.setAttrs(
          moveTemplateLineAnalyticService.getAnalyticMoveLineOnChangeAttrsMap(
              moveTemplateLine, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);

      Beans.get(MoveTemplateLineComputeAnalyticService.class)
          .computeAnalyticDistribution(moveTemplateLine);

      response.setValue("analyticMoveLineList", moveTemplateLine.getAnalyticMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void onLoadAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplateLine moveTemplateLine = request.getContext().asType(MoveTemplateLine.class);
      MoveTemplate moveTemplate = this.getMoveTemplate(request, moveTemplateLine);

      MoveTemplateLineAnalyticService moveTemplateLineAnalyticService =
          Beans.get(MoveTemplateLineAnalyticService.class);
      response.setValues(
          moveTemplateLineAnalyticService.getOnLoadAnalyticDistributionValuesMap(
              moveTemplateLine, moveTemplate));
      response.setAttrs(
          moveTemplateLineAnalyticService.getOnLoadAnalyticDistributionAttrsMap(
              moveTemplateLine, moveTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected MoveTemplate getMoveTemplate(ActionRequest request, MoveTemplateLine moveTemplateLine) {
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && MoveTemplate.class.equals(parentContext.getContextClass())) {
      return request.getContext().getParent().asType(MoveTemplate.class);
    } else {
      return moveTemplateLine.getMoveTemplate();
    }
  }
}
