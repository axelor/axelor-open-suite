/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class IntercoServiceImpl implements IntercoService {

    @Override
    @Transactional
    public SaleOrder generateIntercoSaleFromPurchase(PurchaseOrder purchaseOrder)
            throws AxelorException {

    	SaleOrderCreateService saleOrderCreateService = Beans.get(SaleOrderCreateService.class);
    	SaleOrderComputeService saleOrderComputeService = Beans.get(SaleOrderComputeService.class);

        //create sale order
        SaleOrder saleOrder = saleOrderCreateService.createSaleOrder(
                null,
                findIntercoCompany(purchaseOrder.getSupplierPartner()),
                purchaseOrder.getContactPartner(),
                purchaseOrder.getCurrency(),
                purchaseOrder.getDeliveryDate(),
                null,
                null,
                purchaseOrder.getOrderDate(),
                purchaseOrder.getPriceList(),
                purchaseOrder.getCompany().getPartner(),
                null
        );

        //copy date
        saleOrder.setOrderDate(purchaseOrder.getOrderDate());

        //copy payments
        saleOrder.setPaymentMode(purchaseOrder.getPaymentMode());
        saleOrder.setPaymentCondition(purchaseOrder.getPaymentCondition());

        //copy delivery info
        saleOrder.setDeliveryDate(purchaseOrder.getDeliveryDate());
        saleOrder.setStockLocation(purchaseOrder.getStockLocation());
        saleOrder.setShipmentMode(purchaseOrder.getShipmentMode());
        saleOrder.setFreightCarrierMode(purchaseOrder.getFreightCarrierMode());

        //copy timetable info
        saleOrder.setExpectedRealisationDate(purchaseOrder.getExpectedRealisationDate());
        saleOrder.setAmountToBeSpreadOverTheTimetable(purchaseOrder.getAmountToBeSpreadOverTheTimetable());

        //create lines
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
        if (purchaseOrderLineList != null) {
            for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
                this.createIntercoSaleLineFromPurchaseLine(purchaseOrderLine, saleOrder);
            }
        }

        //compute the sale order
        saleOrderComputeService.computeSaleOrder(saleOrder);

        saleOrder.setCreatedByInterco(true);
        return Beans.get(SaleOrderRepository.class).save(saleOrder);
   }

    @Override
    @Transactional
    public PurchaseOrder generateIntercoPurchaseFromSale(SaleOrder saleOrder)
            throws AxelorException {

        PurchaseOrderService purchaseOrderService =
                Beans.get(PurchaseOrderService.class);

        //create purchase order
        PurchaseOrder purchaseOrder;
        purchaseOrder = purchaseOrderService
                .createPurchaseOrder(
                        null,
                        findIntercoCompany(saleOrder.getClientPartner()),
                        saleOrder.getContactPartner(),
                        saleOrder.getCurrency(),
                        saleOrder.getDeliveryDate(),
                        null,
                        null,
                        saleOrder.getOrderDate(),
                        saleOrder.getPriceList(),
                        saleOrder.getCompany().getPartner()
                );
        //copy date
        purchaseOrder.setOrderDate(saleOrder.getOrderDate());

        //copy payments
        purchaseOrder.setPaymentMode(saleOrder.getPaymentMode());
        purchaseOrder.setPaymentCondition(saleOrder.getPaymentCondition());

        //copy delivery info
        purchaseOrder.setDeliveryDate(saleOrder.getDeliveryDate());
        purchaseOrder.setStockLocation(saleOrder.getStockLocation());
        purchaseOrder.setShipmentMode(saleOrder.getShipmentMode());
        purchaseOrder.setFreightCarrierMode(saleOrder.getFreightCarrierMode());

        //copy timetable info
        purchaseOrder.setExpectedRealisationDate(saleOrder.getExpectedRealisationDate());
        purchaseOrder.setAmountToBeSpreadOverTheTimetable(saleOrder.getAmountToBeSpreadOverTheTimetable());

        //create lines
        List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
        if (saleOrderLineList != null) {
            for (SaleOrderLine saleOrderLine : saleOrderLineList) {
                this.createIntercoPurchaseLineFromSaleLine(saleOrderLine, purchaseOrder);
            }
        }

        //compute the purchase order
        purchaseOrderService.computePurchaseOrder(purchaseOrder);

        purchaseOrder.setCreatedByInterco(true);
        return Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);
    }


    /**
     *
     * @param saleOrderLine the sale order line needed to create the purchase
     *                      order line
     * @param purchaseOrder the purchase order line belongs to this
     *                      purchase order
     * @return the created purchase order line
     */
    protected PurchaseOrderLine createIntercoPurchaseLineFromSaleLine(SaleOrderLine saleOrderLine,
                                                                      PurchaseOrder purchaseOrder) throws AxelorException {
        PurchaseOrderLine purchaseOrderLine = Beans.get(PurchaseOrderLineService.class)
                .createPurchaseOrderLine(purchaseOrder,
                        saleOrderLine.getProduct(),
                        saleOrderLine.getProductName(),
                        saleOrderLine.getDescription(),
                        saleOrderLine.getQty(),
                        saleOrderLine.getUnit()
                );
        //compute amount
        purchaseOrderLine.setPrice(saleOrderLine.getPrice());
        purchaseOrderLine.setExTaxTotal(saleOrderLine.getExTaxTotal());
        purchaseOrderLine.setDiscountTypeSelect(saleOrderLine.getDiscountTypeSelect());
        purchaseOrderLine.setDiscountAmount(saleOrderLine.getDiscountAmount());

        //delivery
        purchaseOrderLine.setEstimatedDelivDate(saleOrderLine.getEstimatedDelivDate());

        //tax
        purchaseOrderLine.setTaxLine(saleOrderLine.getTaxLine());

        purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
        return purchaseOrderLine;
    }

    /**
     *
     * @param purchaseOrderLine  the purchase order line needed to create
     *                           the sale order line
     * @param saleOrder  the sale order line belongs to this
     *                   purchase order
     * @return the created purchase order line
     */
    protected SaleOrderLine createIntercoSaleLineFromPurchaseLine(PurchaseOrderLine purchaseOrderLine,
                                                                  SaleOrder saleOrder) {
        SaleOrderLine saleOrderLine = new SaleOrderLine();

        saleOrderLine.setSaleOrder(saleOrder);
        saleOrderLine.setProduct(purchaseOrderLine.getProduct());
        saleOrderLine.setProductName(purchaseOrderLine.getProductName());

        saleOrderLine.setDescription(purchaseOrderLine.getDescription());
        saleOrderLine.setQty(purchaseOrderLine.getQty());
        saleOrderLine.setUnit(purchaseOrderLine.getUnit());

        //compute amount
        saleOrderLine.setPrice(purchaseOrderLine.getPrice());
        saleOrderLine.setExTaxTotal(purchaseOrderLine.getExTaxTotal());
        saleOrderLine.setDiscountTypeSelect(purchaseOrderLine.getDiscountTypeSelect());
        saleOrderLine.setDiscountAmount(purchaseOrderLine.getDiscountAmount());

        //delivery
        saleOrderLine.setEstimatedDelivDate(purchaseOrderLine.getEstimatedDelivDate());

        //tax
        saleOrderLine.setTaxLine(purchaseOrderLine.getTaxLine());

        saleOrder.addSaleOrderLineListItem(saleOrderLine);
        return saleOrderLine;
    }

    @Override
    public Invoice generateIntercoInvoice(Invoice invoice) throws AxelorException {
        Invoice generatedInvoice = Beans.get(InvoiceManagementRepository.class)
                .copy(invoice, true);

        //set the status
        int generatedOperationTypeSelect = 0;
        switch (invoice.getOperationTypeSelect()) {
            case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
                generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
                break;
            case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
                generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
                break;
            case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
                generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
                break;
            case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
                generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
                break;
        }
        generatedInvoice.setOperationTypeSelect(generatedOperationTypeSelect);

        //set the correct company and partner
        generatedInvoice.setCompany(findIntercoCompany(invoice.getPartner()));
        generatedInvoice.setPartner(invoice.getCompany().getPartner());
        return Beans.get(InvoiceRepository.class).save(generatedInvoice);
    }

    @Override
    public Company findIntercoCompany(Partner partner) {
        return Beans.get(CompanyRepository.class).all()
                .filter("self.partner = ?", partner)
                .fetchOne();
    }
}
