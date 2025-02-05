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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContractYearEndBonusServiceImpl implements ContractYearEndBonusService {

  protected InvoiceLinePricingService invoiceLinePricingService;
  protected AccountConfigService accountConfigService;
  protected AccountManagementContractService accountManagementContractService;

  @Inject
  public ContractYearEndBonusServiceImpl(
      InvoiceLinePricingService invoiceLinePricingService,
      AccountConfigService accountConfigService,
      AccountManagementContractService accountManagementContractService) {
    this.invoiceLinePricingService = invoiceLinePricingService;
    this.accountConfigService = accountConfigService;
    this.accountManagementContractService = accountManagementContractService;
  }

  @Override
  public void invoiceYebContract(Contract contract, Invoice invoice) throws AxelorException {
    if (!isYebContract(contract)) {
      return;
    }

    invoiceLinePricingService.computePricing(invoice);
    replaceAccount(invoice);
  }

  protected void replaceAccount(Invoice invoice) throws AxelorException {
    Company company = invoice.getCompany();
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      replaceAccount(invoiceLine, company, isPurchase);
    }
  }

  protected void replaceAccount(InvoiceLine invoiceLine, Company company, boolean isPurchase)
      throws AxelorException {
    Account accountReplace = getReplacementAccount(invoiceLine, company, isPurchase);
    if (accountReplace != null) {
      invoiceLine.setAccount(accountReplace);
    }
  }

  protected Account getReplacementAccount(
      InvoiceLine invoiceLine, Company company, boolean isPurchase) throws AxelorException {
    Contract contract = invoiceLine.getContractLine().getContractVersion().getContract();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    boolean isYebAccountConfigByProductFamilyEnabled =
        accountConfig.getIsYebAccountConfigByProductFamilyEnabled();

    if (!isYebAccountConfigByProductFamilyEnabled) {
      return getYebProductYebAccount(contract, company, isPurchase);
    }

    Account account =
        accountManagementContractService.getProductYebAccount(
            invoiceLine.getProduct(), company, isPurchase);
    if (account == null) {
      account = getYebProductYebAccount(contract, company, isPurchase);
    }
    return account;
  }

  protected Account getYebProductYebAccount(Contract contract, Company company, boolean isPurchase)
      throws AxelorException {
    Product yebProduct = getYebProduct(contract);

    return Optional.ofNullable(
            accountManagementContractService.getProductYebAccount(yebProduct, company, isPurchase))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_INCONSISTENCY,
                    I18n.get(ContractExceptionMessage.CONTRACT_YEB_PRODUCT_ACCOUNT_MISSING)));
  }

  @Override
  public Product getYebProduct(Contract contract) throws AxelorException {
    return Optional.ofNullable(
            accountConfigService.getAccountConfig(contract.getCompany()).getYearEndBonusProduct())
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(ContractExceptionMessage.CONTRACT_YEB_PRODUCT_MISSING)));
  }

  @Override
  public boolean isYebContract(Contract contract) {
    int targetTypeSelect = contract.getTargetTypeSelect();
    List<Integer> yerTypes = new ArrayList<>();
    yerTypes.add(ContractRepository.YEB_CUSTOMER_CONTRACT);
    yerTypes.add(ContractRepository.YEB_SUPPLIER_CONTRACT);
    return yerTypes.contains(targetTypeSelect);
  }
}
