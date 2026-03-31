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
package com.axelor.apps.account.service.note;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoiceNoteServiceImpl implements InvoiceNoteService {

  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceNoteServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateInvoiceNote(Invoice invoice) throws AxelorException {
    Company company = invoice.getCompany();

    InvoiceNoteCreationHelper.generateGeneralInformationNote(invoice, company);

    InvoiceNoteCreationHelper.generateSellerLegalInformationNote(invoice, company);

    InvoiceNoteCreationHelper.generateLumpSumIndemnityNote(invoice, company);

    InvoiceNoteCreationHelper.generateLateInterestChargesNote(invoice, company);

    InvoiceNoteCreationHelper.generateLegalFormAndCapitalNote(invoice, company);

    InvoiceNoteCreationHelper.generateSupplierNote(invoice);

    InvoiceNoteCreationHelper.generateFinancialDiscountNote(invoice, invoice.getCompany());
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateInvoiceCategoryNote(Invoice invoice) throws AxelorException {
    clearExistingCategoryNotes(invoice);
    InvoiceNoteCreationHelper.generateInvoiceCategoryNote(invoice);
  }

  protected void clearExistingCategoryNotes(Invoice invoice) {
    if (invoice.getInvoiceNoteList() == null) {
      return;
    }
    InvoiceNoteType noteTypeREG =
        InvoiceNoteCreationHelper.getOrCreateInvoiceNoteType(
            "REG", I18n.get("Regulatory information"));
    invoice.getInvoiceNoteList().removeIf(n -> noteTypeREG.equals(n.getInvoiceNoteType()));
  }
}
