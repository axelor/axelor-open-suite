/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.TrackingNumberConfiguration;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class StockMoveLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StockMoveLineService.class); 

	@Inject 
	private LocationLineService locationLineService;
	
	@Inject
	private ProductVariantService productVariantService;
	
	@Inject
	private TrackingNumberService trackingNumberService;
	
	
	
	/**
	 * Méthode générique permettant de créer une ligne de mouvement de stock en gérant les numéros de suivi en fonction du type d'opération.
	 * @param product le produit
	 * @param quantity la quantité
	 * @param parent le StockMove parent
	 * @param type
	 * 1 : Sales
	 * 2 : Purchases
	 * 3 : Productions
	 * 
	 * @return l'objet StockMoveLine
	 * @throws AxelorException 
	 */
	public StockMoveLine createStockMoveLine(Product product, BigDecimal quantity, Unit unit, BigDecimal price, StockMove stockMove, ProductVariant productVariant, int type ) throws AxelorException {

		if(product != null && product.getApplicationTypeSelect() == IProduct.APPLICATION_TYPE_PRODUCT) {

			ProductVariant stockProductVariant = productVariant;
			if(productVariant != null && !productVariant.getUsedForStock())  {
				stockProductVariant = productVariantService.getStockProductVariant(productVariant);
			}
			
			StockMoveLine stockMoveLine = this.createStockMoveLine(product, quantity, unit, price, stockMove, stockProductVariant, null);
			
			TrackingNumberConfiguration trackingNumberConfiguration = product.getTrackingNumberConfiguration();
			if(trackingNumberConfiguration != null)  {

				switch (type) {
					case 1:
						if(trackingNumberConfiguration.getIsSaleTrackingManaged())  {
							if(trackingNumberConfiguration.getGenerateSaleAutoTrackingNbr())  {
								// Générer numéro de série si case cochée
								stockMoveLine.setTrackingNumber(
										trackingNumberService.getTrackingNumber(product, stockProductVariant, trackingNumberConfiguration.getSaleQtyByTracking(), stockMove.getCompany(), stockMove.getEstimatedDate()));
							}
						}
						else if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() || trackingNumberConfiguration.getIsProductionTrackingManaged())  {
							// Rechercher le numéro de suivi d'apèrs FIFO/LIFO
							this.assignTrackingNumber(stockMoveLine, product, stockMove.getFromLocation(), stockProductVariant);
						}
						break;
					case 2:
						if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() && trackingNumberConfiguration.getGeneratePurchaseAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							stockMoveLine.setTrackingNumber(
									trackingNumberService.getTrackingNumber(product, stockProductVariant, trackingNumberConfiguration.getPurchaseQtyByTracking(), stockMove.getCompany(), stockMove.getEstimatedDate()));
						}
						break;
					case 3:
						if(trackingNumberConfiguration.getIsProductionTrackingManaged() && trackingNumberConfiguration.getGenerateProductionAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							stockMoveLine.setTrackingNumber(
									trackingNumberService.getTrackingNumber(product, stockProductVariant, trackingNumberConfiguration.getProductionQtyByTracking(), stockMove.getCompany(), stockMove.getEstimatedDate()));
						}
						break;
	
					default:
						break;
				}
			}
			
			return stockMoveLine;
		}
		return null;
	}
	
	
	/**
	 * Méthode générique permettant de créer une ligne de mouvement de stock
	 * @param product
	 * @param quantity
	 * @param unit
	 * @param price
	 * @param stockMove
	 * @param trackingNumber
	 * @return
	 * @throws AxelorException
	 */
	public StockMoveLine createStockMoveLine(Product product, BigDecimal quantity, Unit unit, BigDecimal price, StockMove stockMove, 
			ProductVariant productVariant, TrackingNumber trackingNumber) throws AxelorException {

		StockMoveLine stockMoveLine = new StockMoveLine();
		stockMoveLine.setStockMove(stockMove);
		stockMoveLine.setProduct(product);
		stockMoveLine.setQty(quantity);
		stockMoveLine.setUnit(unit);
		stockMoveLine.setPrice(price);
		stockMoveLine.setProductVariant(productVariant);
		stockMoveLine.setTrackingNumber(trackingNumber);
		
		return stockMoveLine;
	}
	
	
	
	public void assignTrackingNumber(StockMoveLine stockMoveLine, Product product, Location location, ProductVariant productVariant) throws AxelorException  {
		
		List<LocationLine> locationLineList = this.getLocationLines(product, location, productVariant);
		
		if(locationLineList != null)  {
			for(LocationLine locationLine : locationLineList)  {
				
				BigDecimal qty = locationLine.getFutureQty();
				if(stockMoveLine.getQty().compareTo(qty) == 1)  {
					this.splitStockMoveLine(stockMoveLine, qty, locationLine.getTrackingNumber());
				}
				else  {
					stockMoveLine.setTrackingNumber(locationLine.getTrackingNumber());
					break;
				}
				
			}
		}
	}
	
	
	
	public List<LocationLine> getLocationLines(Product product, Location location, ProductVariant productVariant) throws AxelorException  {
		
		List<LocationLine> locationLineList = LocationLine.all()
				.filter("self.product = ?1 AND self.futureQty > 0 AND self.trackingNumber IS NOT NULL AND self.detailsLocation = ?2 AND self.productVariant = ?3"
						+trackingNumberService.getOrderMethod(product.getTrackingNumberConfiguration()), product, location, productVariant).fetch();
		
		return locationLineList;
		
	}

	
	
	public StockMoveLine splitStockMoveLine(StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber) throws AxelorException  {
		
		StockMoveLine newStockMoveLine = this.createStockMoveLine(
				stockMoveLine.getProduct(), 
				qty, 
				stockMoveLine.getUnit(), 
				stockMoveLine.getPrice(), 
				stockMoveLine.getStockMove(), 
				stockMoveLine.getProductVariant(),
				trackingNumber);
		
		stockMoveLine.getStockMove().getStockMoveLineList().add(newStockMoveLine);
		
		stockMoveLine.setQty(stockMoveLine.getQty().subtract(qty));
	
		return newStockMoveLine;
	}
	
	

	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList, LocalDate lastFutureStockMoveDate) throws AxelorException  {
		
		for(StockMoveLine stockMoveLine : stockMoveLineList)  {
			
			Unit productUnit = stockMoveLine.getProduct().getUnit();
			Unit stockMoveLineUnit = stockMoveLine.getUnit();
			
			BigDecimal qty = stockMoveLine.getQty();
			if(!productUnit.equals(stockMoveLineUnit))  {
				qty = new UnitConversionService().convert(stockMoveLineUnit, productUnit, qty);
			}
			
			this.updateLocations(fromLocation, toLocation, stockMoveLine.getProduct(), qty, fromStatus, toStatus, lastFutureStockMoveDate, stockMoveLine.getProductVariant(), stockMoveLine.getTrackingNumber());
			
		}
		
	}
	
	
	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate 
			lastFutureStockMoveDate, ProductVariant productVariant, TrackingNumber trackingNumber ) throws AxelorException  {
		
		switch(fromStatus)  {
			case IStockMove.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, true, null, trackingNumber, productVariant);
				locationLineService.updateLocation(toLocation, product, qty, false, true, false, null, trackingNumber, productVariant);
				break;
				
			case IStockMove.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, true, null, trackingNumber, productVariant);
				locationLineService.updateLocation(toLocation, product, qty, true, true, false, null, trackingNumber, productVariant);
				break;
			
			default:
				break;
		}
		
		switch(toStatus)  {
			case IStockMove.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, false, lastFutureStockMoveDate, trackingNumber, productVariant);
				locationLineService.updateLocation(toLocation, product, qty, false, true, true, lastFutureStockMoveDate, trackingNumber, productVariant);
				break;
				
			case IStockMove.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, false, null, trackingNumber, productVariant);
				locationLineService.updateLocation(toLocation, product, qty, true, true, true, null, trackingNumber, productVariant);
				break;
			
			default:
				break;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
}
