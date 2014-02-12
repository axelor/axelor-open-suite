/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PurchaseOrderLineService {
private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private AccountManagementService accountManagementService;
	
	@Inject
	private PriceListService priceListService;
	
	@Inject 
	private ProductVariantService productVariantService;
	
	private int sequence = 0;
	
	/**
	 * Calculer le montant HT d'une ligne de commande.
	 * 
	 * @param quantity
	 *          Quantité.
	 * @param price
	 *          Le prix.
	 * 
	 * @return 
	 * 			Le montant HT de la ligne.
	 */
	public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(2, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
	
	public BigDecimal getUnitPrice(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException  {
		
		Product product = purchaseOrderLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
			product.getPurchaseCurrency(), purchaseOrder.getCurrency(), product.getPurchasePrice(), purchaseOrder.getOrderDate());  
		
	}
	
	
	public TaxLine getTaxLine(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException  {
		
		return accountManagementService.getTaxLine(
				purchaseOrder.getOrderDate(), purchaseOrderLine.getProduct(), purchaseOrder.getCompany(), purchaseOrder.getSupplierPartner().getFiscalPosition(), true);
		
	}
	
	
	public BigDecimal computePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)  {
		
		return purchaseOrderLine.getExTaxTotal();
	}

	
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, PurchaseOrder purchaseOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				purchaseOrder.getCurrency(), purchaseOrder.getCompany().getCurrency(), exTaxTotal, purchaseOrder.getOrderDate());  
	}
	
	
	public PriceListLine getPriceListLine(PurchaseOrderLine purchaseOrderLine, PriceList priceList)  {
		
		return priceListService.getPriceListLine(purchaseOrderLine.getProduct(), purchaseOrderLine.getQty(), priceList);
	
	}
	
	
	public BigDecimal computeDiscount(PurchaseOrderLine purchaseOrderLine)  {
		
		return priceListService.computeDiscount(purchaseOrderLine.getPrice(), purchaseOrderLine.getDiscountTypeSelect(), purchaseOrderLine.getDiscountAmount());
		
	}
	
	
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, SalesOrderLine salesOrderLine) throws AxelorException  {

		LOG.debug("Création d'une ligne de commande fournisseur pour le produit : {}",
				new Object[] { salesOrderLine.getProductName() });
		
		return this.createPurchaseOrderLine(
				purchaseOrder, 
				salesOrderLine.getProduct(), 
				salesOrderLine.getDescription(), 
//				productVariantService.copyProductVariant(salesOrderLine.getProductVariant(), false), TODO doit disparaître
				null,
				salesOrderLine.getQty(), 
				salesOrderLine.getUnit(), 
				salesOrderLine.getTask());
		
	}
	
	
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, Product product, String description, ProductVariant productVariant, BigDecimal qty, Unit unit, Task task) throws AxelorException  {
		
		PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
		purchaseOrderLine.setPurchaseOrder(purchaseOrder);
		purchaseOrderLine.setAmountInvoiced(BigDecimal.ZERO);
		
		purchaseOrderLine.setDeliveryDate(purchaseOrder.getDeliveryDate());
		purchaseOrderLine.setDescription(description);
		
		purchaseOrderLine.setIsInvoiced(false);
		purchaseOrderLine.setIsOrdered(false);
		
		purchaseOrderLine.setProduct(product);
		purchaseOrderLine.setProductName(product.getName());
		purchaseOrderLine.setProductVariant(productVariant);
		
		purchaseOrderLine.setQty(qty);
		purchaseOrderLine.setSequence(sequence);
		sequence++;
		
		purchaseOrderLine.setTask(task);
		purchaseOrderLine.setUnit(unit);
		purchaseOrderLine.setTaxLine(this.getTaxLine(purchaseOrder, purchaseOrderLine));
		
		BigDecimal price = this.getUnitPrice(purchaseOrder, purchaseOrderLine);
		
		PriceList priceList = purchaseOrder.getPriceList();
		if(priceList != null)  {
			PriceListLine priceListLine = this.getPriceListLine(purchaseOrderLine, priceList);
			
			Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
			
			purchaseOrderLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
			purchaseOrderLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
			
			if(discounts.get("price") != null)  {
				price = (BigDecimal) discounts.get("price");
			}
		}
		purchaseOrderLine.setPrice(price);
		
		BigDecimal exTaxTotal = PurchaseOrderLineService.computeAmount(purchaseOrderLine.getQty(), this.computeDiscount(purchaseOrderLine));
			
		BigDecimal companyExTaxTotal = this.getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
			
		purchaseOrderLine.setExTaxTotal(exTaxTotal);
		purchaseOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
		purchaseOrderLine.setAmountRemainingToBeInvoiced(exTaxTotal);
			
		return purchaseOrderLine;
	}
}
