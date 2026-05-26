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
package com.axelor.csv.script;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.note.InvoiceNoteService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.address.AddressService;
import com.google.inject.persist.Transactional;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportInvoice {

  protected final Logger LOG = LoggerFactory.getLogger(ImportInvoice.class);

  @Inject private AddressService addressService;

  @Inject private InvoiceService invoiceService;

  @Inject private InvoiceRepository invoiceRepo;

  @Inject private InvoiceTermService invoiceTermService;

  @Inject private InvoiceNoteService invoiceNoteService;

  @Transactional(rollbackOn = {Exception.class})
  public Object importInvoice(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof Invoice;
    Invoice invoice = (Invoice) bean;

    if (invoice.getAddress() != null) {
      invoice.setAddressStr(addressService.computeAddressStr(invoice.getAddress()));
    }
    invoiceRepo.save(invoice);
    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_DRAFT) {
      invoiceService.setDraftSequence(invoice);
    }
    if (invoice.getStatusSelect() < InvoiceRepository.STATUS_VENTILATED
        && invoice.getPaymentMode() != null
        && invoice.getInTaxTotal() != null
        && !invoiceTermService.checkIfCustomizedInvoiceTerms(invoice.getInvoiceTermList())) {
      invoice = invoiceTermService.computeInvoiceTerms(invoice);
    }

    if (invoice.getStatusSelect() >= InvoiceRepository.STATUS_VENTILATED
        && (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
            || invoice.getOperationTypeSelect()
                == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND)) {
      try {
        invoiceNoteService.generateInvoiceNote(invoice);
      } catch (Exception e) {
        LOG.error(
            "Failed to generate invoice notes for imported invoice {}", invoice.getInvoiceId(), e);
      }
    }

    return invoice;
  }
}
