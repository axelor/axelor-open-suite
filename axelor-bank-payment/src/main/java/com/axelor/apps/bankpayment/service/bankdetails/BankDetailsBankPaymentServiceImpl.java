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

import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankDetailsBankPaymentServiceImpl implements BankDetailsBankPaymentService {
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankDetailsRepository bankDetailsRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankDetailsBankPaymentServiceImpl(
      BankStatementLineFetchService bankStatementLineFetchService,
      BankDetailsRepository bankDetailsRepository,
      CurrencyScaleService currencyScaleService) {
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankDetailsRepository = bankDetailsRepository;
    this.currencyScaleService = currencyScaleService;
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
}
