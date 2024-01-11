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
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagementServiceImpl implements AccountManagementService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final int CONFIG_OBJECT_PRODUCT = 1;
  protected static final int CONFIG_OBJECT_PRODUCT_FAMILY = 2;

  private FiscalPositionService fiscalPositionService;

  private TaxService taxService;

  @Inject
  public AccountManagementServiceImpl(
      FiscalPositionService fiscalPositionService, TaxService taxService) {
    this.fiscalPositionService = fiscalPositionService;
    this.taxService = taxService;
  }

  /**
   * Get the right Account management according to the product and company and configObject
   *
   * @param product
   * @param company
   * @param configObject Specify if we want get the account management from the product or its
   *     product family
   *     <li>1 : product
   *     <li>2 : product family
   * @return
   * @throws AxelorException
   */
  protected AccountManagement getAccountManagement(
      Product product, Company company, int configObject) {

    if (product == null) {
      return null;
    }

    switch (configObject) {
      case CONFIG_OBJECT_PRODUCT:
        return this.getAccountManagement(product, company);

      case CONFIG_OBJECT_PRODUCT_FAMILY:
        return this.getAccountManagement(product.getProductFamily(), company);

      default:
        return null;
    }
  }

  /**
   * Get the right Account management line according to the product and company
   *
   * @param productFamily
   * @param company
   * @return
   * @throws AxelorException
   */
  protected AccountManagement getAccountManagement(Product product, Company company) {

    return this.getAccountManagement(product.getAccountManagementList(), company);
  }

  protected AccountManagement getAccountManagement(ProductFamily productFamily, Company company) {

    if (productFamily == null) {
      return null;
    }

    return this.getAccountManagement(productFamily.getAccountManagementList(), company);
  }

  /**
   * Get the right Account management line according to the company
   *
   * @param accountManagements List of account management
   * @param company
   * @return
   */
  @Override
  public AccountManagement getAccountManagement(
      List<AccountManagement> accountManagements, Company company) {

    if (accountManagements == null || accountManagements.isEmpty()) {
      return null;
    }

    for (AccountManagement accountManagement : accountManagements) {
      if (accountManagement.getCompany().equals(company)) {
        LOG.debug("Get account management configuration => company: {}", company.getName());

        return accountManagement;
      }
    }
    return null;
  }

  @Override
  public TaxEquiv getProductTaxEquiv(
      Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase)
      throws AxelorException {
    Tax tax = getProductTax(product, company, isPurchase);
    return fiscalPositionService.getTaxEquiv(fiscalPosition, tax);
  }

  /**
   * Get the product tax according to the fiscal position
   *
   * @param product
   * @param company
   * @param fiscalPosition
   * @param isPurchase specify if we want get the tax for purchase or sale
   * @return the tax defined for the product, according to the fiscal position
   * @throws AxelorException
   */
  protected Tax getProductTax(
      Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase)
      throws AxelorException {
    Tax generalTax = this.getProductTax(product, company, isPurchase);
    Tax tax = fiscalPositionService.getTax(fiscalPosition, generalTax);

    if (tax != null) {
      return tax;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BaseExceptionMessage.ACCOUNT_MANAGEMENT_3),
        product.getCode(),
        company.getName());
  }

  /**
   * Get product tax before using tax equiv from fiscal position.
   *
   * @param product
   * @param company
   * @param isPurchase specify if we want get the tax for purchase or sale
   * @return the tax defined for the product
   * @throws AxelorException
   */
  protected Tax getProductTax(Product product, Company company, boolean isPurchase)
      throws AxelorException {

    LOG.debug(
        "Get the tax for the product {} (company : {}, purchase : {}",
        product.getCode(),
        company.getName(),
        isPurchase);

    Tax tax = this.getProductTax(product, company, isPurchase, CONFIG_OBJECT_PRODUCT);

    if (tax != null) {
      return tax;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BaseExceptionMessage.ACCOUNT_MANAGEMENT_3),
        product.getCode(),
        company.getName());
  }

  /**
   * Get the product tax
   *
   * @param product
   * @param company
   * @param isPurchase
   * @param configObject Specify if we want get the tax from the product or its product family
   *     <li>1 : product
   *     <li>2 : product family
   * @return
   * @throws AxelorException
   */
  protected Tax getProductTax(
      Product product, Company company, boolean isPurchase, int configObject) {

    AccountManagement accountManagement = this.getAccountManagement(product, company, configObject);

    Tax tax = null;

    if (accountManagement != null) {
      if (isPurchase) {
        tax = accountManagement.getPurchaseTax();
      } else {
        tax = accountManagement.getSaleTax();
      }
    }

    if (tax == null && configObject == CONFIG_OBJECT_PRODUCT) {
      return getProductTax(product, company, isPurchase, CONFIG_OBJECT_PRODUCT_FAMILY);
    }

    return tax;
  }

  /**
   * Obtenir la version de taxe d'un produit.
   *
   * @param product
   * @param amendment
   * @return
   * @throws AxelorException
   */
  @Override
  public TaxLine getTaxLine(
      LocalDate date,
      Product product,
      Company company,
      FiscalPosition fiscalPosition,
      boolean isPurchase)
      throws AxelorException {

    TaxLine taxLine =
        taxService.getTaxLine(
            this.getProductTax(product, company, fiscalPosition, isPurchase), date);
    if (taxLine != null) {
      return taxLine;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BaseExceptionMessage.ACCOUNT_MANAGEMENT_2),
        product.getCode());
  }
}
