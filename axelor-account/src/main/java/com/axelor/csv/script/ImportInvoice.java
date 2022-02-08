/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.service.AddressService;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.Map;
import javax.inject.Inject;

public class ImportInvoice {

  @Inject private AddressService addressService;

  @Inject private InvoiceService invoiceService;

  @Inject private InvoiceRepository invoiceRepo;

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
    return invoice;
  }
}
