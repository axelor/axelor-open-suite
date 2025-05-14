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
package com.axelor.apps.bankpayment.service.bankorder.file;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderComputeService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BankOrderComputeServiceImpl implements BankOrderComputeService {

  protected CurrencyService currencyService;

  @Inject
  public BankOrderComputeServiceImpl(CurrencyService currencyService) {
    this.currencyService = currencyService;
  }

  @Override
  public void updateTotalAmounts(BankOrder bankOrder) throws AxelorException {
    if (bankOrder.getOrderTypeSelect().equals(BankOrderRepository.ORDER_TYPE_SEND_BANK_ORDER)) {
      bankOrder.setArithmeticTotal(bankOrder.getBankOrderTotalAmount());
    } else {
      bankOrder.setArithmeticTotal(this.computeBankOrderTotalAmount(bankOrder));
    }
    bankOrder.setBankOrderTotalAmount(bankOrder.getArithmeticTotal());
    bankOrder.setCompanyCurrencyTotalAmount(this.computeCompanyCurrencyTotalAmount(bankOrder));
  }

  @Override
  public BigDecimal computeBankOrderTotalAmount(BankOrder bankOrder) throws AxelorException {
    BigDecimal bankOrderTotalAmount = BigDecimal.ZERO;

    List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
    if (bankOrderLines != null) {
      for (BankOrderLine bankOrderLine : bankOrderLines) {
        BigDecimal amount = bankOrderLine.getBankOrderAmount();
        if (amount != null) {
          bankOrderTotalAmount = bankOrderTotalAmount.add(amount);
        }
      }
    }
    return bankOrderTotalAmount;
  }

  @Override
  public BigDecimal computeCompanyCurrencyTotalAmount(BankOrder bankOrder) throws AxelorException {
    BigDecimal companyCurrencyTotalAmount = BigDecimal.ZERO;

    List<BankOrderLine> bankOrderLines = bankOrder.getBankOrderLineList();
    if (bankOrderLines != null) {
      for (BankOrderLine bankOrderLine : bankOrderLines) {
        bankOrderLine.setCompanyCurrencyAmount(
            BankOrderToolService.isMultiCurrency(bankOrder)
                ? currencyService
                    .getAmountCurrencyConvertedAtDate(
                        bankOrder.getBankOrderCurrency(),
                        bankOrder.getCompanyCurrency(),
                        bankOrderLine.getBankOrderAmount(),
                        bankOrderLine.getBankOrderDate())
                    .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
                : bankOrderLine.getBankOrderAmount());
        BigDecimal amount = bankOrderLine.getCompanyCurrencyAmount();
        if (amount != null) {
          companyCurrencyTotalAmount = companyCurrencyTotalAmount.add(amount);
        }
      }
    }
    return companyCurrencyTotalAmount;
  }
}
