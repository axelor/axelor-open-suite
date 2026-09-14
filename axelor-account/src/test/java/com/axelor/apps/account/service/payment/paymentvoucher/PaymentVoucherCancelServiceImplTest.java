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
package com.axelor.apps.account.service.payment.paymentvoucher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.DepositSlipService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class PaymentVoucherCancelServiceImplTest {

  private DepositSlipService depositSlipService;
  private PaymentVoucherCancelServiceImpl service;

  @BeforeEach
  void setUp() {
    depositSlipService = mock(DepositSlipService.class);
    service = new PaymentVoucherCancelServiceImpl(depositSlipService);
  }

  @Test
  void cancelPaymentVoucher_withoutDepositSlip_onlyCancels() {
    PaymentVoucher paymentVoucher = confirmedPaymentVoucher();

    PaymentVoucher result = service.cancelPaymentVoucher(paymentVoucher);

    assertSame(paymentVoucher, result);
    assertEquals(PaymentVoucherRepository.STATUS_CANCELED, result.getStatusSelect());
    verifyNoInteractions(depositSlipService);
  }

  @Test
  void cancelPaymentVoucher_inDraftDepositSlip_removesThenRecomputesTotals() {
    DepositSlip depositSlip = depositSlip(null, false);
    PaymentVoucher paymentVoucher = confirmedPaymentVoucher();
    paymentVoucher.setDepositSlip(depositSlip);

    service.cancelPaymentVoucher(paymentVoucher);

    assertEquals(PaymentVoucherRepository.STATUS_CANCELED, paymentVoucher.getStatusSelect());
    InOrder inOrder = inOrder(depositSlipService);
    inOrder.verify(depositSlipService).removePaymentVoucher(depositSlip, paymentVoucher);
    inOrder.verify(depositSlipService).computeTotals(depositSlip);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void cancelPaymentVoucher_inPublishedDepositSlip_removesThenRecomputesTotals() {
    DepositSlip depositSlip = depositSlip(LocalDate.of(2026, 9, 1), false);
    PaymentVoucher paymentVoucher = confirmedPaymentVoucher();
    paymentVoucher.setDepositSlip(depositSlip);

    service.cancelPaymentVoucher(paymentVoucher);

    assertEquals(PaymentVoucherRepository.STATUS_CANCELED, paymentVoucher.getStatusSelect());
    InOrder inOrder = inOrder(depositSlipService);
    inOrder.verify(depositSlipService).removePaymentVoucher(depositSlip, paymentVoucher);
    inOrder.verify(depositSlipService).computeTotals(depositSlip);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void cancelPaymentVoucher_inValidatedDepositSlip_keepsLinkAndCancels() {
    DepositSlip depositSlip = depositSlip(LocalDate.of(2026, 9, 1), true);
    PaymentVoucher paymentVoucher = confirmedPaymentVoucher();
    paymentVoucher.setDepositSlip(depositSlip);

    service.cancelPaymentVoucher(paymentVoucher);

    assertEquals(PaymentVoucherRepository.STATUS_CANCELED, paymentVoucher.getStatusSelect());
    assertSame(depositSlip, paymentVoucher.getDepositSlip());
    verifyNoInteractions(depositSlipService);
  }

  @Test
  void cancelPaymentVoucher_calledTwiceAfterDetach_doesNotTouchDepositSlipAgain() {
    PaymentVoucher paymentVoucher = confirmedPaymentVoucher();
    paymentVoucher.setDepositSlip(null);

    service.cancelPaymentVoucher(paymentVoucher);
    service.cancelPaymentVoucher(paymentVoucher);

    assertEquals(PaymentVoucherRepository.STATUS_CANCELED, paymentVoucher.getStatusSelect());
    verifyNoInteractions(depositSlipService);
  }

  private PaymentVoucher confirmedPaymentVoucher() {
    PaymentVoucher paymentVoucher = new PaymentVoucher();
    paymentVoucher.setStatusSelect(PaymentVoucherRepository.STATUS_CONFIRMED);
    return paymentVoucher;
  }

  private DepositSlip depositSlip(LocalDate publicationDate, boolean bankDepositMoveGenerated) {
    DepositSlip depositSlip = new DepositSlip();
    depositSlip.setPublicationDate(publicationDate);
    depositSlip.setIsBankDepositMoveGenerated(bankDepositMoveGenerated);
    return depositSlip;
  }
}
