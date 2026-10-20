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
package com.axelor.apps.account.service.payment.invoice.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoicePaymentAlertServiceImplTest {

  private MoveReverseService moveReverseService;
  private InvoicePaymentAlertServiceImpl service;

  @BeforeEach
  void setUp() {
    moveReverseService = mock(MoveReverseService.class);
    service = new InvoicePaymentAlertServiceImpl(moveReverseService);
  }

  @Test
  void validateBeforeReverse_nullPayment_returnsEmptyWithoutCheck() throws AxelorException {
    assertEquals("", service.validateBeforeReverse(null));

    verifyNoInteractions(moveReverseService);
  }

  @Test
  void validateBeforeReverse_cancelledPayment_returnsEmptyWithoutCheck() throws AxelorException {
    InvoicePayment invoicePayment = invoicePayment(null);
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);

    assertEquals("", service.validateBeforeReverse(invoicePayment));

    verifyNoInteractions(moveReverseService);
  }

  @Test
  void validateBeforeReverse_blockedMove_propagatesTheError() throws AxelorException {
    InvoicePayment invoicePayment = invoicePayment(depositSlip(LocalDate.of(2026, 9, 1), true));
    doThrow(new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "blocked"))
        .when(moveReverseService)
        .checkBeforeReverse(invoicePayment.getMove());

    assertThrows(AxelorException.class, () -> service.validateBeforeReverse(invoicePayment));
  }

  @Test
  void validateBeforeReverse_publishedDepositSlip_returnsDepositAlert() throws AxelorException {
    InvoicePayment invoicePayment = invoicePayment(depositSlip(LocalDate.of(2026, 9, 1), false));

    String alert = service.validateBeforeReverse(invoicePayment);

    assertEquals(AccountExceptionMessage.INVOICE_PAYMENT_ALERT_VOUCHER_DEPOSIT_REVERSE, alert);
    verify(moveReverseService).checkBeforeReverse(invoicePayment.getMove());
  }

  @Test
  void validateBeforeReverse_draftDepositSlip_returnsDefaultAlert() throws AxelorException {
    InvoicePayment invoicePayment = invoicePayment(depositSlip(null, false));

    assertEquals(
        AccountExceptionMessage.INVOICE_PAYMENT_ALERT_DEFAULT_REVERSE,
        service.validateBeforeReverse(invoicePayment));
  }

  @Test
  void validateBeforeReverse_withoutDepositSlip_returnsDefaultAlert() throws AxelorException {
    assertEquals(
        AccountExceptionMessage.INVOICE_PAYMENT_ALERT_DEFAULT_REVERSE,
        service.validateBeforeReverse(invoicePayment(null)));
  }

  private InvoicePayment invoicePayment(DepositSlip depositSlip) {
    PaymentVoucher paymentVoucher = new PaymentVoucher();
    paymentVoucher.setDepositSlip(depositSlip);
    Move move = new Move();
    move.setPaymentVoucher(paymentVoucher);
    InvoicePayment invoicePayment = new InvoicePayment();
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    invoicePayment.setMove(move);
    return invoicePayment;
  }

  private DepositSlip depositSlip(LocalDate publicationDate, boolean bankDepositMoveGenerated) {
    DepositSlip depositSlip = new DepositSlip();
    depositSlip.setPublicationDate(publicationDate);
    depositSlip.setIsBankDepositMoveGenerated(bankDepositMoveGenerated);
    return depositSlip;
  }
}
