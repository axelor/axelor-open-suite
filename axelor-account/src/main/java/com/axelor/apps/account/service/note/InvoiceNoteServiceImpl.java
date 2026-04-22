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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceNoteTypeRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceNoteServiceImpl implements InvoiceNoteService {

  protected final AccountConfigService accountConfigService;
  protected final InvoiceNoteTypeRepository invoiceNoteTypeRepository;
  protected final Logger LOG = LoggerFactory.getLogger(InvoiceNoteServiceImpl.class);

  @Inject
  public InvoiceNoteServiceImpl(
      AccountConfigService accountConfigService,
      InvoiceNoteTypeRepository invoiceNoteTypeRepository) {
    this.accountConfigService = accountConfigService;
    this.invoiceNoteTypeRepository = invoiceNoteTypeRepository;
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

    InvoiceNoteCreationHelper.generateInvoiceCategoryNote(invoice);
  }
}
