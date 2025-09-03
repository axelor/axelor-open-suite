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
package com.axelor.apps.supplychain.service.saleorder.onchange;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderBankDetailsService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderDateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderUserService;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineFiscalPositionService;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.repo.PartnerStockSettingsRepository;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderIntercoService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockLocationService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderTaxNumberService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderOnChangeSupplychainServiceImpl extends SaleOrderOnChangeServiceImpl {

  protected AccountConfigService accountConfigService;
  protected AccountingSituationService accountingSituationService;
  protected PartnerStockSettingsRepository partnerStockSettingsRepository;
  protected SaleOrderSupplychainService saleOrderSupplychainService;
  protected SaleOrderIntercoService saleOrderIntercoService;
  protected SaleOrderStockLocationService saleOrderStockLocationService;
  protected AppBaseService appBaseService;
  protected SaleOrderTaxNumberService saleOrderTaxNumberService;

  @Inject
  public SaleOrderOnChangeSupplychainServiceImpl(
      PartnerService partnerService,
      SaleOrderUserService saleOrderUserService,
      SaleOrderService saleOrderService,
      PartnerPriceListService partnerPriceListService,
      SaleOrderCreateService saleOrderCreateService,
      SaleOrderProductPrintingService saleOrderProductPrintingService,
      SaleOrderLineFiscalPositionService saleOrderLineFiscalPositionService,
      SaleOrderComputeService saleOrderComputeService,
      SaleConfigService saleConfigService,
      SaleOrderBankDetailsService saleOrderBankDetailsService,
      AppBaseService appBaseService,
      SaleOrderDateService saleOrderDateService,
      AccountConfigService accountConfigService,
      AccountingSituationService accountingSituationService,
      PartnerStockSettingsRepository partnerStockSettingsRepository,
      SaleOrderSupplychainService saleOrderSupplychainService,
      SaleOrderIntercoService saleOrderIntercoService,
      SaleOrderStockLocationService saleOrderStockLocationService,
      AppBaseService appBaseService1,
      SaleOrderTaxNumberService saleOrderTaxNumberService) {
    super(
        partnerService,
        saleOrderUserService,
        saleOrderService,
        partnerPriceListService,
        saleOrderCreateService,
        saleOrderProductPrintingService,
        saleOrderLineFiscalPositionService,
        saleOrderComputeService,
        saleConfigService,
        saleOrderBankDetailsService,
        appBaseService,
        saleOrderDateService);
    this.accountConfigService = accountConfigService;
    this.accountingSituationService = accountingSituationService;
    this.partnerStockSettingsRepository = partnerStockSettingsRepository;
    this.saleOrderSupplychainService = saleOrderSupplychainService;
    this.saleOrderIntercoService = saleOrderIntercoService;
    this.saleOrderStockLocationService = saleOrderStockLocationService;
    this.appBaseService = appBaseService1;
    this.saleOrderTaxNumberService = saleOrderTaxNumberService;
  }

  @Override
  public Map<String, Object> partnerOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = super.partnerOnChange(saleOrder);
    values.putAll(getPaymentCondition(saleOrder));
    values.putAll(getFiscalPosition(saleOrder));
    values.putAll(getPaymentMode(saleOrder));
    values.putAll(getIncoterm(saleOrder));
    values.putAll(getCompanyBankDetails(saleOrder));
    values.putAll(getAdvancePayment(saleOrder));
    values.putAll(saleOrderIntercoService.getInterco(saleOrder));
    values.putAll(saleOrderStockLocationService.getStockLocation(saleOrder, false));
    values.putAll(saleOrderStockLocationService.getToStockLocation(saleOrder));
    values.putAll(getIsIspmRequired(saleOrder));
    values.putAll(setDefaultInvoicedAndDeliveredPartnersAndAddresses(saleOrder));
    return values;
  }

  @Override
  public Map<String, Object> companyOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = super.companyOnChange(saleOrder);
    values.putAll(saleOrderStockLocationService.getStockLocation(saleOrder, true));
    values.putAll(saleOrderStockLocationService.getToStockLocation(saleOrder));
    values.putAll(getIncoterm(saleOrder));
    values.putAll(saleOrderTaxNumberService.getTaxNumber(saleOrder));
    return values;
  }

  @Override
  protected Map<String, Object> getClientPartnerValues(SaleOrder saleOrder) {
    Map<String, Object> values = super.getClientPartnerValues(saleOrder);
    Partner clientPartner = saleOrder.getClientPartner();
    if (clientPartner != null) {
      saleOrder.setIsNeedingConformityCertificate(
          clientPartner.getIsNeedingConformityCertificate());
      values.put("isNeedingConformityCertificate", saleOrder.getIsNeedingConformityCertificate());
      saleOrder.setInvoiceComments(clientPartner.getInvoiceComments());
      values.put("invoiceComments", saleOrder.getInvoiceComments());
      saleOrder.setShipmentMode(clientPartner.getShipmentMode());
      values.put("shipmentMode", saleOrder.getShipmentMode());
      FreightCarrierMode freightCarrierMode = clientPartner.getFreightCarrierMode();
      if (freightCarrierMode != null) {
        saleOrder.setFreightCarrierMode(freightCarrierMode);
        values.put("freightCarriedMode", saleOrder.getFreightCarrierMode());
        saleOrder.setCarrierPartner(freightCarrierMode.getCarrierPartner());
        values.put("carrierPartner", saleOrder.getCarrierPartner());
      }
    }
    return values;
  }

  protected Map<String, Object> getIncoterm(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    PartnerStockSettings partnerStockSettings =
        partnerStockSettingsRepository
            .all()
            .filter("self.company = :company AND self.partner = :clientPartner")
            .bind("company", company)
            .bind("clientPartner", clientPartner)
            .fetchOne();
    if (partnerStockSettings != null) {
      saleOrder.setIncoterm(partnerStockSettings.getIncoterm());
      values.put("incoterm", saleOrder.getIncoterm());
    }

    return values;
  }

  protected Map<String, Object> getPaymentCondition(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    PaymentCondition paymentCondition;
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    if (clientPartner != null && clientPartner.getPaymentCondition() != null) {
      paymentCondition = clientPartner.getPaymentCondition();
    } else {
      paymentCondition = accountConfigService.getAccountConfig(company).getDefPaymentCondition();
    }
    saleOrder.setPaymentCondition(paymentCondition);
    values.put("paymentCondition", saleOrder.getPaymentCondition());
    return values;
  }

  protected Map<String, Object> getFiscalPosition(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    FiscalPosition fiscalPosition = null;
    if (clientPartner != null && clientPartner.getFiscalPosition() != null) {
      fiscalPosition = clientPartner.getFiscalPosition();
    }
    saleOrder.setFiscalPosition(fiscalPosition);
    values.put("fiscalPosition", saleOrder.getFiscalPosition());
    return values;
  }

  protected Map<String, Object> getPaymentMode(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    PaymentMode paymentMode;
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    PaymentMode inPaymentMode = clientPartner.getInPaymentMode();
    if (clientPartner != null && inPaymentMode != null) {
      paymentMode = inPaymentMode;
    } else {
      paymentMode = accountConfigService.getAccountConfig(company).getInPaymentMode();
    }
    saleOrder.setPaymentMode(paymentMode);
    values.put("paymentMode", saleOrder.getPaymentMode());
    return values;
  }

  protected Map<String, Object> getCompanyBankDetails(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    saleOrder.setCompanyBankDetails(
        accountingSituationService.getCompanySalesBankDetails(company, clientPartner));
    values.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    return values;
  }

  protected Map<String, Object> getAdvancePayment(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    saleOrderSupplychainService.setAdvancePayment(saleOrder);
    values.put("advancePaymentNeeded", saleOrder.getAdvancePaymentNeeded());
    values.put("advancePaymentAmountNeeded", saleOrder.getAdvancePaymentAmountNeeded());
    return values;
  }

  protected Map<String, Object> getIsIspmRequired(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    boolean partnerIsIspmRequired =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getIsIspmRequired)
            .orElse(false);
    boolean addressIsIspmRequired =
        Optional.ofNullable(saleOrder.getDeliveryAddress())
            .map(Address::getCountry)
            .map(Country::getIsIspmRequired)
            .orElse(false);
    saleOrder.setIsIspmRequired(partnerIsIspmRequired || addressIsIspmRequired);
    values.put("isIspmRequired", saleOrder.getIsIspmRequired());
    return values;
  }

  protected Map<String, Object> setDefaultInvoicedAndDeliveredPartnersAndAddresses(
      SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    if (!appBase.getActivatePartnerRelations()) {
      return values;
    }
    saleOrderSupplychainService.setDefaultInvoicedAndDeliveredPartnersAndAddresses(saleOrder);
    values.put("invoicedPartner", saleOrder.getInvoicedPartner());
    values.put("deliveredPartner", saleOrder.getDeliveredPartner());
    values.put("mainInvoicingAddress", saleOrder.getMainInvoicingAddress());
    values.put("mainInvoicingAddressStr", saleOrder.getMainInvoicingAddressStr());
    values.put("deliveryAddress", saleOrder.getDeliveryAddress());
    values.put("deliveryAddressStr", saleOrder.getDeliveryAddressStr());
    return values;
  }

  @Override
  protected Map<String, Object> getComputeSaleOrderMap(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = super.getComputeSaleOrderMap(saleOrder);
    values.put("standardDelay", saleOrder.getStandardDelay());
    values.put("amountToBeSpreadOverTheTimetable", saleOrder.getAmountToBeSpreadOverTheTimetable());

    return values;
  }
}
