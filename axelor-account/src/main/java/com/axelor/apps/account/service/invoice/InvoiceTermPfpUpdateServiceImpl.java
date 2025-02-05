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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.move.MovePfpValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

public class InvoiceTermPfpUpdateServiceImpl implements InvoiceTermPfpUpdateService {

  protected InvoiceTermPfpToolService invoiceTermPfpToolService;
  protected InvoiceTermPfpValidateService invoiceTermPfpValidateService;
  protected InvoiceTermPfpService invoiceTermPfpService;
  protected MovePfpValidateService movePfpValidateService;
  protected InvoicePfpValidateService invoicePfpValidateService;

  @Inject
  public InvoiceTermPfpUpdateServiceImpl(
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermPfpValidateService invoiceTermPfpValidateService,
      InvoiceTermPfpService invoiceTermPfpService,
      MovePfpValidateService movePfpValidateService,
      InvoicePfpValidateService invoicePfpValidateService) {
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.invoiceTermPfpValidateService = invoiceTermPfpValidateService;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.movePfpValidateService = movePfpValidateService;
    this.invoicePfpValidateService = invoicePfpValidateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePfp(
      InvoiceTerm invoiceTerm, Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException {
    int pfpValidateStatusSelect;
    if (MapUtils.isEmpty(invoiceTermPfpValidateStatusSelectMap)) {
      pfpValidateStatusSelect = invoiceTerm.getPfpValidateStatusSelect();
    } else {
      pfpValidateStatusSelect = invoiceTermPfpValidateStatusSelectMap.get(invoiceTerm);
    }

    if (invoiceTermPfpToolService.getAlreadyValidatedStatusList().contains(pfpValidateStatusSelect)
        || pfpValidateStatusSelect == InvoiceTermRepository.PFP_STATUS_LITIGATION) {
      return;
    }

    invoiceTermPfpValidateStatusSelectMap.remove(invoiceTerm);

    if (invoiceTerm.getAmountRemaining().signum() > 0) {
      BigDecimal grantedAmount = invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining());

      invoiceTermPfpValidateService.initPftPartialValidation(invoiceTerm, grantedAmount, null);
      invoiceTermPfpService.createPfpInvoiceTerm(
          invoiceTerm, invoiceTerm.getInvoice(), grantedAmount);
    } else {
      invoiceTermPfpValidateService.validatePfp(invoiceTerm, AuthUtils.getUser());
    }

    Invoice invoice = invoiceTerm.getInvoice();
    if (invoice != null
        && invoice.getInvoiceTermList().stream()
            .allMatch(
                it ->
                    invoiceTermPfpToolService.getPfpValidateStatusSelect(it)
                        == InvoiceTermRepository.PFP_STATUS_VALIDATED)) {
      invoicePfpValidateService.validatePfp(invoice.getId());
    }

    Move move = invoiceTerm.getMoveLine().getMove();
    if (move.getMoveLineList().stream()
        .allMatch(
            ml ->
                CollectionUtils.isEmpty(ml.getInvoiceTermList())
                    || ml.getInvoiceTermList().stream()
                        .allMatch(
                            it ->
                                invoiceTermPfpToolService.getPfpValidateStatusSelect(it)
                                    == InvoiceTermRepository.PFP_STATUS_VALIDATED))) {
      movePfpValidateService.validatePfp(move.getId());
    }
  }
}
