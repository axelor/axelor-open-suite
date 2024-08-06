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
package com.axelor.apps.base.web;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

/** Controller called from multiple forms, */
@Singleton
public class CompanyBankDetailsController {

  /**
   * Set the domain of company bank details field
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void fillCompanyBankDetailsDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Partner partner = (Partner) request.getContext().get("partner");
    Company company = (Company) request.getContext().get("company");
    PaymentMode paymentMode = (PaymentMode) request.getContext().get("paymentMode");
    Integer operationTypeSelect = null;
    if (request.getContext().get("_operationTypeSelect") != null) {
      operationTypeSelect =
          Integer.valueOf(request.getContext().get("_operationTypeSelect").toString());
    }
    response.setAttr(
        "companyBankDetails",
        "domain",
        Beans.get(BankDetailsServiceImpl.class)
            .createCompanyBankDetailsDomain(partner, company, paymentMode, operationTypeSelect));
  }
}
