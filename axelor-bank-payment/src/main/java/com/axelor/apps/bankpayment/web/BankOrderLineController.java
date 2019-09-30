/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BankOrderLineController {

  protected BankOrderLineService bankOrderLineService;

  @Inject
  public BankOrderLineController(BankOrderLineService bankOrderLineService) {
    this.bankOrderLineService = bankOrderLineService;
  }

  // settings the domain for the bank details view.
  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    BankOrderLine bankOrderLine = request.getContext().asType(BankOrderLine.class);
    BankOrder bankOrder = request.getContext().getParent().asType(BankOrder.class);
    String domain = bankOrderLineService.createDomainForBankDetails(bankOrderLine, bankOrder);
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

    BankDetails bankDetails = bankOrderLineService.getDefaultBankDetails(bankOrderLine, bankOrder);
    response.setValue("receiverBankDetails", bankDetails);
  }

  public void computeCompanyCurrencyAmount(ActionRequest request, ActionResponse response) {

    BankOrderLine bankOrderLine = request.getContext().asType(BankOrderLine.class);
    BankOrder bankOrder = request.getContext().getParent().asType(BankOrder.class);

    try {

      response.setValue(
          "companyCurrencyAmount",
          bankOrderLineService.computeCompanyCurrencyAmount(bankOrder, bankOrderLine));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
