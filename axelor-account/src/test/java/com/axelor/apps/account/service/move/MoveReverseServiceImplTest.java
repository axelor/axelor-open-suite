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
package com.axelor.apps.account.service.move;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCancelService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.account.service.reconcile.UnreconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveReverseServiceImplTest {

  private static final String BLOCKED_MESSAGE = "Voucher included in a validated deposit slip";

  private MoveCreateService moveCreateService;
  private MoveRepository moveRepository;
  private PaymentVoucherCancelService paymentVoucherCancelService;
  private MoveReverseServiceImpl service;

  @BeforeEach
  void setUp() {
    moveCreateService = mock(MoveCreateService.class);
    moveRepository = mock(MoveRepository.class);
    paymentVoucherCancelService = mock(PaymentVoucherCancelService.class);
    service =
        new MoveReverseServiceImpl(
            moveCreateService,
            mock(ReconcileService.class),
            mock(MoveValidateService.class),
            moveRepository,
            mock(MoveLineCreateService.class),
            mock(ExtractContextMoveService.class),
            mock(InvoicePaymentRepository.class),
            mock(InvoicePaymentCancelService.class),
            mock(MoveLineToolService.class),
            mock(UnreconcileService.class),
            mock(MoveInvoiceTermService.class),
            mock(AnalyticLineService.class),
            paymentVoucherCancelService);
  }

  @Test
  void checkBeforeReverse_nullMove_doesNothing() throws AxelorException {
    service.checkBeforeReverse(null);

    verifyNoInteractions(paymentVoucherCancelService);
  }

  @Test
  void checkBeforeReverse_moveWithoutPaymentVoucher_doesNotCallCancelService()
      throws AxelorException {
    service.checkBeforeReverse(new Move());

    verifyNoInteractions(paymentVoucherCancelService);
  }

  @Test
  void checkBeforeReverse_moveWithPaymentVoucher_delegatesToCancelService() throws AxelorException {
    PaymentVoucher paymentVoucher = new PaymentVoucher();
    Move move = new Move();
    move.setPaymentVoucher(paymentVoucher);

    service.checkBeforeReverse(move);

    verify(paymentVoucherCancelService).checkDepositSlip(paymentVoucher);
  }

  @Test
  void generateReverse_withAssistantMap_blockedPaymentVoucher_throwsBeforeAnyWrite()
      throws AxelorException {
    Move move = moveWithBlockedPaymentVoucher("PAY-1");

    assertThrows(AxelorException.class, () -> service.generateReverse(move, new HashMap<>()));

    verifyNoInteractions(moveCreateService, moveRepository);
  }

  @Test
  void massReverse_blockedPaymentVoucher_reportsMoveInCombinedError() throws AxelorException {
    Move move = moveWithBlockedPaymentVoucher("PAY-1");
    Map<String, Object> assistantMap = new HashMap<>();
    assistantMap.put("isAutomaticReconcile", false);
    assistantMap.put("isAutomaticAccounting", false);
    assistantMap.put("isUnreconcileOriginalMove", false);
    assistantMap.put("dateOfReversionSelect", MoveRepository.DATE_OF_REVERSION_CHOOSE_DATE);
    assistantMap.put("dateOfReversion", LocalDate.of(2026, 10, 1));

    AxelorException exception =
        assertThrows(AxelorException.class, () -> service.massReverse(List.of(move), assistantMap));

    assertTrue(exception.getMessage().contains("PAY-1"));
    assertTrue(exception.getMessage().contains(BLOCKED_MESSAGE));
    verifyNoInteractions(moveCreateService, moveRepository);
  }

  private Move moveWithBlockedPaymentVoucher(String reference) throws AxelorException {
    PaymentVoucher paymentVoucher = new PaymentVoucher();
    Move move = new Move();
    move.setReference(reference);
    move.setPaymentVoucher(paymentVoucher);
    doThrow(new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, BLOCKED_MESSAGE))
        .when(paymentVoucherCancelService)
        .checkDepositSlip(paymentVoucher);
    return move;
  }
}
