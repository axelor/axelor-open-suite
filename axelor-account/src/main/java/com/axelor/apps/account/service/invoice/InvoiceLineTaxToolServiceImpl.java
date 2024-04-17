package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class InvoiceLineTaxToolServiceImpl implements InvoiceLineTaxToolService {

  protected AccountManagementService accountManagementService;

  @Inject
  public InvoiceLineTaxToolServiceImpl(AccountManagementService accountManagementService) {
    this.accountManagementService = accountManagementService;
  }

  @Override
  public List<Pair<InvoiceLineTax, Account>> getInvoiceLineTaxAccountPair(
      List<InvoiceLineTax> invoiceLineTaxList, Company company) {
    List<Pair<InvoiceLineTax, Account>> invoiceLineTaxAccountPair = new ArrayList<>();
    if (ObjectUtils.isEmpty(invoiceLineTaxList)) {
      return invoiceLineTaxAccountPair;
    }

    for (InvoiceLineTax invoiceLineTax : invoiceLineTaxList) {
      List<AccountManagement> accountManagementList =
          Optional.of(invoiceLineTax)
              .map(InvoiceLineTax::getTaxLine)
              .map(TaxLine::getTax)
              .map(Tax::getAccountManagementList)
              .orElse(new ArrayList<>());
      if (invoiceLineTax.getImputedAccount() != null
          && !ObjectUtils.isEmpty(accountManagementList)) {
        AccountManagement accountManagement =
            accountManagementService.getAccountManagement(accountManagementList, company);
        if (accountManagement != null && accountManagement.getVatPendingAccount() != null) {
          Account vatPendingAccount = accountManagement.getVatPendingAccount();
          invoiceLineTaxAccountPair.add(Pair.of(invoiceLineTax, vatPendingAccount));
        }
      }
    }
    return invoiceLineTaxAccountPair;
  }
}
