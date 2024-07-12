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
