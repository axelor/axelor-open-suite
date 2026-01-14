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
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import java.util.List;
import java.util.Objects;

public class InvoiceTermPfpValidatorSyncServiceImpl implements InvoiceTermPfpValidatorSyncService {

  @Override
  public void syncPfpValidatorFromInvoiceToTerms(Invoice invoice) {
    if (invoice == null || ObjectUtils.isEmpty(invoice.getInvoiceTermList())) {
      return;
    }

    User pfpValidatorUser = invoice.getPfpValidatorUser();

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTerm.setPfpValidatorUser(pfpValidatorUser);
    }
  }

  @Override
  public boolean syncPfpValidatorFromTermToInvoice(InvoiceTerm invoiceTerm) {
    if (invoiceTerm == null || invoiceTerm.getInvoice() == null) {
      return false;
    }

    Invoice invoice = invoiceTerm.getInvoice();
    List<InvoiceTerm> invoiceTermList = invoice.getInvoiceTermList();

    if (ObjectUtils.isEmpty(invoiceTermList)) {
      return false;
    }

    if (invoiceTermList.size() == 1) {
      invoice.setPfpValidatorUser(invoiceTerm.getPfpValidatorUser());
      return true;
    }

    User commonValidator = getCommonPfpValidatorUser(invoiceTermList);

    invoice.setPfpValidatorUser(commonValidator);
    return true;
  }

  protected User getCommonPfpValidatorUser(List<InvoiceTerm> invoiceTermList) {
    if (ObjectUtils.isEmpty(invoiceTermList)) {
      return null;
    }

    User firstValidator = invoiceTermList.get(0).getPfpValidatorUser();

    for (int i = 1; i < invoiceTermList.size(); i++) {
      User currentValidator = invoiceTermList.get(i).getPfpValidatorUser();
      if (!Objects.equals(firstValidator, currentValidator)) {
        return null;
      }
    }

    return firstValidator;
  }
}
