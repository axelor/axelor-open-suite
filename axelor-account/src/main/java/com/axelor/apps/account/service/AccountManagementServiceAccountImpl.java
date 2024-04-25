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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagementServiceAccountImpl extends AccountManagementServiceImpl
    implements AccountManagementAccountService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountConfigService accountConfigService;

  @Inject
  public AccountManagementServiceAccountImpl(
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AccountConfigService accountConfigService) {
    super(fiscalPositionService, taxService);
    this.accountConfigService = accountConfigService;
  }

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
      throws AxelorException {

    log.debug(
        "Get the account for the product {} (company : {}, purchase : {}, fixed asset : {}, fiscal position : {})",
        new Object[] {
          product != null ? product.getCode() : null,
          company.getName(),
          isPurchase,
          fixedAsset,
          fiscalPosition != null ? fiscalPosition.getCode() : null
        });

    Account generalAccount =
        this.getProductAccount(product, company, isPurchase, fixedAsset, CONFIG_OBJECT_PRODUCT);

    Account account =
        new FiscalPositionAccountServiceImpl().getAccount(fiscalPosition, generalAccount);

    if (account != null) {
      return account;
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_1_ACCOUNT),
        product != null ? product.getCode() : null,
        company.getName());
  }

  /**
   * Get the product tax
   *
   * @param product
   * @param company
   * @param isPurchase
   * @param fixedAsset Specify if we should get the purchase account for fixed asset or not. Used
   *     only if isPurchase param is true.
   * @param configObject Specify if we want get the tax from the product or its product family
   *     <li>1 : product
   *     <li>2 : product family
   * @return
   * @throws AxelorException
   */
  @CallMethod
  protected Account getProductAccount(
      Product product, Company company, boolean isPurchase, boolean fixedAsset, int configObject) {

    AccountManagement accountManagement = this.getAccountManagement(product, company, configObject);

    Account account = null;

    if (accountManagement != null) {
      if (isPurchase) {
        if (fixedAsset) {
          account = accountManagement.getPurchFixedAssetsAccount();
        } else {
          account = accountManagement.getPurchaseAccount();
        }
      } else {
        account = accountManagement.getSaleAccount();
      }
    }

    if (account == null && configObject == CONFIG_OBJECT_PRODUCT) {
      return getProductAccount(
          product, company, isPurchase, fixedAsset, CONFIG_OBJECT_PRODUCT_FAMILY);
    }

    return account;
  }

  /**
   * Get the product analytic distribution template
   *
   * @param product
   * @param company
   * @return
   * @throws AxelorException
   */
  public AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Product product, Company company, boolean isPurchase) throws AxelorException {

    return getAnalyticDistributionTemplate(product, company, CONFIG_OBJECT_PRODUCT, isPurchase);
  }

  /**
   * Get the product analytic distribution template
   *
   * @param product
   * @param company
   * @param configObject Specify if we want get the tax from the product or its product family
   *     <li>1 : product
   *     <li>2 : product family
   * @return
   * @throws AxelorException
   */
  protected AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Product product, Company company, int configObject, boolean isPurchase)
      throws AxelorException {

    AccountManagement accountManagement = this.getAccountManagement(product, company, configObject);

    AnalyticDistributionTemplate analyticDistributionTemplate = null;

    if (accountManagement != null) {
      analyticDistributionTemplate = accountManagement.getAnalyticDistributionTemplate();
    }

    if (accountManagement != null && analyticDistributionTemplate == null) {
      Account account =
          isPurchase ? accountManagement.getPurchaseAccount() : accountManagement.getSaleAccount();

      if (account != null
          && account.getAnalyticDistributionAuthorized()
          && accountConfigService
                  .getAccountConfig(account.getCompany())
                  .getAnalyticDistributionTypeSelect()
              == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
        analyticDistributionTemplate = account.getAnalyticDistributionTemplate();
      }
    }

    if (analyticDistributionTemplate == null && configObject == CONFIG_OBJECT_PRODUCT) {
      return getAnalyticDistributionTemplate(
          product, company, CONFIG_OBJECT_PRODUCT_FAMILY, isPurchase);
    }

    return analyticDistributionTemplate;
  }

  /**
   * Get the product fixed asset category
   *
   * @param product
   * @param company
   * @return
   * @throws AxelorException
   */
  public FixedAssetCategory getProductFixedAssetCategory(Product product, Company company) {

    return getProductFixedAssetCategory(product, company, CONFIG_OBJECT_PRODUCT);
  }

  /**
   * Get the product fixed asset category
   *
   * @param product
   * @param company
   * @param configObject Specify if we want get the fixed asset category from the product or its
   *     product family
   *     <li>1 : product
   *     <li>2 : product family
   * @return
   * @throws AxelorException
   */
  protected FixedAssetCategory getProductFixedAssetCategory(
      Product product, Company company, int configObject) {

    AccountManagement accountManagement = this.getAccountManagement(product, company, configObject);

    FixedAssetCategory fixedAssetCategory = null;

    if (accountManagement != null) {
      fixedAssetCategory = accountManagement.getFixedAssetCategory();
    }

    if (fixedAssetCategory == null && configObject == CONFIG_OBJECT_PRODUCT) {
      return getProductFixedAssetCategory(product, company, CONFIG_OBJECT_PRODUCT_FAMILY);
    }

    return fixedAssetCategory;
  }

  @Override
  public Account getCashAccount(AccountManagement accountManagement, PaymentMode paymentMode)
      throws AxelorException {
    if (accountManagement == null || paymentMode == null) {
      return null;
    }
    if (accountManagement != null && accountManagement.getCashAccount() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_CASH_ACCOUNT_MISSING_PAYMENT),
          paymentMode.getCode());
    }
    return accountManagement.getCashAccount();
  }

  @Override
  public Account getPurchVatRegulationAccount(
      AccountManagement accountManagement, Tax tax, Company company) throws AxelorException {
    if (accountManagement != null && accountManagement.getPurchVatRegulationAccount() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_PURCH_VAT_ACCOUNT_MISSING_TAX),
          tax.getCode(),
          company.getCode());
    }
    return accountManagement.getPurchVatRegulationAccount();
  }

  @Override
  public Account getSaleVatRegulationAccount(
      AccountManagement accountManagement, Tax tax, Company company) throws AxelorException {
    if (accountManagement != null && accountManagement.getSaleVatRegulationAccount() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_SALE_VAT_ACCOUNT_MISSING_TAX),
          tax.getCode(),
          company.getCode());
    }
    return accountManagement.getSaleVatRegulationAccount();
  }

  @Override
  public Account getTaxAccount(
      AccountManagement accountManagement,
      Tax tax,
      Company company,
      Journal journal,
      int vatSystemSelect,
      int functionalOrigin,
      boolean isFixedAssets,
      boolean isFinancialDiscount)
      throws AxelorException {
    if (accountManagement != null) {
      Account account = null;
      String error = AccountExceptionMessage.ACCOUNT_MANAGEMENT_ACCOUNT_MISSING_TAX;
      if (!isFixedAssets && !isFinancialDiscount) {
        if (functionalOrigin == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
          if (vatSystemSelect == MoveLineRepository.VAT_COMMON_SYSTEM) {
            account = accountManagement.getSaleTaxVatSystem1Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_SALE_TAX_VAT_SYSTEM_1_ACCOUNT_MISSING_TAX;
          } else if (vatSystemSelect == MoveLineRepository.VAT_CASH_PAYMENTS) {
            account = accountManagement.getSaleTaxVatSystem2Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_SALE_TAX_VAT_SYSTEM_2_ACCOUNT_MISSING_TAX;
          }
        } else if (functionalOrigin == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
          if (vatSystemSelect == MoveLineRepository.VAT_COMMON_SYSTEM) {
            account = accountManagement.getPurchaseTaxVatSystem1Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_PURCHASE_TAX_VAT_SYSTEM_1_ACCOUNT_MISSING_TAX;
          } else if (vatSystemSelect == MoveLineRepository.VAT_CASH_PAYMENTS) {
            account = accountManagement.getPurchaseTaxVatSystem2Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_PURCHASE_TAX_VAT_SYSTEM_2_ACCOUNT_MISSING_TAX;
          }
        } else if (functionalOrigin == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
          account = accountManagement.getPurchaseTaxVatSystem2Account();
          error =
              AccountExceptionMessage
                  .ACCOUNT_MANAGEMENT_PURCHASE_TAX_VAT_SYSTEM_2_ACCOUNT_MISSING_TAX;
        }
      } else if (isFixedAssets) {
        if (vatSystemSelect == MoveLineRepository.VAT_COMMON_SYSTEM) {
          account = accountManagement.getPurchFixedAssetsTaxVatSystem1Account();
          error =
              AccountExceptionMessage
                  .ACCOUNT_MANAGEMENT_PURCHASE_FIXED_ASSETS_TAX_VAT_SYSTEM_1_ACCOUNT_MISSING_TAX;
        }
        if (vatSystemSelect == MoveLineRepository.VAT_CASH_PAYMENTS) {
          account = accountManagement.getPurchFixedAssetsTaxVatSystem2Account();
          error =
              AccountExceptionMessage
                  .ACCOUNT_MANAGEMENT_PURCHASE_FIXED_ASSETS_TAX_VAT_SYSTEM_2_ACCOUNT_MISSING_TAX;
        }
      } else {
        if (journal != null
            && (journal.getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE
                || journal.getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                || journal.getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER
                || (journal.getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
                    && functionalOrigin == MoveRepository.FUNCTIONAL_ORIGIN_SALE))) {
          if (vatSystemSelect == MoveLineRepository.VAT_COMMON_SYSTEM) {
            account = accountManagement.getAllowedFinDiscountTaxVatSystem1Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_ALLOWED_FINANCIAL_DISCOUNT_TAX_VAT_SYSTEM_1_ACCOUNT_MISSING_TAX;
          } else if (vatSystemSelect == MoveLineRepository.VAT_CASH_PAYMENTS) {
            account = accountManagement.getAllowedFinDiscountTaxVatSystem2Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_ALLOWED_FINANCIAL_DISCOUNT_TAX_VAT_SYSTEM_2_ACCOUNT_MISSING_TAX;
          }
        } else if (journal != null
            && (journal.getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
                || (journal.getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
                    && functionalOrigin == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE))) {
          if (vatSystemSelect == MoveLineRepository.VAT_COMMON_SYSTEM) {
            account = accountManagement.getObtainedFinDiscountTaxVatSystem1Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_OBTAINED_FINANCIAL_DISCOUNT_TAX_VAT_SYSTEM_1_ACCOUNT_MISSING_TAX;
          } else if (vatSystemSelect == MoveLineRepository.VAT_CASH_PAYMENTS) {
            account = accountManagement.getObtainedFinDiscountTaxVatSystem2Account();
            error =
                AccountExceptionMessage
                    .ACCOUNT_MANAGEMENT_OBTAINED_FINANCIAL_DISCOUNT_TAX_VAT_SYSTEM_2_ACCOUNT_MISSING_TAX;
          }
        }
      }

      if (journal != null
          && (journal.getJournalType().getTechnicalTypeSelect() == null
              || journal.getJournalType().getTechnicalTypeSelect() == 0)
          && !isFixedAssets) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.JOURNAL_TYPE_MISSING_TECHNICAL_TYPE),
            journal.getJournalType().getName().toUpperCase());
      }
      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(error),
            tax.getCode(),
            company.getCode());
      }

      return account;
    }
    return null;
  }
}
