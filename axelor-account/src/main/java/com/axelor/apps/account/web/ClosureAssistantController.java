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

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class ClosureAssistantController {

  public void setClosureAssistantFields(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
      ClosureAssistantService closureAssistantService = Beans.get(ClosureAssistantService.class);

      closureAssistant = closureAssistantService.updateCompany(closureAssistant);

      closureAssistant = closureAssistantService.updateFiscalYear(closureAssistant);

      List<ClosureAssistantLine> closureAssistantLineList =
          Beans.get(ClosureAssistantLineService.class).initClosureAssistantLines(closureAssistant);

      response.setValue("company", closureAssistant.getCompany());
      response.setValue("fiscalYear", closureAssistant.getFiscalYear());
      response.setValue("closureAssistantLineList", closureAssistantLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoExistingClosureAssistantForSameYear(
      ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
      if (Beans.get(ClosureAssistantService.class)
          .checkNoExistingClosureAssistantForSameYear(closureAssistant)) {
        response.setError(
            I18n.get(
                String.format(
                    AccountExceptionMessage.ACCOUNT_CLOSURE_ASSISTANT_ALREADY_EXISTS_FOR_SAME_YEAR,
                    closureAssistant.getFiscalYear().getCode(),
                    closureAssistant.getCompany().getCode())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
