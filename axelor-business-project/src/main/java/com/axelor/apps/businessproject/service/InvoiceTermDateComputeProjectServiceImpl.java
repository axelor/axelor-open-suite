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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermDateComputeServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class InvoiceTermDateComputeProjectServiceImpl extends InvoiceTermDateComputeServiceImpl {

  @Inject
  public InvoiceTermDateComputeProjectServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      AppBaseService appBaseService) {
    super(appAccountService, invoiceTermFinancialDiscountService, appBaseService);
  }

  @Override
  public void computeDueDateValues(InvoiceTerm invoiceTerm, LocalDate invoiceDate) {
    Optional<LocalDate> projectToDate =
        Optional.of(invoiceTerm)
            .map(InvoiceTerm::getInvoice)
            .map(Invoice::getProject)
            .map(Project::getToDate)
            .map(LocalDateTime::toLocalDate);
    if (projectToDate.isPresent() && invoiceTerm.getIsHoldBack()) {
      invoiceDate = projectToDate.get();
    }
    super.computeDueDateValues(invoiceTerm, invoiceDate);
  }
}
