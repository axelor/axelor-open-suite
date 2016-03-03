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
package com.axelor.apps.purchase.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Partner;
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
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PurchaseOrderLineServiceImpl implements PurchaseOrderLineService {
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineServiceImpl.class);

	@Inject
	protected CurrencyService currencyService;

	@Inject
	protected AccountManagementService accountManagementService;

	@Inject
	protected PriceListService priceListService;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected ProductService productService;

	private int sequence = 0;

	/**
	 * Calculer le montant HT d'une ligne de commande.
	 *
	 * @param quantity
	 *          Quantité.
	 * @param price
	 *          Le prix.
	 *
	 * @return
	 * 			Le montant HT de la ligne.
	 */
	public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}


	@Override
	public BigDecimal getUnitPrice(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, TaxLine taxLine) throws AxelorException  {

		Product product = purchaseOrderLine.getProduct();
		
		BigDecimal price = this.convertUnitPrice(product, taxLine, product.getPurchasePrice(), purchaseOrder);

		return currencyService.getAmountCurrencyConverted(
			product.getPurchaseCurrency(), purchaseOrder.getCurrency(), price, purchaseOrder.getOrderDate())
			.setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
	}
	

	@Override
	public BigDecimal getMinSalePrice(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException  {

		Product product = purchaseOrderLine.getProduct();
		
		TaxLine saleTaxLine = accountManagementService.getTaxLine(
				purchaseOrder.getOrderDate(), purchaseOrderLine.getProduct(), purchaseOrder.getCompany(), purchaseOrder.getSupplierPartner().getFiscalPosition(), false);
		
		BigDecimal price = this.convertUnitPrice(product, saleTaxLine, product.getSalePrice(), purchaseOrder);

		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), purchaseOrder.getCurrency(), price, purchaseOrder.getOrderDate())
			.setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
	}

	@Override
	public BigDecimal getSalePrice(PurchaseOrder purchaseOrder, Product product, BigDecimal price) throws AxelorException  {

		TaxLine saleTaxLine = accountManagementService.getTaxLine(
				purchaseOrder.getOrderDate(), product, purchaseOrder.getCompany(), purchaseOrder.getSupplierPartner().getFiscalPosition(), false);
		
		price = this.convertUnitPrice(product, saleTaxLine, price, purchaseOrder);
		price = price.multiply(product.getManagPriceCoef());
		
		return currencyService.getAmountCurrencyConverted(
				product.getSaleCurrency(), purchaseOrder.getCurrency(), price, purchaseOrder.getOrderDate())
				.setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

	}


	@Override
	public TaxLine getTaxLine(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException  {

		return accountManagementService.getTaxLine(
				purchaseOrder.getOrderDate(), purchaseOrderLine.getProduct(), purchaseOrder.getCompany(), purchaseOrder.getSupplierPartner().getFiscalPosition(), true);

	}


	@Override
	public BigDecimal computePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)  {

		return purchaseOrderLine.getExTaxTotal();
	}


	@Override
	public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, PurchaseOrder purchaseOrder) throws AxelorException  {

		return currencyService.getAmountCurrencyConverted(
				purchaseOrder.getCurrency(), purchaseOrder.getCompany().getCurrency(), exTaxTotal, purchaseOrder.getOrderDate())
				.setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
	}


	@Override
	public PriceListLine getPriceListLine(PurchaseOrderLine purchaseOrderLine, PriceList priceList)  {

		return priceListService.getPriceListLine(purchaseOrderLine.getProduct(), purchaseOrderLine.getQty(), priceList);

	}


	@Override
	public BigDecimal computeDiscount(PurchaseOrderLine purchaseOrderLine)  {

		return priceListService.computeDiscount(purchaseOrderLine.getPrice(), purchaseOrderLine.getDiscountTypeSelect(),purchaseOrderLine.getDiscountAmount());

	}


	@Override
	public PurchaseOrderLine createPurchaseOrderLine(PurchaseOrder purchaseOrder, Product product, String productName, String description, BigDecimal qty, Unit unit) throws AxelorException  {

		PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
		purchaseOrderLine.setPurchaseOrder(purchaseOrder);

		purchaseOrderLine.setEstimatedDelivDate(purchaseOrder.getDeliveryDate());
		purchaseOrderLine.setDescription(description);

		purchaseOrderLine.setIsOrdered(false);

		purchaseOrderLine.setQty(qty);
		purchaseOrderLine.setSequence(sequence);
		sequence++;

		purchaseOrderLine.setUnit(unit);
		purchaseOrderLine.setProductName(productName);
		
		if(product == null)  {	 return purchaseOrderLine; 	}
		
		purchaseOrderLine.setProduct(product);
		
		if(productName == null)  {
			purchaseOrderLine.setProductName(product.getName());
		}
		
		TaxLine taxLine = this.getTaxLine(purchaseOrder, purchaseOrderLine);
		purchaseOrderLine.setTaxLine(taxLine);
		
		BigDecimal price = this.getUnitPrice(purchaseOrder, purchaseOrderLine, taxLine);
		
		Map<String, Object> discounts = this.getDiscount(purchaseOrder, purchaseOrderLine, price);
		
		if(discounts != null){
			purchaseOrderLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
			purchaseOrderLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
			if(discounts.get("price") != null)  {
				price = (BigDecimal) discounts.get("price");
			}
		}

		purchaseOrderLine.setPrice(price);

		purchaseOrderLine.setPriceDiscounted(this.computeDiscount(purchaseOrderLine));
		
		BigDecimal exTaxTotal, inTaxTotal, companyExTaxTotal, companyInTaxTotal;
		
		if(!purchaseOrder.getInAti()){
			exTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), this.computeDiscount(purchaseOrderLine));
			inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(purchaseOrderLine.getTaxLine().getValue()));
			companyExTaxTotal = this.getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
			companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(purchaseOrderLine.getTaxLine().getValue()));

		}
		else{
			inTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), this.computeDiscount(purchaseOrderLine));
			exTaxTotal = inTaxTotal.divide(purchaseOrderLine.getTaxLine().getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
			companyInTaxTotal = this.getCompanyExTaxTotal(inTaxTotal, purchaseOrder);
			companyExTaxTotal = companyInTaxTotal.divide(purchaseOrderLine.getTaxLine().getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);

		}
		
		purchaseOrderLine.setExTaxTotal(exTaxTotal);
		purchaseOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
		purchaseOrderLine.setCompanyInTaxTotal(companyInTaxTotal);
		purchaseOrderLine.setInTaxTotal(inTaxTotal);

		return purchaseOrderLine;
	}


	@Override
	public BigDecimal getQty(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine)  {

		SupplierCatalog supplierCatalog = this.getSupplierCatalog(purchaseOrder,purchaseOrderLine);

		if(supplierCatalog != null)  {

			return supplierCatalog.getMinQty();

		}

		return BigDecimal.ONE;

	}

	@Override
	public SupplierCatalog getSupplierCatalog(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine)  {

		Product product = purchaseOrderLine.getProduct();

		SupplierCatalog supplierCatalog = this.getSupplierCatalog(product, purchaseOrder.getSupplierPartner());

//		If there is no catalog for supplier, then we don't take the default catalog.

//		if(supplierCatalog == null)  {
//
//			supplierCatalog = this.getSupplierCatalog(product, product.getDefaultSupplierPartner());
//		}

		return supplierCatalog;

	}


	@Override
	public SupplierCatalog getSupplierCatalog(Product product, Partner supplierPartner)  {

		if(product.getSupplierCatalogList() != null)  {

			for(SupplierCatalog supplierCatalog : product.getSupplierCatalogList())  {

				if(supplierCatalog.getSupplierPartner().equals(supplierPartner))  {
					return supplierCatalog;
				}
			}
		}
		return null;

	}

	@Override
	public BigDecimal convertUnitPrice(Product product, TaxLine taxLine, BigDecimal price, PurchaseOrder purchaseOrder)  {
		
		if(taxLine == null)  {  return price;  }
		
		if(product.getInAti() && !purchaseOrder.getInAti()){
			price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
		}
		else if(!product.getInAti() && purchaseOrder.getInAti()){
			price = price.add(price.multiply(taxLine.getValue()));
		}
		return price;
	}
	

	public Map<String,Object> getDiscount(PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine, BigDecimal price)  {
		
		PriceList priceList = purchaseOrder.getPriceList();
		BigDecimal discountAmount = BigDecimal.ZERO;

		int computeMethodDiscountSelect = generalService.getGeneral().getComputeMethodDiscountSelect();
		
		Map<String, Object> discounts = null;
		
		if(priceList != null)  {
			int discountTypeSelect = 0;

			PriceListLine priceListLine = this.getPriceListLine(purchaseOrderLine, priceList);
			if(priceListLine != null)  {
				discountTypeSelect = priceListLine.getTypeSelect();
			}
			
			discounts = priceListService.getDiscounts(priceList, priceListLine, price);
			discountAmount = (BigDecimal) discounts.get("discountAmount");
			
			if((computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) 
					|| computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT)  {
				discounts.put("price", priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), discountAmount));
			}
		}

		if(discountAmount.compareTo(BigDecimal.ZERO) == 0)  {
			List<SupplierCatalog> supplierCatalogList = purchaseOrderLine.getProduct().getSupplierCatalogList();
			if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
				SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",purchaseOrderLine.getProduct(),purchaseOrderLine.getQty(),purchaseOrder.getSupplierPartner()).fetchOne();
				if(supplierCatalog != null)  {
					
					discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);

					if(computeMethodDiscountSelect != GeneralRepository.DISCOUNT_SEPARATE){
						discounts.put("price", priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount")));
						
					}
				}
			}
		}
		
		return discounts;
	}

	@Override
	public int getDiscountTypeSelect(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder){
		PriceList priceList = purchaseOrder.getPriceList();
		if(priceList != null)  {
			PriceListLine priceListLine = this.getPriceListLine(purchaseOrderLine, priceList);

			return priceListLine.getTypeSelect();
		}
		return 0;
	}
	
	public Unit getPurchaseUnit(PurchaseOrderLine purchaseOrderLine)  {
		Unit unit = purchaseOrderLine.getProduct().getPurchasesUnit();
		if(unit == null){
			unit = purchaseOrderLine.getProduct().getUnit();
		}
		return unit;
	}
	
	public boolean unitPriceShouldBeUpdate(PurchaseOrder purchaseOrder, Product product)  {
		
		if(product != null && product.getInAti() != purchaseOrder.getInAti())  {
			return true;
		}
		return false;
		
	}

}
