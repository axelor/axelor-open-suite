/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class LocationLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocationLineService.class); 
	
	@Inject
	private ProductVariantService productVariantService;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, ProductVariant productVariant)  {
		
		this.updateLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		if(trackingNumber != null || productVariant != null)  {
			this.updateDetailLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, productVariant, trackingNumber);
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
			LocalDate lastFutureStockMoveDate, ProductVariant productVariant, TrackingNumber trackingNumber)  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(location, product, productVariant, trackingNumber);
		
		LOG.debug("Mise à jour du detail du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Variante? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate, productVariant, trackingNumber});
		
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
	
	
	public LocationLine getDetailLocationLine(Location detailLocation, Product product, ProductVariant productVariant, TrackingNumber trackingNumber)  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(detailLocation.getDetailsLocationLineList(), product, productVariant, trackingNumber);
		
		if(detailLocationLine == null)  {
			
			ProductVariant stockProductVariant = productVariant;
			if(productVariant != null && !productVariant.getUsedforStock())  {
				stockProductVariant = productVariantService.getStockProductVariant(productVariant);
			}
			detailLocationLine = this.createDetailLocationLine(detailLocation, product, stockProductVariant, trackingNumber);
		}
		
		LOG.debug("Récupération ligne de détail de stock: Entrepot? {}, Produit? {}, Qté actuelle? {}, Qté future? {}, Date? {}, Variante? {}, Num de suivi? {} ", 
				new Object[] { detailLocationLine.getDetailsLocation().getName(), product.getCode(), 
				detailLocationLine.getCurrentQty(), detailLocationLine.getFutureQty(), detailLocationLine.getLastFutureStockMoveDate(), 
				detailLocationLine.getProductVariant(), detailLocationLine.getTrackingNumber() });
		
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
	
	
	public LocationLine getDetailLocationLine(List<LocationLine> detailLocationLineList, Product product, ProductVariant productVariant, TrackingNumber trackingNumber)  {
		
		for(LocationLine detailLocationLine : detailLocationLineList)  {
			
			if(detailLocationLine.getProduct().equals(product) 
					&& productVariantService.equals(detailLocationLine.getProductVariant(),productVariant)
					&& detailLocationLine.getTrackingNumber().equals(trackingNumber))  {
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
	
	
	public LocationLine createDetailLocationLine(Location location, Product product, ProductVariant productVariant, TrackingNumber trackingNumber)  {
		
		LOG.debug("Création d'une ligne de détail de stock : Entrepot? {}, Produit? {}, Variante? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), productVariant, trackingNumber.getTrackingNumberSeq() });
		
		LocationLine detailLocationLine = new LocationLine();
		
		detailLocationLine.setDetailsLocation(location);
		detailLocationLine.setProduct(product);
		detailLocationLine.setCurrentQty(BigDecimal.ZERO);
		detailLocationLine.setFutureQty(BigDecimal.ZERO);
		detailLocationLine.setProductVariant(productVariant);
		detailLocationLine.setTrackingNumber(trackingNumber);
		
		
		return detailLocationLine;
		
	}
		
}
