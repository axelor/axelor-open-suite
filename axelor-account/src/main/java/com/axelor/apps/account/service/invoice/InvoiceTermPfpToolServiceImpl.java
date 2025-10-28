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
