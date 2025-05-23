package com.axelor.apps.account.service.umr;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.db.Umr;
import com.axelor.apps.account.db.repo.InvoicingPaymentSituationRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;

public class UmrActiveServiceImpl implements UmrActiveService {

  protected InvoicingPaymentSituationRepository invoicingPaymentSituationRepository;

  @Inject
  public UmrActiveServiceImpl(
      InvoicingPaymentSituationRepository invoicingPaymentSituationRepository) {
    this.invoicingPaymentSituationRepository = invoicingPaymentSituationRepository;
  }

  @Override
  public Umr getActiveUmr(Company company, BankDetails bankDetails) {
    if (company == null || bankDetails == null) {
      return null;
    }

    InvoicingPaymentSituation invoicingPaymentSituation =
        invoicingPaymentSituationRepository.findByCompanyAndBankDetails(company, bankDetails);
    if (invoicingPaymentSituation != null) {
      if (invoicingPaymentSituation.getActiveUmr() != null) {
        return invoicingPaymentSituation.getActiveUmr();
      }
      List<Umr> umrList = invoicingPaymentSituation.getUmrList();
      if (!ObjectUtils.isEmpty(umrList) && umrList.size() == 1) {
        return umrList.get(0);
      }
    }
    return null;
  }
}
