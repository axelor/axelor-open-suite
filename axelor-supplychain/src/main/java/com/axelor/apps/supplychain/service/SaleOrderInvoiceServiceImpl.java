/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.db.repo.SubscriptionRepository;
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

	private final Logger log = LoggerFactory.getLogger( getClass() );

	private LocalDate today;

	protected GeneralService generalService;

	@Inject
	private SaleOrderRepository saleOrderRepo;


	@Inject
	public SaleOrderInvoiceServiceImpl(GeneralService generalService) {

		this.generalService = generalService;
		this.today = this.generalService.getTodayDate();

	}


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(SaleOrder saleOrder) throws AxelorException  {

		Invoice invoice = this.createInvoice(saleOrder);

		Beans.get(InvoiceRepository.class).save(invoice);

		saleOrderRepo.save(fillSaleOrder(saleOrder, invoice));

		return invoice;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLinesSelected) throws AxelorException  {

		Invoice invoice = this.createInvoice(saleOrder, saleOrderLinesSelected);

		Beans.get(InvoiceRepository.class).save(invoice);

		saleOrderRepo.save(fillSaleOrder(saleOrder, invoice));

		return invoice;
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
	public Invoice createInvoice(SaleOrder saleOrder) throws AxelorException  {

		return createInvoice(saleOrder, saleOrder.getSaleOrderLineList());

	}

	@Override
	public Invoice createInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);

		Invoice invoice = invoiceGenerator.generate();

		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, saleOrderLineList));
//		advancePaymentService.fillAdvancePayment(invoice, saleOrder, saleOrderLineList);  //TODO
		log.debug("fillAdvancePayment : methode terminée");
		this.fillInLines(invoice);

		return invoice;

	}


	@Override
	public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getCurrency() == null)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SO_INVOICE_6), saleOrder.getSaleOrderSeq()), IException.CONFIGURATION_ERROR);
		}

		InvoiceGenerator invoiceGenerator = new InvoiceGeneratorSupplyChain(saleOrder) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		return invoiceGenerator;

	}



	// TODO ajouter tri sur les séquences
	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			//Lines of subscription type are invoiced directly from sale order line or from the subscription batch
			if (!ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(saleOrderLine.getProduct().getProductTypeSelect())){
				invoiceLineList.addAll(this.createInvoiceLine(invoice, saleOrderLine));

				saleOrderLine.setInvoiced(true);
			}
		}

		return invoiceLineList;

	}

	//Need to create this new method because createInvoiceLines doesn't take into account lines with subscriptable products anymore
	public List<InvoiceLine> createSubscriptionInvoiceLines(Invoice invoice, List<Subscription> subscriptionList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		int sequence = 1;

		for(Subscription subscription : subscriptionList)  {

			invoiceLineList.addAll(this.createSubscriptionInvoiceLine(invoice, subscription, sequence));

			subscription.setInvoiced(true);

			sequence++;
		}

		return invoiceLineList;

	}

	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, SaleOrderLine saleOrderLine) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGeneratorSupplyChain(invoice, product, saleOrderLine.getProductName(),
				saleOrderLine.getDescription(), saleOrderLine.getQty(), saleOrderLine.getUnit(),
				saleOrderLine.getSequence(), false, saleOrderLine, null, null, null)  {

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

	public List<InvoiceLine> createSubscriptionInvoiceLine(Invoice invoice, Subscription subscription, Integer sequence) throws AxelorException  {

		SaleOrderLine saleOrderLine = subscription.getSaleOrderLine();
		Product product = saleOrderLine.getProduct();

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGeneratorSupplyChain(invoice, product, saleOrderLine.getProductName()+"("+saleOrderLine.getPeriodicity()+" "+I18n.get("month(s)")+")",
				saleOrderLine.getDescription(), saleOrderLine.getQty(), saleOrderLine.getUnit(),
				sequence, false, saleOrderLine, null, null, subscription)  {

			@Override
			public List<InvoiceLine> creates() throws AxelorException {

				InvoiceLine invoiceLine = this.createInvoiceLine();

				invoiceLine.setSubscriptionFromDate(subscription.getFromPeriodDate());
				invoiceLine.setSubscriptionToDate(subscription.getToPeriodDate());

				List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
				invoiceLines.add(invoiceLine);

				return invoiceLines;
			}
		};

		return invoiceLineGenerator.creates();
	}


	@Override
	public BigDecimal getInvoicedAmount(SaleOrder saleOrder)  {
		return this.getInvoicedAmount(saleOrder, null, true);
	}

	/**
	 * Return the remaining amount to invoice for the saleOrder in parameter
	 *
	 * @param saleOrder
	 *
	 * @param currentInvoiceId
	 * In the case of invoice ventilation or cancellation, the invoice status isn't modify in database but it will be integrated in calculation
	 * For ventilation, the invoice should be integrated in calculation
	 * For cancellation, the invoice shouldn't be integrated in calculation
	 *
	 * @param includeInvoice
	 * To know if the invoice should be or not integrated in calculation
	 */
	@Override
	public BigDecimal getInvoicedAmount(SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice)  {

		BigDecimal invoicedAmount = BigDecimal.ZERO;
		
		BigDecimal saleAmount = this.getAmountVentilated(saleOrder, currentInvoiceId, excludeCurrentInvoice, InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
		BigDecimal refundAmount = this.getAmountVentilated(saleOrder, currentInvoiceId, excludeCurrentInvoice, InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);

		if(saleAmount != null)  {  invoicedAmount = invoicedAmount.add(saleAmount);  }
		if(refundAmount != null)  {  invoicedAmount = invoicedAmount.subtract(refundAmount);  }
		
		if (!saleOrder.getCurrency().equals(saleOrder.getCompany().getCurrency()) && saleOrder.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) != 0){
			BigDecimal rate = invoicedAmount.divide(saleOrder.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
			invoicedAmount = saleOrder.getExTaxTotal().multiply(rate);
		}

		log.debug("Compute the invoiced amount ({}) of the sale order : {}", invoicedAmount, saleOrder.getSaleOrderSeq());
		
		return invoicedAmount;

	}
	
	
	private BigDecimal getAmountVentilated(SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice, int invoiceOperationTypeSelect)  {
		
		String query = "SELECT SUM(self.companyExTaxTotal)"
					+ " FROM InvoiceLine as self"
					+ " WHERE (self.saleOrderLine.saleOrder.id = :saleOrderId OR self.saleOrder.id = :saleOrderId )"
						+ " AND self.invoice.operationTypeSelect = :invoiceOperationTypeSelect"
						+ " AND self.invoice.statusSelect = :statusVentilated";
		
		if (currentInvoiceId != null)  {
			if(excludeCurrentInvoice)  {
				query += " AND self.invoice.id <> :invoiceId";
			}  else  {
				query += " OR (self.invoice.id = :invoiceId AND self.invoice.operationTypeSelect = :invoiceOperationTypeSelect) ";
			}
		}
			
		Query q = JPA.em().createQuery(query, BigDecimal.class);

		q.setParameter("saleOrderId", saleOrder.getId());
		q.setParameter("statusVentilated", InvoiceRepository.STATUS_VENTILATED);
		q.setParameter("invoiceOperationTypeSelect", invoiceOperationTypeSelect);
		if (currentInvoiceId != null){
			q.setParameter("invoiceId", currentInvoiceId);
		}

		BigDecimal invoicedAmount = (BigDecimal) q.getSingleResult();
		
		if(invoicedAmount != null)  {  return invoicedAmount;  }
		else  {  return BigDecimal.ZERO;  }
		
	}
	

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubscriptionInvoice(List<Subscription> subscriptionList, SaleOrder saleOrder) throws AxelorException{

		if(subscriptionList == null || subscriptionList.isEmpty()){
			return null;
		}

		InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);

		Invoice invoice = invoiceGenerator.generate();

		invoiceGenerator.populate(invoice, this.createSubscriptionInvoiceLines(invoice, subscriptionList));

		this.fillInLines(invoice);

		invoice.setIsSubscription(true);

		Beans.get(InvoiceRepository.class).save(invoice);

		return invoice;
	}

	@Override
	public void fillInLines(Invoice invoice){
		List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
		for (InvoiceLine invoiceLine : invoiceLineList) {
			invoiceLine.setSaleOrder(invoice.getSaleOrder());
		}
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubcriptionInvoiceForSaleOrder(SaleOrder saleOrder) throws AxelorException{
		List<Subscription> subscriptionList = Beans.get(SubscriptionRepository.class).all().filter("self.invoicingDate <= ?1 AND self.saleOrderLine.saleOrder.id = ?2 AND self.invoiced = false",generalService.getTodayDate(),saleOrder.getId()).fetch();
		if(subscriptionList != null && !subscriptionList.isEmpty()){
			return this.generateSubscriptionInvoice(subscriptionList,saleOrder);
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubcriptionInvoiceForSaleOrderAndListSubscrip(Long saleOrderId, List<Long> subscriptionIdList) throws AxelorException{
		List<Subscription> subscriptionList = Beans.get(SubscriptionRepository.class).all().filter("self.id IN (:subscriptionIds)").bind("subscriptionIds", subscriptionIdList).fetch();
		if(subscriptionList != null && !subscriptionList.isEmpty()){
			return this.generateSubscriptionInvoice(subscriptionList, saleOrderRepo.find(saleOrderId));
		}
		return null;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateSubcriptionInvoiceForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException{
		List<Subscription> subscriptionList = Beans.get(SubscriptionRepository.class).all().filter("self.invoicingDate <= ?1 AND self.saleOrderLine.id = ?2 AND self.invoiced = false",generalService.getTodayDate(),saleOrderLine.getId()).fetch();
		if(subscriptionList != null && !subscriptionList.isEmpty()){
			return this.generateSubscriptionInvoice(subscriptionList, saleOrderLine.getSaleOrder());
		}
		return null;
	}


}



