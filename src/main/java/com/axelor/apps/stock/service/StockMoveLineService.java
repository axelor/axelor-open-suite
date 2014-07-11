/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.TrackingNumberConfiguration;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.organisation.db.Project;
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
	private TrackingNumberService trackingNumberService;
	
	public static final int TYPE_SALES = 1;
	public static final int TYPE_PURCHASES = 2;
	public static final int TYPE_PRODUCTIONS = 3;
	
	
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
	public StockMoveLine createStockMoveLine(Product product, BigDecimal quantity, Unit unit, BigDecimal price, StockMove stockMove, int type ) throws AxelorException {

		if(product != null && product.getApplicationTypeSelect() == IProduct.APPLICATION_TYPE_PRODUCT) {

			StockMoveLine stockMoveLine = this.createStockMoveLine(product, quantity, unit, price, stockMove, null);
			
			TrackingNumberConfiguration trackingNumberConfiguration = product.getTrackingNumberConfiguration();
			if(trackingNumberConfiguration != null)  {

				switch (type) {
					case 1:
						if(trackingNumberConfiguration.getIsSaleTrackingManaged())  {
							if(trackingNumberConfiguration.getGenerateSaleAutoTrackingNbr())  {
								// Générer numéro de série si case cochée
								this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getSaleQtyByTracking());
				
							}
							else  {
								// Rechercher le numéro de suivi d'apèrs FIFO/LIFO
								this.assignTrackingNumber(stockMoveLine, product, stockMove.getFromLocation());
							}
						}
						break;
					case 2:
						if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() && trackingNumberConfiguration.getGeneratePurchaseAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getPurchaseQtyByTracking());
							
						}
						break;
					case 3:
						if(trackingNumberConfiguration.getIsProductionTrackingManaged() && trackingNumberConfiguration.getGenerateProductionAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getProductionQtyByTracking());

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
	
	
	public void generateTrackingNumber(StockMoveLine stockMoveLine, TrackingNumberConfiguration trackingNumberConfiguration, Product product, BigDecimal qtyByTracking) throws AxelorException  {
		
		StockMove stockMove = stockMoveLine.getStockMove();
		
		while(stockMoveLine.getQty().compareTo(trackingNumberConfiguration.getSaleQtyByTracking()) == 1)  {
			
			BigDecimal minQty = stockMoveLine.getQty().min(qtyByTracking);
			
			this.splitStockMoveLine(stockMoveLine, minQty, trackingNumberService.getTrackingNumber(product, qtyByTracking, stockMove.getCompany(), stockMove.getEstimatedDate()));
			
		}
		if(stockMoveLine.getTrackingNumber() == null)  {
			
			stockMoveLine.setTrackingNumber(trackingNumberService.getTrackingNumber(product, qtyByTracking, stockMove.getCompany(), stockMove.getEstimatedDate()));
			
		}
		
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
	public StockMoveLine createStockMoveLine(Product product, BigDecimal quantity, Unit unit, BigDecimal price, StockMove stockMove, TrackingNumber trackingNumber) throws AxelorException {

		StockMoveLine stockMoveLine = new StockMoveLine();
		stockMoveLine.setStockMove(stockMove);
		stockMoveLine.setProduct(product);
		stockMoveLine.setQty(quantity);
		stockMoveLine.setUnit(unit);
		stockMoveLine.setPrice(price);
		stockMoveLine.setTrackingNumber(trackingNumber);
		
		return stockMoveLine;
	}
	
	
	
	public void assignTrackingNumber(StockMoveLine stockMoveLine, Product product, Location location) throws AxelorException  {
		
		List<LocationLine> locationLineList = this.getLocationLines(product, location);
		
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
	
	
	
	public List<LocationLine> getLocationLines(Product product, Location location) throws AxelorException  {
		
		List<LocationLine> locationLineList = LocationLine.all()
				.filter("self.product = ?1 AND self.futureQty > 0 AND self.trackingNumber IS NOT NULL AND self.detailsLocation = ?2"
						+trackingNumberService.getOrderMethod(product.getTrackingNumberConfiguration()), product, location).fetch();
		
		return locationLineList;
		
	}

	
	
	public StockMoveLine splitStockMoveLine(StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber) throws AxelorException  {
		
		StockMoveLine newStockMoveLine = this.createStockMoveLine(
				stockMoveLine.getProduct(), 
				qty, 
				stockMoveLine.getUnit(), 
				stockMoveLine.getPrice(), 
				stockMoveLine.getStockMove(), 
				trackingNumber);
		
		stockMoveLine.getStockMove().getStockMoveLineList().add(newStockMoveLine);
		
		stockMoveLine.setQty(stockMoveLine.getQty().subtract(qty));
	
		return newStockMoveLine;
	}
	
	

	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList, 
			LocalDate lastFutureStockMoveDate, Project businessProject, boolean realQty) throws AxelorException  {
		
		for(StockMoveLine stockMoveLine : stockMoveLineList)  {
			
			Unit productUnit = stockMoveLine.getProduct().getUnit();
			Unit stockMoveLineUnit = stockMoveLine.getUnit();
			
			BigDecimal qty = null;
			if(realQty)  {
				qty = stockMoveLine.getRealQty();
			}
			else  {
				qty = stockMoveLine.getQty();
			}
			
			if(!productUnit.equals(stockMoveLineUnit))  {
				qty = new UnitConversionService().convert(stockMoveLineUnit, productUnit, qty);
			}
			
			this.updateLocations(fromLocation, toLocation, stockMoveLine.getProduct(), qty, fromStatus, toStatus, 
					lastFutureStockMoveDate, stockMoveLine.getTrackingNumber(), businessProject);
			
		}
		
	}
	
	
	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate 
			lastFutureStockMoveDate, TrackingNumber trackingNumber, Project businessProject) throws AxelorException  {
		
		switch(fromStatus)  {
			case IStockMove.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, true, null, trackingNumber, businessProject);
				locationLineService.updateLocation(toLocation, product, qty, false, true, false, null, trackingNumber, businessProject);
				break;
				
			case IStockMove.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, true, null, trackingNumber, businessProject);
				locationLineService.updateLocation(toLocation, product, qty, true, true, false, null, trackingNumber, businessProject);
				break;
			
			default:
				break;
		}
		
		switch(toStatus)  {
			case IStockMove.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, false, lastFutureStockMoveDate, trackingNumber, businessProject);
				locationLineService.updateLocation(toLocation, product, qty, false, true, true, lastFutureStockMoveDate, trackingNumber, businessProject);
				break;
				
			case IStockMove.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, false, null, trackingNumber, businessProject);
				locationLineService.updateLocation(toLocation, product, qty, true, true, true, null, trackingNumber, businessProject);
				break;
			
			default:
				break;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
}
