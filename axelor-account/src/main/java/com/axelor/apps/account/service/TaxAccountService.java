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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class TaxAccountService {

  protected AccountManagementAccountService accountManagementAccountService;

  @Inject
  public TaxAccountService(AccountManagementAccountService accountManagementAccountService) {
    this.accountManagementAccountService = accountManagementAccountService;
  }

  public Account getAccount(
      Tax tax,
      Company company,
      Journal journal,
      Account originalAccount,
      int vatSystemSelect,
      boolean isFixedAssets,
      int functionalOrigin)
      throws AxelorException {

    AccountManagement accountManagement = this.getTaxAccount(tax, company);

    return accountManagementAccountService.getTaxAccount(
        accountManagement,
        tax,
        company,
        journal,
        originalAccount,
        vatSystemSelect,
        functionalOrigin,
        isFixedAssets);
  }

  protected AccountManagement getTaxAccount(Tax tax, Company company) {

    if (tax != null && tax.getAccountManagementList() != null) {

      for (AccountManagement accountManagement : tax.getAccountManagementList()) {

        if (accountManagement.getCompany().equals(company)) {
          return accountManagement;
        }
      }
    }

    return null;
  }

  public Account getVatRegulationAccount(Tax tax, Company company, boolean isPurchase)
      throws AxelorException {
    AccountManagement accountManagement = this.getTaxAccount(tax, company);

    if (accountManagement == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_TAX_CONFIG_MISSING),
          tax.getCode(),
          company.getCode());
    } else if (isPurchase) {
      return accountManagementAccountService.getPurchVatRegulationAccount(
          accountManagement, tax, company);
    } else {
      return accountManagementAccountService.getSaleVatRegulationAccount(
          accountManagement, tax, company);
    }
  }

  /**
   * TaxLines contain taxes, this method reuses checkTaxesNotOnlyNonDeductibleTaxes(Set<Tax> taxes)
   *
   * @param taxLines
   * @return
   */
  public boolean checkTaxLinesNotOnlyNonDeductibleTaxes(Set<TaxLine> taxLines) {
    if (taxLines == null || taxLines.isEmpty()) {
      return true;
    }
    Set<Tax> taxes = new HashSet<>();
    for (TaxLine taxLine : taxLines) {
      taxes.add(taxLine.getTax());
    }
    return checkTaxesNotOnlyNonDeductibleTaxes(taxes);
  }

  public boolean checkTaxesNotOnlyNonDeductibleTaxes(Set<Tax> taxes) {
    if (taxes == null || taxes.isEmpty()) {
      return true;
    }
    int countDeductibleTaxes = 0;
    int countNonDeductibleTaxes = 0;
    for (Tax tax : taxes) {
      Boolean isNonDeductibleTax = tax.getIsNonDeductibleTax();
      if (isNonDeductibleTax) {
        countNonDeductibleTaxes++;
      } else {
        countDeductibleTaxes++;
      }
    }
    if (countDeductibleTaxes == 0 && countNonDeductibleTaxes > 0) {
      return false;
    }
    return true;
  }
}
