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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class BankOrderLineController {

  // settings the domain for the bank details view.
  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    BankOrderLine bankOrderLine = request.getContext().asType(BankOrderLine.class);
    BankOrder bankOrder = request.getContext().getParent().asType(BankOrder.class);
    String domain =
        Beans.get(BankOrderLineService.class).createDomainForBankDetails(bankOrderLine, bankOrder);
    // if nothing was found for the domain, we set it at a default value.
    if (domain.equals("")) {
      response.setAttr("receiverBankDetails", "domain", "self.id IN (0)");
    } else {
      response.setAttr("receiverBankDetails", "domain", domain);
    }
  }

  public void fillBankDetail(ActionRequest request, ActionResponse response) {
    BankOrderLine bankOrderLine = request.getContext().asType(BankOrderLine.class);
    BankOrder bankOrder = request.getContext().getParent().asType(BankOrder.class);

    BankDetails bankDetails =
        Beans.get(BankOrderLineService.class).getDefaultBankDetails(bankOrderLine, bankOrder);
    response.setValue("receiverBankDetails", bankDetails);
    response.setAttr("bankOrderDate", "hidden", !bankOrder.getIsMultiDate());
  }

  public void computeCompanyCurrencyAmount(ActionRequest request, ActionResponse response) {

    BankOrderLine bankOrderLine = request.getContext().asType(BankOrderLine.class);
    BankOrder bankOrder = request.getContext().getParent().asType(BankOrder.class);

    try {

      response.setValue(
          "companyCurrencyAmount",
          Beans.get(BankOrderLineService.class)
              .computeCompanyCurrencyAmount(bankOrder, bankOrderLine));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
