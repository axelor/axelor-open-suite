/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.service.saleorder.views.SaleOrderAttrsService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderViewServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplyChainConfigRepository;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.studio.db.AppSale;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderViewSupplychainServiceImpl extends SaleOrderViewServiceImpl {

  protected StockMoveRepository stockMoveRepository;
  protected CompanyRepository companyRepository;
  protected SupplyChainConfigService supplyChainConfigService;

  @Inject
  public SaleOrderViewSupplychainServiceImpl(
      SaleConfigService saleConfigService,
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      SaleOrderAttrsService saleOrderAttrsService,
      StockMoveRepository stockMoveRepository,
      CompanyRepository companyRepository,
      SupplyChainConfigService supplyChainConfigService) {
    super(
        saleConfigService,
        appBaseService,
        saleOrderRepository,
        appSaleService,
        saleOrderAttrsService);
    this.stockMoveRepository = stockMoveRepository;
    this.companyRepository = companyRepository;
    this.supplyChainConfigService = supplyChainConfigService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getOnNewAttrs(saleOrder);
    MapTools.addMap(attrs, hideAvailability(saleOrder));
    MapTools.addMap(attrs, hideAvailabilityLabel(saleOrder));
    MapTools.addMap(attrs, hideInterco(saleOrder));
    MapTools.addMap(attrs, hideTimetable(saleOrder));
    MapTools.addMap(attrs, hideManagedLinesFields(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getOnLoadAttrs(saleOrder);
    MapTools.addMap(attrs, hideTimetable(saleOrder));
    MapTools.addMap(attrs, hideManagedLinesFields(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getCompanyAttrs(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getCompanyAttrs(saleOrder);
    MapTools.addMap(attrs, hideManagedLinesFields(saleOrder));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getPartnerOnChangeAttrs(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = super.getPartnerOnChangeAttrs(saleOrder);
    MapTools.addMap(attrs, hideInterco(saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideManagedLinesFields(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Company company = saleOrder.getCompany();
    boolean hidden = true;
    if (company != null) {
      SupplyChainConfig config = supplyChainConfigService.getSupplyChainConfig(company);
      hidden =
          config == null
              || config.getOutSmGenerationSelect()
                  != SupplyChainConfigRepository.OUT_SM_GENERATION_MANAGED_LINES;
    }
    attrs.put("saleOrderLineList.qtyToDeliver", Map.of(HIDDEN_ATTRS, hidden));
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
                    ":saleOrderId IN (SELECT so.id FROM self.saleOrderSet so) AND self.availabilityRequest IS TRUE AND self.statusSelect = :plannedStatus")
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

  protected Map<String, Map<String, Object>> hideTimetable(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    boolean isQuotationAndOrderSplit = appSale.getIsQuotationAndOrderSplitEnabled();
    if (!appBaseService.isApp("supplychain") || !isQuotationAndOrderSplit) {
      return attrs;
    }

    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "timetablePanel",
        Map.of(HIDDEN_ATTRS, statusSelect <= SaleOrderRepository.STATUS_FINALIZED_QUOTATION));
    return attrs;
  }
}
