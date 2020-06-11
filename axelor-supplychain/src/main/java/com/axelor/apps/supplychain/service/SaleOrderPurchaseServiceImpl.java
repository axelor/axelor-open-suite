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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
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

public class SaleOrderPurchaseServiceImpl implements SaleOrderPurchaseService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;
  protected PurchaseOrderLineServiceSupplychainImpl purchaseOrderLineServiceSupplychainImpl;

  @Inject
  public SaleOrderPurchaseServiceImpl(
      PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl,
      PurchaseOrderLineServiceSupplychainImpl purchaseOrderLineServiceSupplychainImpl) {
    this.purchaseOrderServiceSupplychainImpl = purchaseOrderServiceSupplychainImpl;
    this.purchaseOrderLineServiceSupplychainImpl = purchaseOrderLineServiceSupplychainImpl;
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
              I18n.get(IExceptionMessage.SO_PURCHASE_1),
              saleOrderLine.getProductName());
        }

        if (!saleOrderLinesBySupplierPartner.containsKey(supplierPartner)) {
          saleOrderLinesBySupplierPartner.put(supplierPartner, new ArrayList<SaleOrderLine>());
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

    LOG.debug(
        "Création d'une commande fournisseur pour le devis client : {}",
        saleOrder.getSaleOrderSeq());

    PurchaseOrder purchaseOrder =
        purchaseOrderServiceSupplychainImpl.createPurchaseOrder(
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
                : Beans.get(StockLocationService.class)
                    .getDefaultReceiptStockLocation(saleOrder.getCompany()),
            Beans.get(AppBaseService.class).getTodayDate(),
            Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(supplierPartner, PriceListRepository.TYPE_PURCHASE),
            supplierPartner,
            saleOrder.getTradingName());

    purchaseOrder.setGeneratedSaleOrderId(saleOrder.getId());
    purchaseOrder.setGroupProductsOnPrintings(supplierPartner.getGroupProductsOnPrintings());


    Integer atiChoice =
        Beans.get(PurchaseConfigService.class)
            .getPurchaseConfig(saleOrder.getCompany())
            .getPurchaseOrderInAtiSelect();
    if (atiChoice == AccountConfigRepository.INVOICE_ATI_ALWAYS
        || atiChoice == AccountConfigRepository.INVOICE_ATI_DEFAULT) {
      purchaseOrder.setInAti(true);
    } else {
      purchaseOrder.setInAti(false);
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      purchaseOrder.addPurchaseOrderLineListItem(
          purchaseOrderLineServiceSupplychainImpl.createPurchaseOrderLine(
              purchaseOrder, saleOrderLine));
    }

    purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);

    purchaseOrder.setNotes(supplierPartner.getPurchaseOrderComments());

    Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);

    return purchaseOrder;
  }
}
