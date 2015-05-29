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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Scheduler;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.scheduler.SchedulerService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
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


	@Override
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


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generatePerOrderInvoice(SaleOrder saleOrder) throws AxelorException  {

		Invoice invoice = this.createInvoice(saleOrder);

		this.assignInvoice(saleOrder, invoice);

		Beans.get(SaleOrderRepository.class).save(fillSaleOrder(saleOrder, invoice));

		return invoice;
	}


	@Override
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


	@Override
	public void checkSubscriptionSaleOrder(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSchedulerInstance() == null || saleOrder.getSchedulerInstance().getScheduler() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_1)), IException.CONFIGURATION_ERROR);
		}

		if(saleOrder.getSubscriptionStartDate() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_2)), IException.CONFIGURATION_ERROR);
		}
		if(saleOrder.getInvoicedFirstDate() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_3)), IException.CONFIGURATION_ERROR);
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
	@Override
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
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_4), saleOrder.getSaleOrderSeq(), nextDate), IException.CONFIGURATION_ERROR);
		}

		return invoice;

	}


	@Override
	public boolean checkIfSaleOrderIsCompletelyInvoiced(SaleOrder saleOrder)  {

		BigDecimal total = BigDecimal.ZERO;

		saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

		for(Invoice invoice : saleOrder.getInvoiceSet())  {
			total = total.add(this.computeInTaxTotalInvoiced(invoice));
		}

		return total.compareTo(saleOrder.getInTaxTotal()) == 0;

	}

	public BigDecimal computeInTaxTotalInvoiced(Invoice invoice)  {

		BigDecimal total = BigDecimal.ZERO;

		if(invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED)  {
			if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)  {
				total = total.add(invoice.getInTaxTotal());
			}
			if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND)  {
				total = total.subtract(invoice.getInTaxTotal());
			}
		}

		if(invoice.getRefundInvoiceList() != null)  {
			for(Invoice refund : invoice.getRefundInvoiceList())  {
				total = total.add(this.computeInTaxTotalInvoiced(refund));
			}
		}

		return total;

	}


	@Override
	public SaleOrder fillSaleOrder(SaleOrder saleOrder, Invoice invoice)  {

		saleOrder.setOrderDate(this.today);

		// TODO Créer une séquence pour les commandes (Porter sur la facture ?)
//		saleOrder.setOrderNumber();

		return saleOrder;

	}


	@Override
	public SaleOrder assignInvoice(SaleOrder saleOrder, Invoice invoice)  {

		saleOrder.addInvoiceSetItem(invoice);

		return saleOrder;
	}


	@Override
	public Invoice createInvoice(SaleOrder saleOrder) throws AxelorException  {

		InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);

		Invoice invoice = invoiceGenerator.generate();

		invoice.setInAti(Beans.get(AccountConfigRepository.class).all().filter("self.company = ?1", saleOrder.getCompany()).fetchOne().getInvoiceInAti());


		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, saleOrder.getSaleOrderLineList(), saleOrder.getShowDetailsInInvoice()));
		return invoice;

	}



	@Override
	public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getCurrency() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_6), saleOrder.getSaleOrderSeq()), IException.CONFIGURATION_ERROR);
		}

		InvoiceGenerator invoiceGenerator = new InvoiceGeneratorSupplyChain(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE, saleOrder.getCompany(),saleOrder.getPaymentCondition(),
				saleOrder.getPaymentMode(), saleOrder.getMainInvoicingAddress(), saleOrder.getClientPartner(), saleOrder.getContactPartner(),
				saleOrder.getCurrency(), saleOrder.getPriceList(), saleOrder.getSaleOrderSeq(), saleOrder.getExternalReference(), saleOrder) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		return invoiceGenerator;

	}



	// TODO ajouter tri sur les séquences
	@Override
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

	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, Product product, String productName, BigDecimal price, BigDecimal priceDiscounted,String description, BigDecimal qty,
			Unit unit, TaxLine taxLine, ProductVariant productVariant, BigDecimal discountAmount, int discountTypeSelect, BigDecimal exTaxTotal, BigDecimal inTaxTotal, int sequence, SaleOrderLine saleOrderLine) throws AxelorException  {

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGeneratorSupplyChain(invoice, product, productName, price, priceDiscounted,description, qty, unit, taxLine, product.getInvoiceLineType(),
				sequence, discountAmount, discountTypeSelect, exTaxTotal, inTaxTotal, false, saleOrderLine, null, null)  {
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



	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine) throws AxelorException  {

		return this.createInvoiceLine(invoice, saleOrderLine.getProduct(), saleOrderLine.getProductName(),
				saleOrderLine.getPrice(), saleOrderLine.getPriceDiscounted(),saleOrderLine.getDescription(), saleOrderLine.getQty(), saleOrderLine.getUnit(), saleOrderLine.getTaxLine(),
				saleOrderLine.getProductVariant(), saleOrderLine.getDiscountAmount(), saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getExTaxTotal(), saleOrderLine.getInTaxTotal(),saleOrderLine.getSequence(), saleOrderLine);


	}


	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderSubLine saleOrderSubLine) throws AxelorException  {

		return this.createInvoiceLine(invoice, saleOrderSubLine.getProduct(), saleOrderSubLine.getProductName(),
				saleOrderSubLine.getPrice(), null,saleOrderSubLine.getDescription(), saleOrderSubLine.getQty(), saleOrderSubLine.getUnit(),
				saleOrderSubLine.getTaxLine(), saleOrderSubLine.getProductVariant(),
				saleOrderSubLine.getDiscountAmount(), saleOrderSubLine.getDiscountTypeSelect(), saleOrderSubLine.getExTaxTotal(), null,saleOrderSubLine.getSequence(), saleOrderSubLine.getSaleOrderLine());

	}

	@Override
	public BigDecimal getAmountInvoiced(SaleOrder saleOrder){
		return this.getAmountInvoiced(saleOrder, null, false);
	}

	/**
	 * Return the remaining amount to invoice for the saleOrder in parameter
	 *
	 * @param saleOrder
	 *
	 * @param currentInvoiceId
	 * In the case of invoice ventilation or cancellation, the invoice status isn't modify in database but it will be integrated in calculation
	 * For ventilation, the invoice should be integrated in calculation
	 * For cancellation,  the invoice shouldn't be integrated in calculation
	 *
	 * @param includeInvoice
	 * To know if the invoice should be or not integrated in calculation
	 */
	@Override
	public BigDecimal getAmountInvoiced(SaleOrder saleOrder, Long currentInvoiceId, boolean includeInvoice){

		Query q = null;
		String query;
		query = "SELECT SUM(self.companyExTaxTotal)"
				+ " FROM InvoiceLine as self"
				+ " WHERE ( (self.saleOrderLine.id IN (SELECT id FROM SaleOrderLine WHERE saleOrder.id = :saleOrderId)"
							+ " AND self.invoice.saleOrder IS NULL)"
						+ " OR self.invoice.saleOrder.id = :saleOrderId )"
					+ " AND self.invoice.statusSelect = :statusVentilated";
		if (currentInvoiceId != null){
			if (includeInvoice){
				query += " OR self.invoice.id = :invoiceId";
			}else{
				query += " AND self.invoice.id <> :invoiceId";
			}
		}
		q = JPA.em().createQuery(query, BigDecimal.class);

		q.setParameter("saleOrderId", saleOrder.getId());
		q.setParameter("statusVentilated", InvoiceRepository.STATUS_VENTILATED);
		if (currentInvoiceId != null){
			q.setParameter("invoiceId", currentInvoiceId);
		}

		BigDecimal invoicedAmount = (BigDecimal) q.getSingleResult();
		if(invoicedAmount != null){
			if (!saleOrder.getCurrency().equals(saleOrder.getCompany().getCurrency())){
				BigDecimal rate = invoicedAmount.divide(saleOrder.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
				invoicedAmount = saleOrder.getExTaxTotal().multiply(rate);
			}
		}

		return invoicedAmount;

	}

}


