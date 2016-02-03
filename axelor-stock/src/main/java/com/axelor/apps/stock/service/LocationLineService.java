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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.db.mapper.types.DecimalAdapter;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;


public interface LocationLineService {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber) throws AxelorException;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate) throws AxelorException;
	
	
	public void minStockRules(Product product, BigDecimal qty, LocationLine locationLine, boolean current, boolean future) throws AxelorException;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateDetailLocation(Location location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber) throws AxelorException;
	
	
	public void checkStockMin(LocationLine locationLine, boolean isDetailLocationLine) throws AxelorException;
	
	
	
	public LocationLine updateLocation(LocationLine locationLine, BigDecimal qty, boolean current, boolean future, boolean isIncrement, 
			LocalDate lastFutureStockMoveDate);
	
	public LocationLine getLocationLine(Location location, Product product);
	
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
	public LocationLine getDetailLocationLine(Location detailLocation, Product product, TrackingNumber trackingNumber);
	
	
	/**
	 * Permet de récupérer la ligne de stock d'un entrepot en fonction d'un produit donné.
	 * @param locationLineList
	 * 		Une liste de ligne de stock
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public LocationLine getLocationLine(List<LocationLine> locationLineList, Product product);
	
	
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
	public LocationLine getDetailLocationLine(List<LocationLine> detailLocationLineList, Product product, TrackingNumber trackingNumber);
	
	
	
	/**
	 * Permet de créer une ligne de stock pour un entrepot et un produit donnés.
	 * @param location
	 * 		Un entrepot
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public LocationLine createLocationLine(Location location, Product product);
	
	
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
	public LocationLine createDetailLocationLine(Location location, Product product, TrackingNumber trackingNumber);
		
}
