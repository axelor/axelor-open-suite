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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DepositSlipServiceImplTest {

  private DepositSlipServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new DepositSlipServiceImpl(
            mock(InvoicePaymentRepository.class),
            mock(PaymentVoucherRepository.class),
            mock(AccountConfigService.class),
            mock(PrintingTemplatePrintService.class));
  }

  @Test
  void removePaymentVoucher_detachesOnlyTheGivenVoucher() {
    DepositSlip depositSlip = new DepositSlip();
    depositSlip.setTotalAmount(new BigDecimal("110"));
    depositSlip.setChequeCount(2);
    PaymentVoucher kept = paymentVoucher(depositSlip, "60");
    PaymentVoucher removed = paymentVoucher(depositSlip, "50");

    service.removePaymentVoucher(depositSlip, removed);

    assertEquals(1, depositSlip.getPaymentVoucherList().size());
    assertSame(kept, depositSlip.getPaymentVoucherList().get(0));
    assertNull(removed.getDepositSlip());
    assertSame(depositSlip, kept.getDepositSlip());
    assertEquals(new BigDecimal("110"), depositSlip.getTotalAmount());
    assertEquals(2, depositSlip.getChequeCount());
  }

  @Test
  void removePaymentVoucher_onSlipWithoutList_doesNothing() {
    DepositSlip depositSlip = new DepositSlip();
    PaymentVoucher paymentVoucher = new PaymentVoucher();

    service.removePaymentVoucher(depositSlip, paymentVoucher);

    assertNull(depositSlip.getPaymentVoucherList());
    assertNull(paymentVoucher.getDepositSlip());
  }

  @Test
  void computeTotals_sumsPaidAmountsAndCountsVouchers() {
    DepositSlip depositSlip = new DepositSlip();
    paymentVoucher(depositSlip, "60");
    paymentVoucher(depositSlip, "50");

    service.computeTotals(depositSlip);

    assertEquals(0, new BigDecimal("110").compareTo(depositSlip.getTotalAmount()));
    assertEquals(2, depositSlip.getChequeCount());
  }

  @Test
  void computeTotals_onEmptyList_yieldsZero() {
    DepositSlip depositSlip = new DepositSlip();
    depositSlip.setTotalAmount(new BigDecimal("60"));
    depositSlip.setChequeCount(1);
    paymentVoucher(depositSlip, "60");
    depositSlip.clearPaymentVoucherList();

    service.computeTotals(depositSlip);

    assertEquals(0, BigDecimal.ZERO.compareTo(depositSlip.getTotalAmount()));
    assertEquals(0, depositSlip.getChequeCount());
  }

  @Test
  void computeTotals_onNullList_yieldsZero() {
    DepositSlip depositSlip = new DepositSlip();
    depositSlip.setTotalAmount(new BigDecimal("60"));
    depositSlip.setChequeCount(1);
    assertNull(depositSlip.getPaymentVoucherList());

    service.computeTotals(depositSlip);

    assertEquals(0, BigDecimal.ZERO.compareTo(depositSlip.getTotalAmount()));
    assertEquals(0, depositSlip.getChequeCount());
  }

  private PaymentVoucher paymentVoucher(DepositSlip depositSlip, String paidAmount) {
    PaymentVoucher paymentVoucher = new PaymentVoucher();
    paymentVoucher.setPaidAmount(new BigDecimal(paidAmount));
    depositSlip.addPaymentVoucherListItem(paymentVoucher);
    return paymentVoucher;
  }
}
