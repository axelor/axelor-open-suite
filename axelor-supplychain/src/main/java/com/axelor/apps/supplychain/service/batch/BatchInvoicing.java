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
package com.axelor.apps.supplychain.service.batch;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchInvoicing extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private SaleOrderRepository saleOrderRepo;

	@Inject
	public BatchInvoicing(SaleOrderInvoiceService saleOrderInvoiceService) {

		super(saleOrderInvoiceService);
	}


	@Override
	protected void process() {

		int i = 0;
		LocalDate todayDate = appBaseService.getTodayDate();
		
		List<SaleOrder> subscrptionOrders = saleOrderRepo.all()
				.filter("self.saleOrderTypeSelect = 2 and self.statusSelect = ?1"
						+ " and ?2 >= self.nextInvoicingDate and (self.contractEndDate is null OR self.contractEndDate >= ?2)"
						, ISaleOrder.STATUS_ORDER_CONFIRMED, todayDate).fetch();
		
		TemporalUnit temporalUnit = ChronoUnit.MONTHS;

		for (SaleOrder saleOrder : subscrptionOrders) {
			
			try {

				Invoice invoice = saleOrderInvoiceService.generateInvoice(saleOrderRepo.find(saleOrder.getId()));

				if(invoice != null)  {
					
					invoice = saleOrderInvoiceService.generateInvoice(saleOrder);
					if (invoice == null) { continue; }
					if (saleOrder.getPeriodicityTypeSelect() == 1) {
						temporalUnit = ChronoUnit.DAYS;
					}
					invoice.setInvoiceDate(todayDate);
					invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_SUBSCRIPTION);

					LocalDate invoicingPeriodStartDate = saleOrder.getNextInvoicingStartPeriodDate();
					invoice.setSubscriptionFromDate(invoicingPeriodStartDate);
					if (invoicingPeriodStartDate != null) {
						LocalDate subscriptionToDate = invoicingPeriodStartDate.plus(saleOrder.getNumberOfPeriods(), temporalUnit);
						saleOrder.setNextInvoicingStartPeriodDate(subscriptionToDate);
						invoice.setSubscriptionToDate(subscriptionToDate.minusDays(1));
					}
					
					LocalDate nextInvoicingDate = saleOrder.getNextInvoicingDate();
					if (nextInvoicingDate != null) {
						nextInvoicingDate = nextInvoicingDate.plus(saleOrder.getNumberOfPeriods(), temporalUnit);
					}
					saleOrder.setNextInvoicingDate(nextInvoicingDate);
					
					updateSaleOrder(saleOrder);
					LOG.debug("Facture créée ({}) pour le devis {}", invoice.getInvoiceId(), saleOrder.getSaleOrderSeq());
					i++;

				}

			} catch (AxelorException e) {

				TraceBackService.trace(new AxelorException(e, e.getCategory(), I18n.get("Order")+" %s", saleOrderRepo.find(saleOrder.getId()).getSaleOrderSeq()), IException.INVOICE_ORIGIN, batch.getId());
				incrementAnomaly();

			} catch (Exception e) {

				TraceBackService.trace(new Exception(String.format(I18n.get("Order")+" %s", saleOrderRepo.find(saleOrder.getId()).getSaleOrderSeq()), e), IException.INVOICE_ORIGIN, batch.getId());

				incrementAnomaly();

				LOG.error("Bug(Anomalie) généré(e) pour le devis {}", saleOrderRepo.find(saleOrder.getId()).getSaleOrderSeq());

			} finally {

				if (i % 10 == 0) { JPA.clear(); }

			}

		}


	}


	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_INVOICING_1) + " ";
		comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_INVOICING_2)+"\n", batch.getDone());
		comment += String.format("\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		super.stop();
		addComment(comment);

	}

}