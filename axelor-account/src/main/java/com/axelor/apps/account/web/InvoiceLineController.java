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
package com.axelor.apps.account.web;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.Product;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InvoiceLineController {

	@Inject
	private InvoiceLineService invoiceLineService;

	public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response) throws AxelorException{
		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
		Invoice invoice = invoiceLine.getInvoice();
		if(invoice == null){
			invoice = request.getContext().getParent().asType(Invoice.class);
			invoiceLine.setInvoice(invoice);
		}
		if(invoiceLine.getAnalyticDistributionTemplate() != null){
			invoiceLine = invoiceLineService.createAnalyticDistributionWithTemplate(invoiceLine);
			response.setValue("analyticMoveLineList", invoiceLine.getAnalyticMoveLineList());
		} else {
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("No template selected"));
		}
	}
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) throws AxelorException{
		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
		Invoice invoice = invoiceLine.getInvoice();
		if(invoice == null){
			invoice = request.getContext().getParent().asType(Invoice.class);
			invoiceLine.setInvoice(invoice);
		}
		if(Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()){
			invoiceLine = invoiceLineService.computeAnalyticDistribution(invoiceLine);
			response.setValue("analyticMoveLineList", invoiceLine.getAnalyticMoveLineList());
		}
	}
	
	public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();
		
		InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

		Invoice invoice = this.getInvoice(context);
		
		if(invoice == null || invoiceLine.getPrice() == null || invoiceLine.getQty() == null)  {  return;  }
		
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		BigDecimal companyInTaxTotal = BigDecimal.ZERO;
		BigDecimal priceDiscounted = invoiceLineService.computeDiscount(invoiceLine,invoice);
		
		response.setValue("priceDiscounted", priceDiscounted);
		response.setAttr("priceDiscounted", "hidden", priceDiscounted.compareTo(invoiceLine.getPrice()) == 0);

		BigDecimal taxRate = BigDecimal.ZERO;
		if(invoiceLine.getTaxLine() != null)  {
			taxRate = invoiceLine.getTaxLine().getValue();
			response.setValue("taxRate", taxRate);
			response.setValue("taxCode", invoiceLine.getTaxLine().getTax().getCode());
		}

		if(!invoice.getInAti()) {
			exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), invoiceLineService.computeDiscount(invoiceLine,invoice));
			inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
		} else {
			inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), invoiceLineService.computeDiscount(invoiceLine,invoice));
			exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
		}
		
		companyExTaxTotal = invoiceLineService.getCompanyExTaxTotal(exTaxTotal, invoice);
		companyInTaxTotal = invoiceLineService.getCompanyExTaxTotal(inTaxTotal, invoice);
		
		response.setValue("exTaxTotal", exTaxTotal);
		response.setValue("inTaxTotal", inTaxTotal);
		response.setValue("companyInTaxTotal", companyInTaxTotal);
		response.setValue("companyExTaxTotal", companyExTaxTotal);

	}
	
	
	public void getProductInformation(ActionRequest request, ActionResponse response) throws AxelorException {
		Context context = request.getContext();
		InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
		Invoice invoice = this.getInvoice(context);
		Product product = invoiceLine.getProduct();
		Map<String, Object> productInformation = invoiceLineService.resetProductInformation();

		if(invoice != null && product != null) {
			try  {
			    productInformation = invoiceLineService.fillProductInformation(invoice, invoiceLine);
			}
			catch(Exception e) {
				TraceBackService.trace(response, e);
			}
		}
		response.setValues(productInformation);
	}

	public void getDiscount(ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();
		
		InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

		Invoice invoice = this.getInvoice(context);
		
		if(invoice == null || invoiceLine.getProduct() == null) {  return;  }

		try  {
			BigDecimal price = invoiceLine.getPrice();

			Map<String, Object> discounts = invoiceLineService.getDiscount(invoice, invoiceLine, price);
			
			if(discounts != null)  {
				response.setValue("discountAmount", discounts.get("discountAmount"));
				response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
				
				if(discounts.get("price") != null)  {
					response.setValue("price", (BigDecimal) discounts.get("price"));
				}
			}
		}
		catch(Exception e) {
			response.setFlash(e.getMessage());
		}
	}

	public void convertUnitPrice(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		
		InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

		Invoice invoice = this.getInvoice(context);
		
		if(invoice == null || invoiceLine.getProduct() == null || !invoiceLineService.unitPriceShouldBeUpdate(invoice, invoiceLine.getProduct())) {  return;  }

		try  {

			BigDecimal price = invoiceLineService.getUnitPrice(invoice, invoiceLine, invoiceLine.getTaxLine(), invoiceLineService.isPurchase(invoice));
			
			Map<String,Object> discounts = invoiceLineService.getDiscount(invoice, invoiceLine, price);
			
			if(discounts != null)  {  
			
				response.setValue("discountAmount", discounts.get("discountAmount"));
				response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
				if(discounts.get("price") != null)  {
					price = (BigDecimal) discounts.get("price");
				}
			}
			response.setValue("price", price);

		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}
	}

	
	public void emptyLine(ActionRequest request, ActionResponse response){
		InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
		if(invoiceLine.getIsTitleLine()){
			InvoiceLine newInvoiceLine = new InvoiceLine();
			newInvoiceLine.setIsTitleLine(true);
			newInvoiceLine.setQty(BigDecimal.ZERO);
			newInvoiceLine.setId(invoiceLine.getId());
			newInvoiceLine.setVersion(invoiceLine.getVersion());
			response.setValues(Mapper.toMap(newInvoiceLine));
		}
	}
	
	
	public Invoice getInvoice(Context context)  {
		
		Context parentContext = context.getParent();
		
		Invoice invoice = parentContext.asType(Invoice.class);
		
		if(!parentContext.getContextClass().toString().equals(Invoice.class.toString())){
			
			InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
			
			invoice = invoiceLine.getInvoice();
		}
		
		return invoice;
	}

}
