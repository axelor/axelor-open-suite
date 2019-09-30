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
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.script.ImportPurchaseOrder;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
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

  @Inject private PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;

  @Inject private InvoiceService invoiceService;

  @Inject private SaleOrderStockService saleOrderStockService;

  @Inject private StockMoveRepository stockMoveRepo;

  @Inject private SaleOrderRepository saleOrderRepo;

  @Inject private SaleConfigRepository saleConfigRepo;

  @Inject private SupplychainSaleConfigService configService;

  @Inject private StockConfigService stockConfigService;

  @Inject private ImportPurchaseOrder importPurchaseOrder;

  @Inject private ImportSaleOrder importSaleOrder;

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
        if (product.getWeightUnit() == null) {
          product.setWeightUnit(
              stockConfigService.getStockConfig(purchaseOrder.getCompany()).getCustomsWeightUnit());
        }
      }

      if (status == IPurchaseOrder.STATUS_VALIDATED || status == IPurchaseOrder.STATUS_FINISHED) {
        purchaseOrderServiceSupplychainImpl.validatePurchaseOrder(purchaseOrder);
      }

      if (status == IPurchaseOrder.STATUS_FINISHED) {
        purchaseOrderServiceSupplychainImpl.createStocksMove(purchaseOrder);
        StockMove stockMove =
            stockMoveRepo.all().filter("purchaseOrder.id = ?1", purchaseOrder.getId()).fetchOne();
        if (stockMove != null) {
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
        if (purchaseOrder.getValidationDate() != null) {
          invoice.setInvoiceDate(purchaseOrder.getValidationDate());
        } else {
          invoice.setInvoiceDate(LocalDate.now());
        }
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
        if (product.getWeightUnit() == null) {
          product.setWeightUnit(
              stockConfigService.getStockConfig(saleOrder.getCompany()).getCustomsWeightUnit());
        }
      }
      if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
        // taskSaleOrderService.createTasks(saleOrder); TODO once we will have done the generation
        // of tasks in project module
        saleOrderWorkflowService.confirmSaleOrder(saleOrder);
        saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
        // Beans.get(SaleOrderPurchaseService.class).createPurchaseOrders(saleOrder);
        //				productionOrderSaleOrderService.generateProductionOrder(saleOrder);
        // saleOrder.setClientPartner(saleOrderWorkflowService.validateCustomer(saleOrder));
        // Generate invoice from sale order
        Invoice invoice = Beans.get(SaleOrderInvoiceService.class).generateInvoice(saleOrder);
        if (saleOrder.getConfirmationDate() != null) {
          invoice.setInvoiceDate(saleOrder.getConfirmationDate());
        } else {
          invoice.setInvoiceDate(LocalDate.now());
        }
        invoiceService.validateAndVentilate(invoice);
        StockMove stockMove = stockMoveRepo.all().filter("saleOrder = ?1", saleOrder).fetchOne();
        if (stockMove != null
            && stockMove.getStockMoveLineList() != null
            && !stockMove.getStockMoveLineList().isEmpty()) {
          stockMoveService.copyQtyToRealQty(stockMove);
          stockMoveService.validate(stockMove);
          stockMove.setRealDate(saleOrder.getConfirmationDate());
        }
      }
      saleOrderRepo.save(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return null;
  }
}
