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
package com.axelor.apps.sale.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderBankDetailsService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderDateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderUserService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineFiscalPositionService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderOnChangeServiceImpl implements SaleOrderOnChangeService {

  protected PartnerService partnerService;
  protected SaleOrderUserService saleOrderUserService;
  protected SaleOrderService saleOrderService;
  protected PartnerPriceListService partnerPriceListService;
  protected SaleOrderCreateService saleOrderCreateService;
  protected SaleOrderProductPrintingService saleOrderProductPrintingService;
  protected SaleOrderLineFiscalPositionService saleOrderLineFiscalPositionService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleConfigService saleConfigService;
  protected SaleOrderBankDetailsService saleOrderBankDetailsService;
  protected AppBaseService appBaseService;
  protected SaleOrderDateService saleOrderDateService;

  @Inject
  public SaleOrderOnChangeServiceImpl(
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
      SaleOrderDateService saleOrderDateService) {
    this.partnerService = partnerService;
    this.saleOrderUserService = saleOrderUserService;
    this.saleOrderService = saleOrderService;
    this.partnerPriceListService = partnerPriceListService;
    this.saleOrderCreateService = saleOrderCreateService;
    this.saleOrderProductPrintingService = saleOrderProductPrintingService;
    this.saleOrderLineFiscalPositionService = saleOrderLineFiscalPositionService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleConfigService = saleConfigService;
    this.saleOrderBankDetailsService = saleOrderBankDetailsService;
    this.appBaseService = appBaseService;
    this.saleOrderDateService = saleOrderDateService;
  }

  @Override
  public Map<String, Object> partnerOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    values.putAll(getDefaultValues(saleOrder));
    values.putAll(getAddresses(saleOrder));
    values.putAll(getClientPartnerValues(saleOrder));
    values.putAll(getPriceList(saleOrder));
    values.putAll(getHideDiscount(saleOrder));
    values.putAll(getAddressStr(saleOrder));
    values.putAll(getContactPartner(saleOrder));
    values.putAll(updateSaleOrderLineList(saleOrder));
    values.putAll(saleOrderProductPrintingService.getGroupProductsOnPrintings(saleOrder));
    values.putAll(updateLinesAfterFiscalPositionChange(saleOrder));
    values.putAll(getComputeSaleOrderMap(saleOrder));
    values.putAll(getEndOfValidityDate(saleOrder));
    return values;
  }

  @Override
  public Map<String, Object> companyOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    values.putAll(getCompanyConfig(saleOrder));
    values.putAll(saleOrderBankDetailsService.getBankDetails(saleOrder));
    values.putAll(getEndOfValidityDate(saleOrder));
    values.putAll(resetTradingName(saleOrder));
    values.putAll(getInAti(saleOrder));
    return values;
  }

  protected Map<String, Object> getDefaultValues(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    saleOrder.setSalespersonUser(saleOrderUserService.getUser(saleOrder));
    values.put("salespersonUser", saleOrder.getSalespersonUser());
    saleOrder.setTeam(saleOrderUserService.getTeam(saleOrder));
    values.put("team", saleOrder.getTeam());

    return values;
  }

  protected Map<String, Object> getClientPartnerValues(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    if (clientPartner != null) {
      saleOrder.setCurrency(clientPartner.getCurrency());
      values.put("currency", saleOrder.getCurrency());
      saleOrder.setDeliveryComments(clientPartner.getDeliveryComments());
      values.put("deliveryComments", saleOrder.getDeliveryComments());
      saleOrder.setDescription(clientPartner.getSaleOrderComments());
      values.put("description", saleOrder.getDescription());
      saleOrder.setPickingOrderComments(clientPartner.getPickingOrderComments());
      values.put("pickingOrderComments", saleOrder.getPickingOrderComments());
      saleOrder.setProformaComments(clientPartner.getProformaComments());
      values.put("proformaComments", saleOrder.getProformaComments());
    }
    return values;
  }

  protected Map<String, Object> getAddresses(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    if (clientPartner != null) {
      saleOrder.setMainInvoicingAddress(partnerService.getInvoicingAddress(clientPartner));
      saleOrder.setDeliveryAddress(partnerService.getDeliveryAddress(clientPartner));
    }
    values.put("mainInvoicingAddress", saleOrder.getMainInvoicingAddress());
    values.put("deliveryAddress", saleOrder.getDeliveryAddress());
    return values;
  }

  protected Map<String, Object> getHideDiscount(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    PriceList priceList = saleOrder.getPriceList();
    if (priceList != null) {
      saleOrder.setHideDiscount(priceList.getHideDiscount());
    } else {
      saleOrder.setHideDiscount(false);
    }
    values.put("hideDiscount", saleOrder.getHideDiscount());
    return values;
  }

  protected Map<String, Object> getAddressStr(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    saleOrderService.computeAddressStr(saleOrder);
    values.put("mainInvoicingAddressStr", saleOrder.getMainInvoicingAddressStr());
    values.put("deliveryAddressStr", saleOrder.getDeliveryAddressStr());
    return values;
  }

  protected Map<String, Object> getPriceList(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    if (saleOrder.getTemplate() || CollectionUtils.isNotEmpty(saleOrder.getSaleOrderLineList())) {
      return values;
    }
    Partner clientPartner = saleOrder.getClientPartner();
    if (clientPartner != null) {
      saleOrder.setPriceList(
          partnerPriceListService.getDefaultPriceList(
              clientPartner, PriceListRepository.TYPE_SALE));
    }
    values.put("priceList", saleOrder.getPriceList());
    return values;
  }

  protected Map<String, Object> getContactPartner(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    saleOrder.setContactPartner(null);
    if (clientPartner != null) {
      Set<Partner> contactPartnerSet = clientPartner.getContactPartnerSet();
      if (CollectionUtils.isNotEmpty(contactPartnerSet) && contactPartnerSet.size() == 1) {
        saleOrder.setContactPartner(contactPartnerSet.stream().findFirst().orElse(null));
      }
    }
    values.put("contactPartner", saleOrder.getContactPartner());
    return values;
  }

  protected Map<String, Object> updateSaleOrderLineList(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    if (saleOrder.getTemplate()) {
      saleOrderCreateService.updateSaleOrderLineList(saleOrder);
    }
    values.put("saleOrderLineList", saleOrder.getSaleOrderLineList());
    return values;
  }

  protected Map<String, Object> updateLinesAfterFiscalPositionChange(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    saleOrderLineFiscalPositionService.updateLinesAfterFiscalPositionChange(saleOrder);
    values.put("saleOrderLineList", saleOrder.getSaleOrderLineList());
    return values;
  }

  protected Map<String, Object> getComputeSaleOrderMap(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    saleOrderComputeService.computeSaleOrder(saleOrder);
    values.put("saleOrderLineTaxList", saleOrder.getSaleOrderLineTaxList());
    values.put("saleOrderLineList", saleOrder.getSaleOrderLineList());
    values.put("exTaxTotal", saleOrder.getExTaxTotal());
    values.put("companyExTaxTotal", saleOrder.getCompanyExTaxTotal());
    values.put("taxTotal", saleOrder.getTaxTotal());
    values.put("inTaxTotal", saleOrder.getInTaxTotal());
    values.put("advanceTotal", saleOrder.getAdvanceTotal());

    return values;
  }

  protected Map<String, Object> getEndOfValidityDate(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    saleOrderDateService.computeEndOfValidityDate(saleOrder);
    values.put("duration", saleOrder.getDuration());
    values.put("endOfValidityDate", saleOrder.getEndOfValidityDate());
    return values;
  }

  protected Map<String, Object> getCompanyConfig(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    Company company = saleOrder.getCompany();
    if (company != null) {
      SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
      saleOrder.setDuration(saleConfig.getDefaultValidityDuration());
      values.put("duration", saleOrder.getDuration());

      saleOrder.setPrintingSettings(company.getPrintingSettings());
      values.put("printingSettings", saleOrder.getPrintingSettings());

      saleOrder.setCurrency(company.getCurrency());
      values.put("currency", saleOrder.getCurrency());

      saleOrder.setCreationDate(appBaseService.getTodayDate(company));
      values.put("creationDate", saleOrder.getCreationDate());
    }

    return values;
  }

  protected Map<String, Object> resetTradingName(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    if (appBase.getEnableTradingNamesManagement()) {
      saleOrder.setTradingName(null);
      values.put("tradingName", saleOrder.getTradingName());
    }

    return values;
  }

  protected Map<String, Object> getInAti(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    saleOrder.setInAti(saleOrderService.getInAti(saleOrder, saleOrder.getCompany()));
    values.put("inAti", saleOrder.getInAti());
    return values;
  }
}
