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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
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

  protected MoveTemplate getMoveTemplate(ActionRequest request, MoveTemplateLine moveTemplateLine) {
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && MoveTemplate.class.equals(parentContext.getContextClass())) {
      return request.getContext().getParent().asType(MoveTemplate.class);
    } else {
      return moveTemplateLine.getMoveTemplate();
    }
  }
}
