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
package com.axelor.apps.account.service.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.AdvancePaymentMoveLineCreateService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceImplTest {

  private AccountConfigService accountConfigService;
  private PaymentServiceImpl service;
  private Company company;

  @BeforeEach
  void setUp() throws AxelorException {
    accountConfigService = mock(AccountConfigService.class);
    company = new Company();

    service =
        spy(
            new PaymentServiceImpl(
                mock(AppAccountService.class),
                mock(AppBaseService.class),
                mock(ReconcileService.class),
                mock(MoveLineCreateService.class),
                mock(CurrencyService.class),
                mock(MoveInvoiceTermService.class),
                mock(InvoicePaymentRepository.class),
                mock(AdvancePaymentMoveLineCreateService.class),
                accountConfigService));

    // Avoid the heavy reconcile work and the static TraceBackService call.
    doNothing().when(service).createReconcile(any(), any(), any(), any());
    doNothing().when(service).traceExcessPaymentReconcileLimitReached(any(), anyInt());
  }

  private void mockLimit(Integer limit) throws AxelorException {
    AccountConfig accountConfig = new AccountConfig();
    accountConfig.setExcessPaymentReconcileNumberLimit(limit);
    when(accountConfigService.getAccountConfig(company)).thenReturn(accountConfig);
  }

  private MoveLine moveLine(BigDecimal amountRemaining) {
    Move move = new Move();
    move.setCompany(company);
    MoveLine moveLine = mock(MoveLine.class);
    when(moveLine.getMove()).thenReturn(move);
    when(moveLine.getAmountRemaining()).thenReturn(amountRemaining);
    when(moveLine.getMaxAmountToReconcile()).thenReturn(BigDecimal.ZERO);
    return moveLine;
  }

  private List<MoveLine> moveLines(int count) {
    List<MoveLine> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(moveLine(new BigDecimal("100")));
    }
    return list;
  }

  @Test
  void useExcessPaymentOnMoveLines_capsReconciliationsAtLimit() throws AxelorException {
    mockLimit(2);

    service.useExcessPaymentOnMoveLines(moveLines(5), moveLines(1));

    verify(service, times(2)).createReconcile(any(), any(), any(), any());
    verify(service, times(1)).traceExcessPaymentReconcileLimitReached(any(), eq(2));
  }

  @Test
  void useExcessPaymentOnMoveLines_noLimitWhenNull() throws AxelorException {
    mockLimit(null);

    service.useExcessPaymentOnMoveLines(moveLines(3), moveLines(1));

    verify(service, times(3)).createReconcile(any(), any(), any(), any());
    verify(service, never()).traceExcessPaymentReconcileLimitReached(any(), anyInt());
  }

  @Test
  void useExcessPaymentOnMoveLines_belowLimitReconcilesAllWithoutMessage() throws AxelorException {
    mockLimit(5);

    service.useExcessPaymentOnMoveLines(moveLines(3), moveLines(1));

    verify(service, times(3)).createReconcile(any(), any(), any(), any());
    verify(service, never()).traceExcessPaymentReconcileLimitReached(any(), anyInt());
  }
}
