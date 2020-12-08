/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.service.JournalService;
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
}
