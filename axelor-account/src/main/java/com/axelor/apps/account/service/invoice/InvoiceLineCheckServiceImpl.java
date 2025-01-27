package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceLineCheckServiceImpl implements InvoiceLineCheckService {

  protected TaxAccountService taxAccountService;

  @Inject
  public InvoiceLineCheckServiceImpl(TaxAccountService taxAccountService) {
    this.taxAccountService = taxAccountService;
  }

  public void checkTaxLinesNotOnlyNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    // split in for loop, catch the exception, and throw another exception with the specific account
    taxAccountService.checkTaxLinesNotOnlyNonDeductibleTaxes(
        invoiceLineList.stream()
            .filter(invoiceLine -> invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_NORMAL)
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkSumOfNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    taxAccountService.checkSumOfNonDeductibleTaxes(
        invoiceLineList.stream()
            .filter(invoiceLine -> invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_NORMAL)
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }
}
