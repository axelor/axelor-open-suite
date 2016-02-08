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
package com.axelor.apps.supplychain.service.batch;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.apps.supplychain.db.repo.SubscriptionRepository;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class BatchSubscription extends AbstractBatch{

	protected String comment = "";

	@Inject
	protected SaleOrderInvoiceService saleOrderInvoiceService;

	@Override
	protected void process() {
		List<Subscription> lateSubscriptionsList = Beans.get(SubscriptionRepository.class).all().filter("self.invoicingDate <= now() AND self.invoiced = false").fetch();
		int i = 0;
		for (Subscription subscription : lateSubscriptionsList) {

			SaleOrderLine saleOrderLine = subscription.getSaleOrderLine();

			SaleOrder saleOrder = saleOrderLine.getSaleOrder();
			List<SaleOrderLine> saleOrderLineList = new ArrayList<SaleOrderLine>();
			saleOrderLineList.add(saleOrderLine);
			try {
				saleOrderInvoiceService.generateSubscriptionInvoice(lateSubscriptionsList,saleOrder);
				i++;
				incrementDone();

			}
			catch(Exception e)  {
				incrementAnomaly();
				comment += e.getMessage();
				TraceBackService.trace(new Exception(e),IException.INVOICE_ORIGIN,batch.getId());
			}
			finally{
				if (i % 10 == 0) { JPA.clear(); }
			}
		}
	}


	@Override
	protected void stop() {

		comment += String.format("\t* %s abonnement(s) facturé(s) \n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());


		super.stop();
		addComment(comment);

	}
}
