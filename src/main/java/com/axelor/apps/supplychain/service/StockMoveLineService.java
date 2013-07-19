package com.axelor.apps.supplychain.service;

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

		if(product.getApplicationTypeSelect() == IProduct.PRODUCT_TYPE) {
			
			StockMoveLine stockMoveLine = this.createStockMoveLine(product, quantity, unit, price, stockMove, null);
			
			TrackingNumberConfiguration trackingNumberConfiguration = product.getTrackingNumberConfiguration();
			if(trackingNumberConfiguration != null)  {
				
				switch (type) {
					case 1:
						if(trackingNumberConfiguration.getIsSaleTrackingManaged())  {
							if(trackingNumberConfiguration.getGenerateSaleAutoTrackingNbr())  {
								// Générer numéro de série si case cochée
								stockMoveLine.setTrackingNumber(
										trackingNumberService.getTrackingNumber(product, trackingNumberConfiguration.getSaleQtyByTracking(), stockMove.getCompany(), stockMove.getEstimatedDate()));
							}
						}
						else if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() || trackingNumberConfiguration.getIsProductionTrackingManaged())  {
							// Rechercher le numéro de suivi d'apèrs FIFO/LIFO
							this.assignTrackingNumber(stockMoveLine, product, stockMove.getFromLocation());
						}
						break;
					case 2:
						if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() && trackingNumberConfiguration.getGeneratePurchaseAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							stockMoveLine.setTrackingNumber(
									trackingNumberService.getTrackingNumber(product, trackingNumberConfiguration.getPurchaseQtyByTracking(), stockMove.getCompany(), stockMove.getEstimatedDate()));
						}
						break;
					case 3:
						if(trackingNumberConfiguration.getIsProductionTrackingManaged() && trackingNumberConfiguration.getGenerateProductionAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							stockMoveLine.setTrackingNumber(
									trackingNumberService.getTrackingNumber(product, trackingNumberConfiguration.getProductionQtyByTracking(), stockMove.getCompany(), stockMove.getEstimatedDate()));
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
	public StockMoveLine createStockMoveLine(Product product, BigDecimal quantity, Unit unit, BigDecimal price, StockMove stockMove, TrackingNumber trackingNumber ) throws AxelorException {

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
		
		
		for(LocationLine locationLine : this.getLocationLines(product, location))  {
			
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
	
	

	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList, LocalDate lastFutureStockMoveDate) throws AxelorException  {
		
		for(StockMoveLine stockMoveLine : stockMoveLineList)  {
			
			Unit productUnit = stockMoveLine.getProduct().getUnit();
			Unit stockMoveLineUnit = stockMoveLine.getUnit();
			
			BigDecimal qty = stockMoveLine.getQty();
			if(!productUnit.equals(stockMoveLineUnit))  {
				qty = new UnitConversionService().convert(stockMoveLineUnit, productUnit, qty);
			}
			
			this.updateLocations(fromLocation, toLocation, stockMoveLine.getProduct(), qty, fromStatus, toStatus, lastFutureStockMoveDate, stockMoveLine.getTrackingNumber());
			
		}
		
	}
	
	
	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate 
			lastFutureStockMoveDate, TrackingNumber trackingNumber)  {
		
		switch(fromStatus)  {
			case IStockMove.PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, true, null, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, false, true, false, null, trackingNumber);
				break;
				
			case IStockMove.REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, true, null, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, true, true, false, null, trackingNumber);
				break;
			
			default:
				break;
		}
		
		switch(toStatus)  {
			case IStockMove.PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, false, lastFutureStockMoveDate, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, false, true, true, lastFutureStockMoveDate, trackingNumber);
				break;
				
			case IStockMove.REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, false, null, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, true, true, true, null, trackingNumber);
				break;
			
			default:
				break;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
}
