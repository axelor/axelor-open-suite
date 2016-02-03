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
package com.axelor.apps.account.service.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.SupplierCatalogRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class InvoiceLineService {

	protected AccountManagementService accountManagementService;
	protected CurrencyService currencyService;
	protected PriceListService priceListService;
	protected GeneralService generalService;
	protected AnalyticDistributionLineService analyticDistributionLineService;
	protected ProductService productService;

	@Inject
	public InvoiceLineService(AccountManagementService accountManagementService, CurrencyService currencyService, PriceListService priceListService, 
			GeneralService generalService, AnalyticDistributionLineService analyticDistributionLineService, ProductService productService)  {
		
		this.accountManagementService = accountManagementService;
		this.currencyService = currencyService;
		this.priceListService = priceListService;
		this.generalService = generalService;
		this.analyticDistributionLineService = analyticDistributionLineService;
		this.productService = productService;
		
	}
	
	public InvoiceLine createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = null;
		analyticDistributionLineList = analyticDistributionLineService.generateLinesWithTemplate(invoiceLine.getAnalyticDistributionTemplate(), invoiceLine.getExTaxTotal());
		if(analyticDistributionLineList != null){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setInvoiceLine(invoiceLine);
			}
		}
		invoiceLine.setAnalyticDistributionLineList(analyticDistributionLineList);
		return invoiceLine;
	}
	
	public InvoiceLine computeAnalyticDistribution(InvoiceLine invoiceLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = invoiceLine.getAnalyticDistributionLineList();
		if((analyticDistributionLineList == null || analyticDistributionLineList.isEmpty()) && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			analyticDistributionLineList = analyticDistributionLineService.generateLines(invoiceLine.getInvoice().getPartner(), invoiceLine.getProduct(), invoiceLine.getInvoice().getCompany(), invoiceLine.getExTaxTotal());
			if(analyticDistributionLineList != null){
				for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
					analyticDistributionLine.setInvoiceLine(invoiceLine);
					analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
					analyticDistributionLine.setDate(generalService.getTodayDate());
				}
				invoiceLine.setAnalyticDistributionLineList(analyticDistributionLineList);
			}
		}
		else if(analyticDistributionLineList != null && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setInvoiceLine(invoiceLine);
				analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
				analyticDistributionLine.setDate(generalService.getTodayDate());
			}
		}
		return invoiceLine;
	}
	
	
	public TaxLine getTaxLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException  {

		return accountManagementService.getTaxLine(
				generalService.getTodayDate(), invoiceLine.getProduct(), invoice.getCompany(), invoice.getPartner().getFiscalPosition(), isPurchase);

	}


	public BigDecimal getUnitPrice(Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase) throws AxelorException  {

		Product product = invoiceLine.getProduct();
		
		BigDecimal price = null;
		Currency productCurrency;
		
		if(isPurchase)  {
			price = this.convertUnitPrice(product, taxLine, product.getPurchasePrice(), invoice);
			productCurrency = product.getPurchaseCurrency();
		}
		else  {
			price = this.convertUnitPrice(product, taxLine, product.getSalePrice(), invoice);
			productCurrency = product.getSaleCurrency();
		}
		
		return currencyService.getAmountCurrencyConverted(
				productCurrency, invoice.getCurrency(), price, invoice.getInvoiceDate()).setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
	}


	public boolean isPurchase(Invoice invoice)  {
		int operation = invoice.getOperationTypeSelect();
		if(operation == 1 || operation == 2)  { return true; }
		else  { return false; }
	}


	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {

		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getPartner().getCurrency(), exTaxTotal, invoice.getInvoiceDate()).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
	}


	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice) throws AxelorException  {

		return currencyService.getAmountCurrencyConverted(
				invoice.getCurrency(), invoice.getCompany().getCurrency(), exTaxTotal, invoice.getInvoiceDate()).setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
	}


	public PriceListLine getPriceListLine(InvoiceLine invoiceLine, PriceList priceList)  {

		return priceListService.getPriceListLine(invoiceLine.getProduct(), invoiceLine.getQty(), priceList);

	}

	public BigDecimal computeDiscount(InvoiceLine invoiceLine, Invoice invoice)  {
		BigDecimal unitPrice = invoiceLine.getPrice();

		return priceListService.computeDiscount(unitPrice, invoiceLine.getDiscountTypeSelect(), invoiceLine.getDiscountAmount());
	}
	
	public BigDecimal computeDiscount(int discountTypeSelect, BigDecimal discountAmount, BigDecimal unitPrice,Invoice invoice)  {

		return priceListService.computeDiscount(unitPrice,discountTypeSelect, discountAmount);
	}

	public BigDecimal convertUnitPrice(Product product, TaxLine taxLine, BigDecimal price, Invoice invoice){
		
		if(taxLine == null)  {  return price;  }

		if(product.getInAti() && !invoice.getInAti()){
			price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
		}
		else if(!product.getInAti() && invoice.getInAti()){
			price = price.add(price.multiply(taxLine.getValue()));
		}
		return price;
	}
	
	
	public Map<String,Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)  {
		
		PriceList priceList = invoice.getPriceList();
		BigDecimal discountAmount = BigDecimal.ZERO;
		Map<String, Object> discounts = null;
		
		int computeMethodDiscountSelect = generalService.getGeneral().getComputeMethodDiscountSelect();

		if(priceList != null)  {
			int discountTypeSelect = 0;
			
			PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList);
			if(priceListLine!=null){
				discountTypeSelect = priceListLine.getTypeSelect();
			}

			discounts = priceListService.getDiscounts(priceList, priceListLine, price);
			discountAmount = (BigDecimal) discounts.get("discountAmount");
			
			if((computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) 
					|| computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT)  {
				discounts.put("price", priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), discountAmount));

			}
		}

		if (invoice.getOperationTypeSelect() < InvoiceRepository.OPERATION_TYPE_CLIENT_SALE && discountAmount.compareTo(BigDecimal.ZERO) == 0){
			List<SupplierCatalog> supplierCatalogList = invoiceLine.getProduct().getSupplierCatalogList();
			if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
				SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",invoiceLine.getProduct(),invoiceLine.getQty(),invoice.getPartner()).fetchOne();
				if(supplierCatalog != null){
					
					discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);

					if(computeMethodDiscountSelect != GeneralRepository.DISCOUNT_SEPARATE){
						discounts.put("price", priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount")));
					}
				}
			}
		}
		
		return discounts;
	}
	
	
	public int getDiscountTypeSelect(Invoice invoice, InvoiceLine invoiceLine){
		PriceList priceList = invoice.getPriceList();
		if(priceList != null)  {
			PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList);

			return priceListLine.getTypeSelect();
		}
		return 0;
	}
	
	public Unit getUnit(Product product, boolean isPurchase){
		return product.getUnit();
	}
	
	public boolean unitPriceShouldBeUpdate(Invoice invoice, Product product)  {
		
		if(product != null && product.getInAti() != invoice.getInAti())  {
			return true;
		}
		return false;
		
	}
}
