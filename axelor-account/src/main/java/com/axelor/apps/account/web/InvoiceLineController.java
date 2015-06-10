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
package com.axelor.apps.account.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.SupplierCatalogRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceLineController {

	@Inject
	private InvoiceLineService invoiceLineService;

	@Inject
	private PriceListService priceListService;

	@Inject
	private ProductService productService;

	public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal accountingExTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		BigDecimal companyInTaxTotal = BigDecimal.ZERO;
		BigDecimal priceDiscounted = BigDecimal.ZERO;
		
		
		if(invoiceLine.getTaxLine()==null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.INVOICE_LINE_TAX_LINE)), IException.CONFIGURATION_ERROR);
		}
		Invoice invoice = invoiceLine.getInvoice();
		if(invoice == null){
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}
		if(!request.getContext().getParentContext().asType(Invoice.class).getInAti()){
			if(invoiceLine.getPrice() != null && invoiceLine.getQty() != null) {

				exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), invoiceLineService.computeDiscount(invoiceLine,invoice));
				inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(invoiceLine.getTaxLine().getValue()));
				priceDiscounted = invoiceLineService.computeDiscount(invoiceLine,invoice);
			}

			if(exTaxTotal != null) {

				if(invoice != null) {

					accountingExTaxTotal = invoiceLineService.getAccountingExTaxTotal(exTaxTotal, invoice);
					companyExTaxTotal = invoiceLineService.getCompanyExTaxTotal(exTaxTotal, invoice);
				}
			}
			response.setValue("exTaxTotal", exTaxTotal);
			response.setValue("inTaxTotal", inTaxTotal);
			response.setValue("accountingExTaxTotal", accountingExTaxTotal);
			response.setValue("companyExTaxTotal", companyExTaxTotal);
			response.setValue("priceDiscounted", priceDiscounted);
			
		}
		else{
			if(invoiceLine.getPrice() != null && invoiceLine.getQty() != null) {

				inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), invoiceLineService.computeDiscount(invoiceLine,invoice));
				exTaxTotal = inTaxTotal.divide(invoiceLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
				priceDiscounted = invoiceLineService.computeDiscount(invoiceLine,invoice);
			}

			if(inTaxTotal != null) {

				if(invoice != null) {
					
					accountingExTaxTotal = invoiceLineService.getAccountingExTaxTotal(inTaxTotal, invoice);
					companyInTaxTotal = invoiceLineService.getCompanyExTaxTotal(inTaxTotal, invoice);
				}
			}

			response.setValue("exTaxTotal", exTaxTotal);
			response.setValue("inTaxTotal", inTaxTotal);
			response.setValue("accountingExTaxTotal", accountingExTaxTotal);
			response.setValue("companyInTaxTotal", companyInTaxTotal);
			response.setValue("priceDiscounted", priceDiscounted);
			
		}

	}

	public void getProductInformation(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

		Invoice invoice = invoiceLine.getInvoice();

		if(invoice == null)  {
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}

		if(invoice != null && invoiceLine.getProduct() != null)  {

			try  {

				boolean isPurchase = invoiceLineService.isPurchase(invoice);

				BigDecimal price = invoiceLineService.getUnitPrice(invoice, invoiceLine, isPurchase);
				int discountTypeSelect = 0;

				response.setValue("taxLine", invoiceLineService.getTaxLine(invoice, invoiceLine, isPurchase));
				response.setValue("productName", invoiceLine.getProduct().getName());
				response.setValue("unit", invoiceLine.getProduct().getUnit());

				PriceList priceList = invoice.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = invoiceLineService.getPriceListLine(invoiceLine, priceList);
					if(priceListLine!=null){
						discountTypeSelect = priceListLine.getTypeSelect();
					}
					if((GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) || GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT)
					{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
						price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
						
					}
					else{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);

						response.setValue("discountAmount", discounts.get("discountAmount"));
						response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
						if(discounts.get("price") != null)  {
							price = (BigDecimal) discounts.get("price");
						}
					}
					
				}

				else if (invoice.getOperationTypeSelect()<InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
					List<SupplierCatalog> supplierCatalogList = invoiceLine.getProduct().getSupplierCatalogList();
					if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
						SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",invoiceLine.getProduct(),invoiceLine.getQty(),invoice.getPartner()).fetchOne();
						if(supplierCatalog!=null){
							Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);
							response.setValue("discountAmount", discounts.get("discountAmount"));
							response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
						}
					}
				}

				response.setValue("price", price);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
				this.resetProductInformation(response);
			}
		}
		else {
			this.resetProductInformation(response);
		}
	}


	public void resetProductInformation(ActionResponse response)  {

		response.setValue("taxLine", null);
		response.setValue("productName", null);
		response.setValue("unit", null);
		response.setValue("discountAmount", null);
		response.setValue("discountTypeSelect", null);
		response.setValue("price", null);

	}


	public void getDiscount(ActionRequest request, ActionResponse response) throws AxelorException {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

		Invoice invoice = invoiceLine.getInvoice();

		if(invoice == null)  {
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}

		if(invoice != null && invoiceLine.getProduct() != null)  {

			try  {

				BigDecimal price = invoiceLine.getProduct().getSalePrice();
				if(invoice.getOperationTypeSelect()<2){
					price = invoiceLine.getProduct().getPurchasePrice();
				}

				PriceList priceList = invoice.getPriceList();
				int discountTypeSelect = 0;
				
				if(priceList != null)  {
					PriceListLine priceListLine = invoiceLineService.getPriceListLine(invoiceLine, priceList);
					if(priceListLine!=null){
						discountTypeSelect = priceListLine.getTypeSelect();
					}
					
					if((GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) || GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT)
					{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
						price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
						
					}
					else{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);

						response.setValue("discountAmount", discounts.get("discountAmount"));
						response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
						if(discounts.get("price") != null)  {
							price = (BigDecimal) discounts.get("price");
						}
					}
					
				}

				else if (invoice.getOperationTypeSelect()<InvoiceRepository.OPERATION_TYPE_CLIENT_SALE){
					List<SupplierCatalog> supplierCatalogList = invoiceLine.getProduct().getSupplierCatalogList();
					if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
						for (SupplierCatalog supplierCatalog : supplierCatalogList) {
							if(supplierCatalog.getProduct().equals(invoiceLine.getProduct()) && supplierCatalog.getMinQty().compareTo(invoiceLine.getQty())<=0 && supplierCatalog.getSupplierPartner().equals(invoice.getPartner())){
								Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);
								response.setValue("discountAmount", discounts.get("discountAmount"));
								response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
							}
						}

					}
				}

				response.setValue("price", price);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
			}
		}
	}

	public void convertUnitPrice(ActionRequest request, ActionResponse response) {

		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

		Invoice invoice = invoiceLine.getInvoice();
		if(invoice == null)  {
			invoice = request.getContext().getParentContext().asType(Invoice.class);
		}

		if(invoice != null) {

			try  {

				BigDecimal price = invoiceLineService.convertUnitPrice(invoiceLine, invoice);
				BigDecimal discountAmount = invoiceLineService.convertDiscountAmount(invoiceLine, invoice);

				response.setValue("price", price);
				response.setValue("discountAmount",discountAmount);

			}
			catch(Exception e)  {
				response.setFlash(e.getMessage());
			}
		}
	}
}
