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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.google.inject.Inject;

public class InvoiceDomainServiceImpl implements InvoiceDomainService {

  protected InvoiceService invoiceService;

  @Inject
  public InvoiceDomainServiceImpl(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @Override
  public String getPartnerBaseDomain(Company company, Invoice invoice, int invoiceTypeSelect) {
    long companyId = company.getPartner() == null ? 0 : company.getPartner().getId();
    String domain =
        String.format(
            "self.id != %d "
                + "AND self.isContact = false "
                + "AND :company member of self.companySet",
            companyId);

    if (invoiceTypeSelect == PriceListRepository.TYPE_SALE) {
      domain += " AND self.isCustomer = true ";
    } else {
      domain += " AND self.isSupplier = true ";
    }
    return domain;
  }
}
