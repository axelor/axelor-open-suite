/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.IMinStockRules;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.LocationLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class LocationLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocationLineService.class); 
	
	
	@Inject
	private ProductVariantService productVariantService;
	
	@Inject
	private MinStockRulesService minStockRulesService;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, ProductVariant productVariant, Project businessProject) throws AxelorException  {
		
		this.updateLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, businessProject);
		
		if(trackingNumber != null || productVariant != null)  {
			this.updateDetailLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, productVariant, trackingNumber);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, Project businessProject) throws AxelorException  {
		
		LocationLine locationLine = this.getLocationLine(location, product);
		
		LOG.debug("Mise à jour du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate });
		
		if(!isIncrement)  {
			this.minStockRules(product, qty, locationLine, businessProject, current, future);
		}
		
		locationLine = this.updateLocation(locationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		this.checkStockMin(locationLine, false);
		
		locationLine.save();
		
	}
	
	
	public void minStockRules(Product product, BigDecimal qty, LocationLine locationLine, Project businessProject, boolean current, boolean future) throws AxelorException  {
		
		if(current)  {
			minStockRulesService.generatePurchaseOrder(product, qty, locationLine, businessProject, IMinStockRules.TYPE_CURRENT);			
		}
		if(future)  {
			minStockRulesService.generatePurchaseOrder(product, qty, locationLine, businessProject, IMinStockRules.TYPE_FUTURE);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateDetailLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, ProductVariant productVariant, TrackingNumber trackingNumber) throws AxelorException  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(location, product, productVariant, trackingNumber);
		
		LOG.debug("Mise à jour du detail du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Variante? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate, productVariant, trackingNumber});
		
		detailLocationLine = this.updateLocation(detailLocationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		this.checkStockMin(detailLocationLine, true);
		
		detailLocationLine.save();
		
	}
	
	
	public void checkStockMin(LocationLine locationLine, boolean isDetailLocationLine) throws AxelorException  {
		if(!isDetailLocationLine && locationLine.getCurrentQty().compareTo(BigDecimal.ZERO) == -1 && locationLine.getLocation().getTypeSelect() == ILocation.INTERNAL)  {
			throw new AxelorException(String.format("Les stocks du produit %s (%s) sont insuffisants pour réaliser la livraison", 
					locationLine.getProduct().getName(), locationLine.getProduct().getCode()), IException.CONFIGURATION_ERROR);
		}
		else if(isDetailLocationLine && locationLine.getCurrentQty().compareTo(BigDecimal.ZERO) == -1 && locationLine.getLocation().getTypeSelect() == ILocation.INTERNAL)  {
			String variantName = "";
			if(locationLine.getProductVariant() != null)  {
				variantName = locationLine.getProductVariant().getName();
			}
			
			String trackingNumber = "";
			if(locationLine.getTrackingNumber() != null)  {
				trackingNumber = locationLine.getTrackingNumber().getTrackingNumberSeq();
			}
			
			throw new AxelorException(String.format("Les stocks du produit %s (%s), variante {}, numéro de suivi {}  sont insuffisants pour réaliser la livraison", 
					locationLine.getProduct().getName(), locationLine.getProduct().getCode(), variantName, trackingNumber), IException.CONFIGURATION_ERROR);
		}
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
	
	
	
	/**
	 * Récupération de la ligne détaillée de stock :
	 * On vérifie si l'entrepot contient une ligne détaillée de stock pour un produit, une vairante de produit et un numéro de suivi donnés.
	 * Si l'entrepot ne contient pas de ligne détaillée de stock pour ces paramètres, alors on vérifie que
	 * et on créé une ligne détaillée de stock. 
	 * 
	 * @param detailLocation
	 * 			Entrepot détaillé
	 * @param product
	 * 			Produit concerné
	 * @param productVariant
	 * 			La variante de produit concernée
	 * @param trackingNumber
	 * 			Le numéro de suivi concerné
	 * @return
	 * 			Une ligne détaillée de stock
	 */
	public LocationLine getDetailLocationLine(Location detailLocation, Product product, ProductVariant productVariant, TrackingNumber trackingNumber)  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(detailLocation.getDetailsLocationLineList(), product, productVariant, trackingNumber);
		
		if(detailLocationLine == null)  {
			
			ProductVariant stockProductVariant = productVariant;
			if(productVariant != null && !productVariant.getUsedForStock())  {
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
	
	
	/**
	 * Permet de récupérer la ligne de stock d'un entrepot en fonction d'un produit donné.
	 * @param locationLineList
	 * 		Une liste de ligne de stock
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public LocationLine getLocationLine(List<LocationLine> locationLineList, Product product)  {
		
		for(LocationLine locationLine : locationLineList)  {
			
			if(locationLine.getProduct().equals(product))  {
				return locationLine;
			}
			
		}
		
		return null;
	}
	
	
	/**
	 * Permet de récupérer la ligne détaillée de stock d'un entrepot en fonction d'un produit, d'une variante de produit et d'un numéro de suivi donnés.
	 * @param detailLocationLineList
	 * 		Une liste de ligne détaillée de stock
	 * @param product
	 * 		Un produit
	 * @param productVariant
	 * 		Une variante de produit
	 * @param trackingNumber
	 * 		Un numéro de suivi
	 * @return
	 * 		Un ligne de stock
	 */
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
	
	
	
	/**
	 * Permet de créer une ligne de stock pour un entrepot et un produit donnés.
	 * @param location
	 * 		Un entrepot
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public LocationLine createLocationLine(Location location, Product product)  {
		
		LOG.debug("Création d'une ligne de stock : Entrepot? {}, Produit? {} ", new Object[] { location.getName(), product.getCode() });
		
		LocationLine locationLine = new LocationLine();
		
		locationLine.setLocation(location);
		locationLine.setProduct(product);
		locationLine.setCurrentQty(BigDecimal.ZERO);
		locationLine.setFutureQty(BigDecimal.ZERO);
		
		return locationLine;
		
	}
	
	
	/**
	 * Permet de créer une ligne détaillée de stock pour un entrepot, un produit, une variante de produit et un numéro de suivi donnés.
	 * @param location
	 * 		Un entrepot
	 * @param product
	 * 		Un produit
	 * @param productVariant
	 * 		Une variante de produit
	 * @param trackingNumber
	 * 		Un numéro de suivi
	 * @return
	 * 		La ligne détaillée de stock
	 */
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
