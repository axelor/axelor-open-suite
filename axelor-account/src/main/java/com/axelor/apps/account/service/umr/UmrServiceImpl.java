package com.axelor.apps.account.service.umr;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.db.Umr;
import com.axelor.apps.account.db.repo.InvoicingPaymentSituationRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UmrServiceImpl implements UmrService {

  protected AppBaseService appBaseService;
  protected PartnerService partnerService;
  protected UmrNumberService umrNumberService;
  protected InvoicingPaymentSituationRepository invoicingPaymentSituationRepository;

  @Inject
  public UmrServiceImpl(
      AppBaseService appBaseService,
      PartnerService partnerService,
      UmrNumberService umrNumberService,
      InvoicingPaymentSituationRepository invoicingPaymentSituationRepository) {
    this.appBaseService = appBaseService;
    this.partnerService = partnerService;
    this.umrNumberService = umrNumberService;
    this.invoicingPaymentSituationRepository = invoicingPaymentSituationRepository;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(InvoicingPaymentSituation invoicingPaymentSituation)
      throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    LocalDate date =
        Beans.get(AppBaseService.class).getTodayDate(invoicingPaymentSituation.getCompany());

    valuesMap.put("creationDate", date);
    valuesMap.put("mandateSignatureDate", date);

    if (invoicingPaymentSituation.getPartner() != null) {
      Partner partner = invoicingPaymentSituation.getPartner();
      valuesMap.put("debtorName", partner.getName());
      valuesMap.put("debtorAddress", partnerService.getInvoicingAddress(partner));
    }
    valuesMap.put("umrNumber", umrNumberService.getUmrNumber(invoicingPaymentSituation, date));

    return valuesMap;
  }

  @Override
  public Umr getActiveUmr(Company company, Partner partner) {
    if (company == null || partner == null) {
      return null;
    }

    InvoicingPaymentSituation invoicingPaymentSituation =
        invoicingPaymentSituationRepository.findByCompanyAndPartner(company, partner);
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
