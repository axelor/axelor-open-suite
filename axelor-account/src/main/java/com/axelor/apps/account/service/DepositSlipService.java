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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.AxelorException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DepositSlipService {

  /**
   * Get payments into deposit slip.
   *
   * @param depositSlip
   * @throws AxelorException
   */
  List<PaymentVoucher> fetchPaymentVouchers(DepositSlip depositSlip);

  /**
   * Publish deposit slip.
   *
   * @param depositSlip
   * @return
   * @throws AxelorException
   * @throws IOException
   */
  LocalDate publish(DepositSlip depositSlip) throws AxelorException;

  /**
   * Validate deposit slip using value for collection account.
   *
   * @param depositSlip
   * @return
   * @throws AxelorException
   * @throws IOException
   */
  void validate(DepositSlip depositSlip) throws AxelorException;

  List<PaymentVoucher> getSelectedPaymentVoucherDueList(
      List<Map<String, Object>> paymentVoucherDueList);

  BigDecimal getTotalAmount(DepositSlip depositSlip, List<Integer> selectedPaymentVoucherDueIdList);

  void updateInvoicePayments(DepositSlip depositSlip, LocalDate depositDate);

  /**
   * Removes a payment voucher from the given deposit slip.
   *
   * <p>The voucher is taken out of {@link DepositSlip#getPaymentVoucherList()} and its {@code
   * depositSlip} reference is cleared. The slip totals are left untouched; call {@link
   * #computeTotals(DepositSlip)} afterwards to keep them consistent.
   *
   * @param depositSlip the deposit slip currently holding the voucher
   * @param paymentVoucher the payment voucher to detach
   */
  void removePaymentVoucher(DepositSlip depositSlip, PaymentVoucher paymentVoucher);

  /**
   * Recomputes the totals of a deposit slip from its payment voucher list.
   *
   * <p>Sets {@code totalAmount} to the sum of the vouchers' paid amounts and {@code chequeCount} to
   * the number of vouchers. An empty or {@code null} list yields zero for both.
   *
   * @param depositSlip the deposit slip whose totals are recomputed
   */
  void computeTotals(DepositSlip depositSlip);
}
