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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderServiceSupplychainImpl extends PurchaseOrderServiceImpl {

	@Inject
	protected UnitConversionService unitConversionService;
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderServiceSupplychainImpl.class);

	public PurchaseOrder createPurchaseOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, Location location, LocalDate orderDate,
			PriceList priceList, Partner supplierPartner) throws AxelorException  {

		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Fournisseur = {}",
				new Object[] { company.getName(), externalReference, supplierPartner.getFullName() });

		PurchaseOrder purchaseOrder = super.createPurchaseOrder(buyerUser, company, contactPartner, currency, deliveryDate,
				internalReference, externalReference, orderDate, priceList, supplierPartner);

		purchaseOrder.setLocation(location);

		return purchaseOrder;
	}



	/**
	 * Méthode permettant de créer un StockMove à partir d'un PurchaseOrder.
	 * @param purchaseOrder une commande
	 * @throws AxelorException Aucune séquence de StockMove n'a été configurée
	 */
	public Long createStocksMove(PurchaseOrder purchaseOrder) throws AxelorException {

		Long stockMoveId = null;
		if(purchaseOrder.getPurchaseOrderLineList() != null && purchaseOrder.getCompany() != null) {
			StockConfigService stockConfigService = Beans.get(StockConfigService.class);
			Company company = purchaseOrder.getCompany();

			StockConfig stockConfig = stockConfigService.getStockConfig(company);

			Location startLocation = Beans.get(LocationRepository.class).findByPartner(purchaseOrder.getSupplierPartner());

			if(startLocation == null)  {
				startLocation = stockConfigService.getSupplierVirtualLocation(stockConfig);
			}
			if(startLocation == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PURCHASE_ORDER_1),
						company.getName()), IException.CONFIGURATION_ERROR);
			}

			Partner supplierPartner = purchaseOrder.getSupplierPartner();

			Address address = Beans.get(PartnerService.class).getDeliveryAddress(supplierPartner);

			StockMove stockMove = Beans.get(StockMoveService.class).createStockMove(address, null, company, supplierPartner, startLocation, purchaseOrder.getLocation(), purchaseOrder.getDeliveryDate(), purchaseOrder.getNotes());
			stockMove.setPurchaseOrder(purchaseOrder);
			stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
			stockMove.setEstimatedDate(purchaseOrder.getDeliveryDate());

			for(PurchaseOrderLine purchaseOrderLine: purchaseOrder.getPurchaseOrderLineList()) {

				Product product = purchaseOrderLine.getProduct();
				// Check if the company field 'hasInSmForStorableProduct' = true and productTypeSelect = 'storable' or 'hasInSmForNonStorableProduct' = true and productTypeSelect = 'service' or productTypeSelect = 'other'
				if(product != null
						&& ((stockConfig.getHasInSmForStorableProduct() && ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect()))
								|| (stockConfig.getHasInSmForNonStorableProduct() && !ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())))
						&& !ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(product.getProductTypeSelect())) {
					
					Unit unit = purchaseOrderLine.getProduct().getUnit();
					BigDecimal qty = purchaseOrderLine.getQty();
					BigDecimal priceDiscounted = purchaseOrderLine.getPriceDiscounted();
					if(!unit.equals(purchaseOrderLine.getUnit())){
						qty = unitConversionService.convertWithProduct(purchaseOrderLine.getUnit(), unit, qty, purchaseOrderLine.getProduct());
						priceDiscounted = unitConversionService.convertWithProduct(purchaseOrderLine.getUnit(), unit, priceDiscounted, purchaseOrderLine.getProduct());
					}
					
					StockMoveLine stockMoveLine = Beans.get(StockMoveLineService.class).createStockMoveLine(
							product, purchaseOrderLine.getProductName(), 
							purchaseOrderLine.getDescription(), qty, 
							priceDiscounted,unit, 
							stockMove, 2, purchaseOrder.getInAti(), purchaseOrderLine.getTaxLine().getValue());
					if(stockMoveLine != null) {

						stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);

						stockMove.getStockMoveLineList().add(stockMoveLine);
					}
				}
				else if(purchaseOrderLine.getIsTitleLine()){
					StockMoveLine stockMoveLine = Beans.get(StockMoveLineService.class).createStockMoveLine(
							product, purchaseOrderLine.getProductName(), 
							purchaseOrderLine.getDescription(), BigDecimal.ZERO, 
							BigDecimal.ZERO,null, 
							stockMove, 2, purchaseOrder.getInAti(), null);
					if(stockMoveLine != null) {

						stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);

						stockMove.getStockMoveLineList().add(stockMoveLine);
					}
				}
			}
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMove.setExTaxTotal(Beans.get(StockMoveService.class).compute(stockMove));
				Beans.get(StockMoveService.class).plan(stockMove);
			}
			stockMoveId = stockMove.getId();
		}
		return stockMoveId;
	}


	public Location getLocation(Company company)  {

		return Beans.get(LocationRepository.class).all().filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3", company, true, LocationRepository.TYPE_INTERNAL).fetchOne();
	}


	public void clearPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {

		List<StockMove> stockMoveList = Beans.get(StockMoveRepository.class).all().filter("self.purchaseOrder = ?1 AND self.statusSelect = 2", purchaseOrder).fetch();

		for(StockMove stockMove : stockMoveList)  {

			Beans.get(StockMoveService.class).cancel(stockMove);

		}


	}

	//Check if existing at least one stockMove not canceled for the purchaseOrder
	public boolean existActiveStockMoveForPurchaseOrder(Long purchaseOrderId){
		long nbStockMove = Beans.get(StockMoveRepository.class).all().filter("self.purchaseOrder.id = ? AND self.statusSelect <> ?", purchaseOrderId, StockMoveRepository.STATUS_CANCELED).count();
		return nbStockMove > 0;
	}
	
	@Transactional
	public void generateBudgetDistribution(PurchaseOrder purchaseOrder){
		if(purchaseOrder.getPurchaseOrderLineList() != null){
			for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
				if(purchaseOrderLine.getBudget() != null && purchaseOrderLine.getBudgetDistributionList().isEmpty()){
					BudgetDistribution budgetDistribution = new BudgetDistribution();
					budgetDistribution.setBudget(purchaseOrderLine.getBudget());
					budgetDistribution.setAmount(purchaseOrderLine.getExTaxTotal());
					purchaseOrderLine.addBudgetDistributionListItem(budgetDistribution);
				}
			}
			//purchaseOrderRepo.save(purchaseOrder);
		}
	}
}
