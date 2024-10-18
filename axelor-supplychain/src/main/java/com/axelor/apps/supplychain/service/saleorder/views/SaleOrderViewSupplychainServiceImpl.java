package com.axelor.apps.supplychain.service.saleorder.views;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderViewServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderViewSupplychainServiceImpl extends SaleOrderViewServiceImpl {

  protected StockMoveRepository stockMoveRepository;
  protected CompanyRepository companyRepository;

  @Inject
  public SaleOrderViewSupplychainServiceImpl(
      SaleConfigService saleConfigService,
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      StockMoveRepository stockMoveRepository,
      CompanyRepository companyRepository) {
    super(saleConfigService, appBaseService, saleOrderRepository, appSaleService);
    this.stockMoveRepository = stockMoveRepository;
    this.companyRepository = companyRepository;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getOnNewAttrs(saleOrder);
    MapTools.addMap(attrs, hideAvailability(saleOrder));
    MapTools.addMap(attrs, hideAvailabilityLabel(saleOrder));
    MapTools.addMap(attrs, hideInterco(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnChangeAttrs(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = super.getPartnerOnChangeAttrs(saleOrder);
    MapTools.addMap(attrs, hideInterco(saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideAvailabilityLabel(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    if (!appBaseService.isApp("stock") || saleOrder.getId() == null) {
      return attrs;
    }
    boolean hideLabel =
        stockMoveRepository
                .all()
                .filter(
                    ":saleOrderId MEMBER OF self.saleOrderSet AND self.availabilityRequest IS TRUE AND self.statusSelect = :plannedStatus")
                .bind("saleOrderId", saleOrder.getId())
                .bind("plannedStatus", StockMoveRepository.STATUS_PLANNED)
                .count()
            > 0;
    attrs.put("availabilityRequestLabel", Map.of(HIDDEN_ATTRS, !hideLabel));

    return attrs;
  }

  protected Map<String, Map<String, Object>> hideAvailability(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    if (!appBaseService.isApp("supplychain")) {
      return attrs;
    }

    attrs.put(
        "saleOrderLineList.availableStatus",
        Map.of(
            HIDDEN_ATTRS,
            saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideInterco(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    if (!appBaseService.isApp("supplychain")) {
      return attrs;
    }

    boolean createdByInterco = saleOrder.getCreatedByInterco();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company =
        companyRepository
            .all()
            .filter("self.partner = :clientPartner")
            .bind("clientPartner", clientPartner)
            .fetchOne();
    int statusSelect = saleOrder.getStatusSelect();
    Map<String, Object> attrsMap = new HashMap<>();
    attrsMap.put(HIDDEN_ATTRS, createdByInterco || clientPartner == null || company == null);
    attrsMap.put(READONLY_ATTRS, statusSelect > SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    attrs.put("interco", attrsMap);
    return attrs;
  }
}
