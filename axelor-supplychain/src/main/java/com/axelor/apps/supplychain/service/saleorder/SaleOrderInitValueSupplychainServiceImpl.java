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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderBankDetailsService;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderUserService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderInitValueSupplychainServiceImpl extends SaleOrderInitValueServiceImpl {

  protected SaleOrderShipmentService saleOrderShipmentService;
  protected SaleOrderIntercoService saleOrderIntercoService;
  protected SaleOrderStockLocationService saleOrderStockLocationService;
  protected BankDetailsService bankDetailsService;
  protected SaleOrderTaxNumberService saleOrderTaxNumberService;

  @Inject
  public SaleOrderInitValueSupplychainServiceImpl(
      AppBaseService appBaseService,
      UserService userService,
      SaleOrderBankDetailsService saleOrderBankDetailsService,
      SaleConfigService saleConfigService,
      CompanyService companyService,
      SaleOrderUserService saleOrderUserService,
      SaleOrderProductPrintingService saleOrderProductPrintingService,
      SaleOrderShipmentService saleOrderShipmentService,
      SaleOrderIntercoService saleOrderIntercoService,
      SaleOrderStockLocationService saleOrderStockLocationService,
      BankDetailsService bankDetailsService,
      SaleOrderTaxNumberService saleOrderTaxNumberService) {
    super(
        appBaseService,
        userService,
        saleOrderBankDetailsService,
        saleConfigService,
        companyService,
        saleOrderUserService,
        saleOrderProductPrintingService);
    this.saleOrderShipmentService = saleOrderShipmentService;
    this.saleOrderIntercoService = saleOrderIntercoService;
    this.saleOrderStockLocationService = saleOrderStockLocationService;
    this.bankDetailsService = bankDetailsService;
    this.saleOrderTaxNumberService = saleOrderTaxNumberService;
  }

  @Override
  public Map<String, Object> getOnNewInitValues(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> initValues = super.getOnNewInitValues(saleOrder);
    initValues.putAll(getPaymentMode(saleOrder));
    initValues.putAll(getBankDetails(saleOrder));
    initValues.putAll(saleOrderStockLocationService.getStockLocation(saleOrder));
    initValues.putAll(saleOrderIntercoService.getInterco(saleOrder));
    initValues.putAll(getShipmentCostLine(saleOrder));
    initValues.putAll(saleOrderTaxNumberService.getTaxNumber(saleOrder));
    return initValues;
  }

  @Override
  protected Map<String, Object> saleOrderDefaultValuesMap(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderMap = super.saleOrderDefaultValuesMap(saleOrder);

    TradingName tradingName = saleOrder.getTradingName();
    saleOrderMap.put("amountInvoiced", BigDecimal.ZERO);
    if (tradingName != null) {
      saleOrderMap.put("stockLocation", tradingName.getShippingDefaultStockLocation());
    }

    return saleOrderMap;
  }

  protected Map<String, Object> getBankDetails(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrder.setCompanyBankDetails(
        bankDetailsService.getDefaultCompanyBankDetails(
            saleOrder.getCompany(),
            saleOrder.getPaymentMode(),
            saleOrder.getClientPartner(),
            null));
    saleOrderMap.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    return saleOrderMap;
  }

  protected Map<String, Object> getPaymentMode(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    if (clientPartner != null) {
      PaymentMode paymentMode = clientPartner.getInPaymentMode();
      if (paymentMode != null) {
        saleOrder.setPaymentMode(paymentMode);
      } else {
        paymentMode =
            Optional.ofNullable(AuthUtils.getUser())
                .map(User::getActiveCompany)
                .map(Company::getAccountConfig)
                .map(AccountConfig::getInPaymentMode)
                .orElse(null);
        saleOrder.setPaymentMode(paymentMode);
      }
    }
    saleOrderMap.put("paymentMode", saleOrder.getPaymentMode());
    return saleOrderMap;
  }

  protected Map<String, Object> getShipmentCostLine(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrderShipmentService.createShipmentCostLine(saleOrder, saleOrder.getShipmentMode());
    saleOrderMap.put("saleOrderLineList", saleOrder.getSaleOrderLineList());
    return saleOrderMap;
  }
}
