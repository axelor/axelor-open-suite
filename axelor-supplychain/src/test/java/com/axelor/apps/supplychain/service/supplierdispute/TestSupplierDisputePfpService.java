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
package com.axelor.apps.supplychain.service.supplierdispute;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.auth.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestSupplierDisputePfpService {

  protected SupplierDisputePfpService supplierDisputePfpService;
  protected InvoiceVisibilityService invoiceVisibilityService;
  protected User user;

  @BeforeEach
  void setUp() throws AxelorException {
    invoiceVisibilityService = mock(InvoiceVisibilityService.class);
    when(invoiceVisibilityService.isPfpButtonVisible(any(Invoice.class), any(), eq(false)))
        .thenReturn(true);
    user = new User();
    supplierDisputePfpService = new SupplierDisputePfpServiceImpl(invoiceVisibilityService);
  }

  @Test
  void testCanSwitchWithoutInvoiceReturnsFalse() throws AxelorException {
    SupplierDispute supplierDispute =
        createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN, null);

    assertFalse(supplierDisputePfpService.canSwitchInvoiceToLitigation(supplierDispute, user));
    verifyNoInteractions(invoiceVisibilityService);
  }

  @ParameterizedTest
  @ValueSource(
      ints = {
        SupplierDisputeRepository.STATUS_OPEN,
        SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION,
        SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE,
        SupplierDisputeRepository.STATUS_RESOLVED
      })
  void testCanSwitchWhileNotTerminalReturnsTrue(int statusSelect) throws AxelorException {
    SupplierDispute supplierDispute = createSupplierDispute(statusSelect, new Invoice());

    assertTrue(supplierDisputePfpService.canSwitchInvoiceToLitigation(supplierDispute, user));
  }

  @ParameterizedTest
  @ValueSource(
      ints = {SupplierDisputeRepository.STATUS_CLOSED, SupplierDisputeRepository.STATUS_CANCELLED})
  void testCanSwitchWhenTerminalReturnsFalse(int statusSelect) throws AxelorException {
    SupplierDispute supplierDispute = createSupplierDispute(statusSelect, new Invoice());

    assertFalse(supplierDisputePfpService.canSwitchInvoiceToLitigation(supplierDispute, user));
  }

  @Test
  void testCanSwitchWhenPfpButtonHiddenReturnsFalse() throws AxelorException {
    when(invoiceVisibilityService.isPfpButtonVisible(any(Invoice.class), any(), eq(false)))
        .thenReturn(false);
    SupplierDispute supplierDispute =
        createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN, new Invoice());

    assertFalse(supplierDisputePfpService.canSwitchInvoiceToLitigation(supplierDispute, user));
  }

  protected SupplierDispute createSupplierDispute(int statusSelect, Invoice invoice) {
    SupplierDispute supplierDispute = new SupplierDispute();
    supplierDispute.setStatusSelect(statusSelect);
    supplierDispute.setInvoice(invoice);
    return supplierDispute;
  }
}
