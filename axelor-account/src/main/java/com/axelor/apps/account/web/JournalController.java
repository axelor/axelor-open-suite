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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.journal.JournalControlService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;

@Singleton
public class JournalController {

  @Inject JournalService journalService;

  public void computeBalance(ActionRequest request, ActionResponse response) {

    Journal journal = request.getContext().asType(Journal.class);

    Map<String, BigDecimal> resultMap = journalService.computeBalance(journal);

    response.setValue("$balance", resultMap.get("balance"));
    response.setValue("$totalDebit", resultMap.get("debit"));
    response.setValue("$totalCredit", resultMap.get("credit"));
  }

  public void setReadOnly(ActionRequest request, ActionResponse response) {
    try {
      Journal journalContext = request.getContext().asType(Journal.class);
      if (journalContext.getId() != null) {
        Journal journal = Beans.get(JournalRepository.class).find(journalContext.getId());
        if (journal != null) {
          Boolean isInMove = Beans.get(JournalControlService.class).isLinkedToMove(journal);
          boolean isActive = journal.getStatusSelect() == JournalRepository.STATUS_ACTIVE;
          response.setAttr("name", "readonly", isInMove || isActive);
          response.setAttr("code", "readonly", isInMove || isActive);
          response.setAttr("journalType", "readonly", isInMove || isActive);
          response.setAttr("company", "readonly", isInMove || isActive);
        }
      } else {
        // We need to do that because setReadOnly is called onNew too.
        response.setAttr("name", "readonly", false);
        response.setAttr("code", "readonly", false);
        response.setAttr("journalType", "readonly", false);
        response.setAttr("company", "readonly", false);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void toggleStatus(ActionRequest request, ActionResponse response) {
    try {
      Journal journal = request.getContext().asType(Journal.class);
      journal = Beans.get(JournalRepository.class).find(journal.getId());
      Beans.get(JournalService.class).toggleStatusSelect(journal);

      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
