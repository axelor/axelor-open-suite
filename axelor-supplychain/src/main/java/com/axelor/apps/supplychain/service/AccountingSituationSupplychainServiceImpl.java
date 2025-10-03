/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Singleton
public class AccountingSituationSupplychainServiceImpl extends AccountingSituationServiceImpl
    implements AccountingSituationSupplychainService {

  protected AppAccountService appAccountService;
  protected SaleOrderRepository saleOrderRepository;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected CurrencyScaleService currencyScaleService;
  protected AppSaleService appSaleService;

  @Inject
  public AccountingSituationSupplychainServiceImpl(
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService,
      AccountingSituationRepository accountingSituationRepo,
      AppAccountService appAccountService,
      SaleOrderRepository saleOrderRepository,
      InvoicePaymentRepository invoicePaymentRepository,
      CurrencyScaleService currencyScaleService,
      AppSaleService appSaleService) {
    super(accountConfigService, paymentModeService, accountingSituationRepo);
    this.appAccountService = appAccountService;
    this.saleOrderRepository = saleOrderRepository;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.currencyScaleService = currencyScaleService;
    this.appSaleService = appSaleService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateUsedCredit(Partner partner) throws AxelorException {
    if (appAccountService.getAppAccount().getManageCustomerCredit()) {
      List<AccountingSituation> accountingSituationList =
          accountingSituationRepo.all().filter("self.partner = ?1", partner).fetch();
      for (AccountingSituation accountingSituation : accountingSituationList) {
        accountingSituationRepo.save(this.computeUsedCredit(accountingSituation));
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateCustomerCredit(Partner partner) throws AxelorException {
    if (!appAccountService.isApp("supplychain")) {
      super.updateCustomerCredit(partner);
      return;
    }
    if (!appAccountService.getAppAccount().getManageCustomerCredit()
        || partner.getIsContact()
        || !partner.getIsCustomer()) {
      return;
    }

    List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();

    for (AccountingSituation accountingSituation : accountingSituationList) {
      computeUsedCredit(accountingSituation);
    }
  }

  @Override
  public AccountingSituation computeUsedCredit(AccountingSituation accountingSituation)
      throws AxelorException {
    BigDecimal sum = BigDecimal.ZERO;
    Company company = accountingSituation.getCompany();
    List<SaleOrder> saleOrderList = getSaleOrders(accountingSituation, company);
    for (SaleOrder saleOrder : saleOrderList) {
      sum = sum.add(saleOrder.getInTaxTotal().subtract(getInTaxInvoicedAmount(saleOrder)));
    }
    // subtract the amount of payments if there is no move created for
    // invoice payments
    if (company != null
        && !accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()) {
      List<InvoicePayment> invoicePaymentList =
          invoicePaymentRepository
              .all()
              .filter(
                  "self.invoice.company = :company"
                      + " AND self.invoice.partner = :partner"
                      + " AND self.statusSelect = :validated"
                      + " AND self.typeSelect != :imputation")
              .bind("company", company)
              .bind("partner", accountingSituation.getPartner())
              .bind("validated", InvoicePaymentRepository.STATUS_VALIDATED)
              .bind("imputation", InvoicePaymentRepository.TYPE_ADV_PAYMENT_IMPUTATION)
              .fetch();
      if (invoicePaymentList != null) {
        for (InvoicePayment invoicePayment : invoicePaymentList) {
          sum = sum.subtract(invoicePayment.getAmount());
        }
      }
    }
    sum = accountingSituation.getBalanceCustAccount().add(sum);
    accountingSituation.setUsedCredit(currencyScaleService.getCompanyScaledValue(company, sum));

    return accountingSituation;
  }

  protected List<SaleOrder> getSaleOrders(
      AccountingSituation accountingSituation, Company company) {
    List<SaleOrder> saleOrderList;
    if (appSaleService.getAppSale().getIsQuotationAndOrderSplitEnabled()) {
      saleOrderList =
          saleOrderRepository
              .all()
              .filter(
                  "self.company = :company AND "
                      + "self.clientPartner = :clientPartner AND "
                      + "self.statusSelect = :confirmedStatus")
              .bind("company", company)
              .bind("clientPartner", accountingSituation.getPartner())
              .bind("confirmedStatus", SaleOrderRepository.STATUS_ORDER_CONFIRMED)
              .fetch();
    } else {
      saleOrderList =
          saleOrderRepository
              .all()
              .filter(
                  "self.company = ?1 AND self.clientPartner = ?2 AND self.statusSelect > ?3 AND self.statusSelect < ?4",
                  company,
                  accountingSituation.getPartner(),
                  SaleOrderRepository.STATUS_DRAFT_QUOTATION,
                  SaleOrderRepository.STATUS_ORDER_COMPLETED)
              .fetch();
    }

    return saleOrderList;
  }

  @Override
  public boolean isUsedCreditExceeded(SaleOrder saleOrder) throws AxelorException {
    updateCustomerCreditFromSaleOrder(saleOrder);
    Partner partner = saleOrder.getClientPartner();
    for (AccountingSituation accountingSituation : partner.getAccountingSituationList()) {
      if (isUsedCreditExceeded(accountingSituation)) {
        return true;
      }
    }
    return false;
  }

  @Transactional(
      rollbackOn = {AxelorException.class, Exception.class},
      ignore = {BlockedSaleOrderException.class})
  @Override
  public void checkExceededUsedCredit(SaleOrder saleOrder) throws AxelorException {

    if (!appAccountService.getAppAccount().getManageCustomerCredit()) {
      return;
    }

    updateCustomerCreditFromSaleOrder(saleOrder);
    Partner partner = saleOrder.getClientPartner();
    for (AccountingSituation accountingSituation : partner.getAccountingSituationList()) {
      Company company = accountingSituation.getCompany();
      boolean usedCreditExceeded = isUsedCreditExceeded(accountingSituation);
      if (usedCreditExceeded) {
        saleOrder.setBlockedOnCustCreditExceed(true);
        if (!saleOrder.getManualUnblock()) {
          String message = company.getOrderBloquedMessage();
          if (Strings.isNullOrEmpty(message)) {
            message =
                String.format(
                    I18n.get(SupplychainExceptionMessage.SALE_ORDER_CLIENT_PARTNER_EXCEEDED_CREDIT),
                    partner.getFullName(),
                    saleOrder.getSaleOrderSeq());
          }
          throw new BlockedSaleOrderException(accountingSituation, message);
        }
      }
    }
  }

  protected boolean isUsedCreditExceeded(AccountingSituation accountingSituation) {
    return accountingSituation.getUsedCredit().compareTo(accountingSituation.getAcceptedCredit())
        > 0;
  }

  @Transactional(
      rollbackOn = {Exception.class},
      ignore = {BlockedSaleOrderException.class})
  @Override
  public void updateCustomerCreditFromSaleOrder(SaleOrder saleOrder) throws AxelorException {
    boolean isSeparationEnabled = appSaleService.getAppSale().getIsQuotationAndOrderSplitEnabled();
    Partner partner = saleOrder.getClientPartner();
    List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();
    for (AccountingSituation accountingSituation : accountingSituationList) {
      Company company = accountingSituation.getCompany();
      if (company.equals(saleOrder.getCompany())) {
        // Update UsedCredit
        accountingSituation = this.computeUsedCredit(accountingSituation);
        if ((!isSeparationEnabled
                && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION)
            || (isSeparationEnabled
                && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED)) {

          BigDecimal usedCredit = accountingSituation.getUsedCredit();

          accountingSituation.setUsedCredit(
              currencyScaleService.getCompanyScaledValue(company, usedCredit));
        }
      }
    }
  }

  /**
   * Compute the invoiced amount of the taxed amount of the invoice.
   *
   * @param saleOrder
   * @return the tax invoiced amount
   */
  protected BigDecimal getInTaxInvoicedAmount(SaleOrder saleOrder) {
    BigDecimal exTaxTotal = saleOrder.getExTaxTotal();
    BigDecimal inTaxTotal = saleOrder.getInTaxTotal();

    BigDecimal exTaxAmountInvoiced = saleOrder.getAmountInvoiced();
    if (exTaxTotal.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    } else {
      return inTaxTotal.multiply(exTaxAmountInvoiced).divide(exTaxTotal, 2, RoundingMode.HALF_UP);
    }
  }
}
