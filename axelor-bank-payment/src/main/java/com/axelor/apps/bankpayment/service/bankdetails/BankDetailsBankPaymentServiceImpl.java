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
package com.axelor.apps.bankpayment.service.bankdetails;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineFetchAFB120Service;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankDetailsBankPaymentServiceImpl implements BankDetailsBankPaymentService {
  protected BankStatementLineFetchAFB120Service bankStatementLineFetchAFB120Service;
  protected BankDetailsRepository bankDetailsRepository;
  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  @Inject
  public BankDetailsBankPaymentServiceImpl(
      BankStatementLineFetchAFB120Service bankStatementLineFetchAFB120Service,
      BankDetailsRepository bankDetailsRepository,
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment) {
    this.bankStatementLineFetchAFB120Service = bankStatementLineFetchAFB120Service;
    this.bankDetailsRepository = bankDetailsRepository;
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
  }

  @Override
  @Transactional
  public void updateBankDetailsBalanceAndDate(List<BankDetails> bankDetails) {
    if (CollectionUtils.isEmpty(bankDetails)) {
      return;
    }
    BankStatementLineAFB120 lastLine;
    for (BankDetails bankDetail : bankDetails) {
      lastLine =
          bankStatementLineFetchAFB120Service.getLastBankStatementLineAFB120FromBankDetails(
              bankDetail);
      if (lastLine != null) {
        bankDetail.setBalance(
            currencyScaleServiceBankPayment.getScaledValue(
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
}
