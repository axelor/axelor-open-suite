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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DepositSlipService {

  /**
   * Get payments into deposit slip.
   *
   * @param depositSlip
   * @return List of payment vouchers
   */
  List<PaymentVoucher> fetchPaymentVouchers(DepositSlip depositSlip);

  /**
   * Publish deposit slip.
   *
   * @param depositSlip
   * @return today date
   * @throws AxelorException
   */
  LocalDate publish(DepositSlip depositSlip) throws AxelorException;

  /**
   * Validate deposit slip using value for collection account.
   *
   * @param depositSlip
   * @throws AxelorException
   */
  void validate(DepositSlip depositSlip) throws AxelorException;

  List<Integer> getSelectedPaymentVoucherDueIdList(List<Map<String, Object>> paymentVoucherDueList);

  BigDecimal getTotalAmount(DepositSlip depositSlip, List<Integer> selectedPaymentVoucherDueIdList);

  void updateInvoicePayments(DepositSlip depositSlip, LocalDate depositDate);
}
