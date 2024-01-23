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
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import java.util.List;

public class AccountingSituationServiceImpl implements AccountingSituationService {

  protected AccountConfigService accountConfigService;
  protected PaymentModeService paymentModeService;
  protected AccountingSituationRepository accountingSituationRepo;
  protected CompanyRepository companyRepo;

  @Inject
  public AccountingSituationServiceImpl(
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService,
      AccountingSituationRepository accountingSituationRepo,
      CompanyRepository companyRepo) {
    this.accountConfigService = accountConfigService;
    this.paymentModeService = paymentModeService;
    this.accountingSituationRepo = accountingSituationRepo;
    this.companyRepo = companyRepo;
  }

  @Override
  public AccountingSituation getAccountingSituation(Partner partner, Company company) {
    if (partner == null || partner.getAccountingSituationList() == null) {
      return null;
    }

    for (AccountingSituation accountingSituation : partner.getAccountingSituationList()) {
      if (accountingSituation.getCompany() != null
          && accountingSituation.getCompany().equals(company)) {
        return accountingSituation;
      }
    }

    return null;
  }

  /**
   * Creates the domain for the bank details in Accounting Situation
   *
   * @param accountingSituation
   * @param isInBankDetails true if the field is companyInBankDetails false if the field is
   *     companyOutBankDetails
   * @return the domain of the bank details field
   */
  @Override
  public String createDomainForBankDetails(
      AccountingSituation accountingSituation, boolean isInBankDetails) {
    String domain = "";
    List<BankDetails> authorizedBankDetails;
    if (accountingSituation.getPartner() != null) {

      if (isInBankDetails) {
        authorizedBankDetails =
            paymentModeService.getCompatibleBankDetailsList(
                accountingSituation.getPartner().getInPaymentMode(),
                accountingSituation.getCompany());
      } else {
        authorizedBankDetails =
            paymentModeService.getCompatibleBankDetailsList(
                accountingSituation.getPartner().getOutPaymentMode(),
                accountingSituation.getCompany());
      }
      String idList = StringTool.getIdListString(authorizedBankDetails);
      if (idList.equals("")) {
        return domain;
      }
      domain = "self.id IN (" + idList + ") AND self.active = true";
    }
    return domain;
  }

  @Override
  public void updateCustomerCredit(Partner partner) throws AxelorException {
    // Nothing to do if the supplychain module is not loaded.
  }

  @Override
  public Account getCustomerAccount(Partner partner, Company company) throws AxelorException {
    Account account = null;
    AccountingSituation accountingSituation = getAccountingSituation(partner, company);

    if (accountingSituation != null) {
      account = accountingSituation.getCustomerAccount();
    }

    if (account == null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      account = accountConfigService.getCustomerAccount(accountConfig);
    }

    return account;
  }

  @Override
  public Account getSupplierAccount(Partner partner, Company company) throws AxelorException {
    Account account = null;
    AccountingSituation accountingSituation = getAccountingSituation(partner, company);

    if (accountingSituation != null) {
      account = accountingSituation.getSupplierAccount();
    }

    if (account == null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      account = accountConfigService.getSupplierAccount(accountConfig);
    }

    return account;
  }

  @Override
  public Account getEmployeeAccount(Partner partner, Company company) throws AxelorException {
    Account account = null;
    AccountingSituation accountingSituation = getAccountingSituation(partner, company);

    if (accountingSituation != null) {
      account = accountingSituation.getEmployeeAccount();
    }

    if (account == null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      account = accountConfigService.getEmployeeAccount(accountConfig);
    }

    return account;
  }

  @Override
  public BankDetails getCompanySalesBankDetails(Company company, Partner partner) {
    AccountingSituation situation = getAccountingSituation(partner, company);
    if (situation != null
        && situation.getCompanyInBankDetails() != null
        && situation.getCompanyInBankDetails().getActive()) {
      return situation.getCompanyInBankDetails();
    }

    return company.getDefaultBankDetails();
  }

  @Override
  public Account getHoldBackCustomerAccount(Partner partner, Company company)
      throws AxelorException {
    Account account = null;
    AccountingSituation accountingSituation = getAccountingSituation(partner, company);

    if (accountingSituation != null) {
      account = accountingSituation.getHoldBackCustomerAccount();
    }

    if (account == null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      account = accountConfigService.getHoldBackCustomerAccount(accountConfig);
    }

    return account;
  }

  @Override
  public Account getHoldBackSupplierAccount(Partner partner, Company company)
      throws AxelorException {
    Account account = null;
    AccountingSituation accountingSituation = getAccountingSituation(partner, company);

    if (accountingSituation != null) {
      account = accountingSituation.getHoldBackSupplierAccount();
    }

    if (account == null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      account = accountConfigService.getHoldBackSupplierAccount(accountConfig);
    }

    return account;
  }

  @Override
  public void setHoldBackAccounts(AccountingSituation accountingSituation, Partner partner)
      throws AxelorException {
    try {
      Company company = accountingSituation.getCompany();

      if (company != null && partner != null) {
        accountingSituation.setHoldBackCustomerAccount(
            this.getHoldBackCustomerAccount(partner, company));
        accountingSituation.setHoldBackSupplierAccount(
            this.getHoldBackSupplierAccount(partner, company));
      } else {
        accountingSituation.setHoldBackCustomerAccount(null);
        accountingSituation.setHoldBackSupplierAccount(null);
      }
    } catch (AxelorException e) {
      accountingSituation.setHoldBackCustomerAccount(null);
      accountingSituation.setHoldBackSupplierAccount(null);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  @Override
  public int determineVatSystemSelect(AccountingSituation accountingSituation, Account account)
      throws AxelorException {

    if (accountingSituation != null) {
      if (accountingSituation != null
          && (accountingSituation.getVatSystemSelect() == null
              || accountingSituation.getVatSystemSelect()
                  == AccountingSituationRepository.VAT_SYSTEM_DEFAULT)) {
        if (account.getVatSystemSelect() == null
            || account.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_ACCOUNT_PARTNER),
              account.getCode(),
              accountingSituation.getPartner().getFullName());
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_PARTNER),
              accountingSituation.getPartner().getFullName());
        }
      }
      if (accountingSituation.getVatSystemSelect() == AccountingSituationRepository.VAT_DELIVERY) {
        return AccountRepository.VAT_SYSTEM_GOODS;
      } else if (account.getVatSystemSelect() == null
          || account.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_ACCOUNT),
            account.getCode());
      }
      return account.getVatSystemSelect();
    }
    return AccountRepository.VAT_SYSTEM_DEFAULT;
  }

  @Override
  public int determineVatSystemSelect(
      AccountingSituation accountingSituation, InvoiceLineTax invoiceLineTax)
      throws AxelorException {

    if (accountingSituation != null) {
      if (accountingSituation != null
          && (accountingSituation.getVatSystemSelect() == null
              || accountingSituation.getVatSystemSelect()
                  == AccountingSituationRepository.VAT_SYSTEM_DEFAULT)) {
        if (invoiceLineTax.getVatSystemSelect() == null
            || invoiceLineTax.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_INVOICE_TAX_PARTNER),
              invoiceLineTax.getInvoice().getInvoiceId(),
              accountingSituation.getPartner().getFullName());
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_PARTNER),
              accountingSituation.getPartner().getFullName());
        }
      }
      if (accountingSituation.getVatSystemSelect() == AccountingSituationRepository.VAT_DELIVERY) {
        return AccountRepository.VAT_SYSTEM_GOODS;
      } else if (invoiceLineTax.getVatSystemSelect() == null
          || invoiceLineTax.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_INVOICE_TAX),
            invoiceLineTax.getInvoice().getInvoiceId());
      }
      return invoiceLineTax.getVatSystemSelect();
    }
    return AccountRepository.VAT_SYSTEM_DEFAULT;
  }

  @Override
  public Account getPartnerAccount(Invoice invoice, boolean isHoldBack) throws AxelorException {
    if (invoice.getCompany() == null
        || invoice.getOperationTypeSelect() == null
        || invoice.getOperationTypeSelect() == 0
        || invoice.getPartner() == null) {
      return null;
    }

    if (InvoiceToolService.isPurchase(invoice)) {
      return isHoldBack
          ? this.getHoldBackSupplierAccount(invoice.getPartner(), invoice.getCompany())
          : this.getSupplierAccount(invoice.getPartner(), invoice.getCompany());
    } else {
      return isHoldBack
          ? this.getHoldBackCustomerAccount(invoice.getPartner(), invoice.getCompany())
          : this.getCustomerAccount(invoice.getPartner(), invoice.getCompany());
    }
  }
}
