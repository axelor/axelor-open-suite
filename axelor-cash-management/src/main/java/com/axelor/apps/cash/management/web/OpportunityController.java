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
package com.axelor.apps.cash.management.web;

import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class OpportunityController {

  public void fillBankDetails(ActionRequest request, ActionResponse response) {
    if (!Beans.get(AppBaseService.class).getAppBase().getManageMultiBanks()) {
      return;
    }
    Opportunity opportunity = request.getContext().asType(Opportunity.class);
    Company company = opportunity.getCompany();
    if (company == null) {
      response.setValue("bankDetails", null);
      return;
    }
    response.setValue(
        "bankDetails",
        Beans.get(AccountingSituationService.class)
            .getCompanySalesBankDetails(company, opportunity.getPartner()));
  }
}
