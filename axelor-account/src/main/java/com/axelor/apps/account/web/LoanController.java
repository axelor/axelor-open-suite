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

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.repo.LoanRepository;
import com.axelor.apps.account.service.loan.LoanManagementConfigService;
import com.axelor.apps.account.service.loan.LoanValidateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class LoanController {

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      Loan loan =
          Beans.get(LoanRepository.class).find(request.getContext().asType(Loan.class).getId());
      Beans.get(LoanValidateService.class).validate(loan);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultLoanManagementConfig(ActionRequest request, ActionResponse response) {
    Loan loan = request.getContext().asType(Loan.class);
    if (loan.getLoanManagementConfig() == null) {
      response.setValue(
          "loanManagementConfig",
          Beans.get(LoanManagementConfigService.class)
              .getDefaultLoanManagementConfig(loan.getCompany()));
    }
  }
}
