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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MovePfpService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsServiceImpl;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveAttrsBankPaymentServiceImpl extends MoveAttrsServiceImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;
  protected AppBankPaymentService appBankPaymentService;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;

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
      BankDetailsBankPaymentService bankDetailsBankPaymentService,
      AppBankPaymentService appBankPaymentService,
      BankReconciliationLineRepository bankReconciliationLineRepository) {
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
    this.appBankPaymentService = appBankPaymentService;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
  }

  @Override
  public void addPartnerBankDetailsDomain(Move move, Map<String, Map<String, Object>> attrsMap) {
    super.addPartnerBankDetailsDomain(move, attrsMap);
    Partner partner = move.getPartner();
    PaymentMode paymentMode = move.getPaymentMode();

    if (partner != null
        && !CollectionUtils.isEmpty(partner.getBankDetailsList())
        && paymentMode != null
        && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD
        && Boolean.TRUE.equals(
            appBankPaymentService.getAppBankPayment().getManageDirectDebitPayment())) {
      Company company = move.getCompany();
      List<BankDetails> bankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, partner, company);

      String domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
      this.addAttr("partnerBankDetails", "domain", domain, attrsMap);
    }
  }

  @Override
  public void addMoveLineListViewerHidden(Move move, Map<String, Map<String, Object>> attrsMap) {
    super.addMoveLineListViewerHidden(move, attrsMap);

    List<Long> moveLineIdList =
        Optional.ofNullable(move.getMoveLineList()).orElseGet(Collections::emptyList).stream()
            .map(MoveLine::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    boolean isLinkedToValidatedBankReconciliation = false;

    if (!CollectionUtils.isEmpty(moveLineIdList)) {
      isLinkedToValidatedBankReconciliation =
          bankReconciliationLineRepository
                  .all()
                  .filter(
                      "self.moveLine.id IN :moveLineIdList AND self.bankReconciliation.statusSelect = :statusSelect")
                  .bind("moveLineIdList", moveLineIdList)
                  .bind("statusSelect", BankReconciliationRepository.STATUS_VALIDATED)
                  .count()
              > 0;
    }
    this.addAttr(
        "$bankReconciliationTag", "hidden", !isLinkedToValidatedBankReconciliation, attrsMap);
  }
}
