package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.db.Umr;
import com.axelor.apps.account.service.InvoicingPaymentSituationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ContextHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    if (invoicingPaymentSituation != null
        && !ObjectUtils.isEmpty(invoicingPaymentSituation.getUmrList())
        && invoicingPaymentSituation.getCompany() != null
        && invoicingPaymentSituation.getPartner() != null) {
      domain =
          "self.invoicingPaymentSituation.partner = :partner AND self.invoicingPaymentSituation.company = :company";
    }

    response.setAttr("activeUmr", "domain", domain);
  }

  @ErrorException
  public void onNew(ActionRequest request, ActionResponse response) throws AxelorException {
    InvoicingPaymentSituation invoicingPaymentSituation =
        request.getContext().asType(InvoicingPaymentSituation.class);
    Partner partner = getPartner(request, invoicingPaymentSituation);

    response.setValue("partner", partner);
    response.setValue("umrList", new ArrayList<>());

    Company defaultCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (defaultCompany == null) {
      return;
    }

    if (partner != null
        && (ObjectUtils.isEmpty(partner.getInvoicingPaymentSituationList())
            || partner.getInvoicingPaymentSituationList().stream()
                .noneMatch(situation -> defaultCompany.equals(situation.getCompany())))) {
      response.setValue("company", defaultCompany);
    }
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
    if (invoicingPaymentSituation != null && invoicingPaymentSituation.getPartner() != null) {
      return invoicingPaymentSituation.getPartner();
    }

    return EntityHelper.getEntity(
        ContextHelper.getContextParent(request.getContext(), Partner.class, 1));
  }
}
