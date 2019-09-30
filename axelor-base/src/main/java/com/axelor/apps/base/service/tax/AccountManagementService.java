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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.List;

public interface AccountManagementService {

  /**
   * Obtenir la bonne configuration comptable en fonction du produit et de la société.
   *
   * @param product
   * @param company
   * @return
   * @throws AxelorException
   */
  public AccountManagement getAccountManagement(Product product, Company company)
      throws AxelorException;

  /**
   * Generate an exception if account management is missing
   *
   * @param product
   * @param company
   * @throws AxelorException
   */
  public void generateAccountManagementException(Product product, Company company)
      throws AxelorException;

  /**
   * Obtenir la bonne configuration comptable en fonction de la famille de produit et de la société
   *
   * @param productFamily
   * @param company
   * @return
   * @throws AxelorException
   */
  public AccountManagement getAccountManagement(ProductFamily productFamily, Company company)
      throws AxelorException;

  /**
   * Obtenir la bonne configuration comptable en fonction de la société.
   *
   * @param accountManagements
   * @param company
   * @return
   */
  public AccountManagement getAccountManagement(
      List<AccountManagement> accountManagements, Company company);

  /**
   * Obtenir le compte comptable d'une taxe.
   *
   * @param product
   * @param company
   * @param isPurchase
   * @return
   * @throws AxelorException
   */
  public Tax getProductTax(
      Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase)
      throws AxelorException;

  /**
   * Obtenir le compte comptable d'une taxe.
   *
   * @param product
   * @param company
   * @param isPurchase
   * @return
   */
  public Tax getProductTax(AccountManagement accountManagement, boolean isPurchase);

  /**
   * Obtenir la version de taxe d'un produit.
   *
   * @param product
   * @param amendment
   * @return
   * @throws AxelorException
   */
  public TaxLine getTaxLine(
      LocalDate date,
      Product product,
      Company company,
      FiscalPosition fiscalPosition,
      boolean isPurchase)
      throws AxelorException;
}
