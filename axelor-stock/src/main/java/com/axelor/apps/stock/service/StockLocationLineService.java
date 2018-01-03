/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface StockLocationLineService {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(StockLocation location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
                               LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, BigDecimal reservedQty) throws AxelorException;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(StockLocation location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
							   LocalDate lastFutureStockMoveDate, BigDecimal reservedQty) throws AxelorException;
	
	
	public void minStockRules(Product product, BigDecimal qty, StockLocationLine stockLocationLine, boolean current, boolean future) throws AxelorException;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateDetailLocation(StockLocation location, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
                                     LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, BigDecimal reservedQty) throws AxelorException;
	
	
	public void checkStockMin(StockLocationLine stockLocationLine, boolean isDetailLocationLine) throws AxelorException;

	/**
	 * Check if the location has more than qty units of the product
	 * @param location
	 * @param product
	 * @param qty
	 * @throws AxelorException if there is not enough qty in stock
	 */
	public void checkIfEnoughStock(StockLocation location, Product product, BigDecimal qty) throws AxelorException;

	public StockLocationLine updateLocation(StockLocationLine stockLocationLine, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
									   LocalDate lastFutureStockMoveDate, BigDecimal reservedQty);
	
	public StockLocationLine getStockLocationLine(StockLocation location, Product product);
	
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
	public StockLocationLine getDetailLocationLine(StockLocation detailLocation, Product product, TrackingNumber trackingNumber);
	
	
	/**
	 * Permet de récupérer la ligne de stock d'un entrepot en fonction d'un produit donné.
	 * @param stockLocationLineList
	 * 		Une liste de ligne de stock
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public StockLocationLine getLocationLine(List<StockLocationLine> stockLocationLineList, Product product);
	
	
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
	public StockLocationLine getDetailLocationLine(List<StockLocationLine> detailLocationLineList, Product product, TrackingNumber trackingNumber);
	
	
	
	/**
	 * Permet de créer une ligne de stock pour un entrepot et un produit donnés.
	 * @param location
	 * 		Un entrepot
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public StockLocationLine createLocationLine(StockLocation location, Product product);
	
	
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
	public StockLocationLine createDetailLocationLine(StockLocation location, Product product, TrackingNumber trackingNumber);
		
}
