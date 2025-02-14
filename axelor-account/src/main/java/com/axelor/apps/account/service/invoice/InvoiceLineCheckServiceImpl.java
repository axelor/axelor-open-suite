package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
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
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkSumOfNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    taxAccountService.checkSumOfNonDeductibleTaxesOnTaxLines(
        invoiceLineList.stream()
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkInvoiceLineTaxes(Set<TaxLine> taxLineSet) throws AxelorException {
    if (ObjectUtils.notEmpty(taxLineSet) && taxAccountService.isNonDeductibleTaxesSet(taxLineSet)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage.INVOICE_LINE_PRODUCT_WITH_NON_DEDUCTIBLE_TAX_NOT_AUTHORIZED));
    }
  }
}
