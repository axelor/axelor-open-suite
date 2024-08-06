package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.loyalty.LoyaltyAccountService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineFiscalPositionService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
  protected AppSaleService appSaleService;
  protected LoyaltyAccountService loyaltyAccountService;

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
      AppSaleService appSaleService,
      LoyaltyAccountService loyaltyAccountService) {
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
    this.appSaleService = appSaleService;
    this.loyaltyAccountService = loyaltyAccountService;
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
    if (appSaleService.getAppSale().getEnableLoyalty()) {
      Optional<LoyaltyAccount> loyaltyAccount =
          loyaltyAccountService.getLoyaltyAccount(
              saleOrder.getClientPartner(), saleOrder.getCompany());
      values.put(
          "$loyaltyPoints",
          loyaltyAccount
              .map(LoyaltyAccount::getPointsBalance)
              .map(points -> points.setScale(0, RoundingMode.HALF_UP))
              .orElse(null));
    }
    return values;
  }

  @Override
  public Map<String, Object> companyOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    values.putAll(getCompanyConfig(saleOrder));
    values.putAll(saleOrderBankDetailsService.getBankDetails(saleOrder));
    values.putAll(getEndOfValidityDate(saleOrder));
    resetTradingName(saleOrder);
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
      saleOrder.setDescription(clientPartner.getDescription());
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
    saleOrderService.computeEndOfValidityDate(saleOrder);
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

      saleOrder.setCreationDate(appBaseService.getTodayDate(company));
      values.put("creationDate", saleOrder.getCreationDate());
    }

    return values;
  }

  protected Map<String, Object> resetTradingName(SaleOrder saleOrder) {
    Map<String, Object> values = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    if (!appBase.getEnableTradingNamesManagement()) {
      saleOrder.setTradingName(null);
      values.put("tradingName", saleOrder.getTradingName());
    }

    return values;
  }
}
