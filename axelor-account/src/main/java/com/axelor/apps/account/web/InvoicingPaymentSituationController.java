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

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.db.Umr;
import com.axelor.apps.account.service.InvoicingPaymentSituationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;
import java.util.Objects;

public class InvoicingPaymentSituationController {

  @ErrorException
  public void setCompanyDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoicingPaymentSituation invoicingPaymentSituation =
        request.getContext().asType(InvoicingPaymentSituation.class);
    Partner partner = getPartner(request, invoicingPaymentSituation);

    response.setAttr(
        "company",
        "domain",
        Beans.get(InvoicingPaymentSituationService.class)
            .getCompanyDomain(invoicingPaymentSituation, partner));
  }

  @ErrorException
  public void setActiveUmrDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoicingPaymentSituation invoicingPaymentSituation =
        request.getContext().asType(InvoicingPaymentSituation.class);

    String domain = "self.id = 0";

    // A UMR is identified by the (partner, company, bankDetails) triple. Restricting on the bank
    // details prevents selecting a UMR from another situation sharing the same company, while UMRs
    // of a situation with no bank details yet stay selectable.
    if (invoicingPaymentSituation != null
        && !ObjectUtils.isEmpty(invoicingPaymentSituation.getUmrList())
        && invoicingPaymentSituation.getCompany() != null
        && invoicingPaymentSituation.getPartner() != null) {
      domain =
          "self.invoicingPaymentSituation.partner = :partner"
              + " AND self.invoicingPaymentSituation.company = :company"
              + " AND (self.invoicingPaymentSituation.bankDetails = :bankDetails"
              + " OR self.invoicingPaymentSituation.bankDetails is null)";
    }

    response.setAttr("activeUmr", "domain", domain);
  }

  @ErrorException
  public void onNew(ActionRequest request, ActionResponse response) throws AxelorException {
    InvoicingPaymentSituation invoicingPaymentSituation =
        request.getContext().asType(InvoicingPaymentSituation.class);
    Partner partner = getPartner(request, invoicingPaymentSituation);

    response.setValues(
        Beans.get(InvoicingPaymentSituationService.class)
            .initInvoicingPaymentSituation(invoicingPaymentSituation, partner));
  }

  @ErrorException
  public void addUmrInList(ActionRequest request, ActionResponse response) throws AxelorException {
    InvoicingPaymentSituation invoicingPaymentSituation =
        request.getContext().asType(InvoicingPaymentSituation.class);

    List<Umr> umrList = invoicingPaymentSituation.getUmrList();
    Umr umr = invoicingPaymentSituation.getActiveUmr();

    if (umr != null && (ObjectUtils.isEmpty(umrList) || !umrList.contains(umr))) {
      umrList.add(umr);
      response.setValue("umrList", umrList);
    }
  }

  protected Partner getPartner(
      ActionRequest request, InvoicingPaymentSituation invoicingPaymentSituation) {
    if (request.getContext().getParent() != null
        && Partner.class.equals(request.getContext().getParent().getContextClass())) {
      return EntityHelper.getEntity(request.getContext().getParent().asType(Partner.class));
    }

    return invoicingPaymentSituation.getPartner();
  }

  @ErrorException
  public void selectBankDetails(ActionRequest request, ActionResponse response) {
    InvoicingPaymentSituation invoicingPaymentSituation =
        request.getContext().asType(InvoicingPaymentSituation.class);
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && Objects.equals(parentContext.getContextClass(), Partner.class)) {
      Partner partner = EntityHelper.getEntity(parentContext.asType(Partner.class));
      String domain =
          Beans.get(InvoicingPaymentSituationService.class)
              .getBankDetailsDomain(invoicingPaymentSituation, partner);
      response.setAttr("bankDetails", "domain", domain);
    }
  }
}
