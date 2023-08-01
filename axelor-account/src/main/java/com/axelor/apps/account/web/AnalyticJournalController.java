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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.repo.AnalyticJournalRepository;
import com.axelor.apps.account.service.AnalyticJournalControlService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class AnalyticJournalController {

  public void controlUniqueCode(ActionRequest request, ActionResponse response) {

    try {
      Beans.get(AnalyticJournalControlService.class)
          .controlDuplicateCode(request.getContext().asType(AnalyticJournal.class));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setReadOnly(ActionRequest request, ActionResponse response) {
    try {
      AnalyticJournal analyticJournal =
          Beans.get(AnalyticJournalRepository.class)
              .find(request.getContext().asType(AnalyticJournal.class).getId());
      if (analyticJournal != null) {
        Boolean isInMove =
            Beans.get(AnalyticJournalControlService.class).isInAnalyticMoveLine(analyticJournal);
        response.setAttr("name", "readonly", isInMove);
        response.setAttr("code", "readonly", isInMove);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void toggleStatus(ActionRequest request, ActionResponse response) {
    try {
      AnalyticJournal analyticJournal = request.getContext().asType(AnalyticJournal.class);
      analyticJournal = Beans.get(AnalyticJournalRepository.class).find(analyticJournal.getId());

      Beans.get(AnalyticJournalControlService.class).toggleStatusSelect(analyticJournal);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
