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
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePfpValidateServiceImpl implements InvoicePfpValidateService {

  protected InvoiceTermPfpValidateService invoiceTermPfpValidateService;
  protected AppBaseService appBaseService;
  protected InvoiceRepository invoiceRepository;

  @Inject
  public InvoicePfpValidateServiceImpl(
      InvoiceTermPfpValidateService invoiceTermPfpValidateService,
      AppBaseService appBaseService,
      InvoiceRepository invoiceRepository) {
    this.invoiceTermPfpValidateService = invoiceTermPfpValidateService;
    this.appBaseService = appBaseService;
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  @Transactional
  public void validatePfp(Long invoiceId) {
    Invoice invoice = invoiceRepository.find(invoiceId);
    User pfpValidatorUser =
        invoice.getPfpValidatorUser() != null ? invoice.getPfpValidatorUser() : AuthUtils.getUser();

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTermPfpValidateService.validatePfp(invoiceTerm, pfpValidatorUser);
    }

    invoice.setPfpValidatorUser(pfpValidatorUser);
    invoice.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_VALIDATED);
    invoice.setDecisionPfpTakenDateTime(
        appBaseService.getTodayDateTime(invoice.getCompany()).toLocalDateTime());
  }
}
