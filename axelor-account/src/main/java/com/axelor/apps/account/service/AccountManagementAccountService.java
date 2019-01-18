/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;

public interface AccountManagementAccountService extends AccountManagementService {

  /**
   * Get the product tax according to the fiscal position
   *
   * @param product
   * @param company
   * @param fiscalPosition
   * @param isPurchase Specify if we want get the tax for purchase or sale
   * @param fixedAsset Specify if we should get the purchase account for fixed asset or not. Used
   *     only if isPurchase param is true.
   * @return the tax defined for the product, according to the fiscal position
   * @throws AxelorException
   */
  public Account getProductAccount(
      Product product,
      Company company,
      FiscalPosition fiscalPosition,
      boolean isPurchase,
      boolean fixedAsset)
      throws AxelorException;

  /**
   * Get the product analytic distribution template
   *
   * @param product
   * @param company
   * @return
   * @throws AxelorException
   */
  public AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Product product, Company company);

  /**
   * Get the product fixed asset category
   *
   * @param product
   * @param company
   * @return
   * @throws AxelorException
   */
  public FixedAssetCategory getProductFixedAssetCategory(Product product, Company company);
}
