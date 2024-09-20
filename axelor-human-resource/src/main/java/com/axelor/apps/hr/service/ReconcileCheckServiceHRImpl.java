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
package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.reconcile.ReconcileCheckServiceImpl;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.google.inject.Inject;

public class ReconcileCheckServiceHRImpl extends ReconcileCheckServiceImpl {

  @Inject
  public ReconcileCheckServiceHRImpl(
      CurrencyScaleService currencyScaleService,
      InvoiceTermPfpService invoiceTermPfpService,
      InvoiceTermToolService invoiceTermToolService,
      MoveLineToolService moveLineToolService,
      CurrencyService currencyService) {
    super(
        currencyScaleService,
        invoiceTermPfpService,
        invoiceTermToolService,
        moveLineToolService,
        currencyService);
  }

  @Override
  protected boolean isMissingTax(MoveLine it) {
    return it.getMove().getExpense() == null && super.isMissingTax(it);
  }
}
