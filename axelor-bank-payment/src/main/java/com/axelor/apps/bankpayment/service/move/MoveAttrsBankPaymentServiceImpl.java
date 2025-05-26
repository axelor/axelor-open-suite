package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MovePfpService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveAttrsBankPaymentServiceImpl extends MoveAttrsServiceImpl {
  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveAttrsBankPaymentServiceImpl(
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveViewHelperService moveViewHelperService,
      MovePfpService movePfpService,
      AnalyticToolService analyticToolService,
      AnalyticAttrsService analyticAttrsService,
      CompanyRepository companyRepository,
      JournalRepository journalRepository,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        accountConfigService,
        appAccountService,
        moveInvoiceTermService,
        moveViewHelperService,
        movePfpService,
        analyticToolService,
        analyticAttrsService,
        companyRepository,
        journalRepository);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public void addPartnerBankDetailsDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    super.addPartnerBankDetailsDomain(move, attrsMap);
    Partner partner = move.getPartner();

    if (partner != null && !CollectionUtils.isEmpty(partner.getBankDetailsList())) {
      PaymentMode paymentMode = move.getPaymentMode();
      Company company = move.getCompany();
      List<BankDetails> bankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, partner, company);

      String domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
      this.addAttr("partnerBankDetails", "domain", domain, attrsMap);
    }
  }
}
