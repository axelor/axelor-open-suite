package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderOnChangeServiceImpl implements SaleOrderOnChangeService {

  protected PartnerService partnerService;
  protected SaleOrderUserService saleOrderUserService;

  @Inject
  public SaleOrderOnChangeServiceImpl(
      PartnerService partnerService, SaleOrderUserService saleOrderUserService) {
    this.partnerService = partnerService;
    this.saleOrderUserService = saleOrderUserService;
  }

  @Override
  public Map<String, Object> partnerOnChange(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    values.putAll(getDefaultValues(saleOrder));
    values.putAll(getAddresses(saleOrder));
    values.putAll(getClientPartnerValues(saleOrder));
    values.putAll(getHideDiscount(saleOrder));
    return values;
  }

  protected Map<String, Object> getDefaultValues(SaleOrder saleOrder) throws AxelorException {
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

  protected Map<String, Object> getAddresses(SaleOrder saleOrder) throws AxelorException {
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

  protected Map<String, Object> getHideDiscount(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    PriceList priceList = saleOrder.getPriceList();
    if (priceList != null) {
      saleOrder.setHideDiscount(priceList.getHideDiscount());
    }
    values.put("hideDiscount", saleOrder.getHideDiscount());
    return values;
  }
}
