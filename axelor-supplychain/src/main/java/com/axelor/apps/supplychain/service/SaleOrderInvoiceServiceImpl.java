/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Scheduler;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.scheduler.SchedulerService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderLineTaxService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderInvoiceServiceImpl implements SaleOrderInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderInvoiceServiceImpl.class); 

	@Inject
	private SchedulerService schedulerService;
	

	private LocalDate today;
	
	@Inject
	public SaleOrderInvoiceServiceImpl() {

		this.today = GeneralService.getTodayDate();
		
	}
	
	
	public Invoice generateInvoice(SaleOrder saleOrder) throws AxelorException  {
		
		switch (saleOrder.getInvoicingTypeSelect()) {
			case ISaleOrder.INVOICING_TYPE_PER_ORDER:
				
				return this.generatePerOrderInvoice(saleOrder);
				
//			case ISaleOrder.INVOICING_TYPE_WITH_PAYMENT_SCHEDULE:
//						TODO
//				return null;
			case ISaleOrder.INVOICING_TYPE_PER_TASK:
				
				return null;
			case ISaleOrder.INVOICING_TYPE_PER_SHIPMENT:
				
				return null;
			case ISaleOrder.INVOICING_TYPE_FREE:
				
				return this.generatePerOrderInvoice(saleOrder);
				
			case ISaleOrder.INVOICING_TYPE_SUBSCRIPTION:
				
				return this.generateSubscriptionInvoice(saleOrder);
	
			default:
				return null;
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generatePerOrderInvoice(SaleOrder saleOrder) throws AxelorException  {
		
		this.checkIfSaleOrderIsCompletelyInvoiced(saleOrder);
		
		Invoice invoice = this.createInvoice(saleOrder);
		
		this.assignInvoice(saleOrder, invoice);
		
		Beans.get(SaleOrderRepository.class).save(fillSaleOrder(saleOrder, invoice));
		
		return invoice;
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubscriptionInvoice(SaleOrder saleOrder) throws AxelorException  {
		
		Invoice invoice = this.createInvoice(saleOrder);
		
		this.assignInvoice(saleOrder, invoice);
		
		invoice.setIsSubscription(true);
		
		Scheduler scheduler = saleOrder.getSchedulerInstance().getScheduler();
		
		invoice.setSubscriptionFromDate(saleOrder.getNextInvPeriodStartDate());
		
		LocalDate nextInvPeriodStartDate = schedulerService.getComputeDate(scheduler, invoice.getSubscriptionFromDate());
		
		invoice.setSubscriptionToDate(nextInvPeriodStartDate.minusDays(1));
		
		saleOrder.setNextInvPeriodStartDate(nextInvPeriodStartDate);
		
		return invoice;
	}
	
	
	public void checkSubscriptionSaleOrder(SaleOrder saleOrder) throws AxelorException  {
		
		if(saleOrder.getSchedulerInstance() == null || saleOrder.getSchedulerInstance().getScheduler() == null)  {
			throw new AxelorException(String.format("Il est nécessaire de définir un plannificateur."), IException.CONFIGURATION_ERROR);
		}
		
		if(saleOrder.getSubscriptionStartDate() == null)  {
			throw new AxelorException(String.format("Il est nécessaire de définir une date de début d'abonnement."), IException.CONFIGURATION_ERROR);
		}
		if(saleOrder.getInvoicedFirstDate() == null)  {
			throw new AxelorException(String.format("Il est nécessaire de définir une date de première facturation."), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	
	
	/**
	 * Cree une facture mémoire à partir d'un devis.
	 * 
	 * Le planificateur doit être prêt.
	 * 
	 * @param saleOrder
	 * 		Le devis
	 * 
	 * @return Invoice
	 * 		La facture d'abonnement
	 * 
	 * @throws AxelorException 
	 * @throws Exception 
	 */
	public Invoice runSubscriptionInvoicing(SaleOrder saleOrder) throws AxelorException  {
		
		Invoice invoice = null;
		
		LOG.debug("Création de la facture mémoire pour le devis : {}", saleOrder.getSaleOrderSeq());
		
		if(schedulerService.isSchedulerInstanceIsReady(saleOrder.getSchedulerInstance()))  {
			
			LOG.debug("Le mémoire est prêt à etre lancé.");
			invoice = this.generateSubscriptionInvoice(saleOrder);
			
			if(invoice != null)  {
				LOG.debug("Mis à jour de l'historique du planificateur");
				schedulerService.addInHistory(saleOrder.getSchedulerInstance(), today, false);
			}
		}
		else{
			
			LocalDate nextDate = schedulerService.getTheoricalExecutionDate(saleOrder.getSchedulerInstance());
			
			LOG.debug(String.format("La facturation n'est pas prête à etre lancée : %s < %s", today, nextDate));
			throw new AxelorException(String.format("Le devis %s sera facturé le %s.", saleOrder.getSaleOrderSeq(), nextDate), IException.CONFIGURATION_ERROR);
		}
		
		return invoice;
		
	}
	
	
	
	public void checkIfSaleOrderIsCompletelyInvoiced(SaleOrder saleOrder) throws AxelorException  {
		
		BigDecimal total = BigDecimal.ZERO;
		
		for(Invoice invoice : saleOrder.getInvoiceSet())  {
			if(invoice.getStatusSelect() == InvoiceService.STATUS_VENTILATED)  {
				total = total.add(invoice.getInTaxTotal());
			}
		}
		
		if(total.compareTo(saleOrder.getInTaxTotal()) == 0)  {
			throw new AxelorException(String.format("Le devis est déjà complêtement facturé"), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	public SaleOrder fillSaleOrder(SaleOrder saleOrder, Invoice invoice)  {
		
		saleOrder.setOrderDate(this.today);
		
		// TODO Créer une séquence pour les commandes (Porter sur la facture ?)
//		saleOrder.setOrderNumber();
		
		return saleOrder;
		
	}
	
	
	public SaleOrder assignInvoice(SaleOrder saleOrder, Invoice invoice)  {
		
		if(saleOrder.getInvoiceSet() != null)  {
			saleOrder.getInvoiceSet().add(invoice);
		}
		else  {
			saleOrder.setInvoiceSet(new HashSet<Invoice>());
			saleOrder.getInvoiceSet().add(invoice);
		}
		
		return saleOrder;
	}
	
	
	public Invoice createInvoice(SaleOrder saleOrder) throws AxelorException  {
		
		InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);
		
		Invoice invoice = invoiceGenerator.generate();
		
		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, saleOrder.getSaleOrderLineList(), saleOrder.getShowDetailsInInvoice()));
		return invoice;
		
	}
	
	

	public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException  {
		
		if(saleOrder.getCurrency() == null)  {
			throw new AxelorException(String.format("Veuillez selectionner une devise pour le devis %s ", saleOrder.getSaleOrderSeq()), IException.CONFIGURATION_ERROR);
		}
		
		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(InvoiceService.OPERATION_TYPE_CLIENT_SALE, saleOrder.getCompany(),saleOrder.getPaymentCondition(), 
				saleOrder.getPaymentMode(), saleOrder.getMainInvoicingAddress(), saleOrder.getClientPartner(), saleOrder.getContactPartner(), 
				saleOrder.getCurrency(), saleOrder.getPriceList(), saleOrder.getSaleOrderSeq(), saleOrder.getExternalReference()) {
			
			@Override
			public Invoice generate() throws AxelorException {
				
				return super.createInvoiceHeader();
			}
		};
		
		return invoiceGenerator;
		
	}
	
	
	
	// TODO ajouter tri sur les séquences
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList, boolean showDetailsInInvoice) throws AxelorException  {
		
		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		
		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
			
			if(showDetailsInInvoice == true && saleOrderLine.getSaleOrderSubLineList() != null && !saleOrderLine.getSaleOrderSubLineList().isEmpty())  {
				
				for(SaleOrderSubLine saleOrderSubLine : saleOrderLine.getSaleOrderSubLineList())  {
					
					invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderSubLine));
					
				}
			
			}
			else  {
				
				invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine));
				
			}
		}
		
		return invoiceLineList;
		
	}
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, TaxLine taxLine, ProductVariant productVariant, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, int sequence) throws AxelorException  {
		
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, productName, price, description, qty, unit, taxLine, product.getInvoiceLineType(), 
				sequence, discountAmount, discountTypeSelect, exTaxTotal, false)  {
			@Override
			public List<InvoiceLine> creates() throws AxelorException {
				
				InvoiceLine invoiceLine = this.createInvoiceLine();
				
				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);
				
				return invoiceLines;
			}
		};
		
		return invoiceLineGenerator.creates();
	}
	
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine) throws AxelorException  {
		
		return this.createInvoiceLine(invoice, saleOrderLine.getProduct(), saleOrderLine.getProductName(), 
				saleOrderLine.getPrice(), saleOrderLine.getDescription(), saleOrderLine.getQty(), saleOrderLine.getUnit(), saleOrderLine.getTaxLine(), 
				saleOrderLine.getProductVariant(), saleOrderLine.getDiscountAmount(), saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getExTaxTotal(), saleOrderLine.getSequence());
		
		
	}
	
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderSubLine saleOrderSubLine) throws AxelorException  {
		
		return this.createInvoiceLine(invoice, saleOrderSubLine.getProduct(), saleOrderSubLine.getProductName(), 
				saleOrderSubLine.getPrice(), saleOrderSubLine.getDescription(), saleOrderSubLine.getQty(), saleOrderSubLine.getUnit(), 
				saleOrderSubLine.getTaxLine(), saleOrderSubLine.getProductVariant(), 
				saleOrderSubLine.getDiscountAmount(), saleOrderSubLine.getDiscountTypeSelect(), saleOrderSubLine.getExTaxTotal(), saleOrderSubLine.getSequence());
		
	}
	
}


