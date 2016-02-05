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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.MinStockRulesRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class LocationLineServiceImpl implements LocationLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocationLineServiceImpl.class); 
	
	@Inject
	protected LocationLineRepository locationLineRepo;
	
	@Inject
	protected MinStockRulesService minStockRulesService;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber) throws AxelorException  {
		
		this.updateLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		if(trackingNumber != null)  {
			this.updateDetailLocation(location, product, qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate) throws AxelorException  {
		
		LocationLine locationLine = this.getLocationLine(location, product);
		
		LOG.debug("Mise à jour du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate });
		
		if(!isIncrement)  {
			this.minStockRules(product, qty, locationLine, current, future);
		}
		
		locationLine = this.updateLocation(locationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		this.checkStockMin(locationLine, false);
		
		locationLineRepo.save(locationLine);
		
	}
	
	
	public void minStockRules(Product product, BigDecimal qty, LocationLine locationLine, boolean current, boolean future) throws AxelorException  {
		
		if(current)  {
			minStockRulesService.generatePurchaseOrder(product, qty, locationLine, MinStockRulesRepository.TYPE_CURRENT);			
		}
		if(future)  {
			minStockRulesService.generatePurchaseOrder(product, qty, locationLine, MinStockRulesRepository.TYPE_FUTURE);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateDetailLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber) throws AxelorException  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(location, product, trackingNumber);
		
		LOG.debug("Mise à jour du detail du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber});
		
		detailLocationLine = this.updateLocation(detailLocationLine, qty, current, future, isIncrement, lastFutureStockMoveDate);
		
		this.checkStockMin(detailLocationLine, true);
		
		locationLineRepo.save(detailLocationLine);
		
	}
	
	
	public void checkStockMin(LocationLine locationLine, boolean isDetailLocationLine) throws AxelorException  {
		if(!isDetailLocationLine && locationLine.getCurrentQty().compareTo(BigDecimal.ZERO) == -1 && locationLine.getLocation().getTypeSelect() == LocationRepository.TYPE_INTERNAL)  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LOCATION_LINE_1), 
					locationLine.getProduct().getName(), locationLine.getProduct().getCode()), IException.CONFIGURATION_ERROR);
		}
		else if(isDetailLocationLine && locationLine.getCurrentQty().compareTo(BigDecimal.ZERO) == -1 
				&& ((locationLine.getLocation() != null && locationLine.getLocation().getTypeSelect() == LocationRepository.TYPE_INTERNAL)
				    || (locationLine.getDetailsLocation() != null && locationLine.getDetailsLocation().getTypeSelect() == LocationRepository.TYPE_INTERNAL)))  {

			String trackingNumber = "";
			if(locationLine.getTrackingNumber() != null)  {
				trackingNumber = locationLine.getTrackingNumber().getTrackingNumberSeq();
			}
			
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LOCATION_LINE_2), 
					locationLine.getProduct().getName(), locationLine.getProduct().getCode(), trackingNumber), IException.CONFIGURATION_ERROR);
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
	 * @param trackingNumber
	 * 			Le numéro de suivi concerné
	 * @return
	 * 			Une ligne détaillée de stock
	 */
	public LocationLine getDetailLocationLine(Location detailLocation, Product product, TrackingNumber trackingNumber)  {
		
		LocationLine detailLocationLine = this.getDetailLocationLine(detailLocation.getDetailsLocationLineList(), product, trackingNumber);
		
		if(detailLocationLine == null)  {
			
			detailLocationLine = this.createDetailLocationLine(detailLocation, product, trackingNumber);
			
		}
		
		LOG.debug("Récupération ligne de détail de stock: Entrepot? {}, Produit? {}, Qté actuelle? {}, Qté future? {}, Date? {}, Variante? {}, Num de suivi? {} ", 
				new Object[] { detailLocationLine.getDetailsLocation().getName(), product.getCode(), 
				detailLocationLine.getCurrentQty(), detailLocationLine.getFutureQty(), detailLocationLine.getLastFutureStockMoveDate(), detailLocationLine.getTrackingNumber() });
		
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
	 * @param trackingNumber
	 * 		Un numéro de suivi
	 * @return
	 * 		Un ligne de stock
	 */
	public LocationLine getDetailLocationLine(List<LocationLine> detailLocationLineList, Product product, TrackingNumber trackingNumber)  {
		
		for(LocationLine detailLocationLine : detailLocationLineList)  {
			
			if(detailLocationLine.getProduct().equals(product) 
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
		location.addLocationLineListItem(locationLine);
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
	 * @param trackingNumber
	 * 		Un numéro de suivi
	 * @return
	 * 		La ligne détaillée de stock
	 */
	public LocationLine createDetailLocationLine(Location location, Product product, TrackingNumber trackingNumber)  {
		
		LOG.debug("Création d'une ligne de détail de stock : Entrepot? {}, Produit? {}, Num de suivi? {} ", 
				new Object[] { location.getName(), product.getCode(), trackingNumber.getTrackingNumberSeq() });
		
		LocationLine detailLocationLine = new LocationLine();
		
		detailLocationLine.setDetailsLocation(location);
		location.addDetailsLocationLineListItem(detailLocationLine);
		detailLocationLine.setProduct(product);
		detailLocationLine.setCurrentQty(BigDecimal.ZERO);
		detailLocationLine.setFutureQty(BigDecimal.ZERO);
		detailLocationLine.setTrackingNumber(trackingNumber);
		
		
		return detailLocationLine;
		
	}

		
}
