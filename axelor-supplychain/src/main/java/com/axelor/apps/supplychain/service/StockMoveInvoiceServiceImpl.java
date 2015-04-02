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
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveInvoiceServiceImpl implements StockMoveInvoiceService  {

	@Inject
	private SaleOrderInvoiceService saleOrderInvoiceService;

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;

	@Inject
	private StockMoveRepository stockMoveRepo;

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInvoiceFromSaleOrder(StockMove stockMove, SaleOrder saleOrder) throws AxelorException  {

		InvoiceGenerator invoiceGenerator = saleOrderInvoiceService.createInvoiceGenerator(saleOrder);

		Invoice invoice = invoiceGenerator.generate();

		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));

		if (invoice != null) {

			this.extendInternalReference(stockMove, invoice);

			stockMove.setInvoice(invoice);
			stockMoveRepo.save(stockMove);
		}
		return invoice;

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInvoiceFromPurchaseOrder(StockMove stockMove, PurchaseOrder purchaseOrder) throws AxelorException  {

		InvoiceGenerator invoiceGenerator = purchaseOrderInvoiceService.createInvoiceGenerator(purchaseOrder);

		Invoice invoice = invoiceGenerator.generate();

		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));

		if (invoice != null) {

			this.extendInternalReference(stockMove, invoice);

			stockMove.setInvoice(invoice);
			stockMoveRepo.save(stockMove);
		}
		return invoice;
	}


	public Invoice extendInternalReference(StockMove stockMove, Invoice invoice)  {

		invoice.setInternalReference(stockMove.getStockMoveSeq()+":"+invoice.getInternalReference());

		return invoice;
	}


	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<StockMoveLine> stockMoveLineList) throws AxelorException {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for (StockMoveLine stockMoveLine : stockMoveLineList) {
			invoiceLineList.addAll(this.createInvoiceLine(invoice, stockMoveLine));
		}

		return invoiceLineList;
	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine) throws AxelorException {

		Product product = stockMoveLine.getProduct();

		if (product == null)
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_INVOICE_1), stockMoveLine.getStockMove().getStockMoveSeq()), IException.CONFIGURATION_ERROR);

		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), stockMoveLine.getPrice(),
				stockMoveLine.getDescription(), stockMoveLine.getQty(), stockMoveLine.getUnit(), product.getInvoiceLineType(),
				InvoiceLineGenerator.DEFAULT_SEQUENCE, BigDecimal.ZERO, 0, null, false)  {
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
