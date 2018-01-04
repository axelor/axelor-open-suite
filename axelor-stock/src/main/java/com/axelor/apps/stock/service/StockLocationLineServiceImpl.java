/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class StockLocationLineServiceImpl implements StockLocationLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	protected StockLocationLineRepository stockLocationLineRepo;
	
	@Inject
	protected StockRulesService stockRulesService;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(StockLocation StockLocation, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
							   LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, BigDecimal reservedQty) throws AxelorException  {
		
		this.updateLocation(StockLocation, product, qty, current, future, isIncrement, lastFutureStockMoveDate, reservedQty);
		
		if(trackingNumber != null)  {
			this.updateDetailLocation(StockLocation, product, qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber, reservedQty);
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateLocation(StockLocation stockLocation, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
							   LocalDate lastFutureStockMoveDate, BigDecimal reservedQty) throws AxelorException  {
		
		StockLocationLine stockLocationLine = this.getStockLocationLine(stockLocation, product);
		
		LOG.debug("Mise à jour du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { stockLocation.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate });
		
		if (!isIncrement) {
			minStockRules(product, qty, stockLocationLine, current, future);
		} else {
			maxStockRules(product, qty, stockLocationLine, current, future);
		}
		
		stockLocationLine = this.updateLocation(stockLocationLine, qty, current, future, isIncrement, lastFutureStockMoveDate, reservedQty);
		
		this.checkStockMin(stockLocationLine, false);
		
		stockLocationLineRepo.save(stockLocationLine);
		
	}
	
	
	public void minStockRules(Product product, BigDecimal qty, StockLocationLine stockLocationLine, boolean current, boolean future) throws AxelorException  {
		
		if(current)  {
			stockRulesService.generateOrder(product, qty, stockLocationLine, StockRulesRepository.TYPE_CURRENT);
		}
		if(future)  {
			stockRulesService.generateOrder(product, qty, stockLocationLine, StockRulesRepository.TYPE_FUTURE);
		}
		
	}

	public void maxStockRules(Product product, BigDecimal qty, StockLocationLine stockLocationLine, boolean current,
			boolean future) throws AxelorException {

		if (current) {
			checkStockMax(product, qty, stockLocationLine, StockRulesRepository.TYPE_CURRENT, stockLocationLine.getCurrentQty());
		}

		if (future) {
			checkStockMax(product, qty, stockLocationLine, StockRulesRepository.TYPE_FUTURE, stockLocationLine.getFutureQty());
		}

	}

	void checkStockMax(Product product, BigDecimal qty, StockLocationLine stockLocationLine, int type, BigDecimal baseQty) throws AxelorException {
		StockLocation stockLocation = stockLocationLine.getStockLocation();
		StockRules stockRules = stockRulesService.getStockRules(product, stockLocation, type, StockRulesRepository.USE_CASE_STOCK_CONTROL);

		if (stockRules == null || !stockRules.getUseMaxQty()) {
			return;
		}

		if (baseQty.add(qty).compareTo(stockRules.getMaxQty()) > 0) {
			throw new AxelorException(stockLocationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_3),
					stockLocationLine.getProduct().getName(), stockLocationLine.getProduct().getCode());
		}
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateDetailLocation(StockLocation stockLocation, Product product, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
									 LocalDate lastFutureStockMoveDate, TrackingNumber trackingNumber, BigDecimal reservedQty) throws AxelorException  {
		
		StockLocationLine detailLocationLine = this.getDetailLocationLine(stockLocation, product, trackingNumber);
		
		LOG.debug("Mise à jour du detail du stock : Entrepot? {}, Produit? {}, Quantité? {}, Actuel? {}, Futur? {}, Incrément? {}, Date? {}, Num de suivi? {} ", 
				new Object[] { stockLocation.getName(), product.getCode(), qty, current, future, isIncrement, lastFutureStockMoveDate, trackingNumber});
		
		detailLocationLine = this.updateLocation(detailLocationLine, qty, current, future, isIncrement, lastFutureStockMoveDate, reservedQty);
		
		this.checkStockMin(detailLocationLine, true);
		
		stockLocationLineRepo.save(detailLocationLine);
		
	}
	
	
	public void checkStockMin(StockLocationLine stockLocationLine, boolean isDetailLocationLine) throws AxelorException  {
		if (!isDetailLocationLine && stockLocationLine.getCurrentQty().compareTo(BigDecimal.ZERO) < 0 && stockLocationLine.getStockLocation().getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
			throw new AxelorException(stockLocationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_1), stockLocationLine.getProduct().getName(), stockLocationLine.getProduct().getCode());
		
		} else if (isDetailLocationLine && stockLocationLine.getCurrentQty().compareTo(BigDecimal.ZERO) < 0 
				&& ((stockLocationLine.getStockLocation() != null && stockLocationLine.getStockLocation().getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL)
				    || (stockLocationLine.getDetailsLocation() != null && stockLocationLine.getDetailsLocation().getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL))) {

			String trackingNumber = "";
			if (stockLocationLine.getTrackingNumber() != null) {
				trackingNumber = stockLocationLine.getTrackingNumber().getTrackingNumberSeq();
			}
			
			throw new AxelorException(stockLocationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_2), stockLocationLine.getProduct().getName(), stockLocationLine.getProduct().getCode(), trackingNumber);
		}
	}

	public void checkIfEnoughStock(StockLocation stockLocation, Product product, BigDecimal qty) throws AxelorException{
		StockLocationLine stockLocationLine = this.getLocationLine(stockLocation.getStockLocationLineList(), product);

	    if(stockLocationLine != null && stockLocationLine.getCurrentQty().compareTo(qty) < 0) {
			throw new AxelorException(stockLocationLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.LOCATION_LINE_1), stockLocationLine.getProduct().getName(), stockLocationLine.getProduct().getCode());
		}
	}

	public StockLocationLine updateLocation(StockLocationLine stockLocationLine, BigDecimal qty, boolean current, boolean future, boolean isIncrement,
									   LocalDate lastFutureStockMoveDate, BigDecimal reservedQty) {
		
		if(current)  {
			if(isIncrement)  {
				stockLocationLine.setCurrentQty(stockLocationLine.getCurrentQty().add(qty));
			} else {
				stockLocationLine.setCurrentQty(stockLocationLine.getCurrentQty().subtract(qty));
			}
			
		}
		if(future)  {
			if(isIncrement)  {
				stockLocationLine.setFutureQty(stockLocationLine.getFutureQty().add(qty));
			} else {
				stockLocationLine.setFutureQty(stockLocationLine.getFutureQty().subtract(qty));
			}
			stockLocationLine.setLastFutureStockMoveDate(lastFutureStockMoveDate);
		}
		
		return stockLocationLine;
	}
	
	
	public StockLocationLine getStockLocationLine(StockLocation stockLocation, Product product)  {
		
		StockLocationLine stockLocationLine = this.getLocationLine(stockLocation.getStockLocationLineList(), product);
		
		if(stockLocationLine == null)  {
			stockLocationLine = this.createLocationLine(stockLocation, product);
			
		}
		
		LOG.debug("Récupération ligne de stock: Entrepot? {}, Produit? {}, Qté actuelle? {}, Qté future? {}, Date? {} ", 
				new Object[] { stockLocationLine.getStockLocation().getName(), product.getCode(), 
				stockLocationLine.getCurrentQty(), stockLocationLine.getFutureQty(), stockLocationLine.getLastFutureStockMoveDate() });
		
		return stockLocationLine;
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
	public StockLocationLine getDetailLocationLine(StockLocation detailLocation, Product product, TrackingNumber trackingNumber)  {
		
		StockLocationLine detailLocationLine = this.getDetailLocationLine(detailLocation.getDetailsLocationLineList(), product, trackingNumber);
		
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
	 * @param stockLocationLineList
	 * 		Une liste de ligne de stock
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public StockLocationLine getLocationLine(List<StockLocationLine> stockLocationLineList, Product product)  {
		
		for(StockLocationLine stockLocationLine : stockLocationLineList)  {
			
			if(stockLocationLine.getProduct().equals(product))  {
				return stockLocationLine;
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
	public StockLocationLine getDetailLocationLine(List<StockLocationLine> detailLocationLineList, Product product, TrackingNumber trackingNumber)  {
		
		for(StockLocationLine detailLocationLine : detailLocationLineList)  {
			
			if(detailLocationLine.getProduct().equals(product) 
					&& detailLocationLine.getTrackingNumber().equals(trackingNumber))  {
				return detailLocationLine;
			}
			
		}
		
		return null;
	}
	
	
	
	/**
	 * Permet de créer une ligne de stock pour un entrepot et un produit donnés.
	 * @param stockLocation
	 * 		Un entrepot
	 * @param product
	 * 		Un produit
	 * @return
	 * 		La ligne de stock
	 */
	public StockLocationLine createLocationLine(StockLocation stockLocation, Product product)  {
		
		LOG.debug("Création d'une ligne de stock : Entrepot? {}, Produit? {} ", new Object[] { stockLocation.getName(), product.getCode() });
		
		StockLocationLine stockLocationLine = new StockLocationLine();
		
		stockLocationLine.setStockLocation(stockLocation);
		stockLocation.addStockLocationLineListItem(stockLocationLine);
		stockLocationLine.setProduct(product);
		stockLocationLine.setCurrentQty(BigDecimal.ZERO);
		stockLocationLine.setFutureQty(BigDecimal.ZERO);
		
		return stockLocationLine;
		
	}
	
	
	/**
	 * Permet de créer une ligne détaillée de stock pour un entrepot, un produit, une variante de produit et un numéro de suivi donnés.
	 * @param stockLocation
	 * 		Un entrepot
	 * @param product
	 * 		Un produit
	 * @param trackingNumber
	 * 		Un numéro de suivi
	 * @return
	 * 		La ligne détaillée de stock
	 */
	public StockLocationLine createDetailLocationLine(StockLocation stockLocation, Product product, TrackingNumber trackingNumber)  {
		
		LOG.debug("Création d'une ligne de détail de stock : Entrepot? {}, Produit? {}, Num de suivi? {} ", 
				new Object[] { stockLocation.getName(), product.getCode(), trackingNumber.getTrackingNumberSeq() });
		
		StockLocationLine detailLocationLine = new StockLocationLine();
		
		detailLocationLine.setDetailsLocation(stockLocation);
		stockLocation.addDetailsLocationLineListItem(detailLocationLine);
		detailLocationLine.setProduct(product);
		detailLocationLine.setCurrentQty(BigDecimal.ZERO);
		detailLocationLine.setFutureQty(BigDecimal.ZERO);
		detailLocationLine.setTrackingNumber(trackingNumber);
		
		
		return detailLocationLine;
		
	}

		
}
