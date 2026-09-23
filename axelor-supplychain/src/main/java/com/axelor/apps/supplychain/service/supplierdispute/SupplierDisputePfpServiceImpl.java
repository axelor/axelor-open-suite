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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.auth.db.User;
import jakarta.inject.Inject;

public class SupplierDisputePfpServiceImpl implements SupplierDisputePfpService {

  protected InvoiceVisibilityService invoiceVisibilityService;

  @Inject
  public SupplierDisputePfpServiceImpl(InvoiceVisibilityService invoiceVisibilityService) {
    this.invoiceVisibilityService = invoiceVisibilityService;
  }

  @Override
  public boolean canSwitchInvoiceToLitigation(SupplierDispute supplierDispute, User user)
      throws AxelorException {
    Invoice invoice = supplierDispute.getInvoice();
    return invoice != null
        && supplierDispute.getStatusSelect() < SupplierDisputeRepository.STATUS_CLOSED
        && invoiceVisibilityService.isPfpButtonVisible(invoice, user, false);
  }
}
