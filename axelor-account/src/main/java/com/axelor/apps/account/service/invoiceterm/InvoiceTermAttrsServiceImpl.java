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
package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InvoiceTermAttrsServiceImpl implements InvoiceTermAttrsService {

  protected InvoiceTermService invoiceTermService;
  protected MoveToolService moveToolService;

  @Inject
  public void InvoiceTermAttrsServiceImpl(
      InvoiceTermService invoiceTermService, MoveToolService moveToolService) {
    this.invoiceTermService = invoiceTermService;
    this.moveToolService = moveToolService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void hideActionAndPfpPanel(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    boolean getHidden = getActionPanelHidden(invoiceTerm);

    this.addAttr("actionPanel", "hidden", getHidden, attrsMap);
    this.addAttr("pfpPanel", "hidden", getHidden, attrsMap);
  }

  protected boolean getActionPanelHidden(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();
      return invoiceTerm.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_NO_PFP
          || !(Arrays.asList(
                  InvoiceRepository.STATUS_VALIDATED,
                  InvoiceRepository.STATUS_VENTILATED,
                  InvoiceRepository.STATUS_CANCELED)
              .contains(invoice.getStatusSelect()))
          || (invoice.getOriginDate() == null
              && Arrays.asList(
                      InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
                      InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)
                  .contains(invoice.getOperationTypeSelect())
              && (Arrays.asList(InvoiceRepository.STATUS_DRAFT, InvoiceRepository.STATUS_VALIDATED)
                      .contains(invoice.getStatusSelect())
                  || invoice.getOperationSubTypeSelect()
                      == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE));
    }
    Move move =
        Optional.of(invoiceTerm).map(InvoiceTerm::getMoveLine).map(MoveLine::getMove).orElse(null);
    if (move != null) {
      return invoiceTerm.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_NO_PFP
          || !(Arrays.asList(
                  MoveRepository.STATUS_DAYBOOK,
                  MoveRepository.STATUS_ACCOUNTED,
                  MoveRepository.STATUS_CANCELED)
              .contains(move.getStatusSelect()));
    }
    return true;
  }

  @Override
  public void changeAmountsTitle(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    Invoice invoice = invoiceTerm.getInvoice();
    Move move =
        Optional.ofNullable(invoiceTerm)
            .map(InvoiceTerm::getMoveLine)
            .map(MoveLine::getMove)
            .orElse(null);

    if (InvoiceToolService.isMultiCurrency(invoice)
        || (invoice == null && moveToolService.isMultiCurrency(move))) {

      this.addAttr("amount", "title", I18n.get("Amount in currency"), attrsMap);
      this.addAttr("amountRemaining", "title", I18n.get("Amount remaining in currency"), attrsMap);
    }
  }

  @Override
  public void setPfpValidatorUserDomainAttrsMap(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {

    String domain = "self.id in (0)";

    if (invoiceTerm.getCompany() != null) {
      domain =
          invoiceTermService.getPfpValidatorUserDomain(
              invoiceTerm.getPartner(), invoiceTerm.getCompany());
    }

    this.addAttr("pfpValidatorUser", "domain", domain, attrsMap);
  }
}
