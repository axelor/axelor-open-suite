/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagementServiceAccountImpl extends AccountManagementServiceImpl
    implements AccountManagementAccountService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public AccountManagementServiceAccountImpl(
      FiscalPositionService fiscalPositionService, TaxService taxService) {
    super(fiscalPositionService, taxService);
  }

  /**
   * Obtenir le compte comptable d'un produit.
   *
   * @param product
   * @param company
   * @param isPurchase
   * @return
   * @throws AxelorException
   */
  @Override
  public Account getProductAccount(Product product, Company company, boolean isPurchase)
      throws AxelorException {

    log.debug(
        "Obtention du compte comptable pour le produit {} (société : {}, achat ? {})",
        new Object[] {product, company, isPurchase});

    return this.getProductAccount(this.getAccountManagement(product, company), isPurchase);
  }

  /**
   * Obtenir le compte comptable d'un produit.
   *
   * @param product
   * @param company
   * @param isPurchase
   * @return
   */
  @Override
  public Account getProductAccount(AccountManagement accountManagement, boolean isPurchase) {

    if (isPurchase) {
      return accountManagement.getPurchaseAccount();
    } else {
      return accountManagement.getSaleAccount();
    }
  }

  @Override
  public void generateAccountManagementException(Product product, Company company)
      throws AxelorException {

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_1_ACCOUNT),
        product.getCode(),
        company.getName());
  }
}
