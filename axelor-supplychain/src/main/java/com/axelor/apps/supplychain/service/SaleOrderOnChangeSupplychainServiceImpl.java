package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderUserService;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.repo.PartnerStockSettingsRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderOnChangeSupplychainServiceImpl extends SaleOrderOnChangeServiceImpl {

  protected AccountConfigService accountConfigService;
  protected AccountingSituationService accountingSituationService;
  protected PartnerStockSettingsRepository partnerStockSettingsRepository;

  @Inject
  public SaleOrderOnChangeSupplychainServiceImpl(
      PartnerService partnerService,
      SaleOrderUserService saleOrderUserService,
      AccountConfigService accountConfigService,
      AccountingSituationService accountingSituationService,
      PartnerStockSettingsRepository partnerStockSettingsRepository) {
    super(partnerService, saleOrderUserService);
    this.accountConfigService = accountConfigService;
    this.accountingSituationService = accountingSituationService;
    this.partnerStockSettingsRepository = partnerStockSettingsRepository;
  }

  @Override
  public Map<String, Object> partnerOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = super.partnerOnChange(saleOrder);
    values.putAll(getPaymentCondition(saleOrder));
    values.putAll(getFiscalPosition(saleOrder));
    values.putAll(getPaymentMode(saleOrder));
    values.putAll(getIncoterm(saleOrder));
    values.putAll(getCompanyBankDetails(saleOrder));
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

  protected Map<String, Object> getIncoterm(SaleOrder saleOrder) throws AxelorException {
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

  protected Map<String, Object> getFiscalPosition(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    FiscalPosition fiscalPosition = null;
    if (clientPartner.getFiscalPosition() != null) {
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
    if (clientPartner != null && clientPartner.getPaymentCondition() != null) {
      paymentMode = clientPartner.getInPaymentMode();
    } else {
      paymentMode = accountConfigService.getAccountConfig(company).getInPaymentMode();
    }
    saleOrder.setPaymentMode(paymentMode);
    values.put("paymentMode", saleOrder.getPaymentMode());
    return values;
  }

  protected Map<String, Object> getCompanyBankDetails(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    saleOrder.setCompanyBankDetails(
        accountingSituationService.getCompanySalesBankDetails(company, clientPartner));
    values.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    return values;
  }
}
