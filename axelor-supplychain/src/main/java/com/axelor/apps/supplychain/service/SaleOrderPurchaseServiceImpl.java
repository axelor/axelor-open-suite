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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderPurchaseServiceImpl implements SaleOrderPurchaseService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;
  protected PurchaseOrderLineServiceSupplyChain purchaseOrderLineServiceSupplychain;
  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected PurchaseConfigService purchaseConfigService;
  protected AppBaseService appBaseService;
  protected PartnerPriceListService partnerPriceListService;
  protected SupplierCatalogService supplierCatalogService;

  @Inject
  public SaleOrderPurchaseServiceImpl(
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PurchaseOrderLineServiceSupplyChain purchaseOrderLineServiceSupplychain,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseConfigService purchaseConfigService,
      AppBaseService appBaseService,
      PartnerPriceListService partnerPriceListService,
      SupplierCatalogService supplierCatalogService) {
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
    this.purchaseOrderLineServiceSupplychain = purchaseOrderLineServiceSupplychain;
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseConfigService = purchaseConfigService;
    this.appBaseService = appBaseService;
    this.partnerPriceListService = partnerPriceListService;
    this.supplierCatalogService = supplierCatalogService;
  }

  @Override
  public void createPurchaseOrders(SaleOrder saleOrder) throws AxelorException {

    Map<Partner, List<SaleOrderLine>> saleOrderLinesBySupplierPartner =
        this.splitBySupplierPartner(saleOrder.getSaleOrderLineList());

    for (Partner supplierPartner : saleOrderLinesBySupplierPartner.keySet()) {

      this.createPurchaseOrder(
          supplierPartner, saleOrderLinesBySupplierPartner.get(supplierPartner), saleOrder);
    }
  }

  @Override
  public Map<Partner, List<SaleOrderLine>> splitBySupplierPartner(
      List<SaleOrderLine> saleOrderLineList) throws AxelorException {

    Map<Partner, List<SaleOrderLine>> saleOrderLinesBySupplierPartner = new HashMap<>();

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {

      if (saleOrderLine.getSaleSupplySelect() == ProductRepository.SALE_SUPPLY_PURCHASE) {

        Partner supplierPartner = saleOrderLine.getSupplierPartner();

        if (supplierPartner == null) {
          throw new AxelorException(
              saleOrderLine,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(SupplychainExceptionMessage.SO_PURCHASE_1),
              saleOrderLine.getProductName());
        }

        if (!saleOrderLinesBySupplierPartner.containsKey(supplierPartner)) {
          saleOrderLinesBySupplierPartner.put(supplierPartner, new ArrayList<>());
        }

        saleOrderLinesBySupplierPartner.get(supplierPartner).add(saleOrderLine);
      }
    }

    return saleOrderLinesBySupplierPartner;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder createPurchaseOrder(
      Partner supplierPartner, List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder)
      throws AxelorException {

    LOG.debug("Creation of a purchase order for the sale order : {}", saleOrder.getSaleOrderSeq());

    PurchaseOrder purchaseOrder =
        createPurchaseOrderAndLines(supplierPartner, saleOrderLineList, saleOrder);
    purchaseOrderRepository.save(purchaseOrder);

    return purchaseOrder;
  }

  protected PurchaseOrder createPurchaseOrderAndLines(
      Partner supplierPartner, List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder)
      throws AxelorException {
    PurchaseOrder purchaseOrder = createPurchaseOrder(supplierPartner, saleOrder);
    createPurchaseOrderLines(saleOrderLineList, purchaseOrder);
    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    return purchaseOrder;
  }

  protected PurchaseOrder createPurchaseOrder(Partner supplierPartner, SaleOrder saleOrder)
      throws AxelorException {
    PurchaseOrder purchaseOrder =
        purchaseOrderSupplychainService.createPurchaseOrder(
            AuthUtils.getUser(),
            saleOrder.getCompany(),
            supplierPartner.getContactPartnerSet().size() == 1
                ? supplierPartner.getContactPartnerSet().iterator().next()
                : null,
            supplierPartner.getCurrency(),
            null,
            saleOrder.getSaleOrderSeq(),
            saleOrder.getExternalReference(),
            saleOrder.getDirectOrderLocation()
                ? saleOrder.getStockLocation()
                : purchaseOrderSupplychainService.getStockLocation(
                    supplierPartner, saleOrder.getCompany()),
            appBaseService.getTodayDate(saleOrder.getCompany()),
            partnerPriceListService.getDefaultPriceList(
                supplierPartner, PriceListRepository.TYPE_PURCHASE),
            supplierPartner,
            saleOrder.getTradingName());

    purchaseOrder.setGeneratedSaleOrderId(saleOrder.getId());
    purchaseOrder.setGroupProductsOnPrintings(supplierPartner.getGroupProductsOnPrintings());

    Integer atiChoice =
        purchaseConfigService
            .getPurchaseConfig(saleOrder.getCompany())
            .getPurchaseOrderInAtiSelect();
    if (atiChoice == AccountConfigRepository.INVOICE_ATI_ALWAYS
        || atiChoice == AccountConfigRepository.INVOICE_ATI_DEFAULT) {
      purchaseOrder.setInAti(true);
    } else {
      purchaseOrder.setInAti(false);
    }

    purchaseOrder.setNotes(supplierPartner.getPurchaseOrderComments());
    return purchaseOrder;
  }

  protected void createPurchaseOrderLines(
      List<SaleOrderLine> saleOrderLineList, PurchaseOrder purchaseOrder) throws AxelorException {
    Collections.sort(saleOrderLineList, Comparator.comparing(SaleOrderLine::getSequence));
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      purchaseOrder.addPurchaseOrderLineListItem(
          purchaseOrderLineServiceSupplychain.createPurchaseOrderLine(
              purchaseOrder, saleOrderLine));
    }
  }
}
