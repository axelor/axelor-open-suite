/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankdetails;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.umr.UmrService;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BankDetailsBankPaymentServiceImpl implements BankDetailsBankPaymentService {
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankDetailsRepository bankDetailsRepository;
  protected CurrencyScaleService currencyScaleService;
  protected UmrService umrService;
  protected AppBankPaymentService appBankPaymentService;

  @Inject
  public BankDetailsBankPaymentServiceImpl(
      BankStatementLineFetchService bankStatementLineFetchService,
      BankDetailsRepository bankDetailsRepository,
      CurrencyScaleService currencyScaleService,
      UmrService umrService,
      AppBankPaymentService appBankPaymentService) {
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankDetailsRepository = bankDetailsRepository;
    this.currencyScaleService = currencyScaleService;
    this.umrService = umrService;
    this.appBankPaymentService = appBankPaymentService;
  }

  @Override
  @Transactional
  public void updateBankDetailsBalanceAndDate(List<BankDetails> bankDetails) {
    if (CollectionUtils.isEmpty(bankDetails)) {
      return;
    }
    BankStatementLine lastLine;
    for (BankDetails bankDetail : bankDetails) {
      lastLine = bankStatementLineFetchService.getLastBankStatementLineFromBankDetails(bankDetail);
      if (lastLine != null) {
        bankDetail.setBalance(
            currencyScaleService.getScaledValue(
                lastLine,
                lastLine.getDebit().compareTo(BigDecimal.ZERO) > 0
                    ? lastLine.getDebit()
                    : lastLine.getCredit()));
        bankDetail.setBalanceUpdatedDate(lastLine.getOperationDate());
      } else {
        bankDetail.setBalance(BigDecimal.ZERO);
        bankDetail.setBalanceUpdatedDate(null);
      }
      bankDetailsRepository.save(bankDetail);
    }
  }

  @Override
  public List<BankDetails> getBankDetailsLinkedToActiveUmr(
      PaymentMode paymentMode, Partner partner, Company company) {
    if (paymentMode != null && partner != null && company != null) {
      boolean isManageDirectDebitPayment =
          appBankPaymentService.getAppBankPayment().getManageDirectDebitPayment();
      if (isManageDirectDebitPayment
          && (paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD)
          && !partner.getBankDetailsList().isEmpty()) {
        return partner.getBankDetailsList().stream()
            .filter(bankDetails -> umrService.getActiveUmr(company, bankDetails) != null)
            .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isBankDetailsNotLinkedToActiveUmr(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) {
    return paymentMode != null
        && company != null
        && bankDetails != null
        && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD
        && appBankPaymentService.getAppBankPayment().getManageDirectDebitPayment()
        && (umrService.getActiveUmr(company, bankDetails) == null);
  }
}
