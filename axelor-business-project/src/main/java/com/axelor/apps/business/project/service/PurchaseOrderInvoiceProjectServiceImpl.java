package com.axelor.apps.business.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PurchaseOrderInvoiceProjectServiceImpl extends PurchaseOrderInvoiceServiceImpl{



	@Inject
	private PriceListService priceListService;

	@Inject
	private PurchaseOrderLineServiceImpl purchaseOrderLineServiceImpl;


	@Override
	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<PurchaseOrderLine> purchaseOrderLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, purchaseOrderLine));
			invoiceLineList.get(invoiceLineList.size()-1).setProject(purchaseOrderLine.getProjectTask());
			purchaseOrderLine.setInvoiced(true);
		}
		return invoiceLineList;
	}

	@Override
	public List<InvoiceLine> createInvoiceLine(Invoice invoice, PurchaseOrderLine purchaseOrderLine) throws AxelorException  {

		Product product = purchaseOrderLine.getProduct();
		BigDecimal price = product.getCostPrice();
		BigDecimal discountAmount = product.getCostPrice();
		int discountTypeSelect = 1;
		if(invoice.getPartner().getFlatFeePurchase()){
			PriceList priceList = invoice.getPartner().getSalePriceList();
			if(priceList != null)  {
				PriceListLine priceListLine = purchaseOrderLineServiceImpl.getPriceListLine(purchaseOrderLine, priceList);
				if(priceListLine!=null){
					discountTypeSelect = priceListLine.getTypeSelect();
				}
				if((GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) || GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT)
				{
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					discountAmount = (BigDecimal) discounts.get("discountAmount");
					price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), discountAmount);

				}
				else{
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					discountAmount = (BigDecimal) discounts.get("discountAmount");
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}

			}


			InvoiceLineGenerator invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), price,
					purchaseOrderLine.getDescription(),purchaseOrderLine.getQty(),purchaseOrderLine.getUnit(),InvoiceLineGenerator.DEFAULT_SEQUENCE,discountAmount,discountTypeSelect,
					price.multiply(purchaseOrderLine.getQty()), null, false)   {
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


		else{
			InvoiceLineGeneratorSupplyChain invoiceLineGenerator = new InvoiceLineGeneratorSupplyChain(invoice, product, purchaseOrderLine.getProductName(),
					purchaseOrderLine.getDescription(), purchaseOrderLine.getQty(), purchaseOrderLine.getUnit(),
					purchaseOrderLine.getSequence(), false, null, purchaseOrderLine, null)  {
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
}
