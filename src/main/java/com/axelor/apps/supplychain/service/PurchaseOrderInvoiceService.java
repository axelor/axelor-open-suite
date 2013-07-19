package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderInvoiceService {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderInvoiceService.class);

	@Inject
	private CurrencyService currencyService;

	private LocalDate today;

	public PurchaseOrderInvoiceService() {

		this.today = GeneralService.getTodayDate();
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice generateInvoice(PurchaseOrder purchaseOrder) throws AxelorException  {

		Invoice invoice = this.createInvoice(purchaseOrder);
		invoice.save();
		
		return invoice;
	}

	public Invoice createInvoice(PurchaseOrder purchaseOrder) throws AxelorException{

		InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(purchaseOrder);

		Invoice invoice = invoiceGenerator.generate();

		invoiceGenerator.populate(invoice, this.createInvoiceLines(invoice, purchaseOrder.getPurchaseOrderLineList()));
		invoiceGenerator.computeInvoice(invoice);
		return invoice;
	}

	public InvoiceGenerator createInvoiceGenerator(PurchaseOrder purchaseOrder) throws AxelorException  {

		if(purchaseOrder.getCurrency() == null)  {
			throw new AxelorException(String.format("Veuillez selectionner une devise pour la commande %s ", purchaseOrder.getPurchaseOrderSeq()), IException.CONFIGURATION_ERROR);
		}

		InvoiceGenerator invoiceGenerator = new InvoiceGenerator(IInvoice.SUPPLIER_PURCHASE, purchaseOrder.getCompany(), purchaseOrder.getSupplierPartner(), null) {

			@Override
			public Invoice generate() throws AxelorException {

				return super.createInvoiceHeader();
			}
		};

		return invoiceGenerator;
	}

	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, purchaseOrderLine));
		}
		return invoiceLineList;
	}
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, BigDecimal exTaxTotal, Product product, String productName, BigDecimal price, String description, BigDecimal qty,
			Unit unit, VatLine vatLine, ProductVariant productVariant) throws AxelorException  {
		
		InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, productName, price, description, qty, unit, vatLine, null, product.getInvoiceLineType(), productVariant, false)  {
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
	
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, PurchaseOrderLine purchaseOrderLine) throws AxelorException {
		
		return this.createInvoiceLine(invoice, purchaseOrderLine.getExTaxTotal(), purchaseOrderLine.getProduct(), purchaseOrderLine.getProductName(), 
				purchaseOrderLine.getPrice(), purchaseOrderLine.getDescription(), new BigDecimal(purchaseOrderLine.getQty()), purchaseOrderLine.getUnit(), purchaseOrderLine.getVatLine(), purchaseOrderLine.getProductVariant());
	}
			
}
