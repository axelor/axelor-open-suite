package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class InvoicingPaymentSituationServiceImpl implements InvoicingPaymentSituationService {

  @Inject
  public InvoicingPaymentSituationServiceImpl() {}

  @Override
  public String getCompanyDomain(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner) {
    if (invoicingPaymentSituation == null || partner == null) {
      return "self.id = 0";
    }
    String domain = "(self.archived = false OR self.archived is null)";
    List<InvoicingPaymentSituation> partnerInvoicingPaymentSituationList =
        partner.getInvoicingPaymentSituationList();
    partnerInvoicingPaymentSituationList.remove(invoicingPaymentSituation);
    if (ObjectUtils.isEmpty(partnerInvoicingPaymentSituationList)) {
      return domain;
    }

    domain =
        domain.concat(
            String.format(
                " AND self.id NOT IN (%s)",
                StringHelper.getIdListString(
                    partnerInvoicingPaymentSituationList.stream()
                        .map(InvoicingPaymentSituation::getCompany)
                        .collect(Collectors.toList()))));

    return domain;
  }
}
