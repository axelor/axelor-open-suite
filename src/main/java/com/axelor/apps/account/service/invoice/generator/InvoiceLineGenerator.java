/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.invoice.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineType;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.tool.date.Period;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

/**
 * Classe de création de ligne de facture abstraite.
 * 
 */
public abstract class InvoiceLineGenerator extends InvoiceLineManagement {
	
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceLineGenerator.class);
	
	protected AccountManagementService accountManagementService;
	protected CurrencyService currencyService;
	
	protected Invoice invoice;
	protected int type;
	protected Product product;
	protected String productName; 
	protected BigDecimal price;
	protected String description; 
	protected BigDecimal qty;
	protected Unit unit; 
	protected VatLine vatLine; 
	protected Task task; 
	protected LocalDate today;
	protected boolean isTaxInvoice; 
	protected InvoiceLineType invoiceLineType;
	protected ProductVariant productVariant;
	protected BigDecimal discountAmount;
	protected int discountTypeSelect;
	protected BigDecimal exTaxTotal;
	
	protected InvoiceLineGenerator() { }
	
	protected InvoiceLineGenerator(int type) {
		
		this.type = type;
		
	}
	
	protected InvoiceLineGenerator( Invoice invoice ) {

        this.invoice = invoice;
        
    }
	
	protected InvoiceLineGenerator( Invoice invoice, int type ) {

        this.invoice = invoice;
        this.type = type;
        
    }
	

	protected InvoiceLineGenerator( Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, VatLine vatLine, Task task, InvoiceLineType invoiceLineType, ProductVariant productVariant, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, boolean isTaxInvoice) {

        this.invoice = invoice;
        this.product = product;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.qty = qty;
        this.unit = unit;
        this.vatLine = vatLine;
        this.task = task;
        this.invoiceLineType = invoiceLineType;
        this.productVariant = productVariant;
        this.discountTypeSelect = discountTypeSelect;
        this.discountAmount = discountAmount;
        this.exTaxTotal = exTaxTotal;
        this.isTaxInvoice = isTaxInvoice;
        this.today = GeneralService.getTodayDate();
        this.currencyService = new CurrencyService(this.today);
    }
	
	protected InvoiceLineGenerator( Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, Task task, InvoiceLineType invoiceLineType, ProductVariant productVariant, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, boolean isTaxInvoice) {

        this.invoice = invoice;
        this.product = product;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.qty = qty;
        this.unit = unit;
        this.task = task;
        this.invoiceLineType = invoiceLineType;
        this.productVariant = productVariant;
        this.discountTypeSelect = discountTypeSelect;
        this.discountAmount = discountAmount;
        this.exTaxTotal = exTaxTotal;
        this.isTaxInvoice = isTaxInvoice;
        this.today = GeneralService.getTodayDate();
        this.currencyService = new CurrencyService(this.today);
    }
	
	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	
	abstract public List<InvoiceLine> creates() throws AxelorException ;
	
	
	/**
	 * @return
	 * @throws AxelorException 
	 */
	protected InvoiceLine createInvoiceLine() throws AxelorException  {
		
		InvoiceLine invoiceLine = new InvoiceLine();
		
		invoiceLine.setInvoice(invoice);
		
		invoiceLine.setProduct(product);
		invoiceLine.setProductName(productName);
		invoiceLine.setDescription(description);
		invoiceLine.setPrice(price);
		invoiceLine.setQty(qty);
		
		if(exTaxTotal == null)  {
			exTaxTotal = computeAmount(qty, price);
		}
		
		invoiceLine.setExTaxTotal(exTaxTotal);
		
		Partner partner = invoice.getPartner();
		Currency partnerCurrency = partner.getCurrency();
		
		if(partnerCurrency == null)  {
			throw new AxelorException(String.format("Veuillez selectionner une devise pour le tiers %s (%s)", 
					partner.getFullName(), partner.getPartnerSeq()), IException.CONFIGURATION_ERROR);
		}
		
		invoiceLine.setAccountingExTaxTotal(
				currencyService.getAmountCurrencyConverted(
						invoice.getCurrency(), partnerCurrency, exTaxTotal, invoice.getInvoiceDate()));  
		
		Company company = invoice.getCompany();
		
		Currency companyCurrency = company.getCurrency();
		
		if(companyCurrency == null)  {
			throw new AxelorException(String.format("Veuillez selectionner une devise pour la société %s", 
					company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		invoiceLine.setCompanyExTaxTotal(
				currencyService.getAmountCurrencyConverted(
						invoice.getCurrency(), companyCurrency, exTaxTotal, invoice.getInvoiceDate()));
		
		invoiceLine.setPricingListUnit(unit);
		
		if(vatLine == null)  {
			boolean isPurchase = false;
			if(invoice.getOperationTypeSelect() == IInvoice.SUPPLIER_PURCHASE || invoice.getOperationTypeSelect() == IInvoice.SUPPLIER_REFUND)  {
				isPurchase = true;
			}
			vatLine =  new AccountManagementService().getVatLine(invoice.getInvoiceDate(), product, invoice.getCompany(), isPurchase);
		}
		invoiceLine.setVatLine(vatLine);
		
		invoiceLine.setTask(task);
		invoiceLine.setInvoiceLineType(invoiceLineType);
		
		invoiceLine.setProductVariant(productVariant);
		
		invoiceLine.setDiscountTypeSelect(discountTypeSelect);
		invoiceLine.setDiscountAmount(discountAmount);
		
		
		return invoiceLine;
		
	}
	
	
	/**
	 * Rembourser une ligne de facture.
	 * 
	 * @param invoice
	 * 		La facture concernée.
	 * @param invoiceLine
	 *      La ligne de facture.
	 * 
	 * @return 
	 * 			La ligne de facture de remboursement.
	 */
	protected InvoiceLine refundInvoiceLine(InvoiceLine invoiceLine, Period period, int typeSelect, boolean daysQty) {

		LOG.debug("Remboursement d'une ligne de facture (quantité = nb jour ? {}).", daysQty);
		
		BigDecimal qty = invoiceLine.getQty();
		
		InvoiceLine refundInvoiceLine = JPA.copy(invoiceLine, true);
		
		refundInvoiceLine.setInvoice(invoice);
		
		qty =  invoiceLine.getQty() ; 
		
		refundInvoiceLine.setQty( qty.negate() );
		
		LOG.debug( "Quantité remboursée : {}", refundInvoiceLine.getQty() );
		
		refundInvoiceLine.setExTaxTotal( computeAmount( refundInvoiceLine.getQty(), refundInvoiceLine.getPrice() ) );

		LOG.debug("Remboursement de la ligne de facture {} => montant HT: {}", new Object[] { invoiceLine.getId(), refundInvoiceLine.getExTaxTotal() });

		return refundInvoiceLine;
	}
	
	
	protected InvoiceLine substractInvoiceLine(InvoiceLine invoiceLine1, InvoiceLine invoiceLine2){
		
		InvoiceLine substract = JPA.copy(invoiceLine1, false);
		
		substract.setQty(invoiceLine1.getQty().add(invoiceLine2.getQty()));
		substract.setExTaxTotal( computeAmount( substract.getQty(), substract.getPrice() ) );

		LOG.debug("Soustraction de deux lignes de factures: {}", substract);
		
		return substract;
		
	}

	/**
	 * Convertir le prix d'une unité de départ vers une unité d'arrivée.
	 * 
	 * @param price
	 * @param startUnit
	 * @param endUnit
	 * 
	 * @return Le prix converti
	 */
	protected BigDecimal convertPrice(BigDecimal price, Unit startUnit, Unit endUnit) {

		BigDecimal convertPrice = convert(startUnit, endUnit, price);

		LOG.debug("Conversion du prix {} {} : {} {}", new Object[] { price, startUnit, convertPrice, endUnit });

		return convertPrice;
	}

	/**
	 * Récupérer la bonne unité.
	 * 
	 * @param unit
	 * 		Unité de base.
	 * @param unitDisplay
	 * 		Unité à afficher.
	 * 
	 * @return  L'unité à utiliser.
	 */
	protected Unit unit(Unit unit, Unit displayUnit) {

		Unit resUnit = unit;

		if (displayUnit != null) { resUnit = displayUnit; }

		LOG.debug("Obtention de l'unité : Unité {}, Unité affichée {} : {}", new Object[] { unit, displayUnit, resUnit });

		return resUnit;

	}
	
// HELPER
	
	/**
	 * Convertir le prix d'une unité de départ version une unité d'arrivée.
	 * 
	 * @param price
	 * @param startUnit
	 * @param endUnit
	 * 
	 * @return Le prix converti
	 */
	protected BigDecimal convert(Unit startUnit, Unit endUnit, BigDecimal value) {
		 
		if (value == null || startUnit == null || endUnit == null || startUnit.equals(endUnit)) { return value; }
		else { return value.multiply(convertCoef(startUnit, endUnit)).setScale(6, RoundingMode.HALF_EVEN); }
		
	}

	/**
	 * Obtenir le coefficient de conversion d'une unité de départ vers une unité d'arrivée.
	 * 
	 * @param startUnit
	 * @param endUnit
	 * 
	 * @return Le coefficient de conversion.
	 */
	protected BigDecimal convertCoef(Unit startUnit, Unit endUnit){
		
		UnitConversion unitConversion = UnitConversion.all().filter("self.startUnit = ?1 AND self.endUnit = ?2", startUnit, endUnit).fetchOne();
		
		if (unitConversion != null){ return unitConversion.getCoef(); }
		else { return BigDecimal.ONE; }
		
	}
	
	
	
	
	
	protected void addAlarm( Alarm alarm, Partner partner ) {
		
		if ( alarm != null ) {
			
			alarm.setInvoice(invoice);
			alarm.setPartner(partner);
			
		}
		
	}
	
}