/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.suppliermanagement.service;

import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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

  @Inject private PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;

  @Inject private PurchaseOrderLineService purchaseOrderLineService;

  @Inject protected PurchaseOrderRepository poRepo;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void generateAllSuppliersRequests(PurchaseOrder purchaseOrder) {

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
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void generateSuppliersRequests(PurchaseOrderLine purchaseOrderLine) {
    this.generateSuppliersRequests(purchaseOrderLine, purchaseOrderLine.getPurchaseOrder());
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void generateSuppliersRequests(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {

    if (purchaseOrder == null) {
      return;
    }

    Product product = purchaseOrderLine.getProduct();
    Company company = purchaseOrder.getCompany();

    if (product != null && product.getSupplierCatalogList() != null) {

      for (SupplierCatalog supplierCatalog : product.getSupplierCatalogList()) {
        Partner supplierPartner = supplierCatalog.getSupplierPartner();
        Blocking blocking =
            Beans.get(BlockingService.class)
                .getBlocking(supplierPartner, company, BlockingRepository.PURCHASE_BLOCKING);
        if (blocking == null) {
          purchaseOrderLine.addPurchaseOrderSupplierLineListItem(
              purchaseOrderSupplierLineService.create(supplierPartner, supplierCatalog.getPrice()));
        }
      }
    }

    Beans.get(PurchaseOrderLineRepository.class).save(purchaseOrderLine);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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
            I18n.get(IExceptionMessage.SO_PURCHASE_1),
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

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void createPurchaseOrder(
      Partner supplierPartner,
      List<PurchaseOrderLine> purchaseOrderLineList,
      PurchaseOrder parentPurchaseOrder)
      throws AxelorException {

    LOG.debug(
        "Création d'une commande fournisseur depuis le devis fournisseur : {} et le fournisseur : {}",
        parentPurchaseOrder.getPurchaseOrderSeq(),
        supplierPartner.getFullName());

    PurchaseOrder purchaseOrder =
        purchaseOrderServiceSupplychainImpl.createPurchaseOrder(
            AuthUtils.getUser(),
            parentPurchaseOrder.getCompany(),
            null,
            supplierPartner.getCurrency(),
            null,
            parentPurchaseOrder.getPurchaseOrderSeq(),
            parentPurchaseOrder.getExternalReference(),
            Beans.get(StockLocationService.class)
                .getDefaultReceiptStockLocation(parentPurchaseOrder.getCompany()),
            Beans.get(AppBaseService.class).getTodayDate(),
            Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(supplierPartner, PriceListRepository.TYPE_PURCHASE),
            supplierPartner,
            parentPurchaseOrder.getTradingName());

    purchaseOrder.setParentPurchaseOrder(parentPurchaseOrder);

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

      purchaseOrder.addPurchaseOrderLineListItem(
          this.createPurchaseOrderLine(purchaseOrder, purchaseOrderLine));
    }

    purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);

    purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_REQUESTED);
    purchaseOrder.setReceiptState(IPurchaseOrder.STATE_NOT_RECEIVED);

    poRepo.save(purchaseOrder);
  }

  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {

    LOG.debug(
        "Création d'une ligne de commande fournisseur pour le produit : {}",
        new Object[] {purchaseOrderLine.getProductName()});

    return purchaseOrderLineService.createPurchaseOrderLine(
        purchaseOrder,
        purchaseOrderLine.getProduct(),
        purchaseOrderLine.getProductName(),
        purchaseOrderLine.getDescription(),
        purchaseOrderLine.getQty(),
        purchaseOrderLine.getUnit());
  }
}
