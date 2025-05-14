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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderOutsourceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ManufOrderCreatePurchaseOrderServiceImpl
    implements ManufOrderCreatePurchaseOrderService {

  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderCreateService purchaseOrderCreateService;
  protected StockConfigProductionService stockConfigProductionService;
  protected ManufOrderRepository manufOrderRepository;
  protected OperationOrderOutsourceService operationOrderOutsourceService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;

  @Inject
  public ManufOrderCreatePurchaseOrderServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      StockConfigProductionService stockConfigProductionService,
      ManufOrderRepository manufOrderRepository,
      OperationOrderOutsourceService operationOrderOutsourceService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      AppBaseService appBaseService,
      AppProductionService appProductionService) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderCreateService = purchaseOrderCreateService;
    this.stockConfigProductionService = stockConfigProductionService;
    this.manufOrderRepository = manufOrderRepository;
    this.operationOrderOutsourceService = operationOrderOutsourceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createPurchaseOrders(ManufOrder manufOrder) throws AxelorException {

    List<Partner> outsourcePartners = getOutsourcePartnersForGenerationPO(manufOrder);

    List<PurchaseOrder> generatedPurchaseOrders = new ArrayList<>();
    for (Partner outsourcePartner : outsourcePartners) {
      PurchaseOrder purchaseOrder =
          purchaseOrderCreateService.createPurchaseOrder(
              null,
              manufOrder.getCompany(),
              null,
              null,
              null,
              manufOrder.getManufOrderSeq(),
              null,
              null,
              null,
              outsourcePartner,
              null);

      purchaseOrder.setOutsourcingOrder(true);
      purchaseOrder.setTypeSelect(PurchaseOrderRepository.TYPE_SUBCONTRACTING);
      purchaseOrder.setFiscalPosition(outsourcePartner.getFiscalPosition());
      StockConfig stockConfig =
          stockConfigProductionService.getStockConfig(manufOrder.getCompany());
      if (manufOrder.getCompany() != null && manufOrder.getCompany().getStockConfig() != null) {
        purchaseOrder.setStockLocation(
            stockConfigProductionService.getReceiptDefaultStockLocation(stockConfig));
      }
      purchaseOrder.setFromStockLocation(
          stockConfigProductionService.getVirtualOutsourcingStockLocation(stockConfig));

      this.setPurchaseOrderSupplierDetails(purchaseOrder);

      generatedPurchaseOrders.add(purchaseOrder);
      manufOrder.addPurchaseOrderSetItem(purchaseOrder);
    }

    for (PurchaseOrder purchaseOrder : generatedPurchaseOrders) {

      // When manufOrder is fully outsourced
      if (manufOrder.getOutsourcing()) {
        operationOrderOutsourceService.createPurchaseOrderLines(
            manufOrder,
            manufOrder.getProdProcess().getGeneratedPurchaseOrderProductSet(),
            purchaseOrder);
      } else {
        List<OperationOrder> operationOrderGeneratePurchaseOrderList =
            getOperationOrdersForGeneratedPOs(manufOrder, purchaseOrder);

        for (OperationOrder operationOrder : operationOrderGeneratePurchaseOrderList) {
          operationOrderOutsourceService.createPurchaseOrderLines(operationOrder, purchaseOrder);
        }
      }

      purchaseOrderService.computePurchaseOrder(purchaseOrder);
    }

    manufOrderRepository.save(manufOrder);
  }

  protected List<OperationOrder> getOperationOrdersForGeneratedPOs(
      ManufOrder manufOrder, PurchaseOrder purchaseOrder) {

    return manufOrder.getOperationOrderList().stream()
        .filter(
            oo ->
                oo.getProdProcessLine().getGeneratePurchaseOrderOnMoPlanning()
                    && purchaseOrder
                        .getSupplierPartner()
                        .equals(
                            operationOrderOutsourceService.getOutsourcePartner(oo).orElse(null)))
        .collect(Collectors.toList());
  }

  protected PurchaseOrder setPurchaseOrderSupplierDetails(PurchaseOrder purchaseOrder)
      throws AxelorException {
    Partner supplierPartner = purchaseOrder.getSupplierPartner();

    if (supplierPartner != null) {
      purchaseOrder.setCurrency(supplierPartner.getCurrency());
      purchaseOrder.setShipmentMode(supplierPartner.getShipmentMode());
      purchaseOrder.setFreightCarrierMode(supplierPartner.getFreightCarrierMode());
      purchaseOrder.setNotes(supplierPartner.getPurchaseOrderComments());

      if (supplierPartner.getOutPaymentCondition() != null) {
        purchaseOrder.setPaymentCondition(supplierPartner.getOutPaymentCondition());
      } else {
        purchaseOrder.setPaymentCondition(
            purchaseOrder.getCompany().getAccountConfig().getDefPaymentCondition());
      }

      if (supplierPartner.getOutPaymentMode() != null) {
        purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
      } else {
        purchaseOrder.setPaymentMode(
            purchaseOrder.getCompany().getAccountConfig().getOutPaymentMode());
      }

      if (supplierPartner.getContactPartnerSet().size() == 1) {
        purchaseOrder.setContactPartner(supplierPartner.getContactPartnerSet().iterator().next());
      }

      purchaseOrder.setCompanyBankDetails(
          Beans.get(BankDetailsService.class)
              .getDefaultCompanyBankDetails(
                  purchaseOrder.getCompany(),
                  purchaseOrder.getPaymentMode(),
                  purchaseOrder.getSupplierPartner(),
                  null));

      purchaseOrder.setPriceList(
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(
                  purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE));

      if (Beans.get(AppSupplychainService.class).isApp("supplychain")
          && Beans.get(AppSupplychainService.class).getAppSupplychain().getIntercoFromPurchase()
          && !purchaseOrder.getCreatedByInterco()
          && (Beans.get(CompanyRepository.class)
                  .all()
                  .filter("self.partner = ?", supplierPartner)
                  .fetchOne()
              != null)) {
        purchaseOrder.setInterco(true);
      }
    }

    return purchaseOrder;
  }

  protected List<Partner> getOutsourcePartnersForGenerationPO(ManufOrder manufOrder) {

    if (manufOrder.getOutsourcing()
        && manufOrder.getProdProcess().getGeneratePurchaseOrderOnMoPlanning()
        && manufOrderOutsourceService.getOutsourcePartner(manufOrder).isPresent()) {
      return List.of(manufOrderOutsourceService.getOutsourcePartner(manufOrder).get());
    } else {
      return manufOrder.getOperationOrderList().stream()
          .filter(
              oo ->
                  oo.getOutsourcing()
                      && oo.getProdProcessLine().getGeneratePurchaseOrderOnMoPlanning())
          .map(oo -> operationOrderOutsourceService.getOutsourcePartner(oo))
          .map(optPartner -> optPartner.orElse(null))
          .filter(Objects::nonNull)
          .distinct()
          .collect(Collectors.toList());
    }
  }
}
