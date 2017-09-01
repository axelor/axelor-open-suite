/**
 * Axelor Business Solutions
 * <p>
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

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
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.util.List;

public class IntercoServiceImpl implements IntercoService {

    protected PurchaseOrderService purchaseOrderService;
    protected PurchaseOrderLineService purchaseOrderLineService;
    protected PurchaseOrderRepository purchaseOrderRepository;
    protected CompanyRepository companyRepository;

    @Inject
    public IntercoServiceImpl(PurchaseOrderService purchaseOrderService,
                              PurchaseOrderLineService purchaseOrderLineService,
                              PurchaseOrderRepository purchaseOrderRepository,
                              CompanyRepository companyRepository) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseOrderLineService = purchaseOrderLineService;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public SaleOrder generateIntercoSaleFromPurchase(PurchaseOrder purchaseOrder) {
        Beans.get(AppSupplychainService.class).getAppSupplychain().getIntercoFromSale();
        return null;
    }

    @Override
    @Transactional
    public PurchaseOrder generateIntercoPurchaseFromSale(SaleOrder saleOrder)
            throws AxelorException {

        //create purchase order
        PurchaseOrder purchaseOrder;
        purchaseOrder = purchaseOrderService
                .createPurchaseOrder(
                        null,
                        findIntercoCompany(saleOrder),
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
        purchaseOrder.setLocation(saleOrder.getLocation());
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
        return purchaseOrderRepository.save(purchaseOrder);
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
        PurchaseOrderLine purchaseOrderLine = purchaseOrderLineService
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

        purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
        return purchaseOrderLine;
    }

    @Override
    public Company findIntercoCompany(SaleOrder saleOrder) {
        Partner partner = saleOrder.getClientPartner();
        return companyRepository.all()
                .filter("self.partner = ?", partner)
                .fetchOne();
    }

    @Override
    public Company findIntercoCompany(PurchaseOrder purchaseOrder) {
        Partner partner = purchaseOrder.getSupplierPartner();
        return companyRepository.all()
                .filter("self.partner = ?", partner)
                .fetchOne();
    }
}
