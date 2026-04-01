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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceVatLiabilityServiceImpl implements InvoiceVatLiabilityService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected TaxAccountToolService taxAccountToolService;

  @Inject
  public InvoiceVatLiabilityServiceImpl(TaxAccountToolService taxAccountToolService) {
    this.taxAccountToolService = taxAccountToolService;
  }

  @Override
  public Integer computeVatLiability(Invoice invoice) {
    Partner partner = invoice.getPartner();
    Company company = invoice.getCompany();

    if (partner == null || company == null) {
      return null;
    }

    int operationType = invoice.getOperationTypeSelect();
    boolean isExpense =
        operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
    boolean isSale =
        operationType == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
            || operationType == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;

    try {
      return taxAccountToolService.resolveVatLiabilityFromAccountingSituation(
          partner, company, null, isExpense, isSale);
    } catch (AxelorException e) {
      LOG.warn(
          "Could not resolve VAT liability for partner {} and company {}: {}",
          partner.getFullName(),
          company.getName(),
          e.getMessage());
      return null;
    }
  }
}
