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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class TaxAccountService extends TaxService {

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

  public void checkTaxLinesNotOnlyNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    // split in for loop, catch the exception, and throw another exception with the specific account
    this.checkTaxLinesNotOnlyNonDeductibleTaxes(
        invoiceLineList.stream()
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkTaxLinesNotOnlyNonDeductibleTaxes(Set<TaxLine> taxLines) throws AxelorException {
    if (ObjectUtils.isEmpty(taxLines)) {
      return;
    }

    if (!checkTaxesNotOnlyNonDeductibleTaxes(
        taxLines.stream().map(TaxLine::getTax).collect(Collectors.toList()))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR));
    }
  }

  protected boolean checkTaxesNotOnlyNonDeductibleTaxes(List<Tax> taxes) {
    if (ObjectUtils.isEmpty(taxes)) {
      return true;
    }

    return taxes.stream().anyMatch(tax -> !tax.getIsNonDeductibleTax());
  }

  public Set<TaxLine> getNotNonDeductibleTaxesSet(Set<TaxLine> taxesLineSet) {
    return taxesLineSet.stream()
        .filter(Objects::nonNull)
        .filter(it -> !ObjectUtils.isEmpty(it.getTax()) && !it.getTax().getIsNonDeductibleTax())
        .collect(Collectors.toSet());
  }

  public boolean isNonDeductibleTaxesSet(Set<TaxLine> taxesLineSet) {
    if (ObjectUtils.isEmpty(taxesLineSet)) {
      return false;
    }

    return taxesLineSet.stream()
        .map(TaxLine::getTax)
        .filter(Objects::nonNull)
        .anyMatch(Tax::getIsNonDeductibleTax);
  }

  public void checkSumOfNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    this.checkSumOfNonDeductibleTaxes(
        invoiceLineList.stream()
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkSumOfNonDeductibleTaxes(Set<TaxLine> taxLines) throws AxelorException {
    if (CollectionUtils.isEmpty(taxLines)) {
      return;
    }

    if (taxLines.stream()
            .filter(
                taxLine ->
                    Boolean.TRUE.equals(
                        Optional.of(taxLine)
                            .map(TaxLine::getTax)
                            .map(Tax::getIsNonDeductibleTax)
                            .orElse(null)))
            .map(TaxLine::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .compareTo(BigDecimal.valueOf(100))
        > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.SUM_OF_NON_DEDUCTIBLE_TAXES_EXCEEDS_ONE_HUNDRED));
    }
  }

  @Override
  public BigDecimal getTotalTaxRateInPercentage(Set<TaxLine> taxLineSet) {
    if (CollectionUtils.isEmpty(taxLineSet)) {
      return BigDecimal.ZERO;
    }
    return this.getNotNonDeductibleTaxesSet(taxLineSet).stream()
        .map(TaxLine::getValue)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
