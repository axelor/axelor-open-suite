package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;


public class LocationLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocationLineService.class); 
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber)  {
		
		this.updateLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		if(trackingNumber != null)  {
			this.updateDetailLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, LocalDate lastFutureStockMoveDate)  {
		
		LocationLine locationLine = this.getLocationLine(location, product);
		
		LOG.debug("Mise à jour du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate });
		
		locationLine = this.updateLocation(locationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		locationLine.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateDetailLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber)  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(location, product, trackingNumber);
		
		LOG.debug("Mise à jour du detail du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber});
		
		detailLocationLine = this.updateLocation(detailLocationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		detailLocationLine.save();
		
	}
	
	
	public LocationLine updateLocation(LocationLine locationLine, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate)  {
		
		if(current)  {
			if(isIncrement)  {
				locationLine.setCurrentQty(locationLine.getCurrentQty().add(qty));
			}
			else  {
				locationLine.setCurrentQty(locationLine.getCurrentQty().subtract(qty));
			}
			
		}
		if(future)  {
			if(isIncrement)  {
				locationLine.setFutureQty(locationLine.getFutureQty().add(qty));
			}
			else  {
				locationLine.setFutureQty(locationLine.getFutureQty().subtract(qty));
			}
			locationLine.setLastFutureStockMoveDate(lastFutureStockMoveDate);
		}
		
		return locationLine;
	}
	
	
	public LocationLine getLocationLine(Location location, Product product)  {
		
		LocationLine locationLine = this.getLocationLine(location.getLocationLineList(), product);
		
		if(locationLine == null)  {
			locationLine = this.createLocationLine(location, product);
		}
		
		LOG.debug("Récupération ligne de stock: Entrepot? {}, Produit? {}, Qté actuelle? {}, Qté future? {}, Date? {} ", 
				new Object[] { locationLine.getLocation().getName(), product.getCode(), 
				locationLine.getCurrentQty(), locationLine.getFutureQty(), locationLine.getLastFutureStockMoveDate() });
		
		return locationLine;
	}
	
	
	public LocationLine getDetailLocationLine(Location detailLocation, Product product, TrackingNumber trackingNumber)  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(detailLocation.getDetailsLocationLineList(), product, trackingNumber);
		
		if(detailLocationLine == null)  {
			detailLocationLine = this.createDetailLocationLine(detailLocation, product, trackingNumber);
		}
		
		LOG.debug("Récupération ligne de détail de stock: Entrepot? {}, Produit? {}, Qté actuelle? {}, Qté future? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { detailLocationLine.getDetailsLocation().getName(), product.getCode(), 
				detailLocationLine.getCurrentQty(), detailLocationLine.getFutureQty(), detailLocationLine.getLastFutureStockMoveDate(), detailLocationLine.getTrackingNumber() });
		
		return detailLocationLine;
	}
	
	
	public LocationLine getLocationLine(List<LocationLine> locationLineList, Product product)  {
		
		for(LocationLine locationLine : locationLineList)  {
			
			if(locationLine.getProduct().equals(product))  {
				return locationLine;
			}
			
		}
		
		return null;
	}
	
	
	public LocationLine getDetailLocationLine(List<LocationLine> detailLocationLineList, Product product, TrackingNumber trackingNumber)  {
		
		for(LocationLine detailLocationLine : detailLocationLineList)  {
			
			if(detailLocationLine.getProduct().equals(product) && detailLocationLine.getTrackingNumber().equals(trackingNumber))  {
				return detailLocationLine;
			}
			
		}
		
		return null;
	}
	
	
	public LocationLine createLocationLine(Location location, Product product)  {
		
		LOG.debug("Création d'une ligne de stock : Entrepot? {}, Produit? {} ", new Object[] { location.getName(), product.getCode() });
		
		LocationLine locationLine = new LocationLine();
		
		locationLine.setLocation(location);
		locationLine.setProduct(product);
		locationLine.setCurrentQty(BigDecimal.ZERO);
		locationLine.setFutureQty(BigDecimal.ZERO);
		
		return locationLine;
		
	}
	
	
	public LocationLine createDetailLocationLine(Location location, Product product, TrackingNumber trackingNumber)  {
		
		LOG.debug("Création d'une ligne de détail de stock : Entrepot? {}, Produit? {} ", new Object[] { location.getName(), product.getCode() });
		
		LocationLine detailLocationLine = new LocationLine();
		
		detailLocationLine.setDetailsLocation(location);
		detailLocationLine.setProduct(product);
		detailLocationLine.setCurrentQty(BigDecimal.ZERO);
		detailLocationLine.setFutureQty(BigDecimal.ZERO);
		detailLocationLine.setTrackingNumber(trackingNumber);
		
		return detailLocationLine;
		
	}
		
}
