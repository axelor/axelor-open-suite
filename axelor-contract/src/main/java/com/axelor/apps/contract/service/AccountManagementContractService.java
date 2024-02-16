package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;

public interface AccountManagementContractService extends AccountManagementAccountService {
  Account getProductYebAccount(Product product, Company company, boolean isPurchase);
}
