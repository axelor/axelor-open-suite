/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.script.ImportPurchaseOrder;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.SupplychainSaleConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ImportSupplyChain {

  @Inject protected PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;

  @Inject protected PurchaseOrderStockServiceImpl purchaseOrderStockServiceImpl;

  @Inject protected InvoiceService invoiceService;

  @Inject protected SaleOrderStockService saleOrderStockService;

  @Inject protected StockMoveRepository stockMoveRepo;

  @Inject protected SaleOrderRepository saleOrderRepo;

  @Inject protected SaleConfigRepository saleConfigRepo;

  @Inject protected SupplychainSaleConfigService configService;

  @Inject protected StockConfigService stockConfigService;

  @Inject protected ImportPurchaseOrder importPurchaseOrder;

  @Inject protected ImportSaleOrder importSaleOrder;

  @SuppressWarnings("rawtypes")
  public Object importSupplyChain(Object bean, Map values) {

    List<SaleConfig> configs = saleConfigRepo.all().fetch();
    for (SaleConfig config : configs) {
      configService.updateCustomerCredit(config);
    }

    return bean;
  }

  @Transactional
  public Object importPurchaseOrderFromSupplyChain(Object bean, Map<String, Object> values) {

    try {
      StockMoveService stockMoveService = Beans.get(StockMoveService.class);

      PurchaseOrder purchaseOrder = (PurchaseOrder) bean;
      int status = purchaseOrder.getStatusSelect();
      purchaseOrder = (PurchaseOrder) importPurchaseOrder.importPurchaseOrder(bean, values);
      for (PurchaseOrderLine line : purchaseOrder.getPurchaseOrderLineList()) {
        Product product = line.getProduct();
        if (product.getMassUnit() == null) {
          product.setMassUnit(
              stockConfigService.getStockConfig(purchaseOrder.getCompany()).getCustomsMassUnit());
        }
      }

      if (status == PurchaseOrderRepository.STATUS_VALIDATED
          || status == PurchaseOrderRepository.STATUS_FINISHED) {
        purchaseOrderServiceSupplychainImpl.validatePurchaseOrder(purchaseOrder);
      }

      if (status == PurchaseOrderRepository.STATUS_FINISHED) {
        List<Long> idList =
            purchaseOrderStockServiceImpl.createStockMoveFromPurchaseOrder(purchaseOrder);
        for (Long id : idList) {
          StockMove stockMove = Beans.get(StockMoveRepository.class).find(id);
          stockMoveService.copyQtyToRealQty(stockMove);
          stockMoveService.realize(stockMove);
          stockMove.setRealDate(purchaseOrder.getDeliveryDate());
        }
        purchaseOrder.setValidationDate(purchaseOrder.getOrderDate());
        purchaseOrder.setValidatedByUser(AuthUtils.getUser());
        purchaseOrder.setSupplierPartner(
            purchaseOrderServiceSupplychainImpl.validateSupplier(purchaseOrder));
        Invoice invoice =
            Beans.get(PurchaseOrderInvoiceService.class).generateInvoice(purchaseOrder);

        String prefixSupplierSeq = "INV000";
        invoice.setSupplierInvoiceNb(prefixSupplierSeq + purchaseOrder.getImportId());

        invoice.setInternalReference(purchaseOrder.getInternalReference());

        LocalDate date;
        if (purchaseOrder.getValidationDate() != null) {
          date = purchaseOrder.getValidationDate();
        } else {
          date = LocalDate.now();
        }
        invoice.setInvoiceDate(date);
        invoice.setOriginDate(date.minusDays(15));

        invoiceService.validateAndVentilate(invoice);
        purchaseOrderServiceSupplychainImpl.finishPurchaseOrder(purchaseOrder);
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  @Transactional
  public Object importSaleOrderFromSupplyChain(Object bean, Map<String, Object> values) {
    try {
      SaleOrderWorkflowService saleOrderWorkflowService = Beans.get(SaleOrderWorkflowService.class);
      StockMoveService stockMoveService = Beans.get(StockMoveService.class);

      SaleOrder saleOrder = (SaleOrder) importSaleOrder.importSaleOrder(bean, values);

      for (SaleOrderLine line : saleOrder.getSaleOrderLineList()) {
        Product product = line.getProduct();
        if (product.getMassUnit() == null) {
          product.setMassUnit(
              stockConfigService.getStockConfig(saleOrder.getCompany()).getCustomsMassUnit());
        }
      }
      if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
        // taskSaleOrderService.createTasks(saleOrder); TODO once we will have done the generation//
        // of tasks in project module
        saleOrderWorkflowService.confirmSaleOrder(saleOrder);
        // Beans.get(SaleOrderPurchaseService.class).createPurchaseOrders(saleOrder);
        //				productionOrderSaleOrderService.generateProductionOrder(saleOrder);
        // saleOrder.setClientPartner(saleOrderWorkflowService.validateCustomer(saleOrder));
        // Generate invoice from sale order
        Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateInvoice(saleOrder);
        if (saleOrder.getConfirmationDateTime() != null) {
          invoice.setInvoiceDate(saleOrder.getConfirmationDateTime().toLocalDate());

        } else {
          invoice.setInvoiceDate(LocalDate.now());
        }
        invoiceService.validateAndVentilate(invoice);

        List<Long> idList = saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
        for (Long id : idList) {
          StockMove stockMove = Beans.get(StockMoveRepository.class).find(id);
          if (stockMove.getStockMoveLineList() != null
              && !stockMove.getStockMoveLineList().isEmpty()) {
            stockMove = generateManualTrackingNumber(stockMove);
            stockMoveService.copyQtyToRealQty(stockMove);
            stockMoveService.validate(stockMove);
            if (saleOrder.getConfirmationDateTime() != null) {
              stockMove.setRealDate(saleOrder.getConfirmationDateTime().toLocalDate());
            }
          }
        }
      }
      saleOrderRepo.save(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  @Transactional
  protected StockMove generateManualTrackingNumber(StockMove stockMove) {
    try {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

        Product product =
            Beans.get(ProductRepository.class).find(stockMoveLine.getProduct().getId());
        TrackingNumberConfiguration trackingNumberConf = product.getTrackingNumberConfiguration();

        if (trackingNumberConf != null && !trackingNumberConf.getHasSaleAutoSelectTrackingNbr()) {

          StockLocationLine stockLocationLine =
              Beans.get(StockLocationLineRepository.class)
                  .all()
                  .filter(
                      "self.stockLocation = ?1 AND self.product = ?2 AND self.trackingNumber != null",
                      stockMove.getFromStockLocation().getId(),
                      product.getId())
                  .fetchOne();
          stockMoveLine.setTrackingNumber(stockLocationLine.getTrackingNumber());
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return stockMove;
  }
}
