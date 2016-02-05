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
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class AnalyticMoveLineService {
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected AnalyticMoveLineRepository analyticMoveLineRepository;
	
	@Inject
	protected AnalyticDistributionLineService analyticDistributionLineService;
	
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<AnalyticMoveLine> analyticMoveLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(AnalyticMoveLine analyticMoveLine : analyticMoveLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, analyticMoveLine));

		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, AnalyticMoveLine analyticMoveLine) throws AxelorException  {

		Product product = analyticMoveLine.getProduct();
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), product.getSalePrice().multiply(new BigDecimal(-1)),
					product.getSalePrice().multiply(new BigDecimal(-1)),null,analyticMoveLine.getQte(),product.getUnit(), null,10,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					null, null,false)  {

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
}
