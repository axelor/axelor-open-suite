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
import com.axelor.apps.base.db.TrackingNumberConfiguration;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;

public interface StockMoveLineService {

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
	public StockMoveLine createStockMoveLine(Product product, String productName, String description, BigDecimal quantity, BigDecimal unitPrice, Unit unit, StockMove stockMove, int type , boolean taxed, BigDecimal taxRate) throws AxelorException;


	public void generateTrackingNumber(StockMoveLine stockMoveLine, TrackingNumberConfiguration trackingNumberConfiguration, Product product, BigDecimal qtyByTracking) throws AxelorException;


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
	public StockMoveLine createStockMoveLine(Product product, String productName, String description, BigDecimal quantity, BigDecimal unitPriceUntaxed, BigDecimal unitPriceTaxed, Unit unit, StockMove stockMove, TrackingNumber trackingNumber) throws AxelorException;



	public void assignTrackingNumber(StockMoveLine stockMoveLine, Product product, Location location) throws AxelorException;



	public List<? extends LocationLine> getLocationLines(Product product, Location location) throws AxelorException;



	public StockMoveLine splitStockMoveLine(StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber) throws AxelorException;



	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList,
			LocalDate lastFutureStockMoveDate, boolean realQty) throws AxelorException;


	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate
			lastFutureStockMoveDate, TrackingNumber trackingNumber) throws AxelorException;
	
	public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove) throws AxelorException;









}
