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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagementServiceImpl implements AccountManagementService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private FiscalPositionService fiscalPositionService;

  private TaxService taxService;

  @Inject
  public AccountManagementServiceImpl(
      FiscalPositionService fiscalPositionService, TaxService taxService) {
    this.fiscalPositionService = fiscalPositionService;
    this.taxService = taxService;
  }

  /**
   * Obtenir la bonne configuration comptable en fonction du produit et de la société.
   *
   * @param product
   * @param company
   * @return
   * @throws AxelorException
   */
  @Override
  public AccountManagement getAccountManagement(Product product, Company company)
      throws AxelorException {

    AccountManagement accountManagement = null;

    if (product.getAccountManagementList() != null
        && !product.getAccountManagementList().isEmpty()) {
      accountManagement = this.getAccountManagement(product.getAccountManagementList(), company);
    }

    if (accountManagement == null && product.getProductFamily() != null) {
      accountManagement = this.getAccountManagement(product.getProductFamily(), company);
    }

    if (accountManagement == null) {
      this.generateAccountManagementException(product, company);
    }

    return accountManagement;
  }

  @Override
  public void generateAccountManagementException(Product product, Company company)
      throws AxelorException {

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_1),
        product.getCode(),
        company.getName());
  }

  /**
   * Obtenir la bonne configuration comptable en fonction de la famille de produit et de la société
   *
   * @param productFamily
   * @param company
   * @return
   * @throws AxelorException
   */
  @Override
  public AccountManagement getAccountManagement(ProductFamily productFamily, Company company)
      throws AxelorException {

    if (productFamily.getAccountManagementList() != null
        && !productFamily.getAccountManagementList().isEmpty()) {
      return this.getAccountManagement(productFamily.getAccountManagementList(), company);
    }

    return null;
  }

  /**
   * Obtenir la bonne configuration comptable en fonction de la société.
   *
   * @param accountManagements
   * @param company
   * @return
   */
  @Override
  public AccountManagement getAccountManagement(
      List<AccountManagement> accountManagements, Company company) {

    for (AccountManagement accountManagement : accountManagements) {
      if (accountManagement.getCompany().equals(company)) {
        LOG.debug("Obtention de la configuration comptable => société: {}", company.getName());

        return accountManagement;
      }
    }
    return null;
  }

  /**
   * Obtenir le compte comptable d'une taxe.
   *
   * @param product
   * @param company
   * @param isPurchase
   * @return
   * @throws AxelorException
   */
  @Override
  public Tax getProductTax(
      Product product, Company company, FiscalPosition fiscalPosition, boolean isPurchase)
      throws AxelorException {

    LOG.debug(
        "Obtention du compte comptable pour le produit {} (société : {}, achat ? {})",
        new Object[] {product.getCode(), company.getName(), isPurchase});

    Tax tax =
        fiscalPositionService.getTax(
            fiscalPosition,
            this.getProductTax(this.getAccountManagement(product, company), isPurchase));

    if (tax != null) {
      return tax;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_3),
        product.getCode(),
        company.getName());
  }

  /**
   * Obtenir le compte comptable d'une taxe.
   *
   * @param product
   * @param company
   * @param isPurchase
   * @return
   */
  @Override
  public Tax getProductTax(AccountManagement accountManagement, boolean isPurchase) {

    if (isPurchase) {
      return accountManagement.getPurchaseTax();
    } else {
      return accountManagement.getSaleTax();
    }
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
        I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_2),
        product.getCode());
  }
}
