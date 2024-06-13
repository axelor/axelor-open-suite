package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;

public class AccountManagementContractServiceImpl extends AccountManagementServiceAccountImpl
    implements AccountManagementContractService {
  @Inject
  public AccountManagementContractServiceImpl(
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AccountConfigService accountConfigService,
      AccountRepository accountRepository,
      FiscalPositionAccountService fiscalPositionAccountService) {
    super(
        fiscalPositionService,
        taxService,
        accountConfigService,
        accountRepository,
        fiscalPositionAccountService);
  }

  @Override
  public Account getProductYebAccount(Product product, Company company, boolean isPurchase) {
    Account account =
        this.getProductYebAccount(product, company, isPurchase, CONFIG_OBJECT_PRODUCT);
    if (account == null) {
      return this.getProductYebAccount(product, company, isPurchase, CONFIG_OBJECT_PRODUCT_FAMILY);
    }
    return account;
  }

  protected Account getProductYebAccount(
      Product product, Company company, boolean isPurchase, int configObject) {

    AccountManagement accountManagement = this.getAccountManagement(product, company, configObject);

    Account account = null;

    if (accountManagement != null) {
      if (isPurchase) {
        account = accountManagement.getYearEndBonusPurchaseAccount();
      } else {
        account = accountManagement.getYearEndBonusSaleAccount();
      }
    }

    return account;
  }

  @Override
  public Account getProductAccount(
      Product product,
      Company company,
      FiscalPosition fiscalPosition,
      boolean isPurchase,
      boolean fixedAsset)
      throws AxelorException {

    if (product.equals(accountConfigService.getAccountConfig(company).getYearEndBonusProduct())) {
      return null;
    }

    return super.getProductAccount(product, company, fiscalPosition, isPurchase, fixedAsset);
  }
}
