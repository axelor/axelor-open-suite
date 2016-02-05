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
package com.axelor.apps.supplychain.service.invoice.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Subscription;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

/**
 * Classe de création de ligne de facture abstraite.
 *
 */
public abstract class InvoiceLineGeneratorSupplyChain extends InvoiceLineGenerator {


	protected SaleOrderLine saleOrderLine;
	protected PurchaseOrderLine purchaseOrderLine;
	protected StockMove stockMove;
	protected Subscription subscription;
	protected Budget budget;
	
	
	protected InvoiceLineGeneratorSupplyChain( Invoice invoice, Product product, String productName, String description, BigDecimal qty,
			Unit unit, int sequence, boolean isTaxInvoice,
			SaleOrderLine saleOrderLine, PurchaseOrderLine purchaseOrderLine, StockMove stockMove, StockMoveLine stockMoveLine) throws AxelorException {
		this(invoice, product, productName, description, qty, unit, sequence, isTaxInvoice, saleOrderLine, purchaseOrderLine, stockMove, null, stockMoveLine);
    }
	

	protected InvoiceLineGeneratorSupplyChain( Invoice invoice, Product product, String productName, String description, BigDecimal qty,
			Unit unit, int sequence, boolean isTaxInvoice,
			SaleOrderLine saleOrderLine, PurchaseOrderLine purchaseOrderLine, StockMove stockMove,
			Subscription subscription, StockMoveLine stockMoveLine) throws AxelorException {

		super(invoice, product, productName, description, qty, unit, sequence, isTaxInvoice);

		if (subscription != null){
			this.subscription = subscription;
		}

		if (saleOrderLine != null){
			this.saleOrderLine = saleOrderLine;
			this.discountAmount = saleOrderLine.getDiscountAmount();
			this.price = saleOrderLine.getPrice();
			this.priceDiscounted = saleOrderLine.getPriceDiscounted();
			this.taxLine = saleOrderLine.getTaxLine();
			this.discountTypeSelect = saleOrderLine.getDiscountTypeSelect();
			if(!invoice.getInAti()){
				this.exTaxTotal = this.qty.multiply(this.priceDiscounted).setScale(2, RoundingMode.HALF_EVEN);
				if(taxLine != null){
					this.inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxLine.getValue())).setScale(2, RoundingMode.HALF_EVEN);
				}
			}
			else{
				this.inTaxTotal = this.qty.multiply(this.priceDiscounted).setScale(2, RoundingMode.HALF_EVEN);
				if(taxLine != null){
					this.exTaxTotal = inTaxTotal.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
				}
			}
			if (saleOrderLine.getProduct() != null && ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(saleOrderLine.getProduct().getProductTypeSelect())
					&& saleOrderLine.getSubscriptionList() != null
					&& !saleOrderLine.getSubscriptionList().isEmpty()){
				this.exTaxTotal = this.exTaxTotal.divide(new BigDecimal(saleOrderLine.getSubscriptionList().size())).setScale(2, RoundingMode.HALF_EVEN);
				this.inTaxTotal = this.inTaxTotal.divide(new BigDecimal(saleOrderLine.getSubscriptionList().size())).setScale(2, RoundingMode.HALF_EVEN);
			}
			analyticDistributionLineList = new ArrayList<AnalyticDistributionLine>();
			if(saleOrderLine.getAnalyticDistributionLineList() != null){
				for (AnalyticDistributionLine analyticDistributionLineIt : saleOrderLine.getAnalyticDistributionLineList()) {
					AnalyticDistributionLine analyticDistributionLine = Beans.get(AnalyticDistributionLineRepository.class).copy(analyticDistributionLineIt, false);
					analyticDistributionLineList.add(analyticDistributionLine);
				}
			}
			this.isTitleLine = saleOrderLine.getIsTitleLine();
			
		} else if (purchaseOrderLine != null){
			this.purchaseOrderLine = purchaseOrderLine;
			this.discountAmount = purchaseOrderLine.getDiscountAmount();
			this.price = purchaseOrderLine.getPrice();
			this.priceDiscounted = purchaseOrderLine.getPriceDiscounted();
			this.taxLine = purchaseOrderLine.getTaxLine();
			this.discountTypeSelect = purchaseOrderLine.getDiscountTypeSelect();
			if(!invoice.getInAti()){
				this.exTaxTotal = this.qty.multiply(this.priceDiscounted).setScale(2, RoundingMode.HALF_EVEN);
				if(taxLine != null){
					this.inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxLine.getValue())).setScale(2, RoundingMode.HALF_EVEN);
				}
			}
			else{
				this.inTaxTotal = this.qty.multiply(this.priceDiscounted).setScale(2, RoundingMode.HALF_EVEN);
				if(taxLine != null){
					this.exTaxTotal = inTaxTotal.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
				}
			}
			analyticDistributionLineList = new ArrayList<AnalyticDistributionLine>();
			if(purchaseOrderLine.getAnalyticDistributionLineList() != null){
				for (AnalyticDistributionLine analyticDistributionLineIt : purchaseOrderLine.getAnalyticDistributionLineList()) {
					AnalyticDistributionLine analyticDistributionLine = Beans.get(AnalyticDistributionLineRepository.class).copy(analyticDistributionLineIt, false);
					analyticDistributionLineList.add(analyticDistributionLine);
				}
			}
			budgetDistributionList = new ArrayList<BudgetDistribution>();
			if(purchaseOrderLine.getBudgetDistributionList() != null){
				for (BudgetDistribution budgetDistributionIt : purchaseOrderLine.getBudgetDistributionList()) {
					BudgetDistribution budgetDistribution = new BudgetDistribution();
					budgetDistribution.setBudget(budgetDistributionIt.getBudget());
					budgetDistribution.setAmount(budgetDistributionIt.getAmount());
					budgetDistributionList.add(budgetDistribution);
				}
			}
			budget = purchaseOrderLine.getBudget();
		}
		if(stockMoveLine != null && purchaseOrderLine == null && saleOrderLine == null){
			this.priceDiscounted = stockMoveLine.getUnitPriceUntaxed();
			if(invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE 
					|| invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND){
				this.unit = stockMoveLine.getProduct().getSalesUnit();
			}
			else{
				this.unit = stockMoveLine.getProduct().getPurchasesUnit();
			}
			if(this.unit == null){
				this.unit = unit;
			}
			else{
				if(!this.unit.equals(unit)){
					this.qty = Beans.get(UnitConversionService.class).convertWithProduct(unit, this.unit, qty, stockMoveLine.getProduct());
					this.priceDiscounted = Beans.get(UnitConversionService.class).convertWithProduct(unit, this.unit, this.priceDiscounted, stockMoveLine.getProduct());
				}
			}
			this.price = this.priceDiscounted;
			
			if(taxLine == null){
				this.taxLine = Beans.get(AccountManagementService.class).getTaxLine(
						Beans.get(GeneralService.class).getTodayDate(), stockMoveLine.getProduct(), invoice.getCompany(),
						invoice.getPartner().getFiscalPosition(), invoice.getOperationTypeSelect()<InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
			}
			if(!invoice.getInAti()){
				this.exTaxTotal = this.qty.multiply(this.priceDiscounted).setScale(2, RoundingMode.HALF_EVEN);
				if(taxLine != null){
					this.inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxLine.getValue())).setScale(2, RoundingMode.HALF_EVEN);
				}
			}
			else{
				this.inTaxTotal = this.qty.multiply(this.priceDiscounted).setScale(2, RoundingMode.HALF_EVEN);
				if(taxLine != null){
					this.exTaxTotal = inTaxTotal.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
				}
			}
			//Compute discount
			//compute totals and priceDiscounted
			if(!invoice.getInAti()){
				if(price != null && this.qty != null) {

					exTaxTotal = InvoiceLineManagement.computeAmount(this.qty, Beans.get(InvoiceLineService.class).computeDiscount(discountTypeSelect,discountAmount,price,invoice)).setScale(2, RoundingMode.HALF_EVEN);
					if(taxLine != null){
						inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxLine.getValue())).setScale(2, RoundingMode.HALF_EVEN);
					}
				}
			}
			else{
				if(price != null && this.qty != null) {

					inTaxTotal = InvoiceLineManagement.computeAmount(this.qty, Beans.get(InvoiceLineService.class).computeDiscount(discountTypeSelect,discountAmount,price,invoice)).setScale(2, RoundingMode.HALF_EVEN);
					if(taxLine != null){
						exTaxTotal = inTaxTotal.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
					}
				}
			}
		}

		if(stockMove != null){
			this.stockMove = stockMove;
		}
    }
	
	


	/**
	 * @return
	 * @throws AxelorException
	 */
	@Override
	protected InvoiceLine createInvoiceLine() throws AxelorException  {

		InvoiceLine invoiceLine = super.createInvoiceLine();
		if(analyticDistributionLineList != null){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setInvoiceLine(invoiceLine);
			}
			invoiceLine.setAnalyticDistributionLineList(analyticDistributionLineList);
		}
		
		if(budgetDistributionList != null){
			for (BudgetDistribution budgetDistribution : budgetDistributionList) {
				budgetDistribution.setInvoiceLine(invoiceLine);
			}
			invoiceLine.setBudgetDistributionList(budgetDistributionList);
		}
		invoiceLine.setBudget(budget);
		invoiceLine.setIsTitleLine(isTitleLine);
		
		if (Beans.get(GeneralService.class).getGeneral().getManageInvoicedAmountByLine()){
			if (saleOrderLine != null){
				invoiceLine.setSaleOrderLine(saleOrderLine);
				invoiceLine.setOutgoingStockMove(stockMove);
			}else{
				invoiceLine.setPurchaseOrderLine(purchaseOrderLine);
				invoiceLine.setIncomingStockMove(stockMove);
			}
		}

		return invoiceLine;

	}

}