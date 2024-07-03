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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class InvoiceJournalServiceImpl implements InvoiceJournalService {
  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceJournalServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  public Journal getJournal(Invoice invoice) throws AxelorException {
    Company company = invoice.getCompany();
    if (company == null) return null;

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Taken from legacy JournalService but negative cases seem rather strange
    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getSupplierCreditNoteJournal(accountConfig)
            : accountConfigService.getSupplierPurchaseJournal(accountConfig);
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getSupplierPurchaseJournal(accountConfig)
            : accountConfigService.getSupplierCreditNoteJournal(accountConfig);
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getCustomerCreditNoteJournal(accountConfig)
            : accountConfigService.getCustomerSalesJournal(accountConfig);
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        return invoice.getInTaxTotal().signum() < 0
            ? accountConfigService.getCustomerSalesJournal(accountConfig)
            : accountConfigService.getCustomerCreditNoteJournal(accountConfig);
      default:
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.JOURNAL_1),
            invoice.getInvoiceId());
    }
  }
}
