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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SaleOrderServiceStockImpl extends SaleOrderServiceImpl {

	@Inject
	private StockMoveService stockMoveService;

	@Inject
	private StockMoveLineService stockMoveLineService;

	@Inject
	private StockConfigService stockConfigService;

	@Inject
	private LocationRepository locationRepo;
	
	@Inject
	protected StockMoveRepository stockMoveRepo;
	
	@Inject
	protected UnitConversionService unitConversionService;


	public Location getLocation(Company company)  {

		return locationRepo.all().filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3",
				company, true, LocationRepository.TYPE_INTERNAL).fetchOne();
	}


	/**
	 * Méthode permettant de créer un StockMove à partir d'un SaleOrder.
	 * @param saleOrder l'objet saleOrder
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public Long createStocksMovesFromSaleOrder(SaleOrder saleOrder) throws AxelorException {

		Company company = saleOrder.getCompany();

		if(saleOrder.getSaleOrderLineList() != null && company != null) {

			StockMove stockMove = this.createStockMove(saleOrder, company);

			for(SaleOrderLine saleOrderLine: saleOrder.getSaleOrderLineList()) {
				if(saleOrderLine.getProduct() != null){
					this.createStockMoveLine(stockMove, saleOrderLine, company);
				}
			}

			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMove.setExTaxTotal(stockMoveService.compute(stockMove));
				stockMoveService.plan(stockMove);
			}

			return stockMove.getId();
		}
		return null;
	}


	public StockMove createStockMove(SaleOrder saleOrder, Company company) throws AxelorException  {

		Location toLocation = locationRepo.all().filter("self.isDefaultLocation = true and self.company = ?1 and self.typeSelect = ?2", company, LocationRepository.TYPE_EXTERNAL).fetchOne();

		if(toLocation == null)  {

			toLocation = stockConfigService.getCustomerVirtualLocation(stockConfigService.getStockConfig(company));
		}

		StockMove stockMove = stockMoveService.createStockMove(
				null,
				saleOrder.getDeliveryAddress(),
				company,
				saleOrder.getClientPartner(),
				saleOrder.getLocation(),
				toLocation,
				saleOrder.getShipmentDate(),
				saleOrder.getDescription());

		stockMove.setSaleOrder(saleOrder);
		stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
		return stockMove;
	}


	public StockMoveLine createStockMoveLine(StockMove stockMove, SaleOrderLine saleOrderLine, Company company) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		if(product != null && this.isStockMoveProduct(saleOrderLine)
				&& !ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(product.getProductTypeSelect())) {
			
			Unit unit = saleOrderLine.getProduct().getUnit();
			BigDecimal qty = saleOrderLine.getQty();
			BigDecimal priceDiscounted = saleOrderLine.getPriceDiscounted();
			if(!unit.equals(saleOrderLine.getUnit())){
				qty = unitConversionService.convertWithProduct(saleOrderLine.getUnit(), unit, qty, saleOrderLine.getProduct());
				priceDiscounted = unitConversionService.convertWithProduct(saleOrderLine.getUnit(), unit, priceDiscounted, saleOrderLine.getProduct());
			}
			
			StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(
					product,
					saleOrderLine.getProductName(),
					saleOrderLine.getDescription(),
					qty,
					priceDiscounted,
					unit,
					stockMove,
					1, saleOrderLine.getSaleOrder().getInAti(), saleOrderLine.getTaxLine().getValue());

			stockMoveLine.setSaleOrderLine(saleOrderLine);

			if(stockMoveLine != null) {
				stockMove.addStockMoveLineListItem(stockMoveLine);
			}
			return stockMoveLine;
		}
		return null;
	}



	public boolean isStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException  {

		Company company = saleOrderLine.getSaleOrder().getCompany();

		StockConfig stockConfig = stockConfigService.getStockConfig(company);

		Product product = saleOrderLine.getProduct();

		if(product != null
				&& ((ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect()) && stockConfig.getHasOutSmForNonStorableProduct())
						|| (ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect()) && stockConfig.getHasOutSmForStorableProduct())) )  {

			return true;
		}

		return false;
	}

	//Check if existing at least one stockMove not canceled for the saleOrder
	public boolean existActiveStockMoveForSaleOrder(Long saleOrderId){
		long nbStockMove = stockMoveRepo.all().filter("self.saleOrder.id = ? AND self.statusSelect <> ?", saleOrderId, StockMoveRepository.STATUS_CANCELED).count();
		return nbStockMove > 0;
	}
}



