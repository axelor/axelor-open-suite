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
package com.axelor.apps.suppliermanagement.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderSupplierService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private PurchaseOrderSupplierLineService purchaseOrderSupplierLineService;

  @Inject private PurchaseOrderSupplychainService purchaseOrderSupplychainService;

  @Inject private PurchaseOrderService purchaseOrderService;

  @Inject private PurchaseOrderLineService purchaseOrderLineService;

  @Inject protected PurchaseOrderRepository poRepo;

  @Inject protected SupplierCatalogService supplierCatalogService;

  @Transactional(rollbackOn = {Exception.class})
  public void generateAllSuppliersRequests(PurchaseOrder purchaseOrder) throws AxelorException {

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {

      this.generateSuppliersRequests(purchaseOrderLine);
    }
    poRepo.save(purchaseOrder);
  }

  /**
   * Generate supplier requests for one PurchaseOrderLine. Make sure to only call this function when
   * you are sure the line passed as argument has a valid purchaseOrder. Else, use the
   * generateSuppliersRequests(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
   * format below.
   *
   * @param purchaseOrderLine
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void generateSuppliersRequests(PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    this.generateSuppliersRequests(purchaseOrderLine, purchaseOrderLine.getPurchaseOrder());
  }

  @Transactional(rollbackOn = {Exception.class})
  public void generateSuppliersRequests(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder == null) {
      return;
    }

    Product product = purchaseOrderLine.getProduct();
    Company company = purchaseOrder.getCompany();

    if (Beans.get(AppPurchaseService.class).getAppPurchase().getManageSupplierCatalog()
        && product != null
        && product.getSupplierCatalogList() != null) {

      for (SupplierCatalog supplierCatalog : product.getSupplierCatalogList()) {
        Partner supplierPartner = supplierCatalog.getSupplierPartner();
        Blocking blocking =
            Beans.get(BlockingService.class)
                .getBlocking(supplierPartner, company, BlockingRepository.PURCHASE_BLOCKING);
        if (blocking == null) {
          purchaseOrderLine.addPurchaseOrderSupplierLineListItem(
              purchaseOrderSupplierLineService.create(
                  supplierPartner,
                  supplierCatalogService.getPurchasePrice(supplierCatalog, company)));
        }
      }
    }

    Beans.get(PurchaseOrderLineRepository.class).save(purchaseOrderLine);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void generateSuppliersPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getPurchaseOrderLineList() == null) {
      return;
    }

    Map<Partner, List<PurchaseOrderLine>> purchaseOrderLinesBySupplierPartner =
        this.splitBySupplierPartner(purchaseOrder.getPurchaseOrderLineList());

    for (Partner supplierPartner : purchaseOrderLinesBySupplierPartner.keySet()) {

      this.createPurchaseOrder(
          supplierPartner, purchaseOrderLinesBySupplierPartner.get(supplierPartner), purchaseOrder);
    }

    poRepo.save(purchaseOrder);
  }

  public Map<Partner, List<PurchaseOrderLine>> splitBySupplierPartner(
      List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException {

    Map<Partner, List<PurchaseOrderLine>> purchaseOrderLinesBySupplierPartner = new HashMap<>();

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      Partner supplierPartner = purchaseOrderLine.getSupplierPartner();

      if (supplierPartner == null) {
        throw new AxelorException(
            purchaseOrderLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.SO_PURCHASE_1),
            purchaseOrderLine.getProductName());
      }

      if (!purchaseOrderLinesBySupplierPartner.containsKey(supplierPartner)) {
        purchaseOrderLinesBySupplierPartner.put(
            supplierPartner, new ArrayList<PurchaseOrderLine>());
      }

      purchaseOrderLinesBySupplierPartner.get(supplierPartner).add(purchaseOrderLine);
    }

    return purchaseOrderLinesBySupplierPartner;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createPurchaseOrder(
      Partner supplierPartner,
      List<PurchaseOrderLine> purchaseOrderLineList,
      PurchaseOrder parentPurchaseOrder)
      throws AxelorException {

    LOG.debug(
        "Creation of a purchase order from : {} and the supplier : {}",
        parentPurchaseOrder.getPurchaseOrderSeq(),
        supplierPartner.getFullName());

    PurchaseOrder purchaseOrder =
        purchaseOrderSupplychainService.createPurchaseOrder(
            AuthUtils.getUser(),
            parentPurchaseOrder.getCompany(),
            null,
            supplierPartner.getCurrency(),
            null,
            parentPurchaseOrder.getPurchaseOrderSeq(),
            parentPurchaseOrder.getExternalReference(),
            Beans.get(PurchaseOrderSupplychainService.class)
                .getStockLocation(supplierPartner, parentPurchaseOrder.getCompany()),
            Beans.get(AppBaseService.class).getTodayDate(parentPurchaseOrder.getCompany()),
            Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(supplierPartner, PriceListRepository.TYPE_PURCHASE),
            supplierPartner,
            parentPurchaseOrder.getTradingName());

    purchaseOrder.setParentPurchaseOrder(parentPurchaseOrder);

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      purchaseOrder.addPurchaseOrderLineListItem(
          this.createPurchaseOrderLine(purchaseOrder, purchaseOrderLine));
    }

    purchaseOrderService.computePurchaseOrder(purchaseOrder);

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_REQUESTED);
    purchaseOrder.setReceiptState(PurchaseOrderRepository.STATE_NOT_RECEIVED);

    poRepo.save(purchaseOrder);
  }

  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {

    LOG.debug(
        "Creation of a purchase order line for the product : {}",
        purchaseOrderLine.getProductName());

    return purchaseOrderLineService.createPurchaseOrderLine(
        purchaseOrder,
        purchaseOrderLine.getProduct(),
        purchaseOrderLine.getProductName(),
        purchaseOrderLine.getDescription(),
        purchaseOrderLine.getQty(),
        purchaseOrderLine.getUnit());
  }
}
