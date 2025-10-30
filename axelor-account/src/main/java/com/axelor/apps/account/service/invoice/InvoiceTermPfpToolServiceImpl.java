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

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermPfpToolServiceImpl implements InvoiceTermPfpToolService {

  @Override
  public Integer checkOtherInvoiceTerms(List<InvoiceTerm> invoiceTermList) {
    if (CollectionUtils.isEmpty(invoiceTermList)) {
      return null;
    }

    InvoiceTerm firstInvoiceTerm = invoiceTermList.get(0);
    int pfpStatus = getPfpValidateStatusSelect(firstInvoiceTerm);
    int otherPfpStatus;
    for (InvoiceTerm otherInvoiceTerm : invoiceTermList) {
      if (otherInvoiceTerm.getId() != null
          && firstInvoiceTerm.getId() != null
          && !otherInvoiceTerm.getId().equals(firstInvoiceTerm.getId())) {
        otherPfpStatus = getPfpValidateStatusSelect(otherInvoiceTerm);

        if (otherPfpStatus != pfpStatus) {
          pfpStatus = InvoiceTermRepository.PFP_STATUS_AWAITING;
          break;
        }
      }
    }
    return pfpStatus;
  }

  protected int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getPfpValidateStatusSelect()
        == InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED) {
      return InvoiceTermRepository.PFP_STATUS_VALIDATED;
    } else {
      return invoiceTerm.getPfpValidateStatusSelect();
    }
  }
}
