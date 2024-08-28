package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.shiro.util.CollectionUtils;

public class InvoiceValidationServiceImpl implements InvoiceValidationService {
  protected TaxService taxService;

  @Inject
  public InvoiceValidationServiceImpl(TaxService taxService) {
    this.taxService = taxService;
  }

  @Override
  public void checkNotOnlyNonDeductibleTaxes(Invoice invoice) throws AxelorException {
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }
    for (InvoiceLine invoiceLine : invoiceLineList) {
      Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
      try {
        taxService.checkTaxLinesNotOnlyNonDeductibleTaxes(taxLineSet);
      } catch (AxelorException e) {
        String productFullName =
            Optional.of(invoiceLine)
                .map(InvoiceLine::getProduct)
                .map(Product::getFullName)
                .orElse(null);
        String accountLabel =
            Optional.of(invoiceLine)
                .map(InvoiceLine::getAccount)
                .map(Account::getLabel)
                .orElse(null);
        String s;
        if (productFullName != null && accountLabel != null) {
          s = productFullName + "/" + accountLabel;
        } else if (productFullName == null && accountLabel != null) {
          s = accountLabel;
        } else if (productFullName != null && accountLabel == null) {
          s = productFullName;
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR2));
        }
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR1),
            s);
      }
    }
  }
}
