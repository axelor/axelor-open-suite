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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import java.time.LocalDate;
import java.util.List;

public interface AccountManagementService {

  public AccountManagement getAccountManagement(
      List<AccountManagement> accountManagements, Company company);

  /**
   * Get product tax equiv from fiscal position.
   *
   * @param product
   * @param company
   * @param isPurchase specify if we want get the tax for purchase or sale
   * @return the tax equiv found for the product, null if nothing was found.
   * @throws AxelorException
   */
  TaxEquiv getProductTaxEquiv(
      Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase)
      throws AxelorException;

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
